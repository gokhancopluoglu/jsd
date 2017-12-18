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

public class CategoryItemDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(CategoryItemDefServlet.class);
    private static final String CATEGORY_ITEM_DEF_TEMPLATE = "/templates/adminscreens/categoryitem.vm";
    private static final String CATEGORY_ITEM_DETAIL_DEF_TEMPLATE = "/templates/adminscreens/categoryitemdetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private CategoryController categoryController;
    private BusinessRuleController businessRuleController;
    private CategoryItemController categoryItemController;
    private SubCategoryController subCategoryController;

    public CategoryItemDefServlet(TemplateRenderer templateRenderer,
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
                String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
                String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();

                List<Map<String, String>> categoryItemList = new ArrayList<>();

                if (addewrow.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req) && isSubCategorySelected(req)) {
                        JsonParser parser = new JsonParser();
                        JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                        for (int i = 0; i < jsonTableArray.size(); i++) {
                            JsonElement jsonTableRowElement = jsonTableArray.get(i);
                            JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                            Map<String, String> categoryItemMap = new HashMap<>();
                            String categoryItemId = jsonTableRowArray.get(0).getAsString();
                            String categoryItemName = jsonTableRowArray.get(1).getAsString();
                            categoryItemMap.put("categoryItemId", categoryItemId);
                            categoryItemMap.put("categoryItemName", categoryItemName);
                            categoryItemList.add(categoryItemMap);
                        }

                        Map<String, String> categoryItemMap = new HashMap<>();
                        categoryItemMap.put("categoryItemId", "");
                        categoryItemMap.put("categoryItemName", "");

                        categoryItemList.add(categoryItemMap);
                    }

                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", categoryItemList);
                    templateRenderer.render(CATEGORY_ITEM_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (deleterow.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req) && isSubCategorySelected(req)) {
                        JsonParser parser = new JsonParser();
                        JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                        for (int i = 0; i < jsonTableArray.size(); i++) {
                            JsonElement jsonTableRowElement = jsonTableArray.get(i);
                            JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                            Map<String, String> categoryItemMap = new HashMap<>();
                            String categoryItemId = jsonTableRowArray.get(0).getAsString();
                            String categoryItemName = jsonTableRowArray.get(1).getAsString();
                            categoryItemMap.put("categoryItemId", categoryItemId);
                            categoryItemMap.put("categoryItemName", categoryItemName);
                            categoryItemList.add(categoryItemMap);
                        }
                    }

                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", categoryItemList);
                    templateRenderer.render(CATEGORY_ITEM_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (initial.equalsIgnoreCase("yes")) {
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    templateRenderer.render(CATEGORY_ITEM_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (subcategorychanged.equalsIgnoreCase("yes")) {
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
                        } else {
                            Map<String, String> categoryItemMap = new HashMap<>();
                            categoryItemMap.put("categoryItemId", "");
                            categoryItemMap.put("categoryItemName", "");

                            categoryItemList.add(categoryItemMap);
                        }
                    }
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("selectedSubCategoryId", selectedSubCategoryId);
                    context.put("subCategoryList", getSubCategoryList(req, selectedCategoryId));
                    context.put("categoryItemList", categoryItemList);
                    templateRenderer.render(CATEGORY_ITEM_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
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
                    templateRenderer.render(CATEGORY_ITEM_DEF_TEMPLATE, context, resp.getWriter());
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
            List<Map<String, String>> categoryItemMapList = new ArrayList<>();

            JsonParser parser = new JsonParser();
            String tableParameters = req.getParameter("tableData");
            String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
            String selectedSubCategoryId = req.getParameter("selectedSubCategoryId") == null ? "" : req.getParameter("selectedSubCategoryId").trim();
            JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

            for (int i = 0; i < jsonTableArray.size(); i++) {
                JsonElement jsonTableRowElement = jsonTableArray.get(i);
                JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                String categoryItemId = jsonTableRowArray.get(0).getAsString();
                String categoryItemName = jsonTableRowArray.get(1).getAsString();

                Map<String, String> categoryItemMap = new HashMap<>();
                categoryItemMap.put("categoryItemId", categoryItemId);
                categoryItemMap.put("categoryItemName", categoryItemName);
                categoryItemMapList.add(categoryItemMap);
            }

            Iterator categoryItemIterator = categoryItemMapList.iterator();
            List<String> categoryItemIdList = new ArrayList<>();
            while (categoryItemIterator.hasNext()) {
                Map<String, String> categoryItemMap = (Map<String, String>) categoryItemIterator.next();
                String categoryItemId = categoryItemMap.get("categoryItemId");
                String categoryItemName = categoryItemMap.get("categoryItemName");

                if (null != categoryItemId && !categoryItemId.equalsIgnoreCase("")) {
                    if (null != categoryItemName && !categoryItemName.equalsIgnoreCase("")) {
                        CategoryItem categoryItem = categoryItemController.getRecordFromAOTableById(categoryItemId);
                        CategoryItemObject categoryItemObject = new CategoryItemObject();
                        categoryItemObject.setCategoryItemName(categoryItemName);
                        categoryItemController.updateRecordInAOTable(categoryItem, categoryItemObject);
                    }
                    categoryItemIdList.add(categoryItemId);
                } else {
                    if (null != categoryItemName && !categoryItemName.equalsIgnoreCase("")) {
                        CategoryItemObject categoryItemObject = new CategoryItemObject();
                        categoryItemObject.setCategoryItemName(categoryItemName);
                        categoryItemObject.setCategoryId(selectedCategoryId);
                        categoryItemObject.setSubCategoryId(selectedSubCategoryId);
                        CategoryItem categoryItem = categoryItemController.createRecordInAOTable(categoryItemObject);
                        categoryItemIdList.add(String.valueOf(categoryItem.getID()));
                    }
                }
            }

            CategoryItem[] categoryItems = categoryItemController.getRecordFromAOTableBySubCategoryId(selectedSubCategoryId);
            for (CategoryItem categoryItem : categoryItems) {
                if (!categoryItemIdList.contains(String.valueOf(categoryItem.getID()))) {
                    deleteCategoryItem(categoryItem);
                }
            }
        }
    }

    private void deleteCategoryItem (CategoryItem categoryItem) {
        //Delete Business Rule
        BusinessRule [] businessRules = businessRuleController.getAllEntriesFromAOTable();
        for (BusinessRule businessRule : businessRules) {
            if (businessRule.getCategoryItemId().equalsIgnoreCase(String.valueOf(categoryItem.getID()))) {
                businessRuleController.deleteRecordFromAOTable(businessRule);
            }
        }
        //Delete Category Item
        categoryItemController.deleteRecordFromAOTable(categoryItem);
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

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}