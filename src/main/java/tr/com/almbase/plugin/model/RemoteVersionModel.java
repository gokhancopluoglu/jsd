package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteVersionModel {
    private String projectKey;
    private String versionId;
    private String versionName;

    public RemoteVersionModel(String projectKey, String versionId, String versionName) {
        this.projectKey = projectKey;
        this.versionId = versionId;
        this.versionName = versionName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
