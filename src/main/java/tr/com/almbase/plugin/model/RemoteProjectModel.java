package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteProjectModel {
    private String projectId;
    private String projectKey;
    private String projectName;

    public RemoteProjectModel() {
    }

    public RemoteProjectModel(String projectId, String projectKey, String projectName) {
        this.projectId = projectId;
        this.projectKey = projectKey;
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
