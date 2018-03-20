package tr.com.almbase.plugin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteCustomFieldModel {
    private String projectKey;
    private String issueTypeId;
    private String id;
    private String name;
    private String type;
    private List<Map<String,String>> allowedValues;

    public RemoteCustomFieldModel(String projectKey, String issueTypeId) {
        this.projectKey = projectKey;
        this.issueTypeId = issueTypeId;
        allowedValues = new ArrayList<>();
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getIssueTypeId() {
        return issueTypeId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setIssueTypeId(String issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Map<String, String>> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<Map<String, String>> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
