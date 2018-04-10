package tr.com.almbase.plugin.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.RemoteIssue;
import tr.com.almbase.plugin.activeobject.RemoteIssueController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 23/03/2018.
 */
public class RemoteIssueLinkView extends GenericTextCFType {

    private static final Logger log = LoggerFactory.getLogger(RemoteIssueLinkView.class);

    private final RemoteIssueController remoteIssueController;

    public RemoteIssueLinkView(CustomFieldValuePersister customFieldValuePersister,
                               GenericConfigManager genericConfigManager,
                               RemoteIssueController remoteIssueController)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.remoteIssueController = remoteIssueController;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> parameters = super.getVelocityParameters(issue, field, fieldLayoutItem);

        RemoteIssue[] remoteIssues = remoteIssueController.getRecordFromAOTableByIssueKey(issue.getKey());

        Map<String, String> remoteIssueLinks = new HashMap<>();
        for (RemoteIssue remoteIssue : remoteIssues) {
            remoteIssueLinks.put("remoteIssueKey", remoteIssue.getRiKey());
            remoteIssueLinks.put("remoteIssueStatus", remoteIssue.getRiStatus());
            remoteIssueLinks.put("remoteIssueStatusColor", remoteIssue.getRiStatusColor());
        }

        parameters.put("remoteIssueLinks", remoteIssueLinks);
        return parameters;
    }
}
