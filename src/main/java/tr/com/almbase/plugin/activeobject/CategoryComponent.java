package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface CategoryComponent extends Entity {
    String getCategoryId();
    String getSubCategoryId();
    String getCategoryItemId();
    String getCategoryComponentName();
    void setCategoryId(String categoryId);
    void setSubCategoryId(String subCategoryId);
    void setCategoryItemId(String categoryItemId);
    void setCategoryComponentName(String categoryComponentName);
}
