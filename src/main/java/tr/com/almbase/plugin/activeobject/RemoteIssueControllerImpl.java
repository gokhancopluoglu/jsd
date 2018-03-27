package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class RemoteIssueControllerImpl implements RemoteIssueController {

    private static final Logger log = LoggerFactory.getLogger(RemoteIssueControllerImpl.class);
    private ActiveObjects activeObjects;

    public RemoteIssueControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public RemoteIssue[] getRecordFromAOTableByIssueKey(String issueKey) {
        RemoteIssue[] remoteIssues = null;
        try {
            remoteIssues = activeObjects.find(RemoteIssue.class, "ISSUE_KEY = ?", issueKey);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return remoteIssues;
    }

    public RemoteIssue getRecordFromAOTableByRemoteIssueKey(String remoteIssueKey) {
        RemoteIssue remoteIssue = null;
        try {
            RemoteIssue[] tempRemoteIssue = activeObjects.find(RemoteIssue.class, "RI_KEY = ?", remoteIssueKey);
            if (null != tempRemoteIssue) {
                remoteIssue = tempRemoteIssue[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return remoteIssue;
    }

    public RemoteIssue getRecordFromAOTableById(String remoteIssueId) {
        RemoteIssue remoteIssue = null;
        try {
            RemoteIssue[] tempRemoteIssue = activeObjects.find(RemoteIssue.class, "ID = ?", Integer.parseInt(remoteIssueId));
            if (null != tempRemoteIssue && tempRemoteIssue.length > 0) {
                remoteIssue = tempRemoteIssue[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return remoteIssue;
    }

    public RemoteIssue[] getRecordFromAOTableByIntegrationId(String integrationId) {
        RemoteIssue[] remoteIssues = null;
        try {
            remoteIssues = activeObjects.find(RemoteIssue.class, "INTEGRATION_ID = ?", integrationId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return remoteIssues;
    }

    public RemoteIssue[] getAllEntriesFromAOTable() {
        RemoteIssue[] remoteIssues = null;
        try {
            remoteIssues = activeObjects.find(RemoteIssue.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return remoteIssues;
    }

    public RemoteIssue createRecordInAOTable(RemoteIssueObject remoteIssueObject) {
        RemoteIssue returnRemoteIssue = null;
        try {
            RemoteIssue found = null;
            RemoteIssue[] tempRemoteIssues = activeObjects.find(RemoteIssue.class, "ISSUE_KEY = ? AND RI_KEY = ?", remoteIssueObject.getIssueKey(), remoteIssueObject.getRiKey());

            if (null != tempRemoteIssues && tempRemoteIssues.length > 0) {
                found = tempRemoteIssues[0];
            }

            if (null != found) {
                deleteRecordFromAOTable(found);

                RemoteIssue remoteIssue = activeObjects.create(RemoteIssue.class);
                if (remoteIssue != null) {
                    returnRemoteIssue = setAOValuesAndReturnAsObject(remoteIssue, remoteIssueObject);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            } else {
                log.debug("New setting!");
                RemoteIssue remoteIssue = activeObjects.create(RemoteIssue.class);
                if (remoteIssue != null)
                {
                    returnRemoteIssue = setAOValuesAndReturnAsObject(remoteIssue, remoteIssueObject);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnRemoteIssue;
    }

    private void deleteRecordFromAOTable(RemoteIssue remoteIssue) {
        try {
            activeObjects.delete(remoteIssue);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    public RemoteIssue updateRecordFromAOTable(RemoteIssue remoteIssueRecord, RemoteIssueObject remoteIssueObject) {
        try {
            remoteIssueRecord = setAOValuesAndReturnAsObject(remoteIssueRecord, remoteIssueObject);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }

        return remoteIssueRecord;
    }

    private RemoteIssue setAOValuesAndReturnAsObject(RemoteIssue remoteIssueRecord, RemoteIssueObject remoteIssueObject) {
        try {
            remoteIssueRecord.setIntegrationId(remoteIssueObject.getIntegrationId());
            remoteIssueRecord.setIssueKey(remoteIssueObject.getIssueKey());
            remoteIssueRecord.setRiKey(remoteIssueObject.getRiKey());
            remoteIssueRecord.setRiProjectId(remoteIssueObject.getRiProjectId());
            remoteIssueRecord.setRiIssueTypeId(remoteIssueObject.getRiIssueTypeId());
            remoteIssueRecord.setRiAssginee(remoteIssueObject.getRiAssginee());
            remoteIssueRecord.setRiSummary(remoteIssueObject.getRiSummary());
            remoteIssueRecord.setRiStatus(remoteIssueObject.getRiStatus());
            remoteIssueRecord.setRiStatusColor(remoteIssueObject.getRiStatusColor());
            remoteIssueRecord.setLastUpdatedDate(remoteIssueObject.getLastUpdatedDate());
            remoteIssueRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            remoteIssueRecord = null;
        }
        return remoteIssueRecord;
    }
}
