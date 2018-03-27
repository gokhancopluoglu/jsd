package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class IssueTypeMappingControllerImpl implements IssueTypeMappingController {

    private static final Logger log = LoggerFactory.getLogger(IssueTypeMappingControllerImpl.class);
    private ActiveObjects activeObjects;

    public IssueTypeMappingControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public IssueTypeMapping getRecordFromAOTableByName(String name) {
        IssueTypeMapping issueTypeMap = null;
        try {
            IssueTypeMapping[] tempIssueTypeMaps = activeObjects.find(IssueTypeMapping.class, "NAME = ?", name);
            if (null != tempIssueTypeMaps && tempIssueTypeMaps.length > 0) {
                issueTypeMap = tempIssueTypeMaps[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeMap;
    }

    public IssueTypeMapping getRecordFromAOTableById(String integrationId) {
        IssueTypeMapping issueTypeMap = null;
        try {
            IssueTypeMapping[] tempIssueTypeMaps = activeObjects.find(IssueTypeMapping.class, "ID = ?", Integer.parseInt(integrationId));
            if (null != tempIssueTypeMaps && tempIssueTypeMaps.length > 0) {
                issueTypeMap = tempIssueTypeMaps[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeMap;
    }

    public IssueTypeMapping getRecordWithAllParameters(String integrationId, String localProjectId, String localIssueTypeId, String remoteProjectId, String remoteIssueTypeId) {
        IssueTypeMapping issueTypeMap = null;
        try {
            IssueTypeMapping [] issueTypeMappings = activeObjects.find(IssueTypeMapping.class, "INTEGRATION_ID = ? AND LOCAL_PROJECT_ID = ? AND LOCAL_ISSUE_TYPE_ID = ? AND REMOTE_PROJECT_ID = ? AND REMOTE_ISSUE_TYPE_ID = ?", integrationId, localProjectId, localIssueTypeId, remoteProjectId, remoteIssueTypeId);
            if (null != issueTypeMappings && issueTypeMappings.length > 0)
                issueTypeMap = issueTypeMappings[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeMap;
    }

    public IssueTypeMapping[] getRecordFromAOTableByIntegrationId(String integrationId) {
        IssueTypeMapping[] issueTypeMaps = null;
        try {
            issueTypeMaps = activeObjects.find(IssueTypeMapping.class, "INTEGRATION_ID = ?", integrationId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeMaps;
    }

    public IssueTypeMapping[] getAllEntriesFromAOTable() {
        IssueTypeMapping[] issueTypeMaps = null;
        try {
            issueTypeMaps = activeObjects.find(IssueTypeMapping.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return issueTypeMaps;
    }

    public IssueTypeMapping createRecordInAOTable(IssueTypeMappingObject issueTypeMapObject) {
        IssueTypeMapping returnIssueTypeMap = null;
        try {
            log.debug("New setting!");
            IssueTypeMapping issueTypeMapRecord = activeObjects.create(IssueTypeMapping.class);
            if (issueTypeMapRecord != null)
            {
                returnIssueTypeMap = setAOValuesAndReturnAsObject(issueTypeMapRecord, issueTypeMapObject);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnIssueTypeMap;
    }

    public void deleteRecordFromAOTable(IssueTypeMapping issueTypeMapRecord) {
        try {
            activeObjects.delete(issueTypeMapRecord);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    public IssueTypeMapping updateRecordFromAOTable(IssueTypeMapping issueTypeMapRecord, IssueTypeMappingObject issueTypeMapObject) {
        try {
            issueTypeMapRecord = setAOValuesAndReturnAsObject(issueTypeMapRecord, issueTypeMapObject);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }

        return issueTypeMapRecord;
    }

    private IssueTypeMapping setAOValuesAndReturnAsObject(IssueTypeMapping issueTypeMapRecord, IssueTypeMappingObject issueTypeMapObject) {
        try {
            issueTypeMapRecord.setIntegrationId(issueTypeMapObject.getIntegrationId());
            issueTypeMapRecord.setName(issueTypeMapObject.getName());
            issueTypeMapRecord.setLocalProjectId(issueTypeMapObject.getLocalProjectId());
            issueTypeMapRecord.setLocalIssueTypeId(issueTypeMapObject.getLocalIssueTypeId());
            issueTypeMapRecord.setLocalEndStatusId(issueTypeMapObject.getLocalEndStatusId());
            issueTypeMapRecord.setRemoteProjectId(issueTypeMapObject.getRemoteProjectId());
            issueTypeMapRecord.setRemoteIssueTypeId(issueTypeMapObject.getRemoteIssueTypeId());
            issueTypeMapRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            issueTypeMapRecord = null;
        }
        return issueTypeMapRecord;
    }
}
