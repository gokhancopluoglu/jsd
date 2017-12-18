package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface CategoryItem extends Entity {
    String getCategoryId();
    void setCategoryId(String categoryId);
    String getSubCategoryId();
    void setSubCategoryId(String subCategoryId);
    String getCategoryItemName();
    void setCategoryItemName(String categoryItemName);
}
