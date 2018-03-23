package tr.com.almbase.plugin.customfield;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.GenericTextCFType;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.SearchContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.IssueTypeMapping;
import tr.com.almbase.plugin.activeobject.IssueTypeMappingController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 23/03/2018.
 */
public class IssueTypeMapSelectList extends SelectCFType {

    private static final Logger log = LoggerFactory.getLogger(IssueTypeMapSelectList.class);

    private final OptionsManager optionsManager;
    private final IssueTypeMappingController issueTypeMappingController;

    public IssueTypeMapSelectList(CustomFieldValuePersister customFieldValuePersister,
                                  OptionsManager optionsManager,
                                  GenericConfigManager genericConfigManager,
                                  JiraBaseUrls jiraBaseUrls,
                                  IssueTypeMappingController issueTypeMappingController)
    {
        super(customFieldValuePersister, optionsManager, genericConfigManager, jiraBaseUrls);
        this.optionsManager = optionsManager;
        this.issueTypeMappingController = issueTypeMappingController;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {

        final Map<String, Object> parameters = super.getVelocityParameters(issue, field, fieldLayoutItem);

        FieldConfig fieldConfiguration = null;
        if(issue == null) {
            fieldConfiguration = field.getReleventConfig(new SearchContextImpl());
        } else {
            fieldConfiguration = field.getRelevantConfig(issue);
        }

        Options options = this.optionsManager.getOptions(fieldConfiguration);
        if (!options.isEmpty()) {
            optionsManager.removeCustomFieldConfigOptions(fieldConfiguration);
        }

        IssueTypeMapping [] issueTypeMappings = issueTypeMappingController.getAllEntriesFromAOTable();

        for (IssueTypeMapping issueTypeMapping : issueTypeMappings) {
            if (issue.getIssueTypeId().equalsIgnoreCase(issueTypeMapping.getLocalIssueTypeId())) {
                this.optionsManager.createOption(fieldConfiguration, null, Long.getLong(String.valueOf(issueTypeMapping.getID())), issueTypeMapping.getName());
            }
        }

        options = this.optionsManager.getOptions(fieldConfiguration);
        Map<Long, String> results = new HashMap<>();

        Long selectedId= (long) -1;
        boolean selected = false;
        Object value = field.getValue(issue);
        if (value!=null) {
            selected=true;
        }
        for (Option option : options) {
            results.put(option.getOptionId(), option.getValue());
            if (selected && value.toString().equals(option.getValue())) {
                selectedId = option.getOptionId();
            }
        }

        parameters.put("results", results);
        parameters.put("selectedId", selectedId);
        return parameters;
    }
}
