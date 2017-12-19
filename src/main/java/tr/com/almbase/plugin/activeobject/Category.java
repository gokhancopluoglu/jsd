package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface Category extends Entity {
    String getCategoryName();
    void setCategoryName(String categoryName);
    String getRequestType();
    void setRequestType(String requestType);
    String getGroupName();
    void setGroupName(String groupName);
}
