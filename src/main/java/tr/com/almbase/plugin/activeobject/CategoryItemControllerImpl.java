package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class CategoryItemControllerImpl implements CategoryItemController{

    private static final Logger log = LoggerFactory.getLogger(CategoryItemControllerImpl.class);
    private ActiveObjects activeObjects;

    public CategoryItemControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public CategoryItem getRecordFromAOTableByName(String categoryItemName) {
        CategoryItem categoryItem = null;
        try {
            CategoryItem[] tempCategoryItem = activeObjects.find(CategoryItem.class, "CATEGORY_ITEM_NAME = ?", categoryItemName);
            if (null != tempCategoryItem && tempCategoryItem.length > 0)
                categoryItem = tempCategoryItem[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryItem;
    }

    public CategoryItem getRecordFromAOTableById(String categoryItemId) {
        CategoryItem categoryItem = null;
        try {
            CategoryItem[] tempCategoryItem = activeObjects.find(CategoryItem.class, "ID = ?", Integer.parseInt(categoryItemId));
            if (null != tempCategoryItem && tempCategoryItem.length > 0) {
                categoryItem = tempCategoryItem[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryItem;
    }

    public CategoryItem[] getRecordFromAOTableBySubCategoryId(String subCategoryId) {
        CategoryItem[] categoryItems = null;
        try {
            categoryItems = activeObjects.find(CategoryItem.class, "SUB_CATEGORY_ID = ?", subCategoryId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryItems;
    }

    public CategoryItem[] getAllEntriesFromAOTable() {
        CategoryItem[] categoryItems = null;
        try {
            categoryItems = activeObjects.find(CategoryItem.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categoryItems;
    }

    public CategoryItem createRecordInAOTable(CategoryItemObject categoryItemObject) {
        CategoryItem returnItemCategory = null;
        try {
            log.debug("New setting!");
            CategoryItem categoryItemRecord = activeObjects.create(CategoryItem.class);
            if (categoryItemRecord != null)
            {
                returnItemCategory = setAOValuesAndReturnAsObject(categoryItemObject, categoryItemRecord);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnItemCategory;
    }

    public CategoryItem updateRecordInAOTable(CategoryItem categoryItem, CategoryItemObject categoryItemObject) {
        try {
            if (categoryItem != null) {
                categoryItem.setCategoryItemName(categoryItemObject.getCategoryItemName());
                categoryItem.save();
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return categoryItem;
    }

    public void deleteRecordFromAOTable(CategoryItem categoryItem) {
        try {
            activeObjects.delete(categoryItem);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private CategoryItem setAOValuesAndReturnAsObject(CategoryItemObject categoryItemObject, CategoryItem categoryItemRecord) {
        try {
            categoryItemRecord.setCategoryId(categoryItemObject.getCategoryId());
            categoryItemRecord.setSubCategoryId(categoryItemObject.getSubCategoryId());
            categoryItemRecord.setCategoryItemName(categoryItemObject.getCategoryItemName());
            categoryItemRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            categoryItemRecord = null;
        }
        return categoryItemRecord;
    }
}
