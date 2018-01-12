package tr.com.almbase.plugin.workflow.postfunction;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.JiraMailQueueUtils;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.GroupUserController;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 11/01/2018.
 */
public class SendMailToGroupMembers extends AbstractJiraFunctionProvider {

    private static final Logger log = LoggerFactory.getLogger(AssignToIssueTypeGroup.class);
    public static final String GROUP_NAME = "groupName";

    public SendMailToGroupMembers(GroupUserController groupUserController) {
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);
        String groupName = (String) args.get(GROUP_NAME);

        GroupManager groupManager = ComponentAccessor.getGroupManager();
        UserManager userManager = ComponentAccessor.getUserManager();

        Group group = groupManager.getGroup(groupName);
        if (null != group) {
            ApplicationProperties ap = ComponentAccessor.getApplicationProperties();
            String baseUrl = ap.getString(APKeys.JIRA_BASEURL);

            VelocityManager vm = ComponentAccessor.getVelocityManager();
            VelocityParamFactory vp = ComponentAccessor.getVelocityParamFactory();

            Map context = vp.getDefaultVelocityParams();
            context.put("baseurl", baseUrl);
            context.put("currentTimestamp", new Date());
            context.put("issue", issue);

            String renderedText = vm.getEncodedBody("/templates/workflow/postfunctions/", "groupmembers.vm", baseUrl, "UTF-8", context);

            Collection<String> groupUsers = ComponentAccessor.getGroupManager().getUserNamesInGroup(groupName);
            if (null != groupUsers) {
                for (String userName : groupUsers) {
                    ApplicationUser user = userManager.getUserByKey(userName);
                    if (null != user) {
                        Email email = new Email(user.getEmailAddress());
                        email.setMimeType("text/html");
                        email.setEncoding("UTF-8");
                        email.setBody(renderedText);
                        email.setSubject("Kayıt Grubunuza Atanmıştır : " + issue.getKey());

                        try {
                            SingleMailQueueItem item = new SingleMailQueueItem(email);
                            ComponentAccessor.getMailQueue().addItem(item);
                        } catch (Exception e) {
                            log.error("Group Mail Send Error : " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
