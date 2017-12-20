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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class IssueTypeGroupDefServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(IssueTypeGroupDefServlet.class);
    private static final String ISSUE_TYPE_GROUP_DEF_TEMPLATE = "/templates/adminscreens/issuetypegroup.vm";
    private static final String ISSUE_TYPE_GROUP_DETAIL_DEF_TEMPLATE = "/templates/adminscreens/issuetypegroupdetail.vm";
    private static final String NOT_AUTH_TEMPLATE = "/templates/adminscreens/not_auth.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private IssueTypeGroupController issueTypeGroupController;

    public IssueTypeGroupDefServlet(TemplateRenderer templateRenderer,
                                    JiraAuthenticationContext jiraAuthenticationContext,
                                    LoginUriProvider loginUriProvider,
                                    IssueTypeGroupController issueTypeGroupController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.issueTypeGroupController = issueTypeGroupController;
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
                String recordExists = "";
                if (issuetypechanged.equalsIgnoreCase("yes")) {
                    IssueTypeGroup issueTypeGroup = issueTypeGroupController.getRecordFromAOTableByIssueType(selectedIssueType);
                    if (null != issueTypeGroup) {
                        groupName = issueTypeGroup.getGroupName();
                        if (null != groupName && !groupName.equalsIgnoreCase("") ) {
                            Group selectedGroup = ComponentAccessor.getGroupManager().getGroup(groupName);
                            if (null != selectedGroup) {
                                recordExists = "yes";
                                groupName = issueTypeGroup.getGroupName();
                            }
                        }
                    }

                    context.put("recordExists", recordExists);
                    context.put("groupName", groupName);
                    context.put("issueTypes", getIssueTypes());
                    context.put("issueTypeId", selectedIssueType);
                    context.put("selectedIssueType", selectedIssueType);
                    templateRenderer.render(ISSUE_TYPE_GROUP_DETAIL_DEF_TEMPLATE, context, resp.getWriter());
                } else {
                    context.put("issueTypes", getIssueTypes());
                    templateRenderer.render(ISSUE_TYPE_GROUP_DEF_TEMPLATE, context, resp.getWriter());
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

            String actionType = req.getParameter("actionType");

            if (actionType.equalsIgnoreCase("save")) {
                if (null != selectedIssueType && !selectedIssueType.equalsIgnoreCase("")
                        && null != selectedGroupName && !selectedGroupName.equalsIgnoreCase("")) {
                    IssueTypeGroupObject issueTypeGroupObject = new IssueTypeGroupObject();
                    issueTypeGroupObject.setIssueType(selectedIssueType);
                    issueTypeGroupObject.setGroupName(selectedGroupName);
                    issueTypeGroupController.createRecordInAOTable(issueTypeGroupObject);
                }
            } else if (actionType.equalsIgnoreCase("delete")) {
                IssueTypeGroup issueTypeGroup = issueTypeGroupController.getRecordFromAOTableByIssueType(selectedIssueType);
                if (null != issueTypeGroup) {
                    issueTypeGroupController.deleteRecordFromAOTable(issueTypeGroup);
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