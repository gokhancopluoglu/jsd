package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class CategoryControllerImpl implements CategoryController{

    private static final Logger log = LoggerFactory.getLogger(CategoryControllerImpl.class);
    private ActiveObjects activeObjects;

    public CategoryControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public Category getRecordFromAOTableByName(String categoryName) {
        Category category = null;
        try {
            Category[] tempCategory = activeObjects.find(Category.class, "CATEGORY_NAME = ?", categoryName);
            if (null != tempCategory && tempCategory.length > 0)
                category = tempCategory[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return category;
    }

    public Category getRecordFromAOTableById(String categoryId) {
        Category category = null;
        try {
            Category[] tempCategory = activeObjects.find(Category.class, "ID = ?", Integer.parseInt(categoryId));
            if (null != tempCategory && tempCategory.length > 0) {
                category = tempCategory[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return category;
    }

    public Category[] getAllEntriesFromAOTable() {
        Category[] categories = null;
        try {
            categories = activeObjects.find(Category.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return categories;
    }

    public Category createRecordInAOTable(CategoryObject categoryObject) {
        Category returnCategory = null;
        try {
            log.debug("New setting!");
            Category categoryRecord = activeObjects.create(Category.class);
            if (categoryRecord != null)
            {
                returnCategory = setAOValuesAndReturnAsObject(categoryObject, categoryRecord);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnCategory;
    }

    public Category updateRecordInAOTable(Category category, CategoryObject categoryObject) {
        try {
            if (category != null) {
                category.setCategoryName(categoryObject.getCategoryName());
                category.setIssueTypeId(categoryObject.getIssueTypeId());
                category.setGroupName(categoryObject.getGroupName());
                category.save();
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return category;
    }

    public void deleteRecordFromAOTable(Category category) {
        try {
            activeObjects.delete(category);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    public void deleteAllFromAOTable() {
        try {
            Category[] categories = getAllEntriesFromAOTable();
            activeObjects.delete(categories);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private Category setAOValuesAndReturnAsObject(CategoryObject categoryObject, Category categoryRecord) {
        try {
            categoryRecord.setCategoryName(categoryObject.getCategoryName());
            categoryRecord.setIssueTypeId(categoryObject.getIssueTypeId());
            categoryRecord.setGroupName(categoryObject.getGroupName());
            categoryRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            categoryRecord = null;
        }
        return categoryRecord;
    }
}
