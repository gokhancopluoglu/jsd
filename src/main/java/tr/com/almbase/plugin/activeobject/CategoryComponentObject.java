package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 08/12/2017.
 */
public class CategoryComponentObject {

    String categoryId;
    String subCategoryId;
    String categoryItemId;
    String categoryComponentName;

    public String getCategoryId() {
        return categoryId;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getCategoryItemId() {
        return categoryItemId;
    }

    public String getCategoryComponentName() {
        return categoryComponentName;
    }

    public void setCategoryItemId(String categoryItemId) {
        this.categoryItemId = categoryItemId;
    }

    public void setCategoryComponentName(String categoryComponentName) {
        this.categoryComponentName = categoryComponentName;
    }
}
