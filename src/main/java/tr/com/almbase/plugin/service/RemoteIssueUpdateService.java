package tr.com.almbase.plugin.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.service.ServiceManager;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.model.RemoteIssueModel;
import tr.com.almbase.plugin.util.Utils;

import java.text.SimpleDateFormat;

/**
 * Created by kivanc.ahat@almbase.com on 20/02/2018.
 */
public class RemoteIssueUpdateService extends AbstractService{
    private static final Logger log = LoggerFactory.getLogger(RemoteIssueUpdateService.class);

    private final ServiceManager serviceManager;
    private final IssueManager issueManager;

    public RemoteIssueUpdateService (ServiceManager serviceManager,
                                     IssueManager issueManager) {
        this.serviceManager = serviceManager;
        this.issueManager = issueManager;
    }

    @Override
    public void init(PropertySet props) throws ObjectConfigurationException {
        super.init(props);
    }

    @Override
    public void run() {
        try {
            this.serviceManager.refreshServiceByName(this.getName());
        } catch (Exception ex) {
            log.error("Unable to refresh :(");
        }

        try {

            RemoteIssueController remoteIssueController = ComponentAccessor.getOSGiComponentInstanceOfType(RemoteIssueController.class);
            IssueTypeMappingController issueTypeMappingController = ComponentAccessor.getOSGiComponentInstanceOfType(IssueTypeMappingController.class);

            RemoteIssue[] remoteIssues = remoteIssueController.getAllEntriesFromAOTable();
            if (null != remoteIssues) {
                for (RemoteIssue remoteIssue : remoteIssues) {
                    Issue issue = issueManager.getIssueObject(remoteIssue.getIssueKey());
                    if (null != issue) {
                        IssueTypeMapping issueTypeMapping = issueTypeMappingController.getRecordWithAllParameters(remoteIssue.getIntegrationId(),
                                String.valueOf(issue.getProjectId()), issue.getIssueTypeId(), remoteIssue.getRiProjectId(), remoteIssue.getRiIssueTypeId());

                        if (null != issueTypeMapping) {
                            log.debug("issueTypeMapping is not null!");
                            if (!issue.getStatusId().equalsIgnoreCase(issueTypeMapping.getLocalEndStatusId())) {

                                IntegrationObject integrationObject = getIntegrationObject(remoteIssue.getIntegrationId());
                                if (null != integrationObject) {
                                    RemoteIssueModel remoteIssueModel = Utils.getRemoteIssue(remoteIssue.getRiKey(), integrationObject);

                                    if (null != remoteIssueModel) {
                                        RemoteIssueObject remoteIssueObject = new RemoteIssueObject();
                                        remoteIssueObject.setIntegrationId(remoteIssue.getIntegrationId());
                                        remoteIssueObject.setIssueKey(remoteIssue.getIssueKey());
                                        remoteIssueObject.setRiKey(remoteIssue.getRiKey());
                                        remoteIssueObject.setRiProjectId(remoteIssue.getRiProjectId());
                                        remoteIssueObject.setRiIssueTypeId(remoteIssue.getRiIssueTypeId());
                                        remoteIssueObject.setRiSummary(remoteIssueModel.getSummary());
                                        remoteIssueObject.setRiAssginee(remoteIssueModel.getAssignee().get("displayName"));
                                        remoteIssueObject.setRiStatus(remoteIssueModel.getStatus().getStatusName());
                                        remoteIssueObject.setRiStatusColor(remoteIssueModel.getStatus().getStatusColor());

                                        try {
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                            String lastUpdatedDate = sdf.format(format.parse(remoteIssueModel.getUpdatedDate()));
                                            remoteIssueObject.setLastUpdatedDate(lastUpdatedDate);
                                        } catch (Exception e) {
                                            Utils.printError(e);
                                            remoteIssueObject.setLastUpdatedDate("");
                                        }

                                        remoteIssueController.updateRecordFromAOTable(remoteIssue, remoteIssueObject);
                                    } else {
                                        log.error("Broken remote issue link : Issue Key : " + remoteIssue.getIssueKey() + " Remote Issue Key : " + remoteIssue.getRiKey());
                                    }
                                } else {
                                    log.error("Broken remote issue link : Integration Object is null!" + " Issue Key : " + issue.getKey() + " Remote Issue Key : " + remoteIssue.getRiKey());
                                }
                            } else {
                                log.debug("Issue is at Local Issue End Status : " + " Issue Key : " + issue.getKey() + " Issue End Status : " + issue.getStatus().getName() + " Remote Issue Key : " + remoteIssue.getRiKey());
                            }
                        } else {
                            log.debug("Issue Type Mapping is null!" + " Issue Key : " + issue.getKey() + " Remote Issue Key : " + remoteIssue.getRiKey());
                        }
                    } else {
                        log.debug("Issue is null!" + " Issue Key : " + remoteIssue.getIssueKey() + " Remote Issue Key : " + remoteIssue.getRiKey());
                    }
                }
            } else {
                log.debug("There is not any records on Remote Issue Table!");
            }
        }
        catch (Exception e) {
            Utils.printError(e);
        }
    }

    private IntegrationObject getIntegrationObject(String integrationId) {
        IntegrationObject integrationObject = null;
        try {
            IntegrationController integrationController = ComponentAccessor.getOSGiComponentInstanceOfType(IntegrationController.class);
            Integration integration = integrationController.getRecordFromAOTableById(integrationId);
            integrationObject = new IntegrationObject(integration);

            ProxyController proxyController = ComponentAccessor.getOSGiComponentInstanceOfType(ProxyController.class);
            integrationObject.setProxy(proxyController.getProxyRecordFromAOTable());
        } catch (Exception e) {
            Utils.printError(e);
        }

        return integrationObject;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException {
        return getObjectConfiguration("firstpriorityservice", "services/remoteissueupdateservice.xml", null);
    }
}
