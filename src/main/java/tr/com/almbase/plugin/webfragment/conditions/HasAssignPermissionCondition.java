package tr.com.almbase.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.RestrictedProject;
import tr.com.almbase.plugin.activeobject.RestrictedProjectController;

import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 10/01/2018.
 */
public class HasAssignPermissionCondition implements Condition {
    private static final Logger log = LoggerFactory.getLogger(HasAssignPermissionCondition.class);

    private RestrictedProjectController restrictedProjectController;

    private HasAssignPermissionCondition(RestrictedProjectController restrictedProjectController) {
        this.restrictedProjectController = restrictedProjectController;
    }

    @Override
    public void init(Map<String, String> map) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> map) {

        try {
            String issueKey = "";
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.equals("issue")) {
                    issueKey = value.toString();
                }
            }
            Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey);

            if (issue != null) {
                String projectKey = issue.getProjectObject().getKey();
                GroupManager groupManager = ComponentAccessor.getGroupManager();
                ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

                RestrictedProject[] restrictedProjects = restrictedProjectController.getRecordFromAOTableByProjectKey(projectKey);

                for (RestrictedProject restrictedProject : restrictedProjects) {
                    String restrictedGroupName = restrictedProject.getGroupName();
                    Group adminGroup = groupManager.getGroup(restrictedGroupName);
                    if (groupManager.isUserInGroup(loggedInUser, adminGroup)) {
                        return true;
                    }
                }
            }
        } catch (Exception excp) {
            log.error(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(excp));
        };
        return false;
    }
}