package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class IssueTypeMappingObject {
    String integrationId;
    String name;
    String localProjectId;
    String localIssueTypeId;
    String localEndStatusId;
    String remoteProjectId;
    String remoteIssueTypeId;

    public String getName() {
        return name;
    }

    public String getLocalProjectId() {
        return localProjectId;
    }

    public String getLocalIssueTypeId() {
        return localIssueTypeId;
    }

    public String getRemoteProjectId() {
        return remoteProjectId;
    }

    public String getRemoteIssueTypeId() {
        return remoteIssueTypeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocalProjectId(String localProjectId) {
        this.localProjectId = localProjectId;
    }

    public void setLocalIssueTypeId(String localIssueTypeId) {
        this.localIssueTypeId = localIssueTypeId;
    }

    public void setRemoteProjectId(String remoteProjectId) {
        this.remoteProjectId = remoteProjectId;
    }

    public void setRemoteIssueTypeId(String remoteIssueTypeId) {
        this.remoteIssueTypeId = remoteIssueTypeId;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public String getLocalEndStatusId() {
        return localEndStatusId;
    }

    public void setLocalEndStatusId(String localEndStatusId) {
        this.localEndStatusId = localEndStatusId;
    }
}
