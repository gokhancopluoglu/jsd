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

                String tableParameters = req.getParameter("tableData");

                String addewrow = req.getParameter("addnewrow") == null ? "" : req.getParameter("addnewrow").trim();
                String deleterow = req.getParameter("deleterow") == null ? "" : req.getParameter("deleterow").trim();
                String initial = req.getParameter("initial") == null ? "" : req.getParameter("initial").trim();
                String subcategorychanged = req.getParameter("subcategorychanged") == null ? "" : req.getParameter("subcategorychanged").trim();
                String categoryitemchanged = req.getParameter("categoryitemchanged") == null ? "" : req.getParameter("categoryitemchanged").trim();

                String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
                String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
                String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();

                List<Map<String, String>> businessRuleList = new ArrayList<>();

                if (addewrow.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req)) {
                        JsonParser parser = new JsonParser();
                        JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                        for (int i = 0; i < jsonTableArray.size(); i++) {
                            JsonElement jsonTableRowElement = jsonTableArray.get(i);
                            JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                            Map<String, String> businessRuleMap = new HashMap<>();
                            String businessRuleId = jsonTableRowArray.get(0).getAsString();
                            String userName = jsonTableRowArray.get(1).getAsString();
                            businessRuleMap.put("businessRuleId", businessRuleId);
                            businessRuleMap.put("userName", userName);
                            businessRuleList.add(businessRuleMap);
                        }

                        Map<String, String> businessRuleMap = new HashMap<>();
                        businessRuleMap.put("businessRuleId", "");
                        businessRuleMap.put("userName", "");
                        businessRuleList.add(businessRuleMap);
                    }

                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("businessRuleList", businessRuleList);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (deleterow.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req)) {
                        JsonParser parser = new JsonParser();
                        JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                        for (int i = 0; i < jsonTableArray.size(); i++) {
                            JsonElement jsonTableRowElement = jsonTableArray.get(i);
                            JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                            Map<String, String> businessRuleMap = new HashMap<>();
                            String businessRuleId = jsonTableRowArray.get(0).getAsString();
                            String userName = jsonTableRowArray.get(1).getAsString();
                            businessRuleMap.put("businessRuleId", businessRuleId);
                            businessRuleMap.put("userName", userName);
                            businessRuleList.add(businessRuleMap);
                        }
                    }

                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("businessRuleList", businessRuleList);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (initial.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req)) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableByCategoryId(selectedCategoryId);
                        if (businessRules.length > 0) {
                            Arrays.sort(businessRules, Comparator.comparing(BusinessRule::getUserName));
                            for (BusinessRule businessRule : businessRules) {
                                if (businessRule.getSubCategoryId().equalsIgnoreCase("") && businessRule.getCategoryItemId().equalsIgnoreCase("")) {
                                    Map<String, String> businessRuleMap = new HashMap<>();
                                    businessRuleMap.put("businessRuleId", String.valueOf(businessRule.getID()));
                                    businessRuleMap.put("userName", businessRule.getUserName());
                                    businessRuleList.add(businessRuleMap);
                                }
                            }
                        } else {
                            Map<String, String> businessRuleMap = new HashMap<>();
                            businessRuleMap.put("businessRuleId", "");
                            businessRuleMap.put("userName", "");
                            businessRuleList.add(businessRuleMap);
                        }
                    }

                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("businessRuleList", businessRuleList);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (subcategorychanged.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req) && isSubCategorySelected(req)) {
                        BusinessRule[] businessRules = businessRuleController.getRecordFromAOTableBySubCategoryId(selectedSubCategoryId);
                        if (businessRules.length > 0) {
                            Arrays.sort(businessRules, Comparator.comparing(BusinessRule::getUserName));
                            for (BusinessRule businessRule : businessRules) {
                                if (businessRule.getCategoryItemId().equalsIgnoreCase("")) {
                                    Map<String, String> businessRuleMap = new HashMap<>();
                                    businessRuleMap.put("businessRuleId", String.valueOf(businessRule.getID()));
                                    businessRuleMap.put("userName", businessRule.getUserName());
                                    businessRuleList.add(businessRuleMap);
                                }
                            }
                        } else {
                            Map<String, String> businessRuleMap = new HashMap<>();
                            businessRuleMap.put("businessRuleId", "");
                            businessRuleMap.put("userName", "");
                            businessRuleList.add(businessRuleMap);
                        }
                    }
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("businessRuleList", businessRuleList);
                    templateRenderer.render(BUSINESS_RULE_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (categoryitemchanged.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req) && isSubCategorySelected(req) & isCategoryItemSelected(req)) {
                        BusinessRule businessRule = businessRuleController.getRecordFromAOTableByCategoryItemId(selectedCategoryItemId);
                        if (null != businessRule) {
                            Map<String, String> businessRuleMap = new HashMap<>();
                            businessRuleMap.put("businessRuleId", String.valueOf(businessRule.getID()));
                            businessRuleMap.put("userName", businessRule.getUserName());
                            businessRuleList.add(businessRuleMap);
                        } else {
                            Map<String, String> businessRuleMap = new HashMap<>();
                            businessRuleMap.put("businessRuleId", "");
                            businessRuleMap.put("userName", "");
                            businessRuleList.add(businessRuleMap);
                        }
                    }
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("selectedCategoryItemId", selectedCategoryItemId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", getCategoryItemList(req, selectedSubCategoryId));
                    context.put("businessRuleList", businessRuleList);
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
            String tableParameters = req.getParameter("tableData");
            String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
            String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
            String selectedCategoryItemId = req.getParameter("selectedCategoryItemId") == null ? "" : req.getParameter("selectedCategoryItemId").trim();
            JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

            for (int i = 0; i < jsonTableArray.size(); i++) {
                JsonElement jsonTableRowElement = jsonTableArray.get(i);
                JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                String businessRuleId = jsonTableRowArray.get(0).getAsString();
                String userName = jsonTableRowArray.get(1).getAsString();

                Map<String, String> businessRuleMap = new HashMap<>();
                businessRuleMap.put("businessRuleId", businessRuleId);
                businessRuleMap.put("userName", userName);
                businessRuleMapList.add(businessRuleMap);
            }

            Iterator businessRuleIterator = businessRuleMapList.iterator();
            List<String> businessRuleIdList = new ArrayList<>();
            while (businessRuleIterator.hasNext()) {
                Map<String, String> businessRuleMap = (Map<String, String>) businessRuleIterator.next();
                String businessRuleId = businessRuleMap.get("businessRuleId");
                String userName = businessRuleMap.get("userName");

                if (null != businessRuleId && !businessRuleId.equalsIgnoreCase("")) {
                    if (null != userName && !userName.equalsIgnoreCase("")) {
                        BusinessRule businessRule = businessRuleController.getRecordFromAOTableById(businessRuleId);
                        BusinessRuleObject businessRuleObject = new BusinessRuleObject();
                        businessRuleObject.setUserName(userName);
                        businessRuleObject.setCategoryId(selectedCategoryId);
                        businessRuleObject.setSubCategoryId(selectedSubCategoryId);
                        businessRuleObject.setCategoryItemId(selectedCategoryItemId);
                        businessRuleController.updateRecordInAOTable(businessRule, businessRuleObject);
                    }
                    businessRuleIdList.add(businessRuleId);
                } else {
                    if (null != userName && !userName.equalsIgnoreCase("")) {
                        BusinessRuleObject businessRuleObject = new BusinessRuleObject();
                        businessRuleObject.setUserName(userName);
                        businessRuleObject.setCategoryId(selectedCategoryId);
                        businessRuleObject.setSubCategoryId(selectedSubCategoryId);
                        businessRuleObject.setCategoryItemId(selectedCategoryItemId);
                        BusinessRule businessRule = businessRuleController.createRecordInAOTable(businessRuleObject);
                        businessRuleIdList.add(String.valueOf(businessRule.getID()));
                    }
                }
            }

            BusinessRule[] businessRules = businessRuleController.getAllEntriesFromAOTable();
            for (BusinessRule businessRule : businessRules) {
                if (!businessRuleIdList.contains(String.valueOf(businessRule.getID()))) {
                    deleteBusinessRule(businessRule);
                }
            }
        }
    }

    private void deleteBusinessRule (BusinessRule businessRule) {
        //Delete Business Rule
        businessRuleController.deleteRecordFromAOTable(businessRule);
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