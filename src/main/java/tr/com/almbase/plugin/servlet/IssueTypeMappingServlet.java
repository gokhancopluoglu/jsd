package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.model.RemoteIssueTypeModel;
import tr.com.almbase.plugin.model.RemoteProjectModel;
import tr.com.almbase.plugin.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

/**
 * Created by kivanc.ahat@almbase.com on 07/03/2018.
 */
public class IssueTypeMappingServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(IssueTypeMappingServlet.class);
    private static final String ISSUE_TYPE_MAPPING_TEMPLATE = "/templates/adminscreens/issuetypemapping.vm";
    private static final String ISSUE_TYPE_MAPPING_DETAIL_TEMPLATE = "/templates/adminscreens/issuetypemappingdetail.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private final PageBuilderService pageBuilderService;
    private final UserManager userManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;

    private final IntegrationController integrationController;
    private final ProxyController proxyController;
    private final IssueTypeMappingController issueTypeMappingController;

    public IssueTypeMappingServlet(TemplateRenderer templateRenderer,
                                           JiraAuthenticationContext jiraAuthenticationContext,
                                           LoginUriProvider loginUriProvider,
                                           PageBuilderService pageBuilderService,
                                           UserManager userManager,
                                           WorkflowSchemeManager workflowSchemeManager,
                                           ProjectManager projectManager,
                                           ConstantsManager constantsManager,
                                           IssueTypeSchemeManager issueTypeSchemeManager,
                                           IntegrationController integrationController,
                                           ProxyController proxyController,
                                           IssueTypeMappingController issueTypeMappingController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.pageBuilderService = pageBuilderService;
        this.userManager = userManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.integrationController = integrationController;
        this.proxyController = proxyController;
        this.issueTypeMappingController = issueTypeMappingController;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, Object> context = Maps.newHashMap();

        ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
        Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();

        if (null == jiraAuthenticationContext.getLoggedInUser() && !administrators.contains(user))
        {
            redirectToLogin(req, resp);
        } else {
            String initial = req.getParameter("initial") == null ? "" : req.getParameter("initial").trim();
            String issueTypeMappingSelectChanged = req.getParameter("issuetypemappingselectchanged") == null ? "" : req.getParameter("issuetypemappingselectchanged").trim();
            String localProjectChanged = req.getParameter("localprojectchanged") == null ? "" : req.getParameter("localprojectchanged").trim();
            String localIssueTypeChanged = req.getParameter("localissuetypechanged") == null ? "" : req.getParameter("localissuetypechanged").trim();

            String selectedIntegrationId = req.getParameter("selectedIntegrationId") == null ? "" : req.getParameter("selectedIntegrationId").trim();
            String issueTypeMappingSelectId = req.getParameter("issueTypeMappingSelectId") == null ? "" : req.getParameter("issueTypeMappingSelectId").trim();
            String issueTypeMappingName = req.getParameter("issueTypeMappingName") == null ? "" : req.getParameter("issueTypeMappingName").trim();
            String selectedLocalProjectId = req.getParameter("localProjectId") == null ? "" : req.getParameter("localProjectId").trim();;
            String selectedLocalIssueTypeId = req.getParameter("localIssueTypeId") == null ? "" : req.getParameter("localIssueTypeId").trim();;
            String selectedRemoteProjectId = req.getParameter("remoteProjectId") == null ? "" : req.getParameter("remoteProjectId").trim();;
            String selectedRemoteIssueTypeId = req.getParameter("remoteIssueTypeId") == null ? "" : req.getParameter("remoteIssueTypeId").trim();;

            if (initial.equalsIgnoreCase("yes")) {
                context.put("selectedIntegrationId", selectedIntegrationId);
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));
                context.put("issueTypeMappingNameAvail", "no");
                context.put("issueTypeMappingFieldsAvail", "no");
                context.put("recordExists", "no");
                templateRenderer.render(ISSUE_TYPE_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (issueTypeMappingSelectChanged.equalsIgnoreCase("yes")) {
                IntegrationObject integrationObject = getIntegrationObject(selectedIntegrationId);
                if (issueTypeMappingSelectId.equalsIgnoreCase("New")) {
                    context.put("issueTypeMappingNameAvail", "yes");
                    context.put("issueTypeMappingFieldsAvail", "yes");
                    context.put("recordExists", "no");
                    context.put("selectedIssueTypeMappingId", "New");
                } else {
                    if (null != issueTypeMappingSelectId && !issueTypeMappingSelectId.equalsIgnoreCase("")) {
                        IssueTypeMapping issueTypeMapping = issueTypeMappingController.getRecordFromAOTableById(issueTypeMappingSelectId);

                        if (null != issueTypeMapping) {
                            context.put("selectedLocalProjectId", issueTypeMapping.getLocalProjectId());
                            context.put("selectedLocalIssueTypeId", issueTypeMapping.getLocalIssueTypeId());
                            context.put("selectedLocalEndStatusId", issueTypeMapping.getLocalEndStatusId());
                            context.put("selectedRemoteProjectId", issueTypeMapping.getRemoteProjectId());
                            context.put("selectedRemoteIssueTypeId", issueTypeMapping.getRemoteIssueTypeId());
                            context.put("selectedIssueTypeMappingId", String.valueOf(issueTypeMapping.getID()));
                            context.put("selectedIssueTypeMappingName", issueTypeMapping.getName());
                            context.put("localIssueTypeList", getLocalIssueTypeList(issueTypeMapping.getLocalProjectId()));
                            context.put("localEndStatusList", getLocalEndStatusList(issueTypeMapping.getLocalProjectId(), issueTypeMapping.getLocalIssueTypeId()));

                            context.put("issueTypeMappingNameAvail", "yes");
                            context.put("issueTypeMappingFieldsAvail", "yes");
                            context.put("recordExists", "yes");
                        }
                    } else {
                        context.put("issueTypeMappingNameAvail", "no");
                        context.put("issueTypeMappingFieldsAvail", "no");
                        context.put("recordExists", "no");
                    }
                }

                context.put("selectedIntegrationId", selectedIntegrationId);

                context.put("localProjectList", getLocalProjectList());
                context.put("remoteProjectList", getRemoteProjectList(integrationObject));
                context.put("remoteIssueTypeList", getRemoteIssueTypeList(integrationObject));
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));

                templateRenderer.render(ISSUE_TYPE_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (localProjectChanged.equalsIgnoreCase("yes")) {
                IntegrationObject integrationObject = getIntegrationObject(selectedIntegrationId);
                if (issueTypeMappingSelectId.equalsIgnoreCase("New")) {
                    context.put("issueTypeMappingNameAvail", "yes");
                    context.put("issueTypeMappingFieldsAvail", "yes");
                    context.put("recordExists", "no");
                    context.put("selectedIssueTypeMappingId", issueTypeMappingSelectId);
                    context.put("selectedIssueTypeMappingName", issueTypeMappingName);

                    context.put("selectedLocalProjectId", selectedLocalProjectId);
                    context.put("selectedRemoteProjectId", selectedRemoteProjectId);
                    context.put("selectedRemoteIssueTypeId", selectedRemoteIssueTypeId);
                } else {
                    if (null != issueTypeMappingSelectId && !issueTypeMappingSelectId.equalsIgnoreCase("")) {
                        IssueTypeMapping issueTypeMapping = issueTypeMappingController.getRecordFromAOTableById(issueTypeMappingSelectId);

                        if (null != issueTypeMapping) {
                            context.put("selectedLocalProjectId", selectedLocalProjectId);
                            context.put("selectedRemoteProjectId", selectedRemoteProjectId);
                            context.put("selectedRemoteIssueTypeId", selectedRemoteIssueTypeId);
                            context.put("selectedIssueTypeMappingId", issueTypeMappingSelectId);
                            context.put("selectedIssueTypeMappingName", issueTypeMappingName);

                            context.put("issueTypeMappingNameAvail", "yes");
                            context.put("issueTypeMappingFieldsAvail", "yes");
                            context.put("recordExists", "yes");
                        }
                    } else {
                        context.put("issueTypeMappingNameAvail", "no");
                        context.put("issueTypeMappingFieldsAvail", "no");
                        context.put("recordExists", "no");
                    }
                }

                context.put("selectedIntegrationId", selectedIntegrationId);
                context.put("localProjectList", getLocalProjectList());
                context.put("localIssueTypeList", getLocalIssueTypeList(selectedLocalProjectId));
                //context.put("localEndStatusList", getLocalEndStatusList(selectedLocalProjectId, selectedLocalIssueTypeId));
                context.put("remoteProjectList", getRemoteProjectList(integrationObject));
                context.put("remoteIssueTypeList", getRemoteIssueTypeList(integrationObject));
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));

                templateRenderer.render(ISSUE_TYPE_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (localIssueTypeChanged.equalsIgnoreCase("yes")) {
                IntegrationObject integrationObject = getIntegrationObject(selectedIntegrationId);
                if (issueTypeMappingSelectId.equalsIgnoreCase("New")) {
                    context.put("issueTypeMappingNameAvail", "yes");
                    context.put("issueTypeMappingFieldsAvail", "yes");
                    context.put("recordExists", "no");
                    context.put("selectedIssueTypeMappingId", issueTypeMappingSelectId);
                    context.put("selectedIssueTypeMappingName", issueTypeMappingName);

                    context.put("selectedLocalProjectId", selectedLocalProjectId);
                    context.put("selectedLocalIssueTypeId", selectedLocalIssueTypeId);
                    context.put("selectedRemoteProjectId", selectedRemoteProjectId);
                    context.put("selectedRemoteIssueTypeId", selectedRemoteIssueTypeId);
                } else {
                    if (null != issueTypeMappingSelectId && !issueTypeMappingSelectId.equalsIgnoreCase("")) {
                        IssueTypeMapping issueTypeMapping = issueTypeMappingController.getRecordFromAOTableById(issueTypeMappingSelectId);

                        if (null != issueTypeMapping) {
                            context.put("selectedLocalProjectId", selectedLocalProjectId);
                            context.put("selectedLocalIssueTypeId", selectedLocalIssueTypeId);
                            context.put("selectedRemoteProjectId", selectedRemoteProjectId);
                            context.put("selectedRemoteIssueTypeId", selectedRemoteIssueTypeId);
                            context.put("selectedIssueTypeMappingId", issueTypeMappingSelectId);
                            context.put("selectedIssueTypeMappingName", issueTypeMappingName);

                            context.put("issueTypeMappingNameAvail", "yes");
                            context.put("issueTypeMappingFieldsAvail", "yes");
                            context.put("recordExists", "yes");
                        }
                    } else {
                        context.put("issueTypeMappingNameAvail", "no");
                        context.put("issueTypeMappingFieldsAvail", "no");
                        context.put("recordExists", "no");
                    }
                }

                context.put("selectedIntegrationId", selectedIntegrationId);
                context.put("localProjectList", getLocalProjectList());
                context.put("localIssueTypeList", getLocalIssueTypeList(selectedLocalProjectId));
                context.put("localEndStatusList", getLocalEndStatusList(selectedLocalProjectId, selectedLocalIssueTypeId));
                context.put("remoteProjectList", getRemoteProjectList(integrationObject));
                context.put("remoteIssueTypeList", getRemoteIssueTypeList(integrationObject));
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));

                templateRenderer.render(ISSUE_TYPE_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else {
                context.put("integrationList", getIntegrationList());
                templateRenderer.render(ISSUE_TYPE_MAPPING_TEMPLATE, context, resp.getWriter());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
        Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();
        if (null == jiraAuthenticationContext.getLoggedInUser() && !administrators.contains(user))
        {
            redirectToLogin(req, resp);
        } else {
            try {
                String actionType = req.getParameter("actionType");
                String selectedIntegrationId = req.getParameter("selectedIntegrationId");
                String issueTypeMappingSelectId = req.getParameter("issueTypeMappingSelectId");
                String issueTypeMappingName = req.getParameter("issueTypeMappingName");
                String localProjectId = req.getParameter("localProjectId");
                String localIssueTypeId = req.getParameter("localIssueTypeId");
                String localEndStatusId = req.getParameter("localEndStatusId");
                String remoteProjectId = req.getParameter("remoteProjectId");
                String remoteIssueTypeId = req.getParameter("remoteIssueTypeId");

                if (actionType.equalsIgnoreCase("save")) {
                    if (null != selectedIntegrationId) {
                        if (null != issueTypeMappingSelectId && !issueTypeMappingSelectId.equalsIgnoreCase("")) {
                            if (issueTypeMappingSelectId.equalsIgnoreCase("New")) {
                                IssueTypeMappingObject issueTypeMappingObject = new IssueTypeMappingObject();
                                issueTypeMappingObject.setIntegrationId(selectedIntegrationId);
                                issueTypeMappingObject.setName(issueTypeMappingName);
                                issueTypeMappingObject.setLocalProjectId(localProjectId);
                                issueTypeMappingObject.setLocalIssueTypeId(localIssueTypeId);
                                issueTypeMappingObject.setLocalEndStatusId(localEndStatusId);
                                issueTypeMappingObject.setRemoteProjectId(remoteProjectId);
                                issueTypeMappingObject.setRemoteIssueTypeId(remoteIssueTypeId);
                                issueTypeMappingController.createRecordInAOTable(issueTypeMappingObject);
                            } else {
                                IssueTypeMapping issueTypeMapping = issueTypeMappingController.getRecordFromAOTableById(issueTypeMappingSelectId);
                                if (null != issueTypeMapping) {
                                    IssueTypeMappingObject issueTypeMappingObject = new IssueTypeMappingObject();
                                    issueTypeMappingObject.setIntegrationId(issueTypeMapping.getIntegrationId());
                                    issueTypeMappingObject.setName(issueTypeMappingName);
                                    issueTypeMappingObject.setLocalProjectId(localProjectId);
                                    issueTypeMappingObject.setLocalIssueTypeId(localIssueTypeId);
                                    issueTypeMappingObject.setLocalEndStatusId(localEndStatusId);
                                    issueTypeMappingObject.setRemoteProjectId(remoteProjectId);
                                    issueTypeMappingObject.setRemoteIssueTypeId(remoteIssueTypeId);
                                    issueTypeMappingController.updateRecordFromAOTable(issueTypeMapping, issueTypeMappingObject);
                                }
                            }
                        }
                    }
                } else if (actionType.equalsIgnoreCase("delete")) {
                    if (null != selectedIntegrationId) {
                        if (null != issueTypeMappingSelectId && !issueTypeMappingSelectId.equalsIgnoreCase("")) {
                            IssueTypeMapping issueTypeMapping = issueTypeMappingController.getRecordFromAOTableById(issueTypeMappingSelectId);
                            issueTypeMappingController.deleteRecordFromAOTable(issueTypeMapping);
                        }
                    }
                }
            } catch (Exception e) {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                log.error(stack.toString());
                throw new ServletException(e.getMessage());
            }
        }
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }

    private List<Map<String, String>> getIntegrationList () {
        List<Map<String, String>> integrationList = new ArrayList<>();
        try {
            Integration[] integrations = integrationController.getAllEntriesFromAOTable();
            Arrays.sort(integrations, Comparator.comparing(Integration::getName));
            for (Integration integration : integrations) {
                Map<String, String> integrationMap = new HashMap<>();
                integrationMap.put("integrationId", String.valueOf(integration.getID()));
                integrationMap.put("integrationName", integration.getName());
                integrationList.add(integrationMap);
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return integrationList;
    }

    private List<Map<String, String>> getIssueTypeMappingList (String integrationId) {
        List<Map<String, String>> issueTypeMappingList = new ArrayList<>();
        try {
            IssueTypeMapping[] issueTypeMappings = issueTypeMappingController.getRecordFromAOTableByIntegrationId(integrationId);

            for (IssueTypeMapping issueTypeMapping : issueTypeMappings) {
                Map<String, String> issueTypeMappingMap = new HashMap<>();
                issueTypeMappingMap.put("issueTypeMappingId", String.valueOf(issueTypeMapping.getID()));
                issueTypeMappingMap.put("issueTypeMappingName", issueTypeMapping.getName());
                issueTypeMappingList.add(issueTypeMappingMap);
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return issueTypeMappingList;
    }

    private List<Map<String, String>> getLocalProjectList () {
        List<Map<String, String>> localProjectList = new ArrayList<>();
        try {
            List<Project> projects = ComponentAccessor.getProjectManager().getProjectObjects();

            for (Project project : projects){
                Map<String, String> localProjectMap = new HashMap<>();
                localProjectMap.put("localProjectId", String.valueOf(project.getId()));
                localProjectMap.put("localProjectName", project.getName());
                localProjectList.add(localProjectMap);
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        Collections.sort(localProjectList, mapComparatorLocalProject);
        return localProjectList;
    }

    public Comparator<Map<String, String>> mapComparatorLocalProject = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("localProjectName").compareTo(m2.get("localProjectName"));
        }
    };

    private List<Map<String, String>> getLocalIssueTypeList (String localProjectId) {
        List<Map<String, String>> localIssueTypeList = new ArrayList<>();
        try {
            if (!localProjectId.equalsIgnoreCase("")) {
                Project project = projectManager.getProjectObj(Long.parseLong(localProjectId));
                Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(project);

                for (IssueType issueType : issueTypes) {
                    Map<String, String> localIssueTypeMap = new HashMap<>();
                    localIssueTypeMap.put("localIssueTypeId", String.valueOf(issueType.getId()));
                    localIssueTypeMap.put("localIssueTypeName", issueType.getName());
                    localIssueTypeList.add(localIssueTypeMap);
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        Collections.sort(localIssueTypeList, mapComparatorLocalIssueType);
        return localIssueTypeList;
    }

    public Comparator<Map<String, String>> mapComparatorLocalIssueType = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("localIssueTypeName").compareTo(m2.get("localIssueTypeName"));
        }
    };

    private List<Map<String, String>> getLocalEndStatusList (String localProjectId, String localIssueTypeId) {
        List<Map<String, String>> localEndStatusList = new ArrayList<>();
        try {
            if (!localProjectId.equalsIgnoreCase("") && !localIssueTypeId.equalsIgnoreCase("")) {
                Project project = projectManager.getProjectObj(Long.parseLong(localProjectId));
                IssueType issueType = constantsManager.getIssueType(localIssueTypeId);
                String workflowName = workflowSchemeManager.getWorkflowName(project, issueType.getId());

                JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(workflowName);
                List<Status> statusList = jiraWorkflow.getLinkedStatusObjects();

                for (Status status : statusList) {
                    Map<String, String> localEndStatusMap = new HashMap<>();
                    localEndStatusMap.put("localEndStatusId", status.getId());
                    localEndStatusMap.put("localEndStatusName", status.getName());
                    localEndStatusList.add(localEndStatusMap);
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        Collections.sort(localEndStatusList, mapComparatorLocalEndStatus);
        return localEndStatusList;
    }


    public Comparator<Map<String, String>> mapComparatorLocalEndStatus = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("localEndStatusName").compareTo(m2.get("localEndStatusName"));
        }
    };

    private List<Map<String, String>> getRemoteProjectList (IntegrationObject integrationObject) {
        List<Map<String, String>> remoteProjectList = new ArrayList<>();
        try {
            if (null != integrationObject) {
                List<RemoteProjectModel> remoteProjects = Utils.getRemoteProjects(integrationObject);

                for (RemoteProjectModel remoteProject : remoteProjects) {
                    Map<String, String> remoteProjectMap = new HashMap<>();
                    remoteProjectMap.put("remoteProjectId", remoteProject.getProjectId());
                    remoteProjectMap.put("remoteProjectName", remoteProject.getProjectName());
                    remoteProjectList.add(remoteProjectMap);
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        Collections.sort(remoteProjectList, mapComparatorRemoteProject);
        return remoteProjectList;
    }

    public Comparator<Map<String, String>> mapComparatorRemoteProject = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("remoteProjectName").compareTo(m2.get("remoteProjectName"));
        }
    };

    private IntegrationObject getIntegrationObject(String integrationId) {
        IntegrationObject integrationObject = null;
        try {
            Integration integration = integrationController.getRecordFromAOTableById(integrationId);
            integrationObject = new IntegrationObject(integration);
            integrationObject.setId(integrationId);
            integrationObject.setProxy(proxyController.getProxyRecordFromAOTable());
        } catch (Exception e) {
            Utils.printError(e);
        }

        return integrationObject;
    }

    private List<Map<String, String>> getRemoteIssueTypeList (IntegrationObject integrationObject) {
        List<Map<String, String>> remoteIssueTypeList = new ArrayList<>();
        try {
            if (null != integrationObject) {
                List<RemoteIssueTypeModel> remoteIssueTypes = Utils.getRemoteIssueTypes(integrationObject);

                for (RemoteIssueTypeModel remoteIssueType : remoteIssueTypes) {
                    Map<String, String> remoteIssueTypeMap = new HashMap<>();
                    remoteIssueTypeMap.put("remoteIssueTypeId", remoteIssueType.getIssueTypeId());
                    remoteIssueTypeMap.put("remoteIssueTypeName", remoteIssueType.getIssueTypeName());
                    remoteIssueTypeList.add(remoteIssueTypeMap);
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        Collections.sort(remoteIssueTypeList, mapComparatorRemoteIssueType);
        return remoteIssueTypeList;
    }

    public Comparator<Map<String, String>> mapComparatorRemoteIssueType = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("remoteIssueTypeName").compareTo(m2.get("remoteIssueTypeName"));
        }
    };
}