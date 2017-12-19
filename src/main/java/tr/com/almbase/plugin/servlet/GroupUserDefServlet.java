package tr.com.almbase.plugin.servlet;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class GroupUserDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(GroupUserDefServlet.class);
    private static final String GROUP_USER_DEF_TEMPLATE = "/templates/adminscreens/groupuser.vm";
    private static final String GROUP_USER_DETAIL_DEF_TEMPLATE = "/templates/adminscreens/groupuserdetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private GroupUserController groupUserController;

    public GroupUserDefServlet(TemplateRenderer templateRenderer,
                               JiraAuthenticationContext jiraAuthenticationContext,
                               LoginUriProvider loginUriProvider,
                               GroupUserController groupUserController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.groupUserController = groupUserController;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, Object> context = Maps.newHashMap();

        if (null == jiraAuthenticationContext.getLoggedInUser())
        {
            redirectToLogin(req, resp);
        } else {
            ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();
            Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();

            if (administrators.contains(user)) {
                String issuetypechanged = req.getParameter("issuetypechanged") == null ? "" : req.getParameter("issuetypechanged").trim();
                String selectedIssueType = req.getParameter("selectedIssueType") == null ? "" : req.getParameter("selectedIssueType").trim();

                String groupName = "";
                String userName = "";
                String userDisplayName = "";
                String recordExists = "";
                if (issuetypechanged.equalsIgnoreCase("yes")) {
                    GroupUser groupUser = groupUserController.getRecordFromAOTableByIssueType(selectedIssueType);
                    if (null != groupUser) {
                        groupName = groupUser.getGroupName();
                        userName = groupUser.getUserName();
                        if (null != groupName && !groupName.equalsIgnoreCase("") ) {
                            Group selectedGroup = ComponentAccessor.getGroupManager().getGroup(groupName);
                            if (null != selectedGroup) {
                                if (null != userName && !userName.equalsIgnoreCase("")) {
                                    ApplicationUser selectedUser = ComponentAccessor.getUserManager().getUserByKey(userName);
                                    if (null != selectedUser) {
                                        userDisplayName = selectedUser.getDisplayName();
                                        recordExists = "yes";
                                    }
                                }
                            }
                        }
                    }

                    context.put("recordExists", recordExists);
                    context.put("userDisplayName", userDisplayName);
                    context.put("userName", userName);
                    context.put("groupName", groupName);
                    context.put("issueTypes", getIssueTypes());
                    context.put("issueTypeId", selectedIssueType);
                    context.put("selectedIssueType", selectedIssueType);
                    templateRenderer.render(GROUP_USER_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else {
                    context.put("issueTypes", getIssueTypes());
                    templateRenderer.render(GROUP_USER_DEF_TEMPLATE, context, resp.getWriter());
                }
            } else {
                log.debug("User isn't admin!");
                templateRenderer.render(NOT_AUTH_TEMPLATE, context, resp.getWriter());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (null == jiraAuthenticationContext.getLoggedInUser()) {
            redirectToLogin(req, resp);
        } else {
            String selectedIssueType = req.getParameter("selectedIssueType") == null ? "" : req.getParameter("selectedIssueType").trim();
            String selectedGroupName = req.getParameter("selectedGroupName") == null ? "" : req.getParameter("selectedGroupName").trim();
            String selectedUserName = req.getParameter("selectedUserName") == null ? "" : req.getParameter("selectedUserName").trim();

            String actionType = req.getParameter("actionType");

            if (actionType.equalsIgnoreCase("save")) {
                if (null != selectedIssueType && !selectedIssueType.equalsIgnoreCase("")
                        && null != selectedGroupName && !selectedGroupName.equalsIgnoreCase("")
                        && null != selectedUserName && !selectedUserName.equalsIgnoreCase("")) {
                    GroupUserObject groupUserObject = new GroupUserObject();
                    groupUserObject.setIssueType(selectedIssueType);
                    groupUserObject.setGroupName(selectedGroupName);
                    groupUserObject.setUserName(selectedUserName);
                    groupUserController.createRecordInAOTable(groupUserObject);
                }
            } else if (actionType.equalsIgnoreCase("delete")) {
                GroupUser groupUser = groupUserController.getRecordFromAOTableByIssueType(selectedIssueType);
                if (null != groupUser) {
                    groupUserController.deleteRecordFromAOTable(groupUser);
                }
            }
        }
    }

    private Map<String, IssueType> getIssueTypes () {
        Map<String, IssueType> issueTypeMap = new HashMap<>();
        Collection<IssueType> issueTypeList = ComponentAccessor.getConstantsManager().getAllIssueTypeObjects();
        for (IssueType issueType : issueTypeList) {
            issueTypeMap.put(issueType.getId(), issueType);
        }
        return issueTypeMap;
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}