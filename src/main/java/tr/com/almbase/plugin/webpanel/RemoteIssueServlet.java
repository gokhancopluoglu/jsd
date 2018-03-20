package tr.com.almbase.plugin.webpanel;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.RemoteIssue;
import tr.com.almbase.plugin.activeobject.RemoteIssueController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 07/03/2018.
 */
public class RemoteIssueServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(RemoteIssueServlet.class);
    private static final String REMOTE_ISSUE_TEMPLATE = "/templates/webpanels/remote-issue-link.vm";
    private static final String REMOTE_ISSUE_DETAIL_TEMPLATE = "/templates/webpanels/remote-issue-link-detail.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private final I18nResolver i18nResolver;
    private final RemoteIssueController remoteIssueController;

    public RemoteIssueServlet(TemplateRenderer templateRenderer,
                              JiraAuthenticationContext jiraAuthenticationContext,
                              LoginUriProvider loginUriProvider,
                              I18nResolver i18nResolver,
                              RemoteIssueController remoteIssueController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.i18nResolver = i18nResolver;
        this.remoteIssueController = remoteIssueController;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, Object> context = Maps.newHashMap();

        if (null == jiraAuthenticationContext.getLoggedInUser())
        {
            redirectToLogin(req, resp);
        } else {
            String issueKey = req.getParameter("issueKey");

            List<Map<String, String>> remoteIssueLinkMapList = new ArrayList<>();

            RemoteIssue[] remoteIssues = remoteIssueController.getRecordFromAOTableByIssueKey(issueKey);
            if (null != remoteIssues) {
                for (RemoteIssue remoteIssue : remoteIssues) {
                    Map<String, String> remoteIssueLinkMap = new HashMap<>();
                    remoteIssueLinkMap.put("issueKey", remoteIssue.getIssueKey());
                    remoteIssueLinkMap.put("remoteIssueKey", remoteIssue.getRiKey());
                    remoteIssueLinkMap.put("remoteIssueSummary", remoteIssue.getRiSummary());
                    remoteIssueLinkMap.put("remoteIssueStatus", remoteIssue.getRiStatus());
                    remoteIssueLinkMap.put("remoteIssueStatusAssignee", remoteIssue.getRiAssginee());
                    remoteIssueLinkMap.put("remoteIssueStatusColor", remoteIssue.getRiStatusColor());
                    remoteIssueLinkMap.put("lastUpdatedDate", remoteIssue.getLastUpdatedDate());
                    remoteIssueLinkMapList.add(remoteIssueLinkMap);
                }
            }

            context.put("remoteIssueLinkMapList", remoteIssueLinkMapList);
            templateRenderer.render(REMOTE_ISSUE_DETAIL_TEMPLATE, context, resp.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (null == jiraAuthenticationContext.getLoggedInUser())
        {
            redirectToLogin(req, resp);
        } else {
            //TODO
        }
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}
