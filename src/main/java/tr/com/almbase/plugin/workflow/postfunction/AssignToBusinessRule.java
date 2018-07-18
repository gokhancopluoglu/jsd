package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager;
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
import java.util.ArrayList;
import java.util.List;
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
    private CategoryComponentController categoryComponentController;

    public AssignToBusinessRule(CategoryController categoryController,
                                BusinessRuleController businessRuleController,
                                CategoryItemController categoryItemController,
                                SubCategoryController subCategoryController,
                                CategoryComponentController categoryComponentController)
    {
        this.categoryController = categoryController;
        this.businessRuleController = businessRuleController;
        this.categoryItemController = categoryItemController;
        this.subCategoryController = subCategoryController;
        this.categoryComponentController = categoryComponentController;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        MutableIssue issue = getIssue(transientVars);
        try {
            log.debug("AssignToBusinessRule for Issue :  " + issue.getKey() + " started!");
            CustomField categoryCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Constants.CATEGORY_CF_ID);
            CustomField subCategoryCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Constants.SUB_CATEGORY_CF_ID);
            CustomField categoryItemCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Constants.CATEGORY_ITEM_CF_ID);
            CustomField categoryComponentCF = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(Constants.CATEGORY_COMPONENT_CF_ID);

            String categoryCFVal = (String)issue.getCustomFieldValue(categoryCF);
            if (null != categoryCFVal) {
                if (categoryCFVal.contains("<value>") && categoryCFVal.contains("</value>")) {
                    categoryCFVal = categoryCFVal.substring(categoryCFVal.indexOf("<value>") + 7, categoryCFVal.indexOf("</value>"));
                }
            }
            categoryCFVal = categoryCFVal == null ? "" : categoryCFVal;
            log.debug("Category Val : " + categoryCFVal);

            String subCategoryCFVal = (String)issue.getCustomFieldValue(subCategoryCF);
            if (null != subCategoryCFVal) {
                if (subCategoryCFVal.contains("<value>") && subCategoryCFVal.contains("</value>")) {
                    subCategoryCFVal = subCategoryCFVal.substring(subCategoryCFVal.indexOf("<value>") + 7, subCategoryCFVal.indexOf("</value>"));
                }
            }
            subCategoryCFVal = subCategoryCFVal == null ? "" : subCategoryCFVal;
            log.debug("Sub Category Val : " + subCategoryCFVal);

            String categoryItemCFVal = (String)issue.getCustomFieldValue(categoryItemCF);
            if (null != categoryItemCFVal) {
                if (categoryItemCFVal.contains("<value>") && categoryItemCFVal.contains("</value>")) {
                    categoryItemCFVal = categoryItemCFVal.substring(categoryItemCFVal.indexOf("<value>") + 7, categoryItemCFVal.indexOf("</value>"));
                }
            }
            categoryItemCFVal = categoryItemCFVal == null ? "" : categoryItemCFVal;
            log.debug("Category Item Val : " + categoryItemCFVal);

            String categoryComponentCFVal = (String)issue.getCustomFieldValue(categoryComponentCF);
            if (null != categoryComponentCFVal) {
                if (categoryComponentCFVal.contains("<value>") && categoryComponentCFVal.contains("</value>")) {
                    categoryComponentCFVal = categoryComponentCFVal.substring(categoryComponentCFVal.indexOf("<value>") + 7, categoryComponentCFVal.indexOf("</value>"));
                }
            }
            categoryComponentCFVal = categoryComponentCFVal == null ? "" : categoryComponentCFVal;
            log.debug("Category Component Val : " + categoryComponentCFVal);

            String issueTypeId = issue.getIssueType().getId();
            log.debug("Issue Type Val : " + issueTypeId);
            Category category = categoryController.getRecordFromAOTableByName(categoryCFVal);
            String categoryId = category == null ? "" : String.valueOf(category.getID());
            log.debug("categoryId : " + categoryId);
            SubCategory subCategory = subCategoryController.getRecordFromAOTableByName(subCategoryCFVal, categoryId);
            String subCategoryId = subCategory == null ? "" : String.valueOf(subCategory.getID());
            log.debug("subCategoryId : " + subCategoryId);
            CategoryItem categoryItem = categoryItemController.getRecordFromAOTableByName(categoryItemCFVal, categoryId, subCategoryId);
            String categoryItemId = categoryItem == null ? "" : String.valueOf(categoryItem.getID());
            log.debug("categoryItemId : " + categoryItemId);
            CategoryComponent categoryComponent = categoryComponentController.getRecordFromAOTableByName(categoryComponentCFVal, categoryId, subCategoryId, categoryItemId);
            String categoryComponentId = categoryComponent == null ? "" : String.valueOf(categoryComponent.getID());
            log.debug("categoryComponentId : " + categoryComponentId);

            ApplicationUser user = null;
            if (!issueTypeId.equalsIgnoreCase("")) {
                BusinessRule businessRule = businessRuleController.getRecordFromAOTableByIssueType(issueTypeId, categoryId, subCategoryId, categoryItemId, categoryComponentId);
                if (null != businessRule) {
                    log.debug("Business rule is not null. User : " + businessRule.getUserName());
                    user = ComponentAccessor.getUserManager().getUserByName(businessRule.getUserName());
                    log.debug("Business rule will run by issue type division");
                } else {
                    log.debug("There is no any record by issue type");
                }
            }

            if (null == user && !categoryComponentId.equalsIgnoreCase("")) {
                BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryComponentId(categoryComponentId);
                if (null != businessRules) {
                    List<String> userNames = new ArrayList<>();
                    for (BusinessRule br : businessRules) {
                        if (null != br.getUserName() && !br.getUserName().trim().equalsIgnoreCase("")) {
                            userNames.add(br.getUserName().trim());
                        }
                    }
                    if (userNames.size() == 1) {
                        BusinessRule businessRule = businessRules[0];
                        log.debug("Business rule is not null. User : " + businessRule.getUserName());
                        user = ComponentAccessor.getUserManager().getUserByName(businessRule.getUserName());
                        log.debug("Business rule will run by category component division");
                    } else if (userNames.size() == 0) {
                        log.debug("Zero record by category component");
                    } else {
                        log.debug("More than one business rule by category component");
                    }
                } else {
                    log.debug("There is no any record by category component");
                }
            }
            if (null == user && !categoryItemId.equalsIgnoreCase("")) {
                BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryItemId(categoryItemId);
                if (null != businessRules) {
                    List<String> userNames = new ArrayList<>();
                    for (BusinessRule br : businessRules) {
                        if (null != br.getUserName() && !br.getUserName().trim().equalsIgnoreCase("")) {
                            userNames.add(br.getUserName().trim());
                        }
                    }
                    if (userNames.size() == 1) {
                        BusinessRule businessRule = businessRules[0];
                        log.debug("Business rule is not null. User : " + businessRule.getUserName());
                        user = ComponentAccessor.getUserManager().getUserByName(businessRule.getUserName());
                        log.debug("Business rule will run by category item division");
                    } else if (userNames.size() == 0) {
                        log.debug("Zero record by category item");
                    } else {
                        log.debug("More than one business rule by category item");
                    }
                } else {
                    log.debug("There is no any record by category item");
                }
            }
            if (null == user && !subCategoryId.equalsIgnoreCase("")) {
                BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(subCategoryId);
                if (null != businessRules) {
                    List<String> userNames = new ArrayList<>();
                    for (BusinessRule br : businessRules) {
                        if (null != br.getUserName() && !br.getUserName().trim().equalsIgnoreCase("")) {
                            userNames.add(br.getUserName().trim());
                        }
                    }
                    if (userNames.size() == 1) {
                        BusinessRule businessRule = businessRules[0];
                        log.debug("Business rule is not null. User : " + businessRule.getUserName());
                        user = ComponentAccessor.getUserManager().getUserByName(businessRule.getUserName());
                        log.debug("Business rule will run by sub category division");
                    } else if (userNames.size() == 0) {
                        log.debug("Zero record by sub category");
                    } else {
                        log.debug("More than one business rule by sub category");
                    }
                } else {
                    log.debug("There is no any record by sub category");
                }
            }
            if (null == user && !categoryId.equalsIgnoreCase("")) {
                BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryId(categoryId);
                if (null != businessRules) {
                    List<String> userNames = new ArrayList<>();
                    for (BusinessRule br : businessRules) {
                        if (null != br.getUserName() && !br.getUserName().trim().equalsIgnoreCase("")) {
                            userNames.add(br.getUserName().trim());
                        }
                    }
                    if (userNames.size() == 1) {
                        BusinessRule businessRule = businessRules[0];
                        log.debug("Business rule is not null. User : " + businessRule.getUserName());
                        user = ComponentAccessor.getUserManager().getUserByName(businessRule.getUserName());
                        log.debug("Business rule will run by category division");
                    } else if (userNames.size() == 0) {
                        log.debug("Zero record by category");
                    } else {
                        log.debug("More than one business rule by category");
                    }
                } else {
                    log.debug("There is no any record by category");
                }
            }

            if (null != user) {
                ApplicationUser autodispatcher = ComponentAccessor.getUserManager().getUserByName("autodispatcher");
                ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(autodispatcher);
                issue.setAssignee(user);
                IssueManager issueManager = ComponentAccessor.getIssueManager();
                issueManager.updateIssue(autodispatcher, issue, EventDispatchOption.ISSUE_UPDATED, false);

                log.debug("Issue assigned to " + user.getName());
            } else {
                log.debug("User is null");
            }
        }
        catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }

        log.debug("AssignToBusinessRule for Issue :  " + issue.getKey() + " ended!");
    }
}