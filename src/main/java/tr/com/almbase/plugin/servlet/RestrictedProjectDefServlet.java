package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
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

public class RestrictedProjectDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(RestrictedProjectDefServlet.class);
    private static final String RESTRICTED_PROJECT_DEF_TEMPLATE = "/templates/adminscreens/restrictedproject.vm";
    private static final String RESTRICTED_PROJECT_DETAIL_DEF_TEMPLATE = "/templates/adminscreens/restrictedprojectdetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private RestrictedProjectController restrictedProjectController;


    public RestrictedProjectDefServlet(TemplateRenderer templateRenderer,
                                       JiraAuthenticationContext jiraAuthenticationContext,
                                       LoginUriProvider loginUriProvider,
                                       RestrictedProjectController restrictedProjectController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.restrictedProjectController = restrictedProjectController;
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
                String selectedProjectKey = req.getParameter("selectedProjectKey") == null ? "" : req.getParameter("selectedProjectKey").trim();

                List<String> restrictedProjectList = new ArrayList<>();

                if (addewrow.equalsIgnoreCase("yes")) {
                    JsonParser parser = new JsonParser();
                    JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

                    for (int i = 0; i < jsonTableArray.size(); i++) {
                        JsonElement jsonTableRowElement = jsonTableArray.get(i);
                        JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                        String groupName = jsonTableRowArray.get(0).getAsString();
                        restrictedProjectList.add(groupName);
                    }

                    restrictedProjectList.add("");

                    context.put("selectedProjectKey", selectedProjectKey);
                    context.put("restrictedProjectList", restrictedProjectList);
                    templateRenderer.render(RESTRICTED_PROJECT_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else if (deleterow.equalsIgnoreCase("yes")) {
                    JsonParser parser = new JsonParser();
                    JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

                    for (int i = 0; i < jsonTableArray.size(); i++) {
                        JsonElement jsonTableRowElement = jsonTableArray.get(i);
                        JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                        String groupName = jsonTableRowArray.get(0).getAsString();
                        restrictedProjectList.add(groupName);
                    }

                    context.put("selectedProjectKey", selectedProjectKey);
                    context.put("restrictedProjectList", restrictedProjectList);
                    templateRenderer.render(RESTRICTED_PROJECT_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                }  else if (initial.equalsIgnoreCase("yes")) {
                    if (isProjectSelected(req)) {
                        RestrictedProject[] restrictedProjects = restrictedProjectController.getRecordFromAOTableByProjectKey(selectedProjectKey);
                        if (null != restrictedProjects) {
                            Arrays.sort(restrictedProjects, Comparator.comparing(RestrictedProject::getGroupName));
                            for (RestrictedProject restrictedProject : restrictedProjects) {
                                restrictedProjectList.add(restrictedProject.getGroupName());
                            }
                        }
                    }
                    context.put("selectedProjectKey", selectedProjectKey);
                    context.put("restrictedProjectList", restrictedProjectList);
                    templateRenderer.render(RESTRICTED_PROJECT_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else {
                    context.put("projectList", getAllProjects());
                    templateRenderer.render(RESTRICTED_PROJECT_DEF_TEMPLATE, context, resp.getWriter());
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
            List<String> restrictedProjectList = new ArrayList<>();

            JsonParser parser = new JsonParser();
            String tableParameters = req.getParameter("tableData");
            String selectedProjectKey = req.getParameter("selectedProjectKey") == null ? "" : req.getParameter("selectedProjectKey").trim();
            JsonArray jsonTableArray = (JsonArray)parser.parse(tableParameters);

            if (null != selectedProjectKey && !selectedProjectKey.equalsIgnoreCase("")) {
                for (int i = 0; i < jsonTableArray.size(); i++) {
                    JsonElement jsonTableRowElement = jsonTableArray.get(i);
                    JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                    String groupName = jsonTableRowArray.get(0).getAsString();
                    restrictedProjectList.add(groupName);
                }

                for (String groupName : restrictedProjectList) {
                    if (null != groupName && !groupName.equalsIgnoreCase("")) {
                        RestrictedProjectObject restrictedProjectObject = new RestrictedProjectObject();
                        restrictedProjectObject.setProjectKey(selectedProjectKey);
                        restrictedProjectObject.setGroupName(groupName);
                        restrictedProjectController.createRecordInAOTable(restrictedProjectObject);
                    }
                }

                RestrictedProject[] restrictedProjects = restrictedProjectController.getRecordFromAOTableByProjectKey(selectedProjectKey);
                for (RestrictedProject restrictedProject : restrictedProjects) {
                    if (!restrictedProjectList.contains(restrictedProject.getGroupName())) {
                        restrictedProjectController.deleteRecordFromAOTable(restrictedProject);
                    }
                }
            }
        }
    }

    private List<Project> getAllProjects () {
        List<Project> projects = ComponentAccessor.getProjectManager().getProjects();
        return projects;
    }

    private boolean isProjectSelected (HttpServletRequest req) {
        String selectedProjectKey = req.getParameter("selectedProjectKey") == null ? "" : req.getParameter("selectedProjectKey").trim();

        Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(selectedProjectKey);

        return null != project;
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}