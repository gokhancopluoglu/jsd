package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.Integration;
import tr.com.almbase.plugin.activeobject.IntegrationController;
import tr.com.almbase.plugin.activeobject.IntegrationObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Created by kivanc.ahat@almbase.com on 07/03/2018.
 */
public class IntegrationServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(IntegrationServlet.class);
    private static final String INTEGRATION_TEMPLATE = "/templates/adminscreens/integration.vm";
    private static final String INTEGRATION_DETAIL_TEMPLATE = "/templates/adminscreens/integrationdetail.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private final I18nResolver i18nResolver;
    private final IntegrationController integrationController;

    public IntegrationServlet(TemplateRenderer templateRenderer,
                              JiraAuthenticationContext jiraAuthenticationContext,
                              LoginUriProvider loginUriProvider,
                              I18nResolver i18nResolver,
                              IntegrationController integrationController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.i18nResolver = i18nResolver;
        this.integrationController = integrationController;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, Object> context = Maps.newHashMap();
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();

        if (null == loggedInUser && !administrators.contains(loggedInUser)) {
            redirectToLogin(req, resp);
        } else {
            String tableParameters = req.getParameter("tableData");
            String addewrow = req.getParameter("addnewrow") == null ? "" : req.getParameter("addnewrow").trim();
            String deleterow = req.getParameter("deleterow") == null ? "" : req.getParameter("deleterow").trim();
            String initial = req.getParameter("initial") == null ? "" : req.getParameter("initial").trim();

            List<Map<String, String>> integrationList = null;

            if (addewrow.equalsIgnoreCase("yes")) {
                integrationList = parseTableData(tableParameters, addewrow);

                context.put("integrationList", integrationList);
                templateRenderer.render(INTEGRATION_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (deleterow.equalsIgnoreCase("yes")) {
                integrationList = parseTableData(tableParameters, deleterow);

                context.put("integrationList", integrationList);
                templateRenderer.render(INTEGRATION_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (initial.equalsIgnoreCase("yes")) {
                integrationList = getIntegrationList();
                context.put("integrationList", integrationList);
                templateRenderer.render(INTEGRATION_DETAIL_TEMPLATE, context, resp.getWriter());
            } else {
                templateRenderer.render(INTEGRATION_TEMPLATE, context, resp.getWriter());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();

        if (null == loggedInUser && !administrators.contains(loggedInUser)) {
            redirectToLogin(req, resp);
        } else {
            String tableParameters = req.getParameter("tableData");
            List<Map<String, String>> integrationMapList = new ArrayList<>();
            integrationMapList = parseTableData(tableParameters, "submit");

            Iterator integrationIterator = integrationMapList.iterator();
            List<String> integrationIdList = new ArrayList<>();
            while (integrationIterator.hasNext()) {
                Map<String, String> integrationMap = (Map<String, String>)integrationIterator.next();
                String integrationId = integrationMap.get("integrationId");

                if (null != integrationMap.get("integrationId") && !integrationMap.get("integrationId").equalsIgnoreCase("")) {
                    if (null != integrationMap.get("url") && !integrationMap.get("url").equalsIgnoreCase("")) {
                        Integration integration = integrationController.getRecordFromAOTableById(integrationId);
                        IntegrationObject integrationObject = prepareIntegrationObject(integrationMap);
                        integrationController.updateRecordFromAOTable(integration, integrationObject);
                    }
                    integrationIdList.add(integrationId);
                } else {
                    if (null != integrationMap.get("url") && !integrationMap.get("url").equalsIgnoreCase("")) {
                        IntegrationObject integrationObject = prepareIntegrationObject(integrationMap);
                        Integration integration = integrationController.createRecordInAOTable(integrationObject);
                        integrationIdList.add(String.valueOf(integration.getID()));
                    }
                }
            }

            Integration[] integrations = integrationController.getAllEntriesFromAOTable();
            for (Integration integration : integrations) {
                if (!integrationIdList.contains(String.valueOf(integration.getID()))) {
                    integrationController.deleteRecordFromAOTable(integration);
                }
            }
        }
    }

    private List<Map<String, String>> getIntegrationList () {
        List<Map<String, String>> integrationList = new ArrayList<>();

        Integration[] integrations = integrationController.getAllEntriesFromAOTable();
        if (null != integrations) {
            Arrays.sort(integrations, Comparator.comparing(Integration::getUrl));
            for (Integration integration : integrations) {
                Map<String, String> integrationMap = new HashMap<>();
                integrationMap.put("integrationId", String.valueOf(integration.getID()));
                integrationMap.put("name", integration.getName());
                integrationMap.put("url", integration.getUrl());
                integrationMap.put("username", integration.getUsername());
                integrationMap.put("password", integration.getPassword());

                integrationList.add(integrationMap);
            }
        }

        return integrationList;
    }

    private IntegrationObject prepareIntegrationObject (Map<String, String> integrationMap) {
        IntegrationObject integrationObject = null;

        if (null != integrationMap) {
            String name = integrationMap.get("name");
            String url = integrationMap.get("url");
            String username = integrationMap.get("username");
            String password = integrationMap.get("password");

            integrationObject = new IntegrationObject(name, url, username, password);
        }

        return integrationObject;
    }

    private List<Map<String, String>> parseTableData (String tableParameters, String operationType) {
        List<Map<String, String>> integrationList = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

        for (int i = 0; i < jsonTableArray.size(); i++) {
            JsonElement jsonTableRowElement = jsonTableArray.get(i);
            JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

            Map<String, String> integrationMap = new HashMap<>();
            String integrationId = jsonTableRowArray.get(0).getAsString();
            String name = jsonTableRowArray.get(1).getAsString();
            String url = jsonTableRowArray.get(2).getAsString();
            String username = jsonTableRowArray.get(3).getAsString();
            String password = jsonTableRowArray.get(4).getAsString();


            integrationMap.put("integrationId", integrationId);
            integrationMap.put("name", name);
            integrationMap.put("url", url);
            integrationMap.put("username", username);
            integrationMap.put("password", password);
            integrationList.add(integrationMap);
        }

        if (operationType.equalsIgnoreCase("yes")) {
            Map<String, String> integrationMap = new HashMap<>();
            integrationMap.put("integrationId", "");
            integrationMap.put("name", "");
            integrationMap.put("url", "");
            integrationMap.put("username", "");
            integrationMap.put("password", "");
            integrationList.add(integrationMap);
        }

        return integrationList;
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}
