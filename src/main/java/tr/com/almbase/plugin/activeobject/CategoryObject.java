package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 08/12/2017.
 */
public class CategoryObject {
    String categoryName;
    String requestType;
    String groupName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
}
