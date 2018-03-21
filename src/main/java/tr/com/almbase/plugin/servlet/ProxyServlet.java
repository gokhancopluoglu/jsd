package tr.com.almbase.plugin.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.Proxy;
import tr.com.almbase.plugin.activeobject.ProxyController;
import tr.com.almbase.plugin.activeobject.ProxyObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Created by kivanc.ahat@almbase.com on 07/03/2018.
 */
public class ProxyServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ProxyServlet.class);
    private static final String PROXY_TEMPLATE = "/templates/adminscreens/proxy.vm";
    private static final String PROXY_DETAIL_TEMPLATE = "/templates/adminscreens/proxydetail.vm";

    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LoginUriProvider loginUriProvider;
    private final I18nResolver i18nResolver;
    private final ProxyController proxyController;

    public ProxyServlet(TemplateRenderer templateRenderer,
                        JiraAuthenticationContext jiraAuthenticationContext,
                        LoginUriProvider loginUriProvider,
                        I18nResolver i18nResolver,
                        ProxyController proxyController)
    {
        super();
        this.templateRenderer = templateRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.loginUriProvider = loginUriProvider;
        this.i18nResolver = i18nResolver;
        this.proxyController = proxyController;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, Object> context = Maps.newHashMap();
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();

        if (null == loggedInUser && !administrators.contains(loggedInUser)) {
            redirectToLogin(req, resp);
        } else {

            String initial = req.getParameter("initial") == null ? "" : req.getParameter("initial").trim();

            if (initial.equalsIgnoreCase("yes")) {
                Proxy[] proxies = proxyController.getAllEntriesFromAOTable();
                if (null != proxies && proxies.length > 0) {
                    Proxy proxy = proxies[0];
                    context.put("proxyHost", proxy.getHost());
                    context.put("proxyPort", proxy.getPort());
                    context.put("proxyType", proxy.getType());
                }
                templateRenderer.render(PROXY_DETAIL_TEMPLATE, context, resp.getWriter());
            } else {
                templateRenderer.render(PROXY_TEMPLATE, context, resp.getWriter());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        Collection<ApplicationUser> administrators = ComponentAccessor.getUserUtil().getJiraAdministrators();

        if (null == loggedInUser && !administrators.contains(loggedInUser)) {
            redirectToLogin(req, resp);
        } else {
            ProxyObject proxyObject = prepareIntegrationObject(req);
            proxyController.createRecordInAOTable(proxyObject);
        }
    }

    private List<Map<String, String>> getProxyList () {
        List<Map<String, String>> proxyList = new ArrayList<>();

        Proxy[] proxies = proxyController.getAllEntriesFromAOTable();
        if (null != proxies) {
            for (Proxy proxy : proxies) {
                Map<String, String> proxyMap = new HashMap<>();
                proxyMap.put("proxyHost", proxy.getHost());
                proxyMap.put("proxyPort", proxy.getPort());
                proxyMap.put("proxyType", proxy.getType());

                proxyList.add(proxyMap);
            }
        }

        return proxyList;
    }

    private ProxyObject prepareIntegrationObject (HttpServletRequest req) {
        ProxyObject proxyObject = null;

        if (null != req) {
            String proxyHost = req.getParameter("proxyHost");
            String proxyPort = req.getParameter("proxyPort");
            String proxyType = req.getParameter("proxyType");

            proxyObject = new ProxyObject();

            proxyObject.setHost(proxyHost);
            proxyObject.setPort(proxyPort);
            proxyObject.setType(proxyType);
        }

        return proxyObject;
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(loginUriProvider.getLoginUri(URI.create(req.getRequestURL().toString())).toASCIIString());
    }
}
