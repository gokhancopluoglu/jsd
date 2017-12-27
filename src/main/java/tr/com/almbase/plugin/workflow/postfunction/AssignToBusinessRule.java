package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.util.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class AssignToBusinessRule extends AbstractJiraFunctionProvider
{
    private static final Logger log = LoggerFactory.getLogger(AssignToBusinessRule.class);
    private CategoryController categoryController;
    private BusinessRuleController businessRuleController;
    private CategoryItemController categoryItemController;
    private SubCategoryController subCategoryController;

    public AssignToBusinessRule(CategoryController categoryController,
                                BusinessRuleController businessRuleController,
                                CategoryItemController categoryItemController,
                                SubCategoryController subCategoryController)
    {
        this.categoryController = categoryController;
        this.businessRuleController = businessRuleController;
        this.categoryItemController = categoryItemController;
        this.subCategoryController = subCategoryController;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        MutableIssue issue = getIssue(transientVars);
        try {
            CustomField categoryCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Constants.CATEGORY_CF_ID);
            CustomField subCategoryCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Constants.SUB_CATEGORY_CF_ID);
            CustomField categoryItemCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Constants.CATEGORY_ITEM_CF_ID);


            String categoryCFVal = (String)issue.getCustomFieldValue(categoryCF);
            if (null != categoryCFVal) {
                if (categoryCFVal.contains("<value>") && categoryCFVal.contains("</value>")) {
                    categoryCFVal = categoryCFVal.substring(categoryCFVal.indexOf("<value>") + 7, categoryCFVal.indexOf("</value>"));
                }
            }
            log.debug("Category Val" + categoryCFVal);

            String subCategoryCFVal = (String)issue.getCustomFieldValue(subCategoryCF);
            if (null != subCategoryCFVal) {
                if (subCategoryCFVal.contains("<value>") && subCategoryCFVal.contains("</value>")) {
                    subCategoryCFVal = subCategoryCFVal.substring(subCategoryCFVal.indexOf("<value>") + 7, subCategoryCFVal.indexOf("</value>"));
                }
            }
            log.debug("Sub Category Val" + subCategoryCFVal);

            String categoryItemCFVal = (String)issue.getCustomFieldValue(categoryItemCF);
            if (null != categoryItemCFVal) {
                if (categoryItemCFVal.contains("<value>") && categoryItemCFVal.contains("</value>")) {
                    categoryItemCFVal = categoryItemCFVal.substring(categoryItemCFVal.indexOf("<value>") + 7, categoryItemCFVal.indexOf("</value>"));
                }
            }
            log.debug("Category Item Val" + categoryItemCFVal);

            String issueTypeId = issue.getIssueType().getId();
            log.debug("Issue Type Val" + issueTypeId);

            ApplicationUser user = null;
            if (null != categoryCFVal && !categoryCFVal.equalsIgnoreCase("")) {
                if (null != subCategoryCFVal && !subCategoryCFVal.equalsIgnoreCase("")) {
                    if (null != categoryItemCFVal && !categoryItemCFVal.equalsIgnoreCase("")) {
                        log.debug("Category Item is not null");
                        CategoryItem categoryItem = categoryItemController.getRecordFromAOTableByName(categoryItemCFVal);
                        if (null != categoryItem) {
                            String categoryId = "";
                            String subCategoryId = "";
                            String categoryItemId = String.valueOf(categoryItem.getID());
                            Category category = categoryController.getRecordFromAOTableByName(categoryCFVal);
                            if (null != category)
                                categoryId = String.valueOf(category.getID());
                            SubCategory subCategory = subCategoryController.getRecordFromAOTableByName(subCategoryCFVal);
                            if (null != subCategory)
                                subCategoryId = String.valueOf(subCategory.getID());
                            BusinessRule businessRule = businessRuleController.getRecordFromAOTableByCategoryItemId(categoryItemId, categoryId, subCategoryId);
                            if (null != businessRule) {
                                log.debug("Business rule is not null. User : " + businessRule.getUserName());
                                user = ComponentAccessor.getUserManager().getUserByKey(businessRule.getUserName());
                            } else {
                                log.debug("There is no any record by category item");
                            }
                        } else {
                            log.debug("Category Item ao is null");
                        }

                        if (null == user) {
                            if (null != issueTypeId && !issueTypeId.equalsIgnoreCase("")) {
                                log.debug("Issue Type is not null");
                                String categoryId = "";
                                String subCategoryId = "";
                                Category category = categoryController.getRecordFromAOTableByName(categoryCFVal);
                                if (null != category)
                                    categoryId = String.valueOf(category.getID());
                                SubCategory subCategory = subCategoryController.getRecordFromAOTableByName(subCategoryCFVal);
                                if (null != subCategory)
                                    subCategoryId = String.valueOf(subCategory.getID());
                                BusinessRule businessRule = businessRuleController.getRecordFromAOTableByIssueType(issueTypeId, categoryId, subCategoryId);
                                if (null != businessRule) {
                                    log.debug("Business rule is not null. User : " + businessRule.getUserName());
                                    user = ComponentAccessor.getUserManager().getUserByKey(businessRule.getUserName());
                                } else {
                                    log.debug("There is no any record by category item");
                                }
                            }
                        }

                    } else if (null != issueTypeId && !issueTypeId.equalsIgnoreCase("")) {
                        log.debug("Issue Type is not null");
                        String categoryId = "";
                        String subCategoryId = "";
                        Category category = categoryController.getRecordFromAOTableByName(categoryCFVal);
                        if (null != category)
                            categoryId = String.valueOf(category.getID());
                        SubCategory subCategory = subCategoryController.getRecordFromAOTableByName(subCategoryCFVal);
                        if (null != subCategory)
                            subCategoryId = String.valueOf(subCategory.getID());
                        BusinessRule businessRule = businessRuleController.getRecordFromAOTableByIssueType(issueTypeId, categoryId, subCategoryId);
                        if (null != businessRule) {
                            log.debug("Business rule is not null. User : " + businessRule.getUserName());
                            user = ComponentAccessor.getUserManager().getUserByKey(businessRule.getUserName());
                        } else {
                            log.debug("There is no any record by category item");
                        }
                    } else {
                        log.debug("Category Item is null");
                        SubCategory subCategory = subCategoryController.getRecordFromAOTableByName(subCategoryCFVal);
                        if (null != subCategory) {
                            BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(String.valueOf(subCategory.getID()));
                            if (null != businessRules) {
                                if (businessRules.length == 1) {
                                    BusinessRule businessRule = businessRules[0];
                                    log.debug("Business rule is not null. User : " + businessRule.getUserName());
                                    user = ComponentAccessor.getUserManager().getUserByKey(businessRule.getUserName());
                                } else {
                                    log.debug("More than one business rule by sub category");
                                }
                            } else {
                                log.debug("There is no any record by sub category");
                            }
                        } else {
                            log.debug("Sub Category ao is null");
                        }
                    }
                } else {
                    log.debug("Sub Category is null");
                    Category category = categoryController.getRecordFromAOTableByName(categoryCFVal);
                    if (null != category) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryId(String.valueOf(category.getID()));
                        if (null != businessRules) {
                            if (businessRules.length == 1) {
                                BusinessRule businessRule = businessRules[0];
                                log.debug("Business rule is not null. User : " + businessRule.getUserName());
                                user = ComponentAccessor.getUserManager().getUserByKey(businessRule.getUserName());
                            }  else {
                                log.debug("More than one business rule by category");
                            }
                        }  else {
                            log.debug("There is no any record by category");
                        }
                    } else {
                        log.debug("Category ao is null");
                    }
                }

                if (null != user) {
                    issue.setAssignee(user);
                    log.debug("Issue assigned to " + user.getName());
                } else {
                    log.debug("User is null");
                }
            } else {
                log.debug("Category is null");
            }
        }
        catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }
}