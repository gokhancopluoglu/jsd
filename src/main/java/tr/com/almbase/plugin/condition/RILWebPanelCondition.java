package tr.com.almbase.plugin.condition;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.message.I18nResolver;
import tr.com.almbase.plugin.activeobject.RemoteIssue;
import tr.com.almbase.plugin.activeobject.RemoteIssueController;
import tr.com.almbase.plugin.util.Utils;

import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 07/03/2018.
 */
public class RILWebPanelCondition extends AbstractIssueWebCondition {
    private final I18nResolver i18nResolver;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ServiceManager serviceManager;
    private final RemoteIssueController remoteIssueController;

    public RILWebPanelCondition(I18nResolver i18nResolver,
                                JiraAuthenticationContext jiraAuthenticationContext,
                                ServiceManager serviceManager,
                                RemoteIssueController remoteIssueController)
    {
        this.i18nResolver = i18nResolver;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.serviceManager = serviceManager;
        this.remoteIssueController = remoteIssueController;
    }

    private Map<String,String> params;

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
        this.params = params;
    }

    @Override
    public boolean shouldDisplay(ApplicationUser arg0, Issue issue, JiraHelper arg2)
    {
        boolean out = false;
        try {
            RemoteIssue[] remoteIssues = remoteIssueController.getRecordFromAOTableByIssueKey(issue.getKey());
            if (null != remoteIssues && remoteIssues.length > 0) {
                out = true;
            }
        } catch (Exception e) {
            Utils.printError(e);
        }

        return out;
    }
}
