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
            log.debug("Category Val" + categoryCFVal);
            String subCategoryCFVal = (String)issue.getCustomFieldValue(subCategoryCF);
            log.debug("Sub Category Val" + subCategoryCFVal);
            String categoryItemCFVal = (String)issue.getCustomFieldValue(categoryItemCF);
            log.debug("Category Item Val" + categoryItemCFVal);

            ApplicationUser user = null;
            if (null != categoryCFVal && !categoryCFVal.equalsIgnoreCase("")) {
                if (null != subCategoryCFVal && !subCategoryCFVal.equalsIgnoreCase("")) {
                    if (null != categoryItemCFVal && !categoryItemCFVal.equalsIgnoreCase("")) {
                        log.debug("Category Item is not null");
                        CategoryItem categoryItem = categoryItemController.getRecordFromAOTableByName(categoryItemCFVal);
                        if (null != categoryItem) {
                            BusinessRule businessRule = businessRuleController.getRecordFromAOTableByCategoryItemId(String.valueOf(categoryItem.getID()));
                            if (null != businessRule) {
                                log.debug("Business rule is not null. User : " + businessRule.getUserName());
                                user = ComponentAccessor.getUserManager().getUserByKey(businessRule.getUserName());
                            } else {
                                log.debug("There is no any record by category item");
                            }
                        } else {
                            log.debug("Category Item ao is null");
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