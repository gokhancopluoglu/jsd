package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface RemoteIssue extends Entity {
    String getIntegrationId();
    String getIssueKey();
    String getRiKey();
    String getRiSummary();
    String getRiStatus();
    String getRiStatusColor();
    String getRiAssginee();
    String getLastUpdatedDate();
    void setIntegrationId(String integrationId);
    void setIssueKey(String issueKey);
    void setRiKey(String riKey);
    void setRiSummary(String riSummary);
    void setRiStatus(String riStatus);
    void setRiStatusColor(String riStatusColor);
    void setRiAssginee(String riAssginee);
    void setLastUpdatedDate(String lastUpdatedDate);

    String getRiProjectId();
    String getRiIssueTypeId();
    void setRiProjectId(String riProjectId);
    void setRiIssueTypeId(String riIssueTypeId);

    String getDeleted();
    void setDeleted(String deleted);
}
