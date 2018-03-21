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

public class ComponentRelationDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(ComponentRelationDefServlet.class);
    private static final String COMPONENT_RELATION_TEMPLATE = "/templates/adminscreens/componentrelation.vm";
    private static final String COMPONENT_RELATION_DETAIL_TEMPLATE = "/templates/adminscreens/componentrelationdetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private final CategoryComponentController categoryComponentController;
    private final ComponentRelationController componentRelationController;

    public ComponentRelationDefServlet(TemplateRenderer templateRenderer,
                                       JiraAuthenticationContext jiraAuthenticationContext,
                                       LoginUriProvider loginUriProvider,
                                       CategoryComponentController categoryComponentController,
                                       ComponentRelationController componentRelationController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.categoryComponentController = categoryComponentController;
        this.componentRelationController = componentRelationController;
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
                String selectedComponentId = req.getParameter("selectedComponentId") == null ? "" : req.getParameter("selectedComponentId").trim();

                if (initial.equalsIgnoreCase("yes")) {
                    if (null != selectedComponentId && !selectedComponentId.equalsIgnoreCase("")) {
                        ComponentRelation componentRelation = componentRelationController.getRecordFromAOTableByComponentId(selectedComponentId);
                        if (null != componentRelation) {
                            context.put("rilComponentId", componentRelation.getRilComponentId());
                            context.put("selectedComponentId", componentRelation.getComponentId());
                            context.put("recordExists", "yes");
                        }
                    }
                    context.put("componentList", getCategoryComponentList());
                    templateRenderer.render(COMPONENT_RELATION_DETAIL_TEMPLATE, context, resp.getWriter());
                } else {
                    context.put("componentList", getCategoryComponentList());
                    templateRenderer.render(COMPONENT_RELATION_TEMPLATE, context, resp.getWriter());
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
            String actionType = req.getParameter("actionType");

            ComponentRelationObject componentRelationObject = prepareComponentRelationObject(req);

            if (actionType.equalsIgnoreCase("save")) {
                componentRelationController.createRecordInAOTable(componentRelationObject);
            } else if (actionType.equalsIgnoreCase("delete")) {
                ComponentRelation componentRelation = componentRelationController.getRecordFromAOTableByComponentId(componentRelationObject.getComponentId());
                componentRelationController.deleteRecordFromAOTable(componentRelation);
            }
        }
    }

    private ComponentRelationObject prepareComponentRelationObject (HttpServletRequest req) {
        ComponentRelationObject componentRelationObject = null;

        if (null != req) {
            String componentId = req.getParameter("componentId");
            String rilComponentId = req.getParameter("rilComponentId");

            componentRelationObject = new ComponentRelationObject();

            componentRelationObject.setComponentId(componentId);
            componentRelationObject.setRilComponentId(rilComponentId);
        }

        return componentRelationObject;
    }

    private List<Map<String, String>> getCategoryComponentList () {
        List<Map<String, String>> componentList = new ArrayList<>();

        CategoryComponent[] categoryComponents = categoryComponentController.getAllEntriesFromAOTable();
        if (categoryComponents.length > 0) {
            Arrays.sort(categoryComponents, Comparator.comparing(CategoryComponent::getCategoryComponentName));
            for (CategoryComponent categoryComponent : categoryComponents) {
                Map<String, String> categoryComponentMap = new HashMap<>();
                categoryComponentMap.put("componentId", String.valueOf(categoryComponent.getID()));
                categoryComponentMap.put("componentName", categoryComponent.getCategoryComponentName());
                componentList.add(categoryComponentMap);
            }
        }

        Collections.sort(componentList, mapComparator);
        return componentList;
    }

    public Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("componentName").compareTo(m2.get("componentName"));
        }
    };

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}