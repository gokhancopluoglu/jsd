package tr.com.almbase.plugin.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.IntegrationObject;
import tr.com.almbase.plugin.model.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by kivanc.ahat@almbase.com on 16/11/2017.
 */

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("d/MMM/yy");

    public Utils() {

    }

    public static void printError(Exception e) {
        StringWriter stack = new StringWriter();
        e.printStackTrace(new PrintWriter(stack));
        log.error(stack.toString());
    }

    public static void printDebugError(Exception e) {
        StringWriter stack = new StringWriter();
        e.printStackTrace(new PrintWriter(stack));
        log.debug(stack.toString());
    }

    public static void printDebug(String message) {
        log.debug(message);
    }

    public static String createRemoteIssue (String paylod, IntegrationObject integrationObject) throws Exception {
        try {
            Response response = createIssue(paylod, integrationObject);
            log.debug(response.getResponse());

            JSONObject jsonPost = new JSONObject(response.getResponse());
            return jsonPost.getString("key");
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response createIssue (String paylod, IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_CREATE_ISSUE;
            return doPost(restUrl, paylod, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getProject (String projectId, IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_PROJECT;
            restUrl = restUrl.replace("PROJECTKEY", projectId);
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getIssue (String issueKey, IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_ISSUE;
            restUrl = restUrl.replace("ISSUEKEY", issueKey);
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getRemoteCustomField (String projectKey, String issueTypeId, IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_ISSUE_CREATE_METADATA;
            restUrl = restUrl.replace("PROJECTKEY", projectKey);
            restUrl = restUrl.replace("ISSUETYPEID", issueTypeId);
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getProjects (IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_ALL_PROJECTS;
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getProjectComponents (String projectKey, IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_PROJECT_COMPONENTS;
            restUrl = restUrl.replace("PROJECTKEY", projectKey);
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getProjectVersions (String projectKey, IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_PROJECT_VERSIONS;
            restUrl = restUrl.replace("PROJECTKEY", projectKey);
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getIssueTypes (IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_ISSUE_TYPES;
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    private static Response getFields (IntegrationObject integrationObject) throws Exception {
        try {
            String restUrl = integrationObject.getUrl() + Constants.REST_GET_FIELDS;
            return doGet(restUrl, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
            throw e;
        }
    }

    public static String getFormatedDate (Date date)
    {
        try {
            return SIMPLE_DATE_FORMAT.format(date);
        }
        catch (Exception e)
        {
            Utils.printError(e);
        }

        return null;
    }

    public static Project getProjectObject (String projectKey)
    {
        try
        {
            return ComponentAccessor.getProjectManager().getProjectByCurrentKey(projectKey);
        }
        catch (Exception e)
        {
            Utils.printError(e);
        }

        return null;
    }

    public static Object getCustomFieldValue(Issue issue, String customFieldId)
    {
        Object cfVal = null;
        try
        {
            CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(customFieldId);
            if (customField != null)
            {
                if (issue.getCustomFieldValue(customField) != null)
                {
                    if (issue.getCustomFieldValue(customField) instanceof Option)
                    {
                        Option option = (Option)issue.getCustomFieldValue(customField);
                        cfVal = option.getValue();
                    } else if (issue.getCustomFieldValue(customField) instanceof GenericValue) {
                        GenericValue gv = (GenericValue)issue.getCustomFieldValue(customField);
                        cfVal = gv.getOriginalDbValue("originalkey");
                    } else if (issue.getCustomFieldValue(customField) instanceof String) {
                        cfVal = issue.getCustomFieldValue(customField);
                    } else if (issue.getCustomFieldValue(customField) instanceof Date) {
                        cfVal = issue.getCustomFieldValue(customField);
                    } else if (issue.getCustomFieldValue(customField) instanceof ApplicationUser) {
                        ApplicationUser user = (ApplicationUser)issue.getCustomFieldValue(customField);
                        cfVal = user.getDirectoryUser().getName();
                    }
                }
            } else {
                log.debug("Customfield with id: " + customFieldId + " is null!");
            }
        }
        catch (Exception e)
        {
            printError(e);
        }
        return cfVal;
    }

    public static Locale getLocale(ApplicationUser user) {
        try {
            if (user == null) {
                log.debug ("user is null");
                return Locale.getDefault();
            }

            ExtendedPreferences ep = ComponentAccessor.getUserPreferencesManager().getExtendedPreferences(user);
            String userLocaleString = ep.getText(PreferenceKeys.USER_LOCALE);

            if (userLocaleString == null) {
                log.debug ("userLocaleString is null");
                return new Locale("tr", "TR");
            }

            StringTokenizer tokenizer = new StringTokenizer(userLocaleString, "_");
            String language = tokenizer.nextToken();
            String region = tokenizer.nextToken();

            if (language != null && region != null) {
                log.debug ("language is " + language + " and region is " + region);
                return new Locale(language, region);
            } else {
                return Locale.getDefault();
            }
        } catch (Exception e) {
            printError(e);
        }

        return Locale.getDefault();
    }

    public static Response doGet(String confUrl, IntegrationObject integrationObject) throws Exception {
        Credentials credentials = new UsernamePasswordCredentials(integrationObject.getUsername(), integrationObject.getPassword());
        HttpGet httpGet = new HttpGet(confUrl);
        Header header = new BasicScheme(StandardCharsets.UTF_8).authenticate(credentials, httpGet, null);
        httpGet.addHeader(header);
        httpGet.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        return send(httpGet, integrationObject);
    }

    public static Response doPost(String confUrl, String payload, IntegrationObject integrationObject) throws Exception {
        Credentials credentials = new UsernamePasswordCredentials(integrationObject.getUsername(), integrationObject.getPassword());
        HttpPost httpPost = new HttpPost(confUrl);
        Header header = new BasicScheme(StandardCharsets.UTF_8).authenticate(credentials, httpPost, null);
        httpPost.addHeader(header);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setEntity(new StringEntity(payload, StandardCharsets.UTF_8.name()));
        return send(httpPost, integrationObject);
    }

    public static Response doPut(String confUrl, String payload, IntegrationObject integrationObject) throws Exception {
        Credentials credentials = new UsernamePasswordCredentials(integrationObject.getUsername(), integrationObject.getPassword());
        HttpPut httpPut = new HttpPut(confUrl);
        Header header = new BasicScheme(StandardCharsets.UTF_8).authenticate(credentials, httpPut, null);
        httpPut.addHeader(header);
        httpPut.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPut.setEntity(new StringEntity(payload, StandardCharsets.UTF_8.name()));

        return send(httpPut, integrationObject);
    }

    private static HttpClientBuilder getHttpClientBuilder() {
        HttpClientBuilder httpClientBuilder = null;
        try {
            httpClientBuilder = HttpClientBuilder.create();
            String checkRevocation = System.getProperty("com.sun.net.ssl.checkRevocation");
            if (checkRevocation == null || (checkRevocation.contentEquals("false"))) {
                httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
                SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

                try {
                    sslContextBuilder.loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()),
                            new TrustSelfSignedStrategy() {
                                @Override
                                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                    return true;
                                }
                            });
                    SSLContext ssLContext = sslContextBuilder.build();

                    httpClientBuilder.setSSLContext(ssLContext);
                } catch (Exception e) {
                    printError(e);
                }

                httpClientBuilder.setSSLHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            printError(e);
        }
        return httpClientBuilder;
    }

    private static Response send(HttpRequestBase httpRequestBase, IntegrationObject integrationObject) throws Exception {
        CloseableHttpResponse httpResponse = null;
        HttpClientBuilder httpClientBuilder = getHttpClientBuilder();
        if (null != integrationObject.getProxy()) {
            HttpHost httpProxy = new HttpHost(integrationObject.getProxy().getHost(), Integer.parseInt(integrationObject.getProxy().getPort()), integrationObject.getProxy().getType());
            httpClientBuilder.setProxy(httpProxy);
        }
        CloseableHttpClient httpClient = httpClientBuilder.build();
        try {
            Response response = new Response();
            String responseString = "";
            httpResponse = httpClient.execute(httpRequestBase);

            if (httpResponse != null) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (httpResponse.getEntity() != null) {
                    InputStreamReader reader = new InputStreamReader(httpResponse.getEntity().getContent());
                    BufferedReader br = new BufferedReader(reader);
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
                    reader.close();
                    responseString = sb.toString();
                    JsonParser parser = new JsonParser();
                    try {
                        JsonObject responseJSON;
                        if (!responseString.equals("")) {
                            try {
                                responseJSON = (JsonObject) parser.parse(responseString);
                            } catch (ClassCastException e) {
                                JsonArray responseArray = (JsonArray) parser.parse(responseString);
                                responseJSON = new JsonObject();
                                responseJSON.add("jsonArray", responseArray);
                            }
                        } else {
                            responseJSON = new JsonObject();
                            responseJSON.addProperty("200", "succesful but response null");
                        }

                        if (responseJSON.has("error") && statusCode > 400) {
                            JsonObject error = responseJSON.getAsJsonObject("error");
                            String message = "";
                            if (error.has("message"))
                                message = error.get("message").getAsString();

                            throw new Exception(message);
                        } else if (responseJSON.has("errorMessages")) {
                            throw new Exception(responseString);
                        } else if (statusCode > 400) {
                            throw new Exception(responseString);
                        }
                    } catch (JsonParseException e) {
                        // input is not in json format, shall be handled in upper
                        if (statusCode > 400) {
                            throw new Exception("Json Parse Exception - it couse could be authentication fail to endpoint");
                        }
                    }

                } else if (statusCode > 300) {
                    throw new Exception(responseString);
                }

                response.setResponseCode(statusCode);
                response.setResponse(responseString);
            }

            return response;
        } catch (IOException e) {
            printError(e);
            throw new Exception("Error at reading: " + e.getMessage());
        } finally {
            try {
                if (null != httpResponse)
                    httpResponse.close();
                if (null != httpClient)
                    httpClient.close();
            } catch (Exception e2) {
                printError(e2);
            }
        }
    }

    public static RemoteIssueModel getRemoteIssue(String remoteIssueKey, IntegrationObject integrationObject) {
        RemoteIssueModel remoteIssueModel = null;
        try {
            Response response = getIssue(remoteIssueKey, integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                remoteIssueModel = new RemoteIssueModel();

                JSONObject expand = new JSONObject(response.getResponse());
                JSONObject fields = expand.getJSONObject("fields");

                //Issue Key
                if (expand.has("key")) {
                    if (!expand.isNull("key")) {
                        remoteIssueModel.setIssueKey(expand.getString("key"));
                    }
                }

                //Summary
                if (fields.has("summary")) {
                    if (!fields.isNull("summary")) {
                        remoteIssueModel.setSummary(fields.getString("summary"));
                    }
                }

                //Description
                if (fields.has("description")) {
                    if (!fields.isNull("description")) {
                        remoteIssueModel.setDescription(fields.getString("description"));
                    }
                }

                //Created Date
                if (fields.has("created")) {
                    if (!fields.isNull("created")) {
                        remoteIssueModel.setCreateDate(fields.getString("created"));
                    }
                }

                //Updated Date
                if (fields.has("updated")) {
                    if (!fields.isNull("updated")) {
                        remoteIssueModel.setUpdatedDate(fields.getString("updated"));
                    }
                }

                //Due Date
                if (fields.has("duedate")) {
                    if (!fields.isNull("duedate")) {
                        remoteIssueModel.setDueDate(fields.getString("duedate"));
                    }
                }

                //Project Infos
                String projectKey = null;
                if (fields.has("project")) {
                    if (!fields.isNull("project")) {
                        JSONObject project = fields.getJSONObject("project");
                        if (null != project) {
                            String projectId = project.getString("id");
                            projectKey = project.getString("key");
                            String projectName = project.getString("name");
                            remoteIssueModel.setProject(new RemoteProjectModel(projectId, projectKey, projectName));
                        }
                    }
                }

                //Issue Type Info
                if (fields.has("issuetype")) {
                    if (!fields.isNull("issuetype")) {
                        JSONObject issuetype = fields.getJSONObject("issuetype");
                        if (null != issuetype) {
                            String issueTypeId = issuetype.getString("id");
                            String issueTypeName = issuetype.getString("name");
                            remoteIssueModel.setIssueType(new RemoteIssueTypeModel(issueTypeId, issueTypeName));
                        }
                    }
                }

                //Reporter
                if (fields.has("reporter")) {
                    if (!fields.isNull("reporter")) {
                        JSONObject reporter = fields.getJSONObject("reporter");
                        if (null != reporter) {
                            String reporterName = reporter.getString("name");
                            String reporterDisplayName = reporter.getString("displayName");
                            Map<String, String> reporterMap = new HashMap<>();
                            reporterMap.put("name", reporterName);
                            reporterMap.put("displayName", reporterDisplayName);
                            remoteIssueModel.setReporter(reporterMap);
                        }
                    }
                }

                //Assignee
                if (fields.has("assignee")) {
                    if (!fields.isNull("assignee")) {
                        JSONObject assignee = fields.getJSONObject("assignee");
                        if (null != assignee) {
                            String assigneeName = assignee.getString("name");
                            String assigneeDisplayName = assignee.getString("displayName");
                            Map<String, String> assigneeMap = new HashMap<>();
                            assigneeMap.put("name", assigneeName);
                            assigneeMap.put("displayName", assigneeDisplayName);
                            remoteIssueModel.setAssignee(assigneeMap);
                        }
                    }
                }

                //Components
                if (fields.has("components")) {
                    if (!fields.isNull("components")) {
                        JSONArray components = fields.getJSONArray("components");
                        if (null != components) {
                            for (int i = 0; i < components.length(); i++) {
                                JSONObject component = components.getJSONObject(i);
                                String id = component.getString("id");
                                String name = component.getString("name");

                                RemoteComponentModel componentModel = new RemoteComponentModel(projectKey, id, name);
                                remoteIssueModel.getComponents().add(componentModel);
                            }
                        }
                    }
                }

                //Status
                if (fields.has("status")) {
                    if (!fields.isNull("status")) {
                        JSONObject status = fields.getJSONObject("status");
                        if (null != status) {
                            String statusName = status.getString("name");
                            String statusId = status.getString("id");
                            JSONObject statusCategory = status.getJSONObject("statusCategory");
                            String statusColor = statusCategory.getString("colorName");
                            remoteIssueModel.setStatus(new RemoteStatusModel(statusId, statusName, statusColor));
                        }
                    }
                }

                //Affected Versions
                if (fields.has("versions")) {
                    if (!fields.isNull("versions")) {
                        JSONArray versions = fields.getJSONArray("versions");
                        if (null != versions) {
                            for (int i = 0; i < versions.length(); i++) {
                                JSONObject version = versions.getJSONObject(i);
                                String id = version.getString("id");
                                String name = version.getString("name");

                                RemoteVersionModel versionModel = new RemoteVersionModel(projectKey, id, name);
                                remoteIssueModel.getAffectedVersions().add(versionModel);
                            }
                        }
                    }
                }

                //Fix Versions
                if (fields.has("fixVersions")) {
                    if (!fields.isNull("fixVersions")) {
                        JSONArray fixVersions = fields.getJSONArray("fixVersions");
                        if (null != fixVersions) {
                            for (int i = 0; i < fixVersions.length(); i++) {
                                JSONObject fixVersion = fixVersions.getJSONObject(i);
                                String id = fixVersion.getString("id");
                                String name = fixVersion.getString("name");

                                RemoteVersionModel versionModel = new RemoteVersionModel(projectKey, id, name);
                                remoteIssueModel.getFixVersions().add(versionModel);
                            }
                        }
                    }
                }

                //Priority
                if (fields.has("priority")) {
                    if (!fields.isNull("priority")) {
                        JSONObject priority = fields.getJSONObject("priority");
                        if (null != priority) {
                            String priorityId = priority.getString("id");
                            String priorityName = priority.getString("name");
                            remoteIssueModel.setPriority(new RemotePriorityModel(priorityId, priorityName));
                        }
                    }
                }

                //Resolution
                if (fields.has("resolution")) {
                    if (!fields.isNull("resolution")) {
                        JSONObject resolution = fields.getJSONObject("resolution");
                        if (null != resolution) {
                            String resolutionId = resolution.getString("id");
                            String resolutionName = resolution.getString("name");
                            remoteIssueModel.setResolution(new RemoteResolutionModel(resolutionId, resolutionName));
                        }
                    }
                }

                //Labels
                if (fields.has("labels")) {
                    if (!fields.isNull("labels")) {
                        JSONArray labels = fields.getJSONArray("labels");
                        if (null != labels) {
                            for (int i = 0; i < labels.length(); i++) {
                                String label = labels.getString(i);
                                remoteIssueModel.getLabels().add(label);
                            }
                        }
                    }
                }
            } else {
                log.debug("Can not get Issue : " + remoteIssueKey + " : Response code is : " + response.getResponseCode());
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteIssueModel;
    }

    public static List<RemoteProjectModel> getRemoteProjects(IntegrationObject integrationObject) {
        List<RemoteProjectModel> remoteProjectList = new ArrayList<>();
        try {
            Response response = getProjects(integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                JSONArray array = new JSONArray(response.getResponse());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject project = array.getJSONObject(i);
                    String projectId = project.getString("id");
                    String projectName = project.getString("name");
                    String projectKey = project.getString("key");
                    remoteProjectList.add(new RemoteProjectModel(projectId, projectKey, projectName));

                }
            } else {
                log.debug("Can not get Projects : Response code is : " + response.getResponseCode());
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteProjectList;
    }

    public static RemoteProjectModel getRemoteProject(String projectId, IntegrationObject integrationObject) {
        RemoteProjectModel remoteProjectModel = null;
        try {
            Response response = getProject(projectId, integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                JSONObject project = new JSONObject(response.getResponse());
                projectId = project.getString("id");
                String projectKey = project.getString("key");
                String projectName = project.getString("name");

                remoteProjectModel = new RemoteProjectModel(projectId, projectKey, projectName);

            } else {
                log.debug("Can not get Projects : Response code is : " + response.getResponseCode());
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteProjectModel;
    }

    public static List<RemoteComponentModel> getRemoteProjectComponents(String projectKey, IntegrationObject integrationObject) {
        List<RemoteComponentModel> remoteComponentList = new ArrayList<>();
        try {
            Response response = getProjectComponents(projectKey, integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                JSONArray array = new JSONArray(response.getResponse());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject component = array.getJSONObject(i);
                    String componentId = component.getString("id");
                    String componentName = component.getString("name");
                    remoteComponentList.add(new RemoteComponentModel(projectKey, componentId, componentName));
                }
            } else {
                log.debug("Can not get Project's Component : " + projectKey + " Response code is : " + response.getResponseCode());
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteComponentList;
    }

    public static List<RemoteVersionModel> getRemoteProjectVersions(String projectKey, IntegrationObject integrationObject) {
        List<RemoteVersionModel> remoteVersionList = new ArrayList<>();
        try {
            Response response = getProjectVersions(projectKey, integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                JSONArray array = new JSONArray(response.getResponse());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject version = array.getJSONObject(i);
                    String versionId = version.getString("id");
                    String versionName = version.getString("name");
                    remoteVersionList.add(new RemoteVersionModel(projectKey, versionId, versionName));
                }
            } else {
                log.debug("Can not get Project's Version : " + projectKey + " Response code is : " + response.getResponseCode());
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteVersionList;
    }

    public static List<RemoteIssueTypeModel> getRemoteIssueTypes(IntegrationObject integrationObject) {
        List<RemoteIssueTypeModel> remoteIssueTypeList = new ArrayList<>();
        try {
            Response response = getIssueTypes(integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                JSONArray array = new JSONArray(response.getResponse());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject issueType = array.getJSONObject(i);
                    String issueTypeId = issueType.getString("id");
                    String issueTypeName = issueType.getString("name");
                    remoteIssueTypeList.add(new RemoteIssueTypeModel(issueTypeId, issueTypeName));
                }
            } else {
                log.debug("Can not get Issue Types : Response code is : " + response.getResponseCode());
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteIssueTypeList;
    }

    public static List<RemoteFieldModel> getRemoteFields(IntegrationObject integrationObject) {
        List<RemoteFieldModel> remoteFieldList = new ArrayList<>();
        try {
            Response response = getFields(integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                JSONArray array = new JSONArray(response.getResponse());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject field = array.getJSONObject(i);
                    String id = field.getString("id");
                    String name = field.getString("name");
                    String type = "";
                    String system = "";
                    String custom = "";
                    if (field.has("schema")) {
                        JSONObject schema = field.getJSONObject("schema");
                        if (null != schema) {
                            if (schema.has("type"))
                                type = schema.getString("type");
                            if (schema.has("system"))
                                system = schema.getString("system");
                            if (schema.has("custom"))
                                custom = schema.getString("custom");
                        }
                    }
                    remoteFieldList.add(new RemoteFieldModel(id, name, type, system, custom));
                }
            } else {
                log.debug("Can not get Fields : Response code is : " + response.getResponseCode());
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteFieldList;
    }

    public static RemoteCustomFieldModel getRemoteCustomFieldModel (String projectKey, String issueTypeId, String fieldId, IntegrationObject integrationObject) {
        RemoteCustomFieldModel remoteSelectListModel = null;
        try {
            Response response = getRemoteCustomField(projectKey, issueTypeId, integrationObject);

            if (response.getResponseCode() == 200) {
                printDebug(response.getResponse());
                remoteSelectListModel = new RemoteCustomFieldModel(projectKey, issueTypeId);

                JSONObject expand = new JSONObject(response.getResponse());
                JSONArray projectArray = expand.getJSONArray("projects");
                JSONObject projectObject = projectArray.getJSONObject(0);

                if (projectObject.has("issuetypes")) {
                    JSONArray issueTypeArray = projectObject.getJSONArray("issuetypes");
                    JSONObject issueTypeObject = issueTypeArray.getJSONObject(0);

                    if (issueTypeObject.has("fields")) {
                        JSONObject fieldsObject = issueTypeObject.getJSONObject("fields");

                        if (fieldsObject.has(fieldId)) {
                            JSONObject field = fieldsObject.getJSONObject(fieldId);

                            if (null != field) {
                                remoteSelectListModel.setId(fieldId);
                                if (field.has("name")) {
                                    remoteSelectListModel.setName(field.getString("name"));
                                }

                                if (field.has("schema")) {
                                    JSONObject fieldSchema = field.getJSONObject("schema");
                                    if (fieldSchema.has("custom")) {
                                        String custom = fieldSchema.getString("custom");
                                        remoteSelectListModel.setType(custom);

                                        if (custom.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:select")) {
                                            if (field.has("allowedValues")) {
                                                JSONArray allowedValuesArray = field.getJSONArray("allowedValues");

                                                for (int i=0;i<allowedValuesArray.length();i++) {
                                                    JSONObject allowedValueObject = allowedValuesArray.getJSONObject(i);
                                                    Map<String, String> allowedValueMap = new HashMap<>();
                                                    String id = allowedValueObject.getString("id");
                                                    String value = allowedValueObject.getString("value");
                                                    allowedValueMap.put("id", id);
                                                    allowedValueMap.put("value", value);
                                                    remoteSelectListModel.getAllowedValues().add(allowedValueMap);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            printError(e);
        }
        return remoteSelectListModel;
    }
}
