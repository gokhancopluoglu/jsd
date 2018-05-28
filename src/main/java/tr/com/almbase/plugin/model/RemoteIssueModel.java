package tr.com.almbase.plugin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteIssueModel {
    RemoteProjectModel project;
    String issueKey;
    RemoteIssueTypeModel issueType;
    RemoteStatusModel status;
    Map<String, String> assignee;
    Map<String, String> reporter;
    String summary;
    String description;
    String createDate;
    String updatedDate;
    String resolvedDate;
    String dueDate;
    RemoteResolutionModel resolution;
    RemotePriorityModel priority;
    List<RemoteComponentModel> components;
    List<RemoteVersionModel> fixVersions;
    List<RemoteVersionModel> affectedVersions;
    List<String> labels;

    String deleted;

    public RemoteIssueModel() {
        this.assignee = new HashMap<>();
        this.reporter = new HashMap<>();
        this.components = new ArrayList<>();
        this.fixVersions = new ArrayList<>();
        this.affectedVersions = new ArrayList<>();
        this.labels = new ArrayList<>();
    }

    public RemoteProjectModel getProject() {
        return project;
    }

    public void setProject(RemoteProjectModel project) {
        this.project = project;
    }

    public String getDescription() {
        return description;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public String getResolvedDate() {
        return resolvedDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public List<RemoteComponentModel> getComponents() {
        return components;
    }

    public List<RemoteVersionModel> getFixVersions() {
        return fixVersions;
    }

    public List<RemoteVersionModel> getAffectedVersions() {
        return affectedVersions;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setResolvedDate(String resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setComponents(List<RemoteComponentModel> components) {
        this.components = components;
    }

    public void setFixVersions(List<RemoteVersionModel> fixVersions) {
        this.fixVersions = fixVersions;
    }

    public void setAffectedVersions(List<RemoteVersionModel> affectedVersions) {this.affectedVersions = affectedVersions;}

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public RemoteIssueTypeModel getIssueType() {
        return issueType;
    }

    public RemoteStatusModel getStatus() {
        return status;
    }

    public Map<String, String> getAssignee() {
        return assignee;
    }

    public Map<String, String> getReporter() {
        return reporter;
    }

    public RemoteResolutionModel getResolution() {
        return resolution;
    }

    public RemotePriorityModel getPriority() {
        return priority;
    }

    public void setIssueType(RemoteIssueTypeModel issueType) {
        this.issueType = issueType;
    }

    public void setStatus(RemoteStatusModel status) {
        this.status = status;
    }

    public void setAssignee(Map<String, String> assignee) {
        this.assignee = assignee;
    }

    public void setReporter(Map<String, String> reporter) {
        this.reporter = reporter;
    }

    public void setResolution(RemoteResolutionModel resolution) {
        this.resolution = resolution;
    }

    public void setPriority(RemotePriorityModel priority) {
        this.priority = priority;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }
}
