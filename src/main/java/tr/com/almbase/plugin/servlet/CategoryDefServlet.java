package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.issuetype.IssueType;
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

public class CategoryDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(CategoryDefServlet.class);
    private static final String CATEGORY_DEF_TEMPLATE = "/templates/adminscreens/category.vm";
    private static final String CATEGORY_DETAIL_DEF_TEMPLATE = "/templates/adminscreens/categorydetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private CategoryController categoryController;
    private BusinessRuleController businessRuleController;
    private CategoryItemController categoryItemController;
    private SubCategoryController subCategoryController;

    public CategoryDefServlet(TemplateRenderer templateRenderer,
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

                List<Map<String, String>> categoryList = new ArrayList<>();

                if (addewrow.equalsIgnoreCase("yes")) {
                    JsonParser parser = new JsonParser();
                    JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

                    for (int i = 0; i < jsonTableArray.size(); i++) {
                        JsonElement jsonTableRowElement = jsonTableArray.get(i);
                        JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                        Map<String, String> categoryMap = new HashMap<>();
                        String categoryId = jsonTableRowArray.get(0).getAsString();
                        String categoryName = jsonTableRowArray.get(1).getAsString();
                        String issueTypeId = jsonTableRowArray.get(2).getAsString();
                        String groupName = jsonTableRowArray.get(3).getAsString();
                        categoryMap.put("categoryId", categoryId);
                        categoryMap.put("categoryName", categoryName);
                        categoryMap.put("issueTypeId", issueTypeId);
                        categoryMap.put("groupName", groupName);
                        categoryList.add(categoryMap);
                    }

                    Map<String, String> categoryMap = new HashMap<>();
                    categoryMap.put("categoryId", "");
                    categoryMap.put("categoryName", "");
                    categoryMap.put("issueTypeId", "");
                    categoryMap.put("groupName", "");

                    categoryList.add(categoryMap);

                    context.put("issueTypes", getIssueTypes());
                    context.put("categoryList", categoryList);
                    templateRenderer.render(CATEGORY_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (deleterow.equalsIgnoreCase("yes")) {
                    JsonParser parser = new JsonParser();
                    JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

                    for (int i = 0; i < jsonTableArray.size(); i++) {
                        JsonElement jsonTableRowElement = jsonTableArray.get(i);
                        JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                        Map<String, String> categoryMap = new HashMap<>();
                        String categoryId = jsonTableRowArray.get(0).getAsString();
                        String categoryName = jsonTableRowArray.get(1).getAsString();
                        String issueTypeId = jsonTableRowArray.get(2).getAsString();
                        String groupName = jsonTableRowArray.get(3).getAsString();
                        categoryMap.put("categoryId", categoryId);
                        categoryMap.put("categoryName", categoryName);
                        categoryMap.put("issueTypeId", issueTypeId);
                        categoryMap.put("groupName", groupName);
                        categoryList.add(categoryMap);
                    }

                    context.put("issueTypes", getIssueTypes());
                    context.put("categoryList", categoryList);
                    templateRenderer.render(CATEGORY_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (initial.equalsIgnoreCase("yes")) {
                    Category[] categories = categoryController.getAllEntriesFromAOTable();
                    if (null != categories) {
                        Arrays.sort(categories, Comparator.comparing(Category::getCategoryName));
                        for (Category category : categories) {
                            Map<String, String> categoryMap = new HashMap<>();
                            categoryMap.put("categoryId", String.valueOf(category.getID()));
                            categoryMap.put("categoryName", category.getCategoryName());
                            categoryMap.put("issueTypeId", category.getIssueTypeId());
                            categoryMap.put("groupName", category.getGroupName());
                            categoryList.add(categoryMap);
                        }
                    }
                    context.put("issueTypes", getIssueTypes());
                    context.put("categoryList", categoryList);
                    templateRenderer.render(CATEGORY_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else {
                    templateRenderer.render(CATEGORY_DEF_TEMPLATE, context, resp.getWriter());
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
            List<Map<String, String>> categoryMapList = new ArrayList<>();

            JsonParser parser = new JsonParser();
            String tableParameters = req.getParameter("tableData");
            JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

            for (int i = 0; i < jsonTableArray.size(); i++) {
                JsonElement jsonTableRowElement = jsonTableArray.get(i);
                JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                String categoryId = jsonTableRowArray.get(0).getAsString();
                String categoryName = jsonTableRowArray.get(1).getAsString();
                String issueTypeId = jsonTableRowArray.get(2).getAsString();
                String groupName = jsonTableRowArray.get(3).getAsString();

                Map<String, String> categoryMap = new HashMap<>();
                categoryMap.put("categoryId", categoryId);
                categoryMap.put("categoryName", categoryName);
                categoryMap.put("issueTypeId", issueTypeId);
                categoryMap.put("groupName", groupName);
                categoryMapList.add(categoryMap);
            }

            Iterator categoryIterator = categoryMapList.iterator();
            List<String> categoryIdList = new ArrayList<>();
            while (categoryIterator.hasNext()) {
                Map<String, String> categoryMap = (Map<String, String>)categoryIterator.next();
                String categoryId = categoryMap.get("categoryId");
                String categoryName = categoryMap.get("categoryName");
                String issueTypeId = categoryMap.get("issueTypeId");
                String groupName = categoryMap.get("groupName");

                if (null != categoryId && !categoryId.equalsIgnoreCase("")) {
                    if (null != categoryName && !categoryName.equalsIgnoreCase("")) {
                        Category category = categoryController.getRecordFromAOTableById(categoryId);
                        CategoryObject categoryObject = new CategoryObject();
                        categoryObject.setCategoryName(categoryName);
                        categoryObject.setIssueTypeId(issueTypeId);
                        categoryObject.setGroupName(groupName);
                        categoryController.updateRecordInAOTable(category, categoryObject);
                    }
                    categoryIdList.add(categoryId);
                } else {
                    if (null != categoryName && !categoryName.equalsIgnoreCase("")) {
                        CategoryObject categoryObject = new CategoryObject();
                        categoryObject.setCategoryName(categoryName);
                        categoryObject.setIssueTypeId(issueTypeId);
                        categoryObject.setGroupName(groupName);
                        Category category = categoryController.createRecordInAOTable(categoryObject);
                        categoryIdList.add(String.valueOf(category.getID()));
                    }
                }
            }

            Category [] categories = categoryController.getAllEntriesFromAOTable();
            for (Category category : categories) {
                if (!categoryIdList.contains(String.valueOf(category.getID()))) {
                    deleteCategory(category);
                }
            }
        }
    }

    private void deleteCategory (Category category) {
        //Delete Business Rule
        BusinessRule [] businessRules = businessRuleController.getAllEntriesFromAOTable();
        for (BusinessRule businessRule : businessRules) {
            if (businessRule.getCategoryId().equalsIgnoreCase(String.valueOf(category.getID()))) {
                businessRuleController.deleteRecordFromAOTable(businessRule);
            }
        }
        //Delete Category Item
        CategoryItem [] categoryItems = categoryItemController.getAllEntriesFromAOTable();
        for (CategoryItem categoryItem : categoryItems) {
            if (categoryItem.getCategoryId().equalsIgnoreCase(String.valueOf(category.getID()))) {
                categoryItemController.deleteRecordFromAOTable(categoryItem);
            }
        }
        //Delete Sub Category
        SubCategory [] subCategories = subCategoryController.getAllEntriesFromAOTable();
        for (SubCategory subCategory : subCategories) {
            if (subCategory.getCategoryId().equalsIgnoreCase(String.valueOf(category.getID()))) {
                subCategoryController.deleteRecordFromAOTable(subCategory);
            }
        }
        //Delete Category
        categoryController.deleteRecordFromAOTable(category);
    }

    private Map<String, IssueType> getIssueTypes () {
        Map<String, IssueType> issueTypeMap = new HashMap<>();
        Collection<IssueType> issueTypeList = ComponentAccessor.getConstantsManager().getAllIssueTypeObjects();
        for (IssueType issueType : issueTypeList) {
            issueTypeMap.put(issueType.getId(), issueType);
        }
        return issueTypeMap;
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}