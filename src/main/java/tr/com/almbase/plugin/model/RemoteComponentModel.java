package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteComponentModel {
    private String projectKey;
    private String componentId;
    private String componentName;

    public RemoteComponentModel(String projectKey, String componentId, String componentName) {
        this.projectKey = projectKey;
        this.componentId = componentId;
        this.componentName = componentName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}
