package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteIssueTypeModel {
    private String issueTypeId;
    private String issueTypeName;

    public RemoteIssueTypeModel(String issueTypeId, String issueTypeName) {
        this.issueTypeId = issueTypeId;
        this.issueTypeName = issueTypeName;
    }

    public String getIssueTypeId() {
        return issueTypeId;
    }

    public String getIssueTypeName() {
        return issueTypeName;
    }

    public void setIssueTypeId(String issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public void setIssueTypeName(String issueTypeName) {
        this.issueTypeName = issueTypeName;
    }
}
