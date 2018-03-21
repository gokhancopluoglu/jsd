package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.model.RemoteCustomFieldModel;
import tr.com.almbase.plugin.model.RemoteIssueModel;
import tr.com.almbase.plugin.model.RemoteProjectModel;
import tr.com.almbase.plugin.util.Constants;
import tr.com.almbase.plugin.util.Response;
import tr.com.almbase.plugin.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 16/11/2017.
 */

public class CreateIssueOnRemoteSystem extends AbstractJiraFunctionProvider
{
    private static final Logger log = LoggerFactory.getLogger(CreateIssueOnRemoteSystem.class);

    private final IntegrationController integrationController;
    private final IssueTypeMappingController issueTypeMappingController;
    private final CustomFieldManager customFieldManager;
    private final FieldMappingController fieldMappingController;
    private final RemoteIssueController remoteIssueController;
    private final CategoryComponentController categoryComponentController;
    private final ComponentRelationController componentRelationController;
    private final CategoryController categoryController;
    private final SubCategoryController subCategoryController;
    private final CategoryItemController categoryItemController;

    public CreateIssueOnRemoteSystem(IntegrationController integrationController,
                                     IssueTypeMappingController issueTypeMappingController,
                                     CustomFieldManager customFieldManager,
                                     FieldMappingController fieldMappingController,
                                     RemoteIssueController remoteIssueController,
                                     CategoryComponentController categoryComponentController,
                                     ComponentRelationController componentRelationController,
                                     CategoryItemController categoryItemController,
                                     SubCategoryController subCategoryController,
                                     CategoryController categoryController) {
        this.integrationController = integrationController;
        this.issueTypeMappingController = issueTypeMappingController;
        this.customFieldManager = customFieldManager;
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
            ApplicationUser applicationUser = getCallerUser(transientVars, args);

            IssueTypeMapping issueTypeMapping = getIssueTypeMapping(issue);

            if (null != issueTypeMapping) {

                IntegrationObject integrationObject = getIntegrationObject(issueTypeMapping.getIntegrationId());

                String payload = getJSONForIssueCreate(issue, applicationUser, issueTypeMapping);

                String remoteIssueKey = Utils.createRemoteIssue(payload, integrationObject);

                RemoteIssueModel remoteIssueModel = Utils.getRemoteIssue(remoteIssueKey, integrationObject);

                saveRemoteIssueLink(issue, remoteIssueModel, issueTypeMapping.getIntegrationId());
            } else {
                log.debug("Issue Key : " + issue.getKey() + " Issue Type Mapping is null!");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new WorkflowException(e.getMessage());
        }
    }

