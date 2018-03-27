package tr.com.almbase.plugin.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.service.ServiceManager;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.com.almbase.plugin.activeobject.*;
import tr.com.almbase.plugin.model.RemoteIssueModel;
import tr.com.almbase.plugin.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kivanc.ahat@almbase.com on 20/02/2018.
 */
public class RemoteIssueUpdateService extends AbstractService{
    private static final Logger log = LoggerFactory.getLogger(RemoteIssueUpdateService.class);

    private RemoteIssueController remoteIssueController;
    private IntegrationController integrationController;
    private ProxyController proxyController;
    private ServiceManager serviceManager;

    public RemoteIssueUpdateService (RemoteIssueController remoteIssueController,
                                     IntegrationController integrationController,
                                     ProxyController proxyController,
                                     ServiceManager serviceManager) {
        this.remoteIssueController = remoteIssueController;
        this.integrationController = integrationController;
        this.proxyController = proxyController;
        this.serviceManager = serviceManager;
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
            RemoteIssue[] remoteIssues = remoteIssueController.getAllEntriesFromAOTable();
            if (null != remoteIssues) {
                for (RemoteIssue remoteIssue : remoteIssues) {
                    IntegrationObject integrationObject = getIntegrationObject(remoteIssue.getIntegrationId());
                    if (null != integrationObject) {
                        RemoteIssueModel remoteIssueModel = Utils.getRemoteIssue(remoteIssue.getRiKey(), integrationObject);

                        if (null != remoteIssueModel) {
                            RemoteIssueObject remoteIssueObject = new RemoteIssueObject();
                            remoteIssueObject.setIntegrationId(remoteIssue.getIntegrationId());
                            remoteIssueObject.setIssueKey(remoteIssue.getIssueKey());
                            remoteIssueObject.setRiKey(remoteIssue.getRiKey());
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
                        log.error("Broken remote issue link : Integration Object is null!" + " Issue Key : " + remoteIssue.getIssueKey());
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
            Integration integration = integrationController.getRecordFromAOTableById(integrationId);
            integrationObject = new IntegrationObject(integration);
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
