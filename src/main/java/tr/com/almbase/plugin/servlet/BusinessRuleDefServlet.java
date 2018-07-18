package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class BusinessRuleDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleDefServlet.class);
    private static final String BUSINESS_RULE_DEF_TEMPLATE = "/templates/adminscreens/businessrule.vm";
    private static final String BUSINESS_RULE_DETAIL_DEF_TEMPLATE = "/templates/adminscreens/businessruledetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private CategoryController categoryController;
    private BusinessRuleController businessRuleController;
    private CategoryItemController categoryItemController;
    private SubCategoryController subCategoryController;
    private CategoryComponentController categoryComponentController;

    public BusinessRuleDefServlet(TemplateRenderer templateRenderer,
                                  JiraAuthenticationContext jiraAuthenticationContext,
                                  LoginUriProvider loginUriProvider,
                                  CategoryController categoryController,
                                  BusinessRuleController businessRuleController,
                                  CategoryItemController categoryItemController,
                                  SubCategoryController subCategoryController,
                                  CategoryComponentController categoryComponentController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.categoryController = categoryController;
        this.businessRuleController = businessRuleController;
        this.categoryItemController = categoryItemController;
        this.subCategoryController = subCategoryController;
        this.categoryComponentController = categoryComponentController;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, Object> context = Maps.newHashMap();

        if (null == jiraAuthenticationContext.getLoggedInUser())
        {
            redirectToLogin(req, resp);
        } else {
            ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
            Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();

            if (administrators.contains(user)) {
                String initial = req.getParameter("initial") == null ? "" : req.getParameter("initial").trim();
                String subcategorychanged = req.getParameter("subcategorychanged") == null ? "" : req.getParameter("subcategorychanged").trim();
                String categoryitemchanged = req.getParameter("categoryitemchanged") == null ? "" : req.getParameter("categoryitemchanged").trim();
                String categorycomponentchanged = req.getParameter("categorycomponentchanged") == null ? "" : req.getParameter("categorycomponentchanged").trim();
                String issuetypechanged = req.getParameter("issuetypechanged") == null ? "" : req.getParameter("issuetypechanged").trim();

                String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
                String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
                String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();
                String selectedCategoryComponentId = req.getParameter("selectedCategoryComponentId") == null ? "" : req.getParameter("selectedCategoryComponentId").trim();
                String selectedIssueType = req.getParameter("selectedIssueType") == null ? "" : req.getParameter("selectedIssueType").trim();

                String recordExists = "";
                if (initial.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req)) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryId(selectedCategoryId);
                        if (null != businessRules && businessRules.length == 1) {
                            BusinessRule businessRule = businessRules[0];
                            String subCategoryId = businessRule.getSubCategoryId() == null ? "" : businessRule.getSubCategoryId().trim();
                            String issueType = businessRule.getIssueType() == null ? "" : businessRule.getIssueType().trim();
                            if (subCategoryId.equalsIgnoreCase("") && issueType.equalsIgnoreCase("")) {
                                String userKey = businessRule.getUserName();
                                if (null != userKey) {
                                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(userKey);
                                    if (null != applicationUser) {
                                        userName = applicationUser.getName();
                                        userDisplayName = applicationUser.getDisplayName();
                                        recordExists = "yes";
                                    }
                                }
                            }
                        }
                    }

                    context.put("businessRuleList", getBusinessRules(selectedCategoryId, null, null,null,null));
                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("selectedCategoryComponentId", selectedCategoryComponentId);
                    context.put("selectedIssueType", selectedIssueType);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    //context.put("businessRuleMapList", getBusinessRuleMapList(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId, selectedCategoryComponentId, selectedIssueType));
                    context.put("issueTypes", getIssueTypes());
                    context.put("userName", userName);
                    context.put("userDisplayName", userDisplayName);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (subcategorychanged.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req) && isSubCategorySelected(req)) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(selectedSubCategoryId);
                        if (null != businessRules && businessRules.length == 1) {
                            BusinessRule businessRule = businessRules[0];
                            String categoryItemId = businessRule.getCategoryItemId() == null ? "" : businessRule.getCategoryItemId().trim();
                            String issueType = businessRule.getIssueType() == null ? "" : businessRule.getIssueType().trim();
                            if (categoryItemId.equalsIgnoreCase("") && issueType.equalsIgnoreCase("")) {
                                String userKey = businessRule.getUserName();
                                if (null != userKey) {
                                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(userKey);
                                    if (null != applicationUser) {
                                        userName = applicationUser.getName();
                                        userDisplayName = applicationUser.getDisplayName();
                                        recordExists = "yes";
                                    }
                                }
                            }
                        }
                    }

                    context.put("businessRuleList", getBusinessRules(selectedCategoryId, selectedSubCategoryId, null,null,null));
                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("selectedCategoryComponentId", selectedCategoryComponentId);
                    context.put("selectedIssueType", selectedIssueType);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    //context.put("businessRuleMapList", getBusinessRuleMapList(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId, selectedCategoryComponentId, selectedIssueType));
                    context.put("issueTypes", getIssueTypes());
                    context.put("userName", userName);
                    context.put("userDisplayName", userDisplayName);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (categoryitemchanged.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req) && isSubCategorySelected(req) & isCategoryItemSelected(req)) {
                        BusinessRule [] businessRules = businessRuleController.getRecordFromAOTableByCategoryItemId(selectedCategoryItemId);
                        if (null != businessRules && businessRules.length == 1) {
                            BusinessRule businessRule = businessRules[0];
                            String categoryComponentId = businessRule.getCategoryComponentId() == null ? "" : businessRule.getCategoryComponentId().trim();
                            String issueType = businessRule.getIssueType() == null ? "" : businessRule.getIssueType().trim();
                            if (categoryComponentId.equalsIgnoreCase("") && issueType.equalsIgnoreCase("")) {
                                String userKey = businessRule.getUserName();
                                if (null != userKey) {
                                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(userKey);
                                    if (null != applicationUser) {
                                        userName = applicationUser.getName();
                                        userDisplayName = applicationUser.getDisplayName();
                                        recordExists = "yes";
                                    }
                                }
                            }
                        }
                    }
                    context.put("businessRuleList", getBusinessRules(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId,null,null));
                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("selectedCategoryComponentId", selectedCategoryComponentId);
                    context.put("selectedIssueType", selectedIssueType);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("categoryComponentList", getCategoryComponentList(req, selectedCategoryItemId));
                    //context.put("businessRuleMapList", getBusinessRuleMapList(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId, selectedCategoryComponentId, selectedIssueType));
                    context.put("issueTypes", getIssueTypes());
                    context.put("userName", userName);
                    context.put("userDisplayName", userDisplayName);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (categorycomponentchanged.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req) && isSubCategorySelected(req) && isCategoryItemSelected(req) && isCategoryComponentSelected(req)) {
                        BusinessRule [] businessRules = businessRuleController.getRecordFromAOTableByCategoryComponentId(selectedCategoryComponentId);
                        if (null != businessRules && businessRules.length == 1) {
                            BusinessRule businessRule = businessRules[0];
                            String issueType = businessRule.getIssueType() == null ? "" : businessRule.getIssueType().trim();
                            if (issueType.equalsIgnoreCase("")) {
                                String userKey = businessRule.getUserName();
                                if (null != userKey) {
                                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(userKey);
                                    if (null != applicationUser) {
                                        userName = applicationUser.getName();
                                        userDisplayName = applicationUser.getDisplayName();
                                        recordExists = "yes";
                                    }
                                }
                            }
                        }
                    }
                    context.put("businessRuleList", getBusinessRules(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId,selectedCategoryComponentId,null));
                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("selectedCategoryComponentId", selectedCategoryComponentId);
                    context.put("selectedIssueType", selectedIssueType);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("categoryComponentList", getCategoryComponentList(req, selectedCategoryItemId));
                    //context.put("businessRuleMapList", getBusinessRuleMapList(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId, selectedCategoryComponentId, selectedIssueType));
                    context.put("issueTypes", getIssueTypes());
                    context.put("userName", userName);
                    context.put("userDisplayName", userDisplayName);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (issuetypechanged.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req) && isIssueTypeSelected(req)) {
                        Utils.printDebug("issuetypechanged : selectedIssueType : " + selectedIssueType);
                        Utils.printDebug("issuetypechanged : selectedCategoryId : " + selectedCategoryId);
                        Utils.printDebug("issuetypechanged : selectedSubCategoryId : " + selectedSubCategoryId);
                        Utils.printDebug("issuetypechanged : selectedCategoryItemId : " + selectedCategoryItemId);
                        Utils.printDebug("issuetypechanged : selectedCategoryComponentId : " + selectedCategoryComponentId);
                        BusinessRule businessRule = businessRuleController.getRecordFromAOTableByAllParameters(selectedIssueType, selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId, selectedCategoryComponentId);
                        if (null != businessRule) {
                            Utils.printDebug("issuetypechanged : businessRule is founded.");
                            String userKey = businessRule.getUserName();
                            if (null != userKey) {
                                ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(userKey);
                                if (null != applicationUser) {
                                    userName = applicationUser.getName();
                                    userDisplayName = applicationUser.getDisplayName();
                                    Utils.printDebug("issuetypechanged : userDisplayName : " + userDisplayName);
                                    recordExists = "yes";
                                }
                            }
                        } else {
                            Utils.printDebug("issuetypechanged : businessRule is null!");
                        }
                    }

                    context.put("businessRuleList", getBusinessRules(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId,selectedCategoryComponentId,selectedIssueType));
                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("selectedCategoryComponentId", selectedCategoryComponentId);
                    context.put("selectedIssueType", selectedIssueType);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("categoryComponentList", getCategoryComponentList(req, selectedCategoryItemId));
                    //context.put("businessRuleMapList", getBusinessRuleMapList(selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId, selectedCategoryComponentId, selectedIssueType));
                    context.put("issueTypes", getIssueTypes());
                    context.put("userName", userName);
                    context.put("userDisplayName", userDisplayName);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else {
                    Category[] categories = categoryController.getAllEntriesFromAOTable();
                    Arrays.sort(categories, Comparator.comparing(Category::getCategoryName));
                    List<Map<String, String>> categoryList = new ArrayList<>();
                    for (Category category : categories) {
                        Map<String, String> categoryMap = new HashMap<>();
                        categoryMap.put("categoryId", String.valueOf(category.getID()));
                        categoryMap.put("categoryName", category.getCategoryName());
                        categoryList.add(categoryMap);
                    }
                    context.put("categoryList", categoryList);
                    templateRenderer.render(BUSINESS_RULE_DEF_TEMPLATE, context, resp.getWriter());
                }
            } else {
                log.debug("User isn't admin!");
                templateRenderer.render(NOT_AUTH_TEMPLATE, context, resp.getWriter());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (null == jiraAuthenticationContext.getLoggedInUser()) {
            redirectToLogin(req, resp);
        } else {
            String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
            String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
            String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();
            String selectedCategoryComponentId = req.getParameter("selectedCategoryComponentId") == null ? "" : req.getParameter("selectedCategoryComponentId").trim();
            String selectedIssueType = req.getParameter("selectedIssueType") == null ? "" : req.getParameter("selectedIssueType").trim();
            String selectedUserName = req.getParameter("selectedUserName") == null ? "" : req.getParameter("selectedUserName").trim();

            String actionType = req.getParameter("actionType");

            if (actionType.equalsIgnoreCase("save")) {
                if (!selectedCategoryId.equalsIgnoreCase("") && !selectedUserName.equalsIgnoreCase("")) {
                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByName(selectedUserName);
                    if (null != applicationUser) {
                        BusinessRuleObject businessRuleObject = new BusinessRuleObject();
                        businessRuleObject.setUserName(selectedUserName);
                        businessRuleObject.setCategoryId(selectedCategoryId);
                        businessRuleObject.setSubCategoryId(selectedSubCategoryId);
                        businessRuleObject.setCategoryItemId(selectedCategoryItemId);
                        businessRuleObject.setCategoryComponentId(selectedCategoryComponentId);
                        businessRuleObject.setIssueType(selectedIssueType);
                        businessRuleController.createRecordInAOTable(businessRuleObject);
                    }
                }
            } else if (actionType.equalsIgnoreCase("delete")) {
                if (!selectedCategoryId.equalsIgnoreCase("") && !selectedUserName.equalsIgnoreCase("")) {
                    BusinessRule foundAO = null;

                    if (!selectedIssueType.equalsIgnoreCase("")) {
                        BusinessRule businessRule = businessRuleController.getRecordFromAOTableByAllParameters(selectedIssueType, selectedCategoryId, selectedSubCategoryId, selectedCategoryItemId, selectedCategoryComponentId);
                        if (null != businessRule) {
                            foundAO = businessRule;
                        }
                    } else if (!selectedCategoryComponentId.equalsIgnoreCase("")) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryComponentId(selectedCategoryComponentId);
                        if (null != businessRules && businessRules.length == 1) {
                            foundAO = businessRules[0];
                        }
                    } else if (!selectedCategoryItemId.equalsIgnoreCase("")) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryItemId(selectedCategoryItemId);
                        if (null != businessRules && businessRules.length == 1) {
                            foundAO = businessRules[0];
                        }
                    } else if (!selectedSubCategoryId.equalsIgnoreCase("")) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(selectedSubCategoryId);
                        if (null != businessRules && businessRules.length == 1) {
                            foundAO = businessRules[0];
                        }
                    } else if (!selectedCategoryId.equalsIgnoreCase("")) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryId(selectedCategoryId);
                        if (null != businessRules && businessRules.length == 1) {
                            foundAO = businessRules[0];
                        }
                    }

                    if (null != foundAO) {
                        businessRuleController.deleteRecordFromAOTable(foundAO);
                    }
                }
            }
        }
    }



    private boolean isCategorySelected (HttpServletRequest req) {
        String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
        Category category = null;
        if (!selectedCategoryId.trim().equalsIgnoreCase("")) {
            category = categoryController.getRecordFromAOTableById(selectedCategoryId);
        }

        return null != category;
    }

    private boolean isSubCategorySelected (HttpServletRequest req) {
        String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
        SubCategory subCategory = null;
        if (!selectedSubCategoryId.trim().equalsIgnoreCase("")) {
            subCategory = subCategoryController.getRecordFromAOTableById(selectedSubCategoryId);
        }

        return null != subCategory;
    }

    private boolean isCategoryItemSelected (HttpServletRequest req) {
        String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();
        CategoryItem categoryItem = null;
        if (!selectedCategoryItemId.trim().equalsIgnoreCase("")) {
            categoryItem = categoryItemController.getRecordFromAOTableById(selectedCategoryItemId);
        }

        return null != categoryItem;
    }

    private boolean isCategoryComponentSelected (HttpServletRequest req) {
        String selectedCategoryComponentId = req.getParameter("selectedCategoryComponentId") == null ? "" : req.getParameter("selectedCategoryComponentId").trim();
        CategoryComponent categoryComponent = null;
        if (!selectedCategoryComponentId.trim().equalsIgnoreCase("")) {
            categoryComponent = categoryComponentController.getRecordFromAOTableById(selectedCategoryComponentId);
        }

        return null != categoryComponent;
    }

    private boolean isIssueTypeSelected (HttpServletRequest req) {
        String selectedIssueType = req.getParameter("selectedIssueType") == null ? "" : req.getParameter("selectedIssueType").trim();
        IssueType issueType = ComponentAccessor.getConstantsManager().getIssueType(selectedIssueType);
        return null != issueType;
    }

    private List<Map<String, String>> getSubCategoryList (HttpServletRequest req, String selectedCategoryId) {
        List<Map<String, String>> subCategoryList = new ArrayList<>();
        if (isCategorySelected(req)) {
            SubCategory[] subCategories = subCategoryController.getRecordFromAOTableByCategoryId(selectedCategoryId);
            if (subCategories.length > 0) {
                Arrays.sort(subCategories, Comparator.comparing(SubCategory::getSubCategoryName));
                for (SubCategory subCategory : subCategories) {
                    Map<String, String> subCategoryMap = new HashMap<>();
                    subCategoryMap.put("subCategoryId", String.valueOf(subCategory.getID()));
                    subCategoryMap.put("subCategoryName", subCategory.getSubCategoryName());
                    subCategoryList.add(subCategoryMap);
                }
            }
        }

        return subCategoryList;
    }

    private List<Map<String, String>> getCategoryItemList (HttpServletRequest req, String selectedSubCategoryId) {
        List<Map<String, String>> categoryItemList = new ArrayList<>();
        if (isSubCategorySelected(req)) {
            CategoryItem[] categoryItems = categoryItemController.getRecordFromAOTableBySubCategoryId(selectedSubCategoryId);
            if (categoryItems.length > 0) {
                Arrays.sort(categoryItems, Comparator.comparing(CategoryItem::getCategoryItemName));
                for (CategoryItem categoryItem : categoryItems) {
                    Map<String, String> categoryItemMap = new HashMap<>();
                    categoryItemMap.put("categoryItemId", String.valueOf(categoryItem.getID()));
                    categoryItemMap.put("categoryItemName", categoryItem.getCategoryItemName());
                    categoryItemList.add(categoryItemMap);
                }
            }
        }

        return categoryItemList;
    }

    private List<Map<String, String>> getCategoryComponentList (HttpServletRequest req, String selectedCategoryItemId) {
        List<Map<String, String>> categoryComponentList = new ArrayList<>();
        if (isCategoryItemSelected(req)) {
            CategoryComponent[] categoryComponents = categoryComponentController.getRecordFromAOTableByCategoryItemId(selectedCategoryItemId);
            if (categoryComponents.length > 0) {
                Arrays.sort(categoryComponents, Comparator.comparing(CategoryComponent::getCategoryComponentName));
                for (CategoryComponent categoryComponent : categoryComponents) {
                    Map<String, String> categoryComponentMap = new HashMap<>();
                    categoryComponentMap.put("categoryComponentId", String.valueOf(categoryComponent.getID()));
                    categoryComponentMap.put("categoryComponentName", categoryComponent.getCategoryComponentName());
                    categoryComponentList.add(categoryComponentMap);
                }
            }
        }

        return categoryComponentList;
    }

    private Map<String, IssueType> getIssueTypes () {
        Map<String, IssueType> issueTypeMap = new HashMap<>();
        Collection<IssueType> issueTypeList = ComponentAccessor.getConstantsManager().getAllIssueTypeObjects();
        for (IssueType issueType : issueTypeList) {
            issueTypeMap.put(issueType.getId(), issueType);
        }
        return issueTypeMap;
    }

    private List<Map<String, String>> getBusinessRules (String categoryId, String subCategoryId, String categoryItemId, String categoryComponentId, String issueTypeId) {

        List<Map<String, String>> businessRuleList = new ArrayList<>();
        BusinessRule [] businessRules = null;
        try {

            if (issueTypeId != null && !issueTypeId.equalsIgnoreCase("")) {
                businessRules = businessRuleController.getAllRecordFromAOTableByIssueType(issueTypeId, categoryId, subCategoryId, categoryItemId, categoryComponentId);
            } else if (null != categoryComponentId && !categoryComponentId.equalsIgnoreCase("")) {
                businessRules = businessRuleController.getRecordFromAOTableByCategoryComponentId(categoryComponentId);
            } else if (null != categoryItemId && !categoryItemId.equalsIgnoreCase("")) {
                businessRules = businessRuleController.getRecordFromAOTableByCategoryItemId(categoryItemId);
            } else if (null != subCategoryId && !subCategoryId.equalsIgnoreCase("")) {
                businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(subCategoryId);
            } else if (null != categoryId && !categoryId.equalsIgnoreCase("")) {
                businessRules = businessRuleController.getRecordFromAOTableByCategoryId(categoryId);
            }
            if (null != businessRules) {
                for (BusinessRule businessRule : businessRules) {
                    Map<String, String> businessRuleMap = new HashMap<>();
                    if (null != businessRule.getCategoryId() && !businessRule.getCategoryId().equalsIgnoreCase("")) {
                        Category category = categoryController.getRecordFromAOTableById(businessRule.getCategoryId());
                        String categoryName = category == null ? "" : category.getCategoryName();
                        businessRuleMap.put("categoryName", categoryName);
                    }

                    if (null != businessRule.getSubCategoryId() && !businessRule.getSubCategoryId().equalsIgnoreCase("")) {
                        SubCategory subCategory = subCategoryController.getRecordFromAOTableById(businessRule.getSubCategoryId());
                        String subCategoryName = subCategory == null ? "" : subCategory.getSubCategoryName();
                        businessRuleMap.put("subCategoryName", subCategoryName);
                    }

                    if (null != businessRule.getCategoryItemId() && !businessRule.getCategoryItemId().equalsIgnoreCase("")) {
                        CategoryItem categoryItem = categoryItemController.getRecordFromAOTableById(businessRule.getCategoryItemId());
                        String categoryItemName = categoryItem == null ? "" : categoryItem.getCategoryItemName();
                        businessRuleMap.put("categoryItemName", categoryItemName);
                    }

                    if (null != businessRule.getCategoryComponentId() && !businessRule.getCategoryComponentId().equalsIgnoreCase("")) {
                        CategoryComponent categoryComponent = categoryComponentController.getRecordFromAOTableById(businessRule.getCategoryComponentId());
                        String categoryComponentName = categoryComponent == null ? "" : categoryComponent.getCategoryComponentName();
                        businessRuleMap.put("categoryComponentName", categoryComponentName);
                    }

                    IssueType issueType = ComponentAccessor.getConstantsManager().getIssueType(businessRule.getIssueType());
                    businessRuleMap.put("issueTypeName", issueType == null ? "" : issueType.getName());


                    ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(businessRule.getUserName());
                    businessRuleMap.put("userName", user == null ? "" : user.getName());

                    businessRuleList.add(businessRuleMap);
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return  businessRuleList;
    }




    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}
