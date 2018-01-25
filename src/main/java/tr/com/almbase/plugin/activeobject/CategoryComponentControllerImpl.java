package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class CategoryComponentControllerImpl implements CategoryComponentController{

    private static final Logger log = LoggerFactory.getLogger(CategoryComponentControllerImpl.class);
    private ActiveObjects activeObjects;

    public CategoryComponentControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public CategoryComponent getRecordFromAOTableByName(String categoryComponentName) {
        CategoryComponent categoryComponent = null;
        try {
            CategoryComponent[] tempCategoryComponent = activeObjects.find(CategoryComponent.class, "CATEGORY_COMPONENT_NAME = ?", categoryComponentName);
            if (null != tempCategoryComponent && tempCategoryComponent.length > 0)
                categoryComponent = tempCategoryComponent[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryComponent;
    }

    public CategoryComponent getRecordFromAOTableById(String categoryComponentId) {
        CategoryComponent categoryComponent = null;
        try {
            CategoryComponent[] tempCategoryComponent = activeObjects.find(CategoryComponent.class, "ID = ?", Integer.parseInt(categoryComponentId));
            if (null != tempCategoryComponent && tempCategoryComponent.length > 0) {
                categoryComponent = tempCategoryComponent[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryComponent;
    }

    public CategoryComponent[] getRecordFromAOTableByCategoryItemId(String categoryItemId) {
        CategoryComponent[] categoryComponents = null;
        try {
            categoryComponents = activeObjects.find(CategoryComponent.class, "CATEGORY_ITEM_ID = ?", categoryItemId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryComponents;
    }

    public CategoryComponent[] getAllEntriesFromAOTable() {
        CategoryComponent[] categoryComponents = null;
        try {
            categoryComponents = activeObjects.find(CategoryComponent.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryComponents;
    }

    public CategoryComponent createRecordInAOTable(CategoryComponentObject categoryComponentObject) {
        CategoryComponent returnComponentCategory = null;
        try {
            log.debug("New setting!");
            CategoryComponent categoryComponentRecord = activeObjects.create(CategoryComponent.class);
            if (categoryComponentRecord != null)
            {
                returnComponentCategory = setAOValuesAndReturnAsObject(categoryComponentObject, categoryComponentRecord);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnComponentCategory;
    }

    public CategoryComponent updateRecordInAOTable(CategoryComponent categoryComponent, CategoryComponentObject categoryComponentObject) {
        try {
            if (categoryComponent != null) {
                categoryComponent.setCategoryComponentName(categoryComponentObject.getCategoryComponentName());
                categoryComponent.save();
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return categoryComponent;
    }

    public void deleteRecordFromAOTable(CategoryComponent categoryComponent) {
        try {
            activeObjects.delete(categoryComponent);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private CategoryComponent setAOValuesAndReturnAsObject(CategoryComponentObject categoryComponentObject, CategoryComponent categoryComponentRecord) {
        try {
            categoryComponentRecord.setCategoryId(categoryComponentObject.getCategoryId());
            categoryComponentRecord.setSubCategoryId(categoryComponentObject.getSubCategoryId());
            categoryComponentRecord.setCategoryItemId(categoryComponentObject.getCategoryItemId());
            categoryComponentRecord.setCategoryComponentName(categoryComponentObject.getCategoryComponentName());
            categoryComponentRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            categoryComponentRecord = null;
        }
        return categoryComponentRecord;
    }
}
