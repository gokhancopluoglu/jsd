package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.issue.Issue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class IssueTypeGroupControllerImpl implements IssueTypeGroupController{

    private static final Logger log = LoggerFactory.getLogger(IssueTypeGroupControllerImpl.class);
    private ActiveObjects activeObjects;

    public IssueTypeGroupControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public IssueTypeGroup getRecordFromAOTableByGroupName(String groupName) {
        IssueTypeGroup issueTypeGroup = null;
        try {
            IssueTypeGroup[] tempIssueTypeGroup = activeObjects.find(IssueTypeGroup.class, "GROUP_NAME = ?", groupName);
            if (null != tempIssueTypeGroup && tempIssueTypeGroup.length > 0)
                issueTypeGroup = tempIssueTypeGroup[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeGroup;
    }

    public IssueTypeGroup getRecordFromAOTableByIssueType(String issueType) {
        IssueTypeGroup issueTypeGroup = null;
        try {
            IssueTypeGroup[] tempIssueTypeGroup = activeObjects.find(IssueTypeGroup.class, "ISSUE_TYPE = ?", issueType);
            if (null != tempIssueTypeGroup && tempIssueTypeGroup.length > 0)
                issueTypeGroup = tempIssueTypeGroup[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeGroup;
    }

    public IssueTypeGroup getRecordFromAOTableById(String issueTypeGroupId) {
        IssueTypeGroup issueTypeGroup = null;
        try {
            IssueTypeGroup[] tempIssueTypeGroup = activeObjects.find(IssueTypeGroup.class, "ID = ?", Integer.parseInt(issueTypeGroupId));
            if (null != tempIssueTypeGroup && tempIssueTypeGroup.length > 0)
                issueTypeGroup = tempIssueTypeGroup[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeGroup;
    }

    public IssueTypeGroup[] getAllEntriesFromAOTable() {
        IssueTypeGroup[] issueTypeGroups = null;
        try {
            issueTypeGroups = activeObjects.find(IssueTypeGroup.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeGroups;
    }

    public IssueTypeGroup createRecordInAOTable(IssueTypeGroupObject issueTypeGroupObject) {
        IssueTypeGroup returnIssueTypeGroup = null;
        try {
            IssueTypeGroup foundAO = getRecordFromAOTableByIssueType(issueTypeGroupObject.getIssueType());
            if (foundAO != null)
            {
                deleteRecordFromAOTable(foundAO);

                IssueTypeGroup issueTypeGroup = activeObjects.create(IssueTypeGroup.class);
                if (issueTypeGroup != null)
                {
                    returnIssueTypeGroup = setAOValuesAndReturnAsObject(issueTypeGroupObject, issueTypeGroup);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            } else {
                log.debug("New setting!");
                IssueTypeGroup issueTypeGroup = activeObjects.create(IssueTypeGroup.class);
                if (issueTypeGroup != null)
                {
                    returnIssueTypeGroup = setAOValuesAndReturnAsObject(issueTypeGroupObject, issueTypeGroup);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnIssueTypeGroup;
    }

    public void deleteRecordFromAOTable(IssueTypeGroup issueTypeGroup) {
        try {
            activeObjects.delete(issueTypeGroup);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private IssueTypeGroup setAOValuesAndReturnAsObject(IssueTypeGroupObject issueTypeGroupObject, IssueTypeGroup issueTypeGroup) {
        try {
            issueTypeGroup.setGroupName(issueTypeGroupObject.getGroupName());
            issueTypeGroup.setIssueType(issueTypeGroupObject.getIssueType());
            issueTypeGroup.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            issueTypeGroup = null;
        }
        return issueTypeGroup;
    }
}
