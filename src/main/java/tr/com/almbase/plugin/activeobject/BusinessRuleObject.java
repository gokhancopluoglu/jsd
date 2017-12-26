package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 08/12/2017.
 */
public class BusinessRuleObject {
    String categoryId;
    String subCategoryId;
    String categoryItemId;
    String issueType;
    String userName;

    public String getCategoryId() {
        return categoryId;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public String getCategoryItemId() {return categoryItemId;}

    public String getUserName() {
        return userName;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public void setCategoryItemId(String categoryItemId) {
        this.categoryItemId = categoryItemId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssueType() {
        return issueType;
    }
}
