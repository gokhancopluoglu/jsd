package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 08/12/2017.
 */
public class CategoryObject {
    String categoryName;
    String issueTypeId;
    String groupName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIssueTypeId() {
        return issueTypeId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setIssueTypeId(String issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
