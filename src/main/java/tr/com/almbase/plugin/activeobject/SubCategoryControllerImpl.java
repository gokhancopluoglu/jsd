package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class SubCategoryControllerImpl implements SubCategoryController{

    private static final Logger log = LoggerFactory.getLogger(SubCategoryControllerImpl.class);
    private ActiveObjects activeObjects;

    public SubCategoryControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public SubCategory getRecordFromAOTableByName(String subCategoryName) {
        SubCategory subCategory = null;
        try {
            SubCategory[] tempSubCategory = activeObjects.find(SubCategory.class, "SUB_CATEGORY_NAME = ?", subCategoryName);
            if (null != tempSubCategory && tempSubCategory.length > 0)
                subCategory = tempSubCategory[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return subCategory;
    }

    public SubCategory getRecordFromAOTableById(String subCategoryId) {
        SubCategory subCategory = null;
        try {
            SubCategory[] tempSubCategory = activeObjects.find(SubCategory.class, "ID = ?", Integer.parseInt(subCategoryId));
            if (null != tempSubCategory && tempSubCategory.length > 0) {
                subCategory = tempSubCategory[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return subCategory;
    }

    public SubCategory[] getRecordFromAOTableByCategoryId(String categoryId) {
        SubCategory[] subCategories = null;
        try {
            subCategories = activeObjects.find(SubCategory.class, "CATEGORY_ID = ?", categoryId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return subCategories;
    }

    public SubCategory[] getAllEntriesFromAOTable() {
        SubCategory[] subCategories = null;
        try {
            subCategories = activeObjects.find(SubCategory.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return subCategories;
    }

    public SubCategory createRecordInAOTable(SubCategoryObject subCategoryObject) {
        SubCategory returnSubCategory = null;
        try {
            log.debug("New setting!");
            SubCategory subCategoryRecord = activeObjects.create(SubCategory.class);
            if (subCategoryRecord != null)
            {
                returnSubCategory = setAOValuesAndReturnAsObject(subCategoryObject, subCategoryRecord);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnSubCategory;
    }

    public SubCategory updateRecordInAOTable(SubCategory subCategory, SubCategoryObject subCategoryObject) {
        try {
            if (subCategory != null) {
                subCategory.setSubCategoryName(subCategoryObject.getSubCategoryName());
                subCategory.save();
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return subCategory;
    }

    public void deleteRecordFromAOTable(SubCategory subCategory) {
        try {
            activeObjects.delete(subCategory);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private SubCategory setAOValuesAndReturnAsObject(SubCategoryObject subCategoryObject, SubCategory subCategoryRecord) {
        try {
            subCategoryRecord.setCategoryId(subCategoryObject.getCategoryId());
            subCategoryRecord.setSubCategoryName(subCategoryObject.getSubCategoryName());
            subCategoryRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            subCategoryRecord = null;
        }
        return subCategoryRecord;
    }
}
