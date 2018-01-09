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

public class SubCategoryDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(SubCategoryDefServlet.class);
    private static final String SUB_CATEGORY_DEF_TEMPLATE = "/templates/adminscreens/subcategory.vm";
    private static final String SUB_CATEGORY_DETAIL_DEF_TEMPLATE = "/templates/adminscreens/subcategorydetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private CategoryController categoryController;
    private BusinessRuleController businessRuleController;
    private CategoryItemController categoryItemController;
    private SubCategoryController subCategoryController;

    public SubCategoryDefServlet(TemplateRenderer templateRenderer,
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
                String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();

                List<Map<String, String>> subCategoryList = new ArrayList<>();

                if (addewrow.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req)) {
                        JsonParser parser = new JsonParser();
                        JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                        for (int i = 0; i < jsonTableArray.size(); i++) {
                            JsonElement jsonTableRowElement = jsonTableArray.get(i);
                            JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                            Map<String, String> subCategoryMap = new HashMap<>();
                            String subCategoryId = jsonTableRowArray.get(0).getAsString();
                            String subCategoryName = jsonTableRowArray.get(1).getAsString();
                            subCategoryMap.put("subCategoryId", subCategoryId);
                            subCategoryMap.put("subCategoryName", subCategoryName);
                            subCategoryList.add(subCategoryMap);
                        }

                        Map<String, String> subCategoryMap = new HashMap<>();
                        subCategoryMap.put("subCategoryId", "");
                        subCategoryMap.put("subCategoryName", "");

                        subCategoryList.add(subCategoryMap);
                    }

                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("subCategoryList", subCategoryList);
                    templateRenderer.render(SUB_CATEGORY_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (deleterow.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req)) {
                        JsonParser parser = new JsonParser();
                        JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                        for (int i = 0; i < jsonTableArray.size(); i++) {
                            JsonElement jsonTableRowElement = jsonTableArray.get(i);
                            JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                            Map<String, String> subCategoryMap = new HashMap<>();
                            String subCategoryId = jsonTableRowArray.get(0).getAsString();
                            String subCategoryName = jsonTableRowArray.get(1).getAsString();
                            subCategoryMap.put("subCategoryId", subCategoryId);
                            subCategoryMap.put("subCategoryName", subCategoryName);
                            subCategoryList.add(subCategoryMap);
                        }
                    }

                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("subCategoryList", subCategoryList);
                    templateRenderer.render(SUB_CATEGORY_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (initial.equalsIgnoreCase("yes")) {
                    if (isCategorySelected(req)) {
                        SubCategory[] subCategories = subCategoryController.getRecordFromAOTableByCategoryId(selectedCategoryId);
                        if (null != subCategories) {
                            Arrays.sort(subCategories, Comparator.comparing(SubCategory::getSubCategoryName));
                            for (SubCategory subCategory : subCategories) {
                                Map<String, String> subCategoryMap = new HashMap<>();
                                subCategoryMap.put("subCategoryId", String.valueOf(subCategory.getID()));
                                subCategoryMap.put("subCategoryName", subCategory.getSubCategoryName());
                                subCategoryList.add(subCategoryMap);
                            }
                        }
                    }
                    context.put("selectedCategoryId", selectedCategoryId);
                    context.put("subCategoryList", subCategoryList);
                    templateRenderer.render(SUB_CATEGORY_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
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
                    templateRenderer.render(SUB_CATEGORY_DEF_TEMPLATE, context, resp.getWriter());
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
            List<Map<String, String>> subCategoryMapList = new ArrayList<>();

            JsonParser parser = new JsonParser();
            String tableParameters = req.getParameter("tableData");
            String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
            JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

            for (int i = 0; i < jsonTableArray.size(); i++) {
                JsonElement jsonTableRowElement = jsonTableArray.get(i);
                JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                String subCategoryId = jsonTableRowArray.get(0).getAsString();
                String subCategoryName = jsonTableRowArray.get(1).getAsString();

                Map<String, String> subCategoryMap = new HashMap<>();
                subCategoryMap.put("subCategoryId", subCategoryId);
                subCategoryMap.put("subCategoryName", subCategoryName);
                subCategoryMapList.add(subCategoryMap);
            }

            Iterator subCategoryIterator = subCategoryMapList.iterator();
            List<String> subCategoryIdList = new ArrayList<>();
            while (subCategoryIterator.hasNext()) {
                Map<String, String> subCategoryMap = (Map<String, String>)subCategoryIterator.next();
                String subCategoryId = subCategoryMap.get("subCategoryId");
                String subCategoryName = subCategoryMap.get("subCategoryName");

                if (null != subCategoryId && !subCategoryId.equalsIgnoreCase("")) {
                    if (null != subCategoryName && !subCategoryName.equalsIgnoreCase("")) {
                        SubCategory subCategory = subCategoryController.getRecordFromAOTableById(subCategoryId);
                        SubCategoryObject subCategoryObject = new SubCategoryObject();
                        subCategoryObject.setSubCategoryName(subCategoryName);
                        //subCategoryObject.setCategoryId(selectedCategoryId);
                        subCategoryController.updateRecordInAOTable(subCategory, subCategoryObject);
                    }
                    subCategoryIdList.add(subCategoryId);
                } else {
                    if (null != subCategoryName && !subCategoryName.equalsIgnoreCase("")) {
                        SubCategoryObject subCategoryObject = new SubCategoryObject();
                        subCategoryObject.setSubCategoryName(subCategoryName);
                        subCategoryObject.setCategoryId(selectedCategoryId);
                        SubCategory subCategory =subCategoryController.createRecordInAOTable(subCategoryObject);
                        subCategoryIdList.add(String.valueOf(subCategory.getID()));
                    }
                }
            }

            SubCategory [] subCategories = subCategoryController.getRecordFromAOTableByCategoryId(selectedCategoryId);
            for (SubCategory subCategory : subCategories) {
                if (!subCategoryIdList.contains(String.valueOf(subCategory.getID()))) {
                    deleteSubCategory(subCategory);
                }
            }
        }
    }

    private void deleteSubCategory (SubCategory subCategory) {
        //Delete Business Rule
        BusinessRule [] businessRules = businessRuleController.getAllEntriesFromAOTable();
        for (BusinessRule businessRule : businessRules) {
            if (businessRule != null && businessRule.getSubCategoryId() != null && businessRule.getSubCategoryId().equalsIgnoreCase(String.valueOf(subCategory.getID()))) {
                businessRuleController.deleteRecordFromAOTable(businessRule);
            }
        }
        //Delete Category Item
        CategoryItem [] categoryItems = categoryItemController.getAllEntriesFromAOTable();
        for (CategoryItem categoryItem : categoryItems) {
            if (categoryItem.getCategoryId().equalsIgnoreCase(String.valueOf(subCategory.getID()))) {
                categoryItemController.deleteRecordFromAOTable(categoryItem);
            }
        }
        //Delete Sub Category
        subCategoryController.deleteRecordFromAOTable(subCategory);
    }

    private boolean isCategorySelected (HttpServletRequest req) {
        String selectedCategoryId = req.getParameter("selectedCategoryId") == null ? "" : req.getParameter("selectedCategoryId").trim();
        Category category = null;
        if (null != selectedCategoryId && !selectedCategoryId.trim().equalsIgnoreCase("")) {
            category = categoryController.getRecordFromAOTableById(selectedCategoryId);
        }

        return null != category;
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}