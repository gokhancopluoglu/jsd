package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.GroupUser;
import tr.com.almbase.plugin.activeobject.GroupUserController;
import tr.com.almbase.plugin.activeobject.IssueTypeGroup;
import tr.com.almbase.plugin.activeobject.IssueTypeGroupController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class AssignToIssueTypeGroup extends AbstractJiraFunctionProvider
{
    private static final Logger log = LoggerFactory.getLogger(AssignToIssueTypeGroup.class);
    private IssueTypeGroupController issueTypeGroupController;
    private GroupUserController groupUserController;

    public AssignToIssueTypeGroup(IssueTypeGroupController issueTypeGroupController, GroupUserController groupUserController)
    {
        this.issueTypeGroupController = issueTypeGroupController;
        this.groupUserController = groupUserController;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        MutableIssue issue = getIssue(transientVars);
        try {
            String issueTypeId = String.valueOf(issue.getIssueType().getId());
            IssueTypeGroup issueTypeGroup = issueTypeGroupController.getRecordFromAOTableByIssueType(issueTypeId);

            if (null != issueTypeGroup) {
                String groupName = issueTypeGroup.getGroupName();
                if (null != groupName && !groupName.equalsIgnoreCase("")) {
                    GroupUser groupUser = groupUserController.getRecordFromAOTableByGroupName(groupName);
                    if (null != groupUser) {
                        String userKey = groupUser.getUserName();
                        if (null != userKey && !userKey.equalsIgnoreCase("")) {
                            ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(userKey);
                            if (null != user) {
                                ApplicationUser autodispatcher = ComponentAccessor.getUserManager().getUserByName("autodispatcher");
                                ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(autodispatcher);
                                issue.setAssignee(user);
                                IssueManager issueManager = ComponentAccessor.getIssueManager();
                                issueManager.updateIssue(autodispatcher, issue, EventDispatchOption.ISSUE_UPDATED, false);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }
}