    private String getJSONForIssueCreate (Issue issue, ApplicationUser applicationUser, IssueTypeMapping issueTypeMapping) {
        JSONObject jsonObject = new JSONObject();
        try {
            FieldMapping[] fieldMappings = fieldMappingController.getRecordFromAOTableByIssueTypeMappingId(String.valueOf(issueTypeMapping.getID()));

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
                    JSONArray componentArray = new JSONArray();
                    JSONObject componentObject = new JSONObject();
                    componentObject.put("id", getRemoteComponentId(issue));
                    componentArray.put(componentObject);
                    fields.put("components", componentArray);
                }

                if (fieldMapping.getRemoteFieldId().contains("customfield")) {
                    CustomField customField = customFieldManager.getCustomFieldObject(fieldMapping.getLocalFieldId());
                    if (null != customField) {
                        String customFieldType = customField.getCustomFieldType().getKey();
                        if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textfield")) {
                            String customFieldValue = (String)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                            fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                        } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textarea")) {
                            String customFieldValue = (String)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                            fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                        } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:datepicker")) {
                            Date customFieldValue = (Date)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            fields.put(fieldMapping.getRemoteFieldId(), sdf.format(customFieldValue));
                        } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:select")) {
                            String value = getRemoteSelectCustomFieldValue(issue, issueTypeMapping, fieldMapping);
                            if (null != value) {
                                JSONObject customFieldObject = new JSONObject();
                                customFieldObject.put("id", value);
                                fields.put(fieldMapping.getRemoteFieldId(), customFieldObject);
                            }
                        }
                    }
                }
            }

            jsonObject.put("fields", fields);
            log.debug("Issue Key : " + issue.getKey() + " Payload : " + jsonObject.toString());
        } catch (Exception e) {
            Utils.printError(e);
        }
        return jsonObject.toString();
    }

    private String getRemoteComponentId (Issue issue) {
        String remoteComponentId = "";
        try {
            String categoryVal = (String) Utils.getCustomFieldValue(issue, Constants.CATEGORY_CF_ID);
            categoryVal = categoryVal.substring(categoryVal.indexOf("<value>") + 7, categoryVal.indexOf("</value>"));
            Category category = categoryController.getRecordFromAOTableByName(categoryVal);
            String subCategoryVal = (String) Utils.getCustomFieldValue(issue, Constants.SUB_CATEGORY_CF_ID);
            subCategoryVal = subCategoryVal.substring(subCategoryVal.indexOf("<value>") + 7, subCategoryVal.indexOf("</value>"));
            SubCategory subCategory = subCategoryController.getRecordFromAOTableByName(subCategoryVal, String.valueOf(category.getID()));
            String categoryItemVal = (String) Utils.getCustomFieldValue(issue, Constants.CATEGORY_ITEM_CF_ID);
            categoryItemVal = categoryItemVal.substring(categoryItemVal.indexOf("<value>") + 7, categoryItemVal.indexOf("</value>"));
            CategoryItem categoryItem = categoryItemController.getRecordFromAOTableByName(categoryItemVal, String.valueOf(category.getID()), String.valueOf(subCategory.getID()));
            String categoryComponentVal = (String) Utils.getCustomFieldValue(issue, Constants.CATEGORY_COMPONENT_CF_ID);
            categoryComponentVal = categoryComponentVal.substring(categoryComponentVal.indexOf("<value>") + 7, categoryComponentVal.indexOf("</value>"));
            CategoryComponent categoryComponent = categoryComponentController.getRecordFromAOTableByName(categoryComponentVal, String.valueOf(category.getID()), String.valueOf(subCategory.getID()), String.valueOf(categoryItem.getID()));

            ComponentRelation componentRelation = componentRelationController.getRecordFromAOTableByComponentId(categoryComponent.getCategoryId());

            remoteComponentId = componentRelation.getRilComponentId();
            log.debug("Issue Key : " + issue.getKey() + " Remote Issue Component Id : " + remoteComponentId);
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
            issueTypeMappingName = (String)Utils.getCustomFieldValue(issue, Constants.ISSUE_TYPE_MAPPING_CF_ID);
            if (null != issueTypeMappingName) {
                issueTypeMappingName = issueTypeMappingName.substring(issueTypeMappingName.indexOf("<value>") + 7, issueTypeMappingName.indexOf("</value>"));
                issueTypeMappingName = issueTypeMappingName.trim();
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return issueTypeMappingName;
    }

    private IntegrationObject getIntegrationObject(String integrationId) {
        IntegrationObject integrationObject = null;
        try {
            Integration integration = integrationController.getRecordFromAOTableById(integrationId);
            integrationObject = new IntegrationObject(integration);
        } catch (Exception e) {
            Utils.printError(e);
        }

        return integrationObject;
    }

    private RemoteCustomFieldModel getRemoteCustomFieldModel(IssueTypeMapping issueTypeMapping, String fieldId) {
        RemoteCustomFieldModel remoteCustomFieldModel = null;
        try {
            IntegrationObject integrationObject = getIntegrationObject(issueTypeMapping.getIntegrationId());
            RemoteProjectModel remoteProjectModel = Utils.getRemoteProject(issueTypeMapping.getRemoteProjectId(), integrationObject);
            remoteCustomFieldModel = Utils.getRemoteCustomFieldModel(remoteProjectModel.getProjectKey(), issueTypeMapping.getLocalIssueTypeId(), fieldId, integrationObject);
        } catch (Exception e) {
            Utils.printError(e);
        }

        return remoteCustomFieldModel;
    }

    private String getRemoteSelectCustomFieldValue (Issue issue, IssueTypeMapping issueTypeMapping, FieldMapping fieldMapping) {
        String remoteOptionId = null;
        try {
            String localValue = (String)Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
            if (null != localValue) {
                RemoteCustomFieldModel remoteCustomFieldModel = getRemoteCustomFieldModel(issueTypeMapping, fieldMapping.getRemoteFieldId());

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
}