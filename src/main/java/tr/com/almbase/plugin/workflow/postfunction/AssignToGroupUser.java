package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class AssignToGroupUser extends AbstractJiraFunctionProvider
{
    private static final Logger log = LoggerFactory.getLogger(AssignToGroupUser.class);
    private GroupUserController groupUserController;

    public static final String GROUP_NAME = "groupName";

    public AssignToGroupUser(GroupUserController groupUserController)
    {
        this.groupUserController = groupUserController;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        String groupName = (String) args.get(GROUP_NAME);
        MutableIssue issue = getIssue(transientVars);
        try {
            GroupUser groupUser = groupUserController.getRecordFromAOTableByGroupName(groupName);
            if (null != groupUser) {
                String userKey = groupUser.getUserName();
                if (null != userKey && !userKey.equalsIgnoreCase("")) {
                    ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(userKey);
                    if (null != user) {
                        issue.setAssignee(user);
                    }
                }
            }
        }
        catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }
}