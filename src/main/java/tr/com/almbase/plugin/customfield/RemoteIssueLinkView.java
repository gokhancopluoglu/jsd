package tr.com.almbase.plugin.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.RemoteIssue;
import tr.com.almbase.plugin.activeobject.RemoteIssueController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 23/03/2018.
 */
public class RemoteIssueLinkView extends GenericTextCFType {

    private static final Logger log = LoggerFactory.getLogger(RemoteIssueLinkView.class);

    private final RemoteIssueController remoteIssueController;

    public RemoteIssueLinkView(CustomFieldValuePersister customFieldValuePersister,
                               GenericConfigManager genericConfigManager,
                               TextFieldCharacterLengthValidator textFieldCharacterLengthValidator,
                               JiraAuthenticationContext jiraAuthenticationContext,
                               RemoteIssueController remoteIssueController)
    {
        super(customFieldValuePersister, genericConfigManager, textFieldCharacterLengthValidator, jiraAuthenticationContext);
        this.remoteIssueController = remoteIssueController;
    }

    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }

    @Override
    public void createValue(CustomField field, Issue issue, String value) {
        super.createValue(field, issue, value);
    }

    @Override
    protected Object getDbValueFromObject(String customFieldObject) {
        return super.getDbValueFromObject(customFieldObject);
    }

    @Override
    protected String getObjectFromDbValue(Object databaseValue) throws FieldValidationException {
        return super.getObjectFromDbValue(databaseValue);
    }


    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> parameters = super.getVelocityParameters(issue, field, fieldLayoutItem);

        RemoteIssue[] remoteIssues = remoteIssueController.getRecordFromAOTableByIssueKey(issue.getKey());
        List<Map<String, String>> remoteIssueLinksList = new ArrayList<>();
        for (RemoteIssue remoteIssue : remoteIssues) {
            Map<String, String> remoteIssueLinkMap = new HashMap<>();
            remoteIssueLinkMap.put("remoteIssueKey", remoteIssue.getRiKey());
            remoteIssueLinkMap.put("remoteIssueStatus", remoteIssue.getRiStatus());
            remoteIssueLinkMap.put("remoteIssueStatusColor", remoteIssue.getRiStatusColor());
            remoteIssueLinksList.add(remoteIssueLinkMap);
        }

        parameters.put("remoteIssueLinksList", remoteIssueLinksList);
        return parameters;
    }
}
