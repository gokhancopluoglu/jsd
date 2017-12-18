package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface BusinessRule extends Entity {
    String getCategoryId();
    void setCategoryId(String categoryId);
    String getSubCategoryId();
    void setSubCategoryId(String subCategoryId);
    String getCategoryItemId();
    void setCategoryItemId(String categoryItemId);
    String getUserName();
    void setUserName(String userName);
}
