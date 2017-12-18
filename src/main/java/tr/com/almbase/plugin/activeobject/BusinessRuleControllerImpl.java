package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class BusinessRuleControllerImpl implements BusinessRuleController{

    private static final Logger log = LoggerFactory.getLogger(BusinessRuleControllerImpl.class);
    private ActiveObjects activeObjects;

    public BusinessRuleControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public BusinessRule getRecordFromAOTableByUserName(String userName) {
        BusinessRule businessRule = null;
        try {
            BusinessRule[] tempBusinessRule = activeObjects.find(BusinessRule.class, "USER_NAME = ?", userName);
            if (null != tempBusinessRule && tempBusinessRule.length > 0)
                businessRule = tempBusinessRule[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return businessRule;
    }

    public BusinessRule getRecordFromAOTableById(String businessRuleId) {
        BusinessRule businessRule = null;
        try {
            BusinessRule[] tempBusinessRule = activeObjects.find(BusinessRule.class, "ID = ?", Integer.parseInt(businessRuleId));
            if (null != tempBusinessRule && tempBusinessRule.length > 0)
                businessRule = tempBusinessRule[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return businessRule;
    }

    public BusinessRule[] getRecordFromAOTableByCategoryId(String categoryId) {
        BusinessRule[] businessRules = null;
        try {
            businessRules = activeObjects.find(BusinessRule.class, "CATEGORY_ID = ?", categoryId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return businessRules;
    }

    public BusinessRule[] getRecordFromAOTableBySubCategoryId(String subCategoryId) {
        BusinessRule[] businessRules = null;
        try {
            businessRules = activeObjects.find(BusinessRule.class, "SUB_CATEGORY_ID = ?", subCategoryId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return businessRules;
    }

    public BusinessRule getRecordFromAOTableByCategoryItemId(String categoryItemId) {
        BusinessRule businessRule = null;
        try {
            BusinessRule[] tempBusinessRule = activeObjects.find(BusinessRule.class, "CATEGORY_ITEM_ID = ?", categoryItemId);
            if (null != tempBusinessRule && tempBusinessRule.length > 0)
                businessRule = tempBusinessRule[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return businessRule;
    }

    public BusinessRule[] getAllEntriesFromAOTable() {
        BusinessRule[] businessRules = null;
        try {
            businessRules = activeObjects.find(BusinessRule.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return businessRules;
    }

    public BusinessRule createRecordInAOTable(BusinessRuleObject businessRuleObject) {
        BusinessRule returnBusinessRule = null;
        try {
            log.debug("New setting!");
            BusinessRule businessRuleRecord = activeObjects.create(BusinessRule.class);
            if (businessRuleRecord != null)
            {
                returnBusinessRule = setAOValuesAndReturnAsObject(businessRuleObject, businessRuleRecord);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnBusinessRule;
    }

    public BusinessRule updateRecordInAOTable(BusinessRule businessRule, BusinessRuleObject businessRuleObject) {
        try {
            if (businessRule != null) {
                businessRule.setSubCategoryId(businessRuleObject.getSubCategoryId());
                businessRule.setCategoryItemId(businessRuleObject.getCategoryItemId());
                businessRule.setUserName(businessRuleObject.getUserName());
                businessRule.save();
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return businessRule;
    }

    public void deleteRecordFromAOTable(BusinessRule businessRule) {
        try {
            activeObjects.delete(businessRule);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private BusinessRule setAOValuesAndReturnAsObject(BusinessRuleObject businessRuleObject, BusinessRule businessRuleRecord) {
        try {
            businessRuleRecord.setCategoryId(businessRuleObject.getCategoryId());
            businessRuleRecord.setSubCategoryId(businessRuleObject.getSubCategoryId());
            businessRuleRecord.setCategoryItemId(businessRuleObject.getCategoryItemId());
            businessRuleRecord.setUserName(businessRuleObject.getUserName());
            businessRuleRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            businessRuleRecord = null;
        }
        return businessRuleRecord;
    }
}
