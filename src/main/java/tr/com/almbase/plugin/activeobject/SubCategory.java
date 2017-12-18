package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface SubCategory extends Entity {
    String getCategoryId();
    void setCategoryId(String categoryId);
    String getSubCategoryName();
    void setSubCategoryName(String subCategoryName);
}
