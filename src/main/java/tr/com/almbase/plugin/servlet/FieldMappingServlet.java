package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.model.RemoteFieldModel;
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
public class FieldMappingServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(FieldMappingServlet.class);
    private static final String FIELD_MAPPING_TEMPLATE = "/templates/adminscreens/fieldmapping.vm";
    private static final String FIELD_MAPPING_DETAIL_TEMPLATE = "/templates/adminscreens/fieldmappingdetail.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private final PageBuilderService pageBuilderService;
    private final UserManager userManager;

    private final IntegrationController integrationController;
    private final ProxyController proxyController;
    private final IssueTypeMappingController issueTypeMappingController;
    private final FieldMappingController fieldMappingController;

    public FieldMappingServlet(TemplateRenderer templateRenderer,
                               JiraAuthenticationContext jiraAuthenticationContext,
                               LoginUriProvider loginUriProvider,
                               PageBuilderService pageBuilderService,
                               UserManager userManager,
                               IntegrationController integrationController,
                               ProxyController proxyController,
                               IssueTypeMappingController issueTypeMappingController,
                               FieldMappingController fieldMappingController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.pageBuilderService = pageBuilderService;
        this.userManager = userManager;
        this.integrationController = integrationController;
        this.proxyController = proxyController;
        this.issueTypeMappingController = issueTypeMappingController;
        this.fieldMappingController = fieldMappingController;
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
            String tableParameters = req.getParameter("tableData");
            String tableSelectParameters = req.getParameter("tableDataSelect");
            String initial = req.getParameter("initial") == null ? "" : req.getParameter("initial").trim();
            String addnewrow = req.getParameter("addnewrow") == null ? "" : req.getParameter("addnewrow").trim();
            String deleterow = req.getParameter("deleterow") == null ? "" : req.getParameter("deleterow").trim();
            String issueTypeMappingSelectChanged = req.getParameter("issuetypemappingselectchanged") == null ? "" : req.getParameter("issuetypemappingselectchanged").trim();

            String selectedIntegrationId = req.getParameter("selectedIntegrationId") == null ? "" : req.getParameter("selectedIntegrationId").trim();
            String issueTypeMappingSelectId = req.getParameter("issueTypeMappingSelectId") == null ? "" : req.getParameter("issueTypeMappingSelectId").trim();

            if (initial.equalsIgnoreCase("yes")) {
                context.put("selectedIntegrationId", selectedIntegrationId);
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));
                templateRenderer.render(FIELD_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (issueTypeMappingSelectChanged.equalsIgnoreCase("yes")) {

                IntegrationObject integrationObject = getIntegrationObject(selectedIntegrationId);

                List<Map<String, String>> localFieldList = getLocalFieldList();
                List<Map<String, String>> remoteFieldList = getRemoteFieldList(integrationObject);

                if (null != issueTypeMappingSelectId && !issueTypeMappingSelectId.equalsIgnoreCase("")) {
                    IssueTypeMapping issueTypeMapping = issueTypeMappingController.getRecordFromAOTableById(issueTypeMappingSelectId);
                    List<Map<String, String>> fieldMappingList = new ArrayList<>();

                    if (null != issueTypeMapping) {
                        FieldMapping[] fieldMappings = fieldMappingController.getRecordFromAOTableByIssueTypeMappingId(String.valueOf(issueTypeMapping.getID()));
                        for (FieldMapping fieldMapping : fieldMappings) {
                            Map<String, String> fieldMappingMap = new HashMap<>();
                            fieldMappingMap.put("localFieldId", fieldMapping.getLocalFieldId());
                            fieldMappingMap.put("localFieldName", getFieldSelectedName(localFieldList, fieldMapping.getLocalFieldId(), "local"));
                            fieldMappingMap.put("remoteFieldId", fieldMapping.getRemoteFieldId());
                            fieldMappingMap.put("remoteFieldName", getFieldSelectedName(remoteFieldList, fieldMapping.getRemoteFieldId(), "remote"));
                            fieldMappingList.add(fieldMappingMap);
                        }

                        context.put("fieldMappingList", fieldMappingList);
                        context.put("selectedIssueTypeMappingId", String.valueOf(issueTypeMapping.getID()));
                        context.put("fieldMappingFieldsAvail", "yes");
                    }

                    context.put("localFieldList", adjustLocalFieldList(localFieldList, fieldMappingList, "local"));
                    context.put("remoteFieldList", adjustLocalFieldList(remoteFieldList, fieldMappingList, "remote"));
                } else {
                    context.put("localFieldList", getLocalFieldList());
                    context.put("remoteFieldList", getRemoteFieldList(integrationObject));
                }

                context.put("selectedIntegrationId", selectedIntegrationId);
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));
                templateRenderer.render(FIELD_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (addnewrow.equalsIgnoreCase("yes")) {

                IntegrationObject integrationObject = getIntegrationObject(selectedIntegrationId);

                List<Map<String, String>> localFieldList = getLocalFieldList();
                List<Map<String, String>> remoteFieldList = getRemoteFieldList(integrationObject);

                JsonParser parser = new JsonParser();
                JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);
                JsonArray jsonTableSelectArray = (JsonArray) parser.parse(tableSelectParameters);

                List<Map<String, String>> fieldMappingList = new ArrayList<>();
                for (int i = 0; i < jsonTableArray.size(); i++) {
                    JsonElement jsonTableRowElement = jsonTableArray.get(i);
                    JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                    Map<String, String> fieldMap = new HashMap<>();
                    String localFieldId = jsonTableRowArray.get(0).getAsString();
                    String remoteFieldId = jsonTableRowArray.get(1).getAsString();
                    fieldMap.put("localFieldId", localFieldId);
                    fieldMap.put("localFieldName", getFieldSelectedName(localFieldList, localFieldId, "local"));
                    fieldMap.put("remoteFieldId", remoteFieldId);
                    fieldMap.put("remoteFieldName", getFieldSelectedName(remoteFieldList, remoteFieldId, "remote"));
                    fieldMappingList.add(fieldMap);
                }

                for (int i = 0; i < jsonTableSelectArray.size(); i++) {
                    JsonElement jsonTableRowElement = jsonTableSelectArray.get(i);
                    JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                    Map<String, String> fieldMap = new HashMap<>();
                    String localFieldId = jsonTableRowArray.get(0).getAsString();
                    String remoteFieldId = jsonTableRowArray.get(1).getAsString();
                    fieldMap.put("localFieldId", localFieldId);
                    fieldMap.put("localFieldName", getFieldSelectedName(localFieldList, localFieldId, "local"));
                    fieldMap.put("remoteFieldId", remoteFieldId);
                    fieldMap.put("remoteFieldName", getFieldSelectedName(remoteFieldList, remoteFieldId, "remote"));
                    fieldMappingList.add(fieldMap);
                }

                context.put("fieldMappingList", fieldMappingList);
                context.put("selectedIntegrationId", selectedIntegrationId);
                context.put("selectedIssueTypeMappingId", issueTypeMappingSelectId);
                context.put("fieldMappingFieldsAvail", "yes");
                context.put("localFieldList", adjustLocalFieldList(localFieldList, fieldMappingList, "local"));
                context.put("remoteFieldList", adjustLocalFieldList(remoteFieldList, fieldMappingList, "remote"));
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));
                templateRenderer.render(FIELD_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else if (deleterow.equalsIgnoreCase("yes")) {

                IntegrationObject integrationObject = getIntegrationObject(selectedIntegrationId);

                List<Map<String, String>> localFieldList = getLocalFieldList();
                List<Map<String, String>> remoteFieldList = getRemoteFieldList(integrationObject);

                JsonParser parser = new JsonParser();
                JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                List<Map<String, String>> fieldMappingList = new ArrayList<>();
                for (int i = 0; i < jsonTableArray.size(); i++) {
                    JsonElement jsonTableRowElement = jsonTableArray.get(i);
                    JsonArray jsonTableRowArray = (JsonArray)parser.parse(jsonTableRowElement.getAsString());

                    Map<String, String> fieldMap = new HashMap<>();
                    String localFieldId = jsonTableRowArray.get(0).getAsString();
                    String remoteFieldId = jsonTableRowArray.get(1).getAsString();
                    fieldMap.put("localFieldId", localFieldId);
                    fieldMap.put("localFieldName", getFieldSelectedName(localFieldList, localFieldId, "local"));
                    fieldMap.put("remoteFieldId", remoteFieldId);
                    fieldMap.put("remoteFieldName", getFieldSelectedName(remoteFieldList, remoteFieldId, "remote"));
                    fieldMappingList.add(fieldMap);
                }

                context.put("selectedIssueTypeMappingId", issueTypeMappingSelectId);
                context.put("fieldMappingFieldsAvail", "yes");
                context.put("fieldMappingList", fieldMappingList);
                context.put("selectedIntegrationId", selectedIntegrationId);
                context.put("localFieldList", adjustLocalFieldList(localFieldList, fieldMappingList, "local"));
                context.put("remoteFieldList", adjustLocalFieldList(remoteFieldList, fieldMappingList, "remote"));
                context.put("issueTypeMappingList", getIssueTypeMappingList(selectedIntegrationId));
                templateRenderer.render(FIELD_MAPPING_DETAIL_TEMPLATE, context, resp.getWriter());
            } else {
                context.put("integrationList", getIntegrationList());
                templateRenderer.render(FIELD_MAPPING_TEMPLATE, context, resp.getWriter());
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
                String selectedIntegrationId = req.getParameter("selectedIntegrationId");
                String issueTypeMappingSelectId = req.getParameter("issueTypeMappingSelectId");
                String tableParameters = req.getParameter("tableData");

                if (null != selectedIntegrationId && !selectedIntegrationId.equalsIgnoreCase("")) {
                    if (null != issueTypeMappingSelectId && !issueTypeMappingSelectId.equalsIgnoreCase("")) {
                        List<Map<String, String>> fieldMapList = new ArrayList<>();

                        JsonParser parser = new JsonParser();
                        JsonArray jsonTableArray = (JsonArray) parser.parse(tableParameters);

                        for (int i = 0; i < jsonTableArray.size(); i++) {
                            JsonElement jsonTableRowElement = jsonTableArray.get(i);
                            JsonArray jsonTableRowArray = (JsonArray) parser.parse(jsonTableRowElement.getAsString());

                            Map<String, String> fieldMap = new HashMap<>();
                            String localFieldId = jsonTableRowArray.get(0).getAsString();
                            String remoteFieldId = jsonTableRowArray.get(1).getAsString();
                            fieldMap.put("localFieldId", localFieldId);
                            fieldMap.put("remoteFieldId", remoteFieldId);
                            fieldMapList.add(fieldMap);
                        }

                        if (fieldMapList.size() > 0) {
                            FieldMapping[] fieldMappings = fieldMappingController.getRecordFromAOTableByIssueTypeMappingId(issueTypeMappingSelectId);
                            for (FieldMapping fieldMapping : fieldMappings) {
                                fieldMappingController.deleteRecordFromAOTable(fieldMapping);
                            }

                            Iterator fieldMapIterator = fieldMapList.iterator();
                            while (fieldMapIterator.hasNext()) {
                                Map<String, String> fieldMap = (Map<String, String>) fieldMapIterator.next();
                                String localFieldId = fieldMap.get("localFieldId");
                                String remoteFieldId = fieldMap.get("remoteFieldId");

                                if (null != localFieldId && !localFieldId.equalsIgnoreCase("") && null != remoteFieldId && !remoteFieldId.equalsIgnoreCase("")) {
                                    FieldMappingObject fieldMappingObject = new FieldMappingObject();
                                    fieldMappingObject.setIntegrationId(selectedIntegrationId);
                                    fieldMappingObject.setItMappingId(issueTypeMappingSelectId);
                                    fieldMappingObject.setLocalFieldId(localFieldId);
                                    fieldMappingObject.setRemoteFieldId(remoteFieldId);
                                    fieldMappingController.createRecordInAOTable(fieldMappingObject);
                                }
                            }
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

    private List<Map<String, String>> getLocalFieldList () {
        List<Map<String, String>> localFieldList = new ArrayList<>();
        try {
            Iterator<NavigableField> it = ComponentAccessor.getFieldManager().getNavigableFields().iterator();
            while (it.hasNext()) {
                NavigableField nf = it.next();
                Map<String, String> localFieldMap = new HashMap<>();
                localFieldMap.put("localFieldId", nf.getId());
                localFieldMap.put("localFieldName", nf.getName());
                localFieldList.add(localFieldMap);
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        Collections.sort(localFieldList, mapComparatorLocalField);
        return localFieldList;
    }

    public Comparator<Map<String, String>> mapComparatorLocalField = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("localFieldName").compareTo(m2.get("localFieldName"));
        }
    };

    private List<Map<String, String>> getRemoteFieldList (IntegrationObject integrationObject) {
        List<Map<String, String>> remoteFieldList = new ArrayList<>();
        try {
            if (null != integrationObject) {
                List<RemoteFieldModel> remoteFields = Utils.getRemoteFields(integrationObject);

                for (RemoteFieldModel remoteFieldModel : remoteFields) {
                    Map<String, String> localFieldMap = new HashMap<>();
                    localFieldMap.put("remoteFieldId", remoteFieldModel.getId());
                    localFieldMap.put("remoteFieldName", remoteFieldModel.getName());
                    remoteFieldList.add(localFieldMap);
                }

            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        Collections.sort(remoteFieldList, mapComparatorRemoteField);
        return remoteFieldList;
    }

    public Comparator<Map<String, String>> mapComparatorRemoteField = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("remoteFieldName").compareTo(m2.get("remoteFieldName"));
        }
    };

    private String getFieldSelectedName (List<Map<String, String>> fieldList, String fieldId, String type) {
        String fieldName = "";
        try {
            String fieldIdType = "";
            String fieldNameType = "";
            if (type.equalsIgnoreCase("local")) {
                fieldIdType = "localFieldId";
                fieldNameType = "localFieldName";
            } else if (type.equalsIgnoreCase("remote")) {
                fieldIdType = "remoteFieldId";
                fieldNameType = "remoteFieldName";
            }

            for (Map<String, String> fieldMap : fieldList) {
                if (fieldMap.get(fieldIdType).equalsIgnoreCase(fieldId)) {
                    fieldName = fieldMap.get(fieldNameType);
                    break;
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return fieldName;
    }

    private List<Map<String, String>> adjustLocalFieldList (List<Map<String, String>> fieldList, List<Map<String, String>> fieldMappingList, String type) {
        try {
            for (Map<String, String> fieldMappingMap : fieldMappingList) {
                String fieldIdType = "";
                if (type.equalsIgnoreCase("local"))
                    fieldIdType = "localFieldId";
                else if (type.equalsIgnoreCase("remote"))
                    fieldIdType = "remoteFieldId";

                for (Map<String, String> fieldMap : fieldList) {
                    if (fieldMap.get(fieldIdType).equalsIgnoreCase(fieldMappingMap.get(fieldIdType))) {
                        fieldList.remove(fieldMap);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return fieldList;
    }

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
}