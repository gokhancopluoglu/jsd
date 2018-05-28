package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 08/12/2017.
 */
public class RemoteIssueObject {
    String integrationId;
    String issueKey;
    String riKey;
    String riProjectId;
    String riIssueTypeId;
    String riSummary;
    String riStatus;
    String riStatusColor;
    String riAssginee;
    String lastUpdatedDate;
    String deleted;

    public RemoteIssueObject() {}

    public RemoteIssueObject (RemoteIssue remoteIssue) {
        setIntegrationId(remoteIssue.getIntegrationId());
        setIssueKey(remoteIssue.getIssueKey());
        setRiKey(remoteIssue.getRiKey());
        setRiProjectId(remoteIssue.getRiProjectId());
        setRiIssueTypeId(remoteIssue.getRiIssueTypeId());
        setRiSummary(remoteIssue.getRiSummary());
        setRiAssginee(remoteIssue.getRiAssginee());
        setRiStatus(remoteIssue.getRiStatus());
        setRiStatusColor(remoteIssue.getRiStatusColor());
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getRiKey() {
        return riKey;
    }

    public String getRiSummary() {
        return riSummary;
    }

    public String getRiStatus() {
        return riStatus;
    }

    public String getRiStatusColor() {
        return riStatusColor;
    }

    public String getRiAssginee() {
        return riAssginee;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public void setRiKey(String riKey) {
        this.riKey = riKey;
    }

    public void setRiSummary(String riSummary) {
        this.riSummary = riSummary;
    }

    public void setRiStatus(String riStatus) {
        this.riStatus = riStatus;
    }

    public void setRiStatusColor(String riStatusColor) {
        this.riStatusColor = riStatusColor;
    }

    public void setRiAssginee(String riAssginee) {
        this.riAssginee = riAssginee;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getRiProjectId() {
        return riProjectId;
    }

    public String getRiIssueTypeId() {
        return riIssueTypeId;
    }

    public void setRiProjectId(String riProjectId) {
        this.riProjectId = riProjectId;
    }

    public void setRiIssueTypeId(String riIssueTypeId) {
        this.riIssueTypeId = riIssueTypeId;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }
}