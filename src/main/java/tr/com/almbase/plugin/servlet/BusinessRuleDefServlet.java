package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;

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

    public BusinessRuleDefServlet(TemplateRenderer templateRenderer,
                                  JiraAuthenticationContext jiraAuthenticationContext,
                                  LoginUriProvider loginUriProvider,
                                  CategoryController categoryController,
                                  BusinessRuleController businessRuleController,
                                  CategoryItemController categoryItemController,
                                  SubCategoryController subCategoryController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.categoryController = categoryController;
        this.businessRuleController = businessRuleController;
        this.categoryItemController = categoryItemController;
        this.subCategoryController = subCategoryController;
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

                String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
                String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
                String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();

                String recordExists = "";
                if (initial.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req)) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryId(selectedCategoryId);
                        if (null != businessRules && businessRules.length == 1) {
                            if (null == businessRules[0].getSubCategoryId() || businessRules[0].getSubCategoryId().equalsIgnoreCase("")) {
                                String userKey = businessRules[0].getUserName();
                                if (null != userKey) {
                                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(userKey);
                                    if (null != applicationUser) {
                                        userName = applicationUser.getName();
                                        userDisplayName = applicationUser.getDisplayName();
                                        recordExists = "yes";
                                    }
                                }
                            }
                        }
                    }

                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("userName", userName);
                    context.put("userDisplayName", userDisplayName);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (subcategorychanged.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req) && isSubCategorySelected(req)) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(selectedSubCategoryId);
                        if (null != businessRules && businessRules.length == 1) {
                            if (null == businessRules[0].getCategoryItemId() || businessRules[0].getCategoryItemId().equalsIgnoreCase("")) {
                                String userKey = businessRules[0].getUserName();
                                if (null != userKey) {
                                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(userKey);
                                    if (null != applicationUser) {
                                        userName = applicationUser.getName();
                                        userDisplayName = applicationUser.getDisplayName();
                                        recordExists = "yes";
                                    }
                                }
                            }
                        }
                    }

                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("userName", userName);
                    context.put("userDisplayName", userDisplayName);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (categoryitemchanged.equalsIgnoreCase("yes")) {
                    String userName = "";
                    String userDisplayName = "";
                    if (isCategorySelected(req) && isSubCategorySelected(req) & isCategoryItemSelected(req)) {
                        BusinessRule businessRule = businessRuleController.getRecordFromAOTableByCategoryItemId(selectedCategoryItemId);
                        if (null != businessRule) {
                            String userKey = businessRule.getUserName();
                            if (null != userKey) {
                                ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(userKey);
                                if (null != applicationUser) {
                                    userName = applicationUser.getName();
                                    userDisplayName = applicationUser.getDisplayName();
                                    recordExists = "yes";
                                }
                            }
                        }
                    }

                    context.put("recordExists", recordExists);
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
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
            List<Map<String, String>> businessRuleMapList = new ArrayList<>();

            JsonParser parser = new JsonParser();
            String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
            String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
            String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();
            String selectedUserName = req.getParameter("selectedUserName") == null ? "" : req.getParameter("selectedUserName").trim();

            String actionType = req.getParameter("actionType");

            if (actionType.equalsIgnoreCase("save")) {
                if (null != selectedCategoryId && !selectedCategoryId.equalsIgnoreCase("") && null != selectedUserName && !selectedUserName.equalsIgnoreCase("")) {
                    ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(selectedUserName);
                    if (null != applicationUser) {
                        BusinessRuleObject businessRuleObject = new BusinessRuleObject();
                        businessRuleObject.setUserName(selectedUserName);
                        businessRuleObject.setCategoryId(selectedCategoryId);
                        businessRuleObject.setSubCategoryId(selectedSubCategoryId);
                        businessRuleObject.setCategoryItemId(selectedCategoryItemId);
                        businessRuleController.createRecordInAOTable(businessRuleObject);
                    }
                }
            } else if (actionType.equalsIgnoreCase("delete")) {
                if (null != selectedCategoryId && !selectedCategoryId.equalsIgnoreCase("") && null != selectedUserName && !selectedUserName.equalsIgnoreCase("")) {
                    BusinessRule foundAO = null;
                    if (null != selectedCategoryItemId && !selectedCategoryItemId.equalsIgnoreCase("")) {
                        foundAO = businessRuleController.getRecordFromAOTableByCategoryItemId(selectedCategoryItemId);
                    } else if (null != selectedSubCategoryId && !selectedSubCategoryId.equalsIgnoreCase("")) {
                        BusinessRule [] businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(selectedSubCategoryId);
                        if (null != businessRules && businessRules.length == 1) {
                            foundAO = businessRules[0];
                        }
                    } else if (null != selectedCategoryId && !selectedCategoryId.equalsIgnoreCase("")) {
                        BusinessRule [] businessRules = businessRuleController.getRecordFromAOTableByCategoryId(selectedCategoryId);
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
        if (null != selectedCategoryId && !selectedCategoryId.trim().equalsIgnoreCase("")) {
            category = categoryController.getRecordFromAOTableById(selectedCategoryId);
        }

        return null != category;
    }

    private boolean isSubCategorySelected (HttpServletRequest req) {
        String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
        SubCategory subCategory = null;
        if (null != selectedSubCategoryId && !selectedSubCategoryId.trim().equalsIgnoreCase("")) {
            subCategory = subCategoryController.getRecordFromAOTableById(selectedSubCategoryId);
        }

        return null != subCategory;
    }

    private boolean isCategoryItemSelected (HttpServletRequest req) {
        String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();
        CategoryItem categoryItem = null;
        if (null != selectedCategoryItemId && !selectedCategoryItemId.trim().equalsIgnoreCase("")) {
            categoryItem = categoryItemController.getRecordFromAOTableById(selectedCategoryItemId);
        }

        return null != categoryItem;
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
        if (isCategorySelected(req) && isSubCategorySelected(req)) {
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

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}