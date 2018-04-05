package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.model.RemoteComponentModel;
import tr.com.almbase.plugin.model.RemoteCustomFieldModel;
import tr.com.almbase.plugin.model.RemoteIssueModel;
import tr.com.almbase.plugin.model.RemoteProjectModel;
import tr.com.almbase.plugin.util.Constants;
import tr.com.almbase.plugin.util.Utils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by kivanc.ahat@almbase.com on 16/11/2017.
 */

public class CreateIssueInRemoteJira extends AbstractJiraFunctionProvider
{
    private static final Logger log = LoggerFactory.getLogger(CreateIssueInRemoteJira.class);

    private final IntegrationController integrationController;
    private final ProxyController proxyController;
    private final IssueTypeMappingController issueTypeMappingController;
    private final CustomFieldManager customFieldManager;
    private final I18nResolver i18nResolver;
    private final FieldMappingController fieldMappingController;
    private final RemoteIssueController remoteIssueController;
    private final CategoryComponentController categoryComponentController;
    private final ComponentRelationController componentRelationController;
    private final CategoryController categoryController;
    private final SubCategoryController subCategoryController;
    private final CategoryItemController categoryItemController;

    public CreateIssueInRemoteJira(IntegrationController integrationController,
                                   ProxyController proxyController,
                                   IssueTypeMappingController issueTypeMappingController,
                                   CustomFieldManager customFieldManager,
                                   I18nResolver i18nResolver,
                                   FieldMappingController fieldMappingController,
                                   RemoteIssueController remoteIssueController,
                                   CategoryComponentController categoryComponentController,
                                   ComponentRelationController componentRelationController,
                                   CategoryItemController categoryItemController,
                                   SubCategoryController subCategoryController,
                                   CategoryController categoryController) {
        this.integrationController = integrationController;
        this.proxyController = proxyController;
        this.issueTypeMappingController = issueTypeMappingController;
        this.customFieldManager = customFieldManager;
        this.i18nResolver = i18nResolver;
        this.fieldMappingController = fieldMappingController;
        this.remoteIssueController = remoteIssueController;
        this.categoryComponentController = categoryComponentController;
        this.componentRelationController = componentRelationController;
        this.categoryController = categoryController;
        this.subCategoryController = subCategoryController;
        this.categoryItemController = categoryItemController;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        try {
            MutableIssue issue = getIssue(transientVars);
            IssueTypeMapping issueTypeMapping = getIssueTypeMapping(issue);

            if (null != issueTypeMapping) {

                IntegrationObject integrationObject = getIntegrationObject(issueTypeMapping.getIntegrationId());

                if (null != integrationObject) {

                    JSONObject createIssuePayload = getJSONForIssueCreate(issue, issueTypeMapping);

                    if (null != createIssuePayload) {
                        String remoteIssueKey;
                        try {
                            remoteIssueKey = Utils.createRemoteIssue(createIssuePayload.toString(), integrationObject);
                        } catch (Exception e) {
                            throw new Exception("Remote issue could not be created! Reason : " + e.getMessage());
                        }

                        if (null != remoteIssueKey) {
                            try {
                                JSONObject transitionIssuePayload = getTransitionIssuePayload();

                                if (null != transitionIssuePayload) {
                                    log.debug("Issue Key : " + issue.getKey() + ", Remote Issue Key is : " + remoteIssueKey + ", Transition issue payload is : " +transitionIssuePayload.toString());
                                    Utils.doRemoteIssueTransition(remoteIssueKey, transitionIssuePayload.toString(), integrationObject);
                                } else {
                                    log.error("Issue Key : " + issue.getKey() + ", Remote Issue Key is : " + remoteIssueKey + " can not transition issue. Transition issue payload object is null!!");
                                }
                            } catch (Exception e) {
                                log.error("Issue Key : " + issue.getKey() + " transition error on remote issue : " + remoteIssueKey);
                            } finally {
                                RemoteIssueModel remoteIssueModel = Utils.getRemoteIssue(remoteIssueKey, integrationObject);
                                saveRemoteIssueLink(issue, remoteIssueModel, issueTypeMapping.getIntegrationId());
                            }
                        } else {
                            log.error("Issue Key : " + issue.getKey() + " remoteIssueKey is null!");
                            throw new Exception("Remote issue could not be created! Remote issue key is null!");
                        }
                    } else {
                        log.error("Issue Key : " + issue.getKey() + " can not prepare create issue payload object!");
                        throw new Exception("Remote issue could not be created! Reason : Create Issue Payload object could not be prepared!");
                    }
                } else {
                    log.error("Issue Key : " + issue.getKey() + " Integration object is null!");
                    throw new Exception("Remote issue could not be created! Reason : Integration point is incorrect!");
                }
            } else {
                log.error("Issue Key : " + issue.getKey() + " Issue Type Mapping is null!");
                throw new Exception("Remote issue could not be created! Reason : Issue Type Mapping is incorrect");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new WorkflowException(e.getMessage());
        }
    }

    private JSONObject getJSONForIssueCreate (Issue issue, IssueTypeMapping issueTypeMapping) {
        JSONObject jsonObject = new JSONObject();
        try {
            FieldMapping[] fieldMappings = fieldMappingController.getRecordFromAOTableByIssueTypeMappingId(String.valueOf(issueTypeMapping.getID()));

            IntegrationObject integrationObject = getIntegrationObject(issueTypeMapping.getIntegrationId());

            RemoteProjectModel remoteProjectModel = Utils.getRemoteProject(issueTypeMapping.getRemoteProjectId(), integrationObject);

            JSONObject fields = new JSONObject();

            //Project
            JSONObject project = new JSONObject();
            project.put("id", issueTypeMapping.getRemoteProjectId());
            fields.put("project", project);

            //Issue Type
            JSONObject issueType = new JSONObject();
            issueType.put("id", issueTypeMapping.getRemoteIssueTypeId());
            fields.put("issuetype", issueType);

            for (FieldMapping fieldMapping : fieldMappings) {
                if (fieldMapping.getRemoteFieldId().equalsIgnoreCase("summary")) {
                    if (fieldMapping.getLocalFieldId().equalsIgnoreCase("summary")) {
                        fields.put("summary", issue.getSummary());
                    } else if (fieldMapping.getLocalFieldId().contains("customfield")) {
                        String remoteIssueSummary = (String)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                        fields.put("summary", remoteIssueSummary);
                    }
                } else if (fieldMapping.getRemoteFieldId().equalsIgnoreCase("description")) {
                    if (fieldMapping.getLocalFieldId().equalsIgnoreCase("description")) {
                        fields.put("description", issue.getSummary());
                    } else if (fieldMapping.getLocalFieldId().contains("customfield")) {
                        String remoteIssueDescription = (String)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                        fields.put("description", remoteIssueDescription);
                    }
                }

                if (fieldMapping.getRemoteFieldId().equalsIgnoreCase("components")) {
                    if (fieldMapping.getLocalFieldId().contains("customfield")) {
                        CustomField customField = customFieldManager.getCustomFieldObject(fieldMapping.getLocalFieldId());
                        if (null != customField) {
                            JSONArray componentArray = new JSONArray();
                            JSONObject componentObject = new JSONObject();
                            String customFieldType = customField.getCustomFieldType().getKey();
                            String remoteComponentId = null;
                            if (customFieldType.equalsIgnoreCase("com.valiantys.jira.plugins.SQLFeed:nfeed-standard-customfield-type")) {
                                remoteComponentId = getRemoteComponentId(issue);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textfield")) {
                                remoteComponentId = (String)issue.getCustomFieldValue(customField);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:select")) {
                                remoteComponentId = getRemoteComponentValue(issue, issueTypeMapping, fieldMapping, integrationObject);
                            }

                            if (null != remoteComponentId && !remoteComponentId.equalsIgnoreCase("")) {
                                componentObject.put("id", remoteComponentId);
                                componentArray.put(componentObject);
                                fields.put("components", componentArray);
                            }
                        }
                    }
                }

                if (fieldMapping.getRemoteFieldId().contains("customfield")) {
                    if (fieldMapping.getLocalFieldId().contains("customfield")) {
                        CustomField customField = customFieldManager.getCustomFieldObject(fieldMapping.getLocalFieldId());
                        if (null != customField) {
                            String customFieldType = customField.getCustomFieldType().getKey();
                            log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field Type : " + customFieldType);
                            if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textfield")) {
                                String customFieldValue = (String) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                fields = setJSONObjectForRemoteFieldType(customFieldValue,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textarea")) {
                                String customFieldValue = (String) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                fields = setJSONObjectForRemoteFieldType(customFieldValue,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:datepicker")) {
                                Date customFieldValue = (Date) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue.toString());
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                fields = setJSONObjectForRemoteFieldType(sdf.format(customFieldValue),  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:datetime")) {
                                Date customFieldValue = (Date) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue.toString());
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                fields = setJSONObjectForRemoteFieldType(sdf.format(customFieldValue),  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:select")) {
                                String customFieldValue = (String) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                fields = setJSONObjectForRemoteFieldType(customFieldValue,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multiselect")) {
                                Object options = Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                if (null != options && options instanceof  Options) {
                                    List<String> customFieldValueList = new ArrayList<>();
                                    for (Option option : (Collection<Option>)options) {
                                        customFieldValueList.add(option.getValue());
                                    }
                                    log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValueList.toString());
                                    fields = setJSONObjectForRemoteFieldType(customFieldValueList,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                                }
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect")) {
                                Object cascadingSelectList = Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                if (null != cascadingSelectList && cascadingSelectList instanceof CustomFieldParams) {
                                    Map<String, String> customFieldValueMap = ((CustomFieldParams)cascadingSelectList).getKeysAndValues();

                                    log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValueMap.values().toString());
                                    fields = setJSONObjectForRemoteFieldType(customFieldValueMap,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                                }
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons")) {
                                String customFieldValue = (String) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                fields = setJSONObjectForRemoteFieldType(customFieldValue,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes")) {
                                Object options = Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                if (null != options && options instanceof  Options) {
                                    List<String> customFieldValueList = new ArrayList<>();
                                    for (Option option : (Options)options) {
                                        customFieldValueList.add(option.getValue());
                                    }
                                    log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValueList.toString());
                                    fields = setJSONObjectForRemoteFieldType(customFieldValueList,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                                }
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:userpicker")) {
                                String customFieldValue = (String) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                fields = setJSONObjectForRemoteFieldType(customFieldValue,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker")) {
                                Object customFieldValueUsers = Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                if (null != customFieldValueUsers && customFieldValueUsers instanceof Collection) {
                                    List<String> customFieldValueList = new ArrayList<>();
                                    for (ApplicationUser user : (Collection<ApplicationUser>)customFieldValueUsers) {
                                        customFieldValueList.add(user.getName());
                                    }
                                    log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValueList.toString());
                                    fields = setJSONObjectForRemoteFieldType(customFieldValueList,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                                }
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker")) {
                                String customFieldValue = (String) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                fields = setJSONObjectForRemoteFieldType(customFieldValue,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker")) {
                                Object customFieldValueGroups = Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                if (null != customFieldValueGroups && customFieldValueGroups instanceof Collection) {
                                    List<String> customFieldValueList = new ArrayList<>();
                                    for (Group group : (Collection<Group>)customFieldValueGroups) {
                                        customFieldValueList.add(group.getName());
                                    }
                                    log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValueList.toArray().toString());
                                    fields = setJSONObjectForRemoteFieldType(customFieldValueList,  fields, fieldMapping, issueTypeMapping, remoteProjectModel, integrationObject);
                                }
                            }
                        } else {
                            log.debug("Issue Key : " + issue.getKey() + " customfield is null!" + " Custom Field Id : " + fieldMapping.getLocalFieldId());
                        }
                    } else if (fieldMapping.getLocalFieldId().equalsIgnoreCase("issuekey")) {
                        String value = issue.getKey();
                        fields.put(fieldMapping.getRemoteFieldId(), value);
                    }
                }
            }

            jsonObject.put("fields", fields);
            log.debug("Issue Key : " + issue.getKey() + " Payload : " + jsonObject.toString());
        } catch (Exception e) {
            Utils.printError(e);
            jsonObject = null;
        }
        return jsonObject;
    }

    private JSONObject setJSONObjectForRemoteFieldType (Object customFieldValue, JSONObject fields, FieldMapping fieldMapping, IssueTypeMapping issueTypeMapping, RemoteProjectModel remoteProjectModel, IntegrationObject integrationObject) {
        try {
            RemoteCustomFieldModel remoteCustomFieldModel = Utils.getRemoteCustomFieldModel(remoteProjectModel.getProjectKey(), issueTypeMapping.getRemoteIssueTypeId(), fieldMapping.getRemoteFieldId(), integrationObject);
            log.debug("Remote custom field type : " + remoteCustomFieldModel.getType());

            if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textfield")) {
                //SAMPLE : "customfield_10015": "Is anything better than text?"
                if (customFieldValue instanceof String) {
                    fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textarea")) {
                //SAMPLE :  "customfield_10004": "Free text goes here.  Type away!"
                if (customFieldValue instanceof String) {
                    fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:datepicker")) {
                //SAMPLE :  "customfield_10002": "2011-10-03"
                if (customFieldValue instanceof String) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.parse((String)customFieldValue);
                        fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                    } catch (Exception e) {
                        log.error("Date picker format exception");
                    }
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:datetime")) {
                //SAMPLE :  "customfield_10003": "2011-10-19T10:29:29.908+1100"
                if (customFieldValue instanceof String) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        sdf.parse((String)customFieldValue);
                        fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                    } catch (Exception e) {
                        log.error("Date time picker format exception");
                    }
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:select")) {
                //SAMPLE : "customfield_10013": { "value": "red" }
                //String remoteCustomFieldValue = getRemoteSelectCustomFieldValue(issue, issueTypeMapping, fieldMapping, integrationObject);
                if (customFieldValue instanceof String) {
                    JSONObject customFieldObject = new JSONObject();
                    customFieldObject.put("value", customFieldValue);
                    fields.put(fieldMapping.getRemoteFieldId(), customFieldObject);
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multiselect")) {
                //SAMPLE : "customfield_10008": [ {"value": "red" }, {"value": "blue" }, {"value": "green" }]
                if (customFieldValue instanceof Collection) {
                    JSONArray jsonArray = new JSONArray();
                    for (String value : (List<String>) customFieldValue) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("value", value);
                        jsonArray.put(jsonArray.length(), jsonObject);
                    }
                    if (jsonArray.length() > 0) {
                        fields.put(fieldMapping.getRemoteFieldId(), jsonArray);
                    }
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect")) {
                //SAMPLE :  "customfield_10001": {"value": "green", "child": {"value":"blue"} }
                if (customFieldValue instanceof Map) {
                    Map<String, String> cascadingSelectList = (Map<String, String>) customFieldValue;
                    JSONObject parentObject = new JSONObject();
                    JSONObject childObject = new JSONObject();
                    childObject.put("value", cascadingSelectList.get("child"));
                    parentObject.put("value", cascadingSelectList.get("parent"));
                    parentObject.put("child", childObject);
                    fields.put(fieldMapping.getRemoteFieldId(), parentObject);
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons")) {
                //SAMPLE :  "customfield_10012": { "value": "red" }
                if (customFieldValue instanceof String) {
                    JSONObject customFieldObject = new JSONObject();
                    customFieldObject.put("value", customFieldValue);
                    fields.put(fieldMapping.getRemoteFieldId(), customFieldObject);
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons")) {
                //SAMPLE :  "customfield_10012": { "value": "red" }
                if (customFieldValue instanceof String) {
                    JSONObject customFieldObject = new JSONObject();
                    customFieldObject.put("value", customFieldValue);
                    fields.put(fieldMapping.getRemoteFieldId(), customFieldObject);
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes")) {
                //SAMPLE :  "customfield_10000": [ {"value": "Impediment" }]
                if (customFieldValue instanceof Collection) {
                    JSONArray jsonArray = new JSONArray();
                    for (String value : (List<String>) customFieldValue) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("value", value);
                        jsonArray.put(jsonArray.length(), jsonObject);
                    }
                    if (jsonArray.length() > 0) {
                        fields.put(fieldMapping.getRemoteFieldId(), jsonArray);
                    }
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker")) {
                //SAMPLE :  "customfield_10009": [ {"name": "charlie" }, {"name": "bjones" }, {"name": "tdurden" }]
                if (customFieldValue instanceof Collection) {
                    JSONArray jsonArray = new JSONArray();
                    for (String value : (List<String>) customFieldValue) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", value);
                        jsonArray.put(jsonArray.length(), jsonObject);
                    }
                    if (jsonArray.length() > 0) {
                        fields.put(fieldMapping.getRemoteFieldId(), jsonArray);
                    }
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker")) {
                //SAMPLE :  "customfield_10005": { "name": "jira-developers" }
                if (customFieldValue instanceof String) {
                    JSONObject customFieldObject = new JSONObject();
                    customFieldObject.put("name", customFieldValue);
                    fields.put(fieldMapping.getRemoteFieldId(), customFieldObject);
                }
            } else if (remoteCustomFieldModel.getType().equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker")) {
                //SAMPLE :   "customfield_10007": [{ "name": "admins" }, { "name": "jira-developers" }, { "name": "jira-users" }]
                if (customFieldValue instanceof Collection) {
                    JSONArray jsonArray = new JSONArray();
                    for (String value : (List<String>) customFieldValue) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", value);
                        jsonArray.put(jsonArray.length(), jsonObject);
                    }
                    if (jsonArray.length() > 0) {
                        fields.put(fieldMapping.getRemoteFieldId(), jsonArray);
                    }
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return fields;
    }

    private String getRemoteComponentId (Issue issue) {
        String remoteComponentId = "";
        try {
            String categoryVal = (String) Utils.getCustomFieldValue(issue, Constants.CATEGORY_CF_ID);
            log.debug("Issue Key : " + issue.getKey() + " categoryVal : " + categoryVal);
            categoryVal = categoryVal == null ? "" : categoryVal;
            categoryVal = categoryVal.substring(categoryVal.indexOf("<value>") + 7, categoryVal.indexOf("</value>"));
            log.debug("Issue Key : " + issue.getKey() + " categoryVal : " + categoryVal);
            Category category = categoryController.getRecordFromAOTableByName(categoryVal);
            if (null != category) {
                String subCategoryVal = (String) Utils.getCustomFieldValue(issue, Constants.SUB_CATEGORY_CF_ID);
                log.debug("Issue Key : " + issue.getKey() + " subCategoryVal : " + subCategoryVal);
                subCategoryVal = subCategoryVal == null ? "" : subCategoryVal;
                subCategoryVal = subCategoryVal.substring(subCategoryVal.indexOf("<value>") + 7, subCategoryVal.indexOf("</value>"));
                log.debug("Issue Key : " + issue.getKey() + " subCategoryVal : " + subCategoryVal);
                SubCategory subCategory = subCategoryController.getRecordFromAOTableByName(subCategoryVal, String.valueOf(category.getID()));
                if (null != subCategory) {
                    String categoryItemVal = (String) Utils.getCustomFieldValue(issue, Constants.CATEGORY_ITEM_CF_ID);
                    log.debug("Issue Key : " + issue.getKey() + " categoryItemVal : " + categoryItemVal);
                    categoryItemVal = categoryItemVal == null ? "" : categoryItemVal;
                    categoryItemVal = categoryItemVal.substring(categoryItemVal.indexOf("<value>") + 7, categoryItemVal.indexOf("</value>"));
                    log.debug("Issue Key : " + issue.getKey() + " categoryItemVal : " + categoryItemVal);
                    CategoryItem categoryItem = categoryItemController.getRecordFromAOTableByName(categoryItemVal, String.valueOf(category.getID()), String.valueOf(subCategory.getID()));
                    if (null != categoryItem) {
                        String categoryComponentVal = (String) Utils.getCustomFieldValue(issue, Constants.CATEGORY_COMPONENT_CF_ID);
                        log.debug("Issue Key : " + issue.getKey() + " categoryComponentVal : " + categoryComponentVal);
                        categoryComponentVal = categoryComponentVal == null ? "" : categoryComponentVal;
                        categoryComponentVal = categoryComponentVal.substring(categoryComponentVal.indexOf("<value>") + 7, categoryComponentVal.indexOf("</value>"));
                        log.debug("Issue Key : " + issue.getKey() + " categoryComponentVal : " + categoryComponentVal);
                        CategoryComponent categoryComponent = categoryComponentController.getRecordFromAOTableByName(categoryComponentVal, String.valueOf(category.getID()), String.valueOf(subCategory.getID()), String.valueOf(categoryItem.getID()));

                        if (null != categoryComponent) {
                            ComponentRelation componentRelation = componentRelationController.getRecordFromAOTableByComponentId(String.valueOf(categoryComponent.getID()));
                            if (null != componentRelation) {
                                remoteComponentId = componentRelation.getRilComponentId();
                                log.debug("Issue Key : " + issue.getKey() + " Remote Issue Component Id : " + remoteComponentId);
                            } else {
                                log.error("Issue Key : " + issue.getKey() + " componentRelation ao is null!");
                            }
                        } else {
                            log.error("Issue Key : " + issue.getKey() + " categoryComponent ao is null!");
                        }
                    } else {
                        log.error("Issue Key : " + issue.getKey() + " categoryItem ao is null!");
                    }
                } else {
                    log.error("Issue Key : " + issue.getKey() + " subCategory ao is null!");
                }
            } else {
                log.error("Issue Key : " + issue.getKey() + " category ao is null!");
            }
        } catch (Exception e) {
            Utils.printError(e);
        }
        return remoteComponentId;
    }

    private void saveRemoteIssueLink (Issue issue, RemoteIssueModel remoteIssueModel, String integrationId) {
        try {
            if (null != remoteIssueModel) {
                RemoteIssueObject remoteIssueObject = new RemoteIssueObject();

                remoteIssueObject.setIntegrationId(integrationId);
                remoteIssueObject.setIssueKey(issue.getKey());
                remoteIssueObject.setRiKey(remoteIssueModel.getIssueKey());
                remoteIssueObject.setRiSummary(remoteIssueModel.getSummary());
                remoteIssueObject.setRiAssginee(remoteIssueModel.getAssignee().get("displayName"));
                remoteIssueObject.setRiStatus(remoteIssueModel.getStatus().getStatusName());
                remoteIssueObject.setRiStatusColor(remoteIssueModel.getStatus().getStatusColor());

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String lastUpdatedDate = sdf.format(format.parse(remoteIssueModel.getUpdatedDate()));
                remoteIssueObject.setLastUpdatedDate(lastUpdatedDate);

                remoteIssueController.createRecordInAOTable(remoteIssueObject);
                log.debug("Issue Key : " + issue.getKey() + " remote issue link saved!");
            } else {
                log.debug("Issue Key : " + issue.getKey() + " remoteIssueObject is null!");
            }
        } catch (Exception e) {
            Utils.printError(e);
        }
    }

    private IssueTypeMapping getIssueTypeMapping(Issue issue) {
        IssueTypeMapping issueTypeMapping = null;
        try {
            String issueTypeMappingName = getIssueTypeMappingName(issue);

            if (null != issueTypeMappingName) {
                IssueTypeMapping[] issueTypeMappings = issueTypeMappingController.getAllEntriesFromAOTable();

                if (null != issueTypeMappings) {
                    for (IssueTypeMapping itm : issueTypeMappings) {
                        if (itm.getName().equalsIgnoreCase(issueTypeMappingName)) {
                            issueTypeMapping = itm;
                            break;
                        }
                    }
                } else {
                    log.debug("Issue Key : " + issue.getKey() + " issueTypeMappings is null!");
                }
            } else {
                log.debug("Issue Key : " + issue.getKey() + " issueTypeMappingName is null!");
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return issueTypeMapping;
    }

    private String getIssueTypeMappingName (Issue issue) {
        String issueTypeMappingName = null;
        try {
            issueTypeMappingName = (String)Utils.getCustomFieldValue(issue, getIssueTypeMappingSelectListCFID());
        } catch (Exception e) {
            Utils.printError(e);
        }

        return issueTypeMappingName;
    }

    private String getIssueTypeMappingSelectListCFID () {
        String customFieldId = null;
        try {
            List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
            for (CustomField customField : customFields) {
                if (customField.getCustomFieldType().getKey().equalsIgnoreCase("tr.com.almbase.plugin.cardif-jsd-engine:itmsl")) {
                    customFieldId = customField.getId();
                }
            }
        } catch (Exception e) {
            Utils.printError(e);
        }
        return customFieldId;
    }


    private IntegrationObject getIntegrationObject(String integrationId) {
        IntegrationObject integrationObject = null;
        try {
            Integration integration = integrationController.getRecordFromAOTableById(integrationId);
            integrationObject = new IntegrationObject(integration);
            integrationObject.setProxy(proxyController.getProxyRecordFromAOTable());
        } catch (Exception e) {
            Utils.printError(e);
        }

        return integrationObject;
    }

    private String getRemoteSelectCustomFieldValue (Issue issue, IssueTypeMapping issueTypeMapping, FieldMapping fieldMapping, IntegrationObject integrationObject) {
        String remoteOptionId = null;
        try {
            String localValue = (String)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
            if (null != localValue) {
                RemoteProjectModel remoteProjectModel = Utils.getRemoteProject(issueTypeMapping.getRemoteProjectId(), integrationObject);
                RemoteCustomFieldModel remoteCustomFieldModel = Utils.getRemoteCustomFieldModel(remoteProjectModel.getProjectKey(), issueTypeMapping.getRemoteIssueTypeId(), fieldMapping.getRemoteFieldId(), integrationObject);

                for (int i = 0; i < remoteCustomFieldModel.getAllowedValues().size(); i++) {
                    Map<String, String> allowedValuesMap = remoteCustomFieldModel.getAllowedValues().get(i);
                    if (localValue.equalsIgnoreCase(allowedValuesMap.get("value"))) {
                        remoteOptionId = allowedValuesMap.get("id");
                        break;
                    }
                }
                log.debug("Issue Key : " + issue.getKey() + " remoteOptionId : " + remoteOptionId) ;
            } else {
                log.debug("Issue Key : " + issue.getKey() + " localValue is null!") ;
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return remoteOptionId;
    }

    private String getRemoteComponentValue (Issue issue, IssueTypeMapping issueTypeMapping, FieldMapping fieldMapping, IntegrationObject integrationObject) {
        String remoteComponentId = null;
        try {
            String localValue = (String)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
            if (null != localValue) {
                RemoteProjectModel remoteProjectModel = Utils.getRemoteProject(issueTypeMapping.getRemoteProjectId(), integrationObject);
                List<RemoteComponentModel> remoteComponentModels = Utils.getRemoteProjectComponents(remoteProjectModel.getProjectKey(),integrationObject);

                for (RemoteComponentModel remoteComponentModel : remoteComponentModels) {
                    if (remoteComponentModel.getComponentName().equalsIgnoreCase(localValue)) {
                        remoteComponentId = remoteComponentModel.getComponentId();
                        break;
                    }
                }
                log.debug("Issue Key : " + issue.getKey() + " remoteComponentId : " + remoteComponentId) ;
            } else {
                log.debug("Issue Key : " + issue.getKey() + " localValue is null!") ;
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return remoteComponentId;
    }

    private JSONObject getTransitionIssuePayload () {
        //SAMPLE : {"update": {"comment": [{"add": {"body": "Cardif entegrasyonu tarafından otomatik olarak Açık statüsüne çekilmiştir."}}]},"transition": {"id": "41"}}
        JSONObject transtionIssuePayload = new JSONObject();
        try {
            JSONObject commentObject = new JSONObject();
            JSONArray commentArray = new JSONArray();
            JSONObject commentAddObject = new JSONObject();
            JSONObject commentBodyObject = new JSONObject();
            commentBodyObject.put("body", i18nResolver.getText(new Locale("tr", "TR"),"tr.com.almbase.auto.transtion.message"));
            commentAddObject.put("add", commentBodyObject);
            commentArray.put(commentAddObject);
            commentObject.put("comment", commentArray);
            transtionIssuePayload.put("update", commentObject);

            JSONObject transitionObject = new JSONObject();
            transitionObject.put("id", "41");
            transtionIssuePayload.put("transition", transitionObject);

        } catch (Exception e) {
            Utils.printError(e);
            transtionIssuePayload = null;
        }

        return transtionIssuePayload;

    }
}