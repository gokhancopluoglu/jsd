package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 16/11/2017.
 */

public class CreateIssueOnRemoteSystem extends AbstractJiraFunctionProvider
{
    private static final Logger log = LoggerFactory.getLogger(CreateIssueOnRemoteSystem.class);

    private final IntegrationController integrationController;
    private final ProxyController proxyController;
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
                                     ProxyController proxyController,
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
        this.proxyController = proxyController;
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
            IssueTypeMapping issueTypeMapping = getIssueTypeMapping(issue);

            if (null != issueTypeMapping) {

                IntegrationObject integrationObject = getIntegrationObject(issueTypeMapping.getIntegrationId());

                if (null != integrationObject) {

                    String payload = getJSONForIssueCreate(issue, issueTypeMapping);

                    String remoteIssueKey = Utils.createRemoteIssue(payload, integrationObject);

                    RemoteIssueModel remoteIssueModel = Utils.getRemoteIssue(remoteIssueKey, integrationObject);

                    saveRemoteIssueLink(issue, remoteIssueModel, issueTypeMapping.getIntegrationId());
                } else {
                    log.debug("Issue Key : " + issue.getKey() + " Integration object is null!");
                }
            } else {
                log.debug("Issue Key : " + issue.getKey() + " Issue Type Mapping is null!");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new WorkflowException(e.getMessage());
        }
    }

    private String getJSONForIssueCreate (Issue issue, IssueTypeMapping issueTypeMapping) {
        JSONObject jsonObject = new JSONObject();
        try {
            FieldMapping[] fieldMappings = fieldMappingController.getRecordFromAOTableByIssueTypeMappingId(String.valueOf(issueTypeMapping.getID()));

            IntegrationObject integrationObject = getIntegrationObject(issueTypeMapping.getIntegrationId());

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

                            if (null != remoteComponentId) {
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
                                fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:textarea")) {
                                String customFieldValue = (String) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                fields.put(fieldMapping.getRemoteFieldId(), customFieldValue);
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:datepicker")) {
                                Date customFieldValue = (Date) Utils.getCustomFieldValue(issue, fieldMapping.getLocalFieldId());
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue.toString());
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                fields.put(fieldMapping.getRemoteFieldId(), sdf.format(customFieldValue));
                            } else if (customFieldType.equalsIgnoreCase("com.atlassian.jira.plugin.system.customfieldtypes:select")) {
                                String customFieldValue = getRemoteSelectCustomFieldValue(issue, issueTypeMapping, fieldMapping, integrationObject);
                                log.debug("Issue Key : " + issue.getKey() + "Custom Field Id : " + fieldMapping.getLocalFieldId() + " Custom Field value : " + customFieldValue);
                                if (null != customFieldValue) {
                                    JSONObject customFieldObject = new JSONObject();
                                    customFieldObject.put("id", customFieldValue);
                                    fields.put(fieldMapping.getRemoteFieldId(), customFieldObject);
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
        }
        return jsonObject.toString();
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
}