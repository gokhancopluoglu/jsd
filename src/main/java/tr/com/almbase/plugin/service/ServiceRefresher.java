package tr.com.almbase.plugin.service;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kivanc.ahat@almbase.com on 26.03.2018.
 */
public class ServiceRefresher implements LifecycleAware, InitializingBean, DisposableBean
{
    private final Logger logger = LoggerFactory.getLogger(ServiceRefresher.class);

    private final EventPublisher eventPublisher;
    private final ServiceManager serviceManager;
    private final CustomFieldManager customFieldManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;
    private final FieldScreenManager fieldScreenManager;

    @GuardedBy("this")
    private final Set<LifecycleEvent> lifecycleEvents = EnumSet.noneOf(LifecycleEvent.class);

    enum LifecycleEvent {
        AFTER_PROPERTIES_SET,
        PLUGIN_ENABLED,
        LIFECYCLE_AWARE_ON_START
    }

    public ServiceRefresher(EventPublisher eventPublisher,
                            ServiceManager serviceManager,
                            CustomFieldManager customFieldManager,
                            ManagedConfigurationItemService managedConfigurationItemService,
                            FieldScreenManager fieldScreenManager) {
        this.eventPublisher = eventPublisher;
        this.serviceManager = serviceManager;
        this.customFieldManager = customFieldManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.fieldScreenManager = fieldScreenManager;
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
        this.logger.debug("onPluginEnabled");
        this.onLifecycleEvent(LifecycleEvent.PLUGIN_ENABLED);
    }

    @Override
    public void afterPropertiesSet() {
        this.logger.debug("afterPropertiesSet");
        this.registerListener();
        this.onLifecycleEvent(LifecycleEvent.AFTER_PROPERTIES_SET);
    }

    @Override
    public void onStart() {
        this.logger.debug("onStart started!");
        this.onLifecycleEvent(LifecycleEvent.LIFECYCLE_AWARE_ON_START);

        try {/*
            CustomField itmslCustomField = null;
            List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
            if (null != customFields) {
                for (CustomField customField : customFields) {
                    if (null != customField) {
                        if (customField.getCustomFieldType().getKey().equalsIgnoreCase("tr.com.almbase.plugin.cardif-jsd-engine:itmsl")) {
                            itmslCustomField = customField;
                        }
                    } else {
                        this.logger.error("destroy : Custom Field is null");
                    }
                }
            }

            if (null == itmslCustomField) {
                List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
                contexts.add(GlobalIssueContext.getInstance());

                List<IssueType> issueTypes = new ArrayList<>();
                issueTypes.add(null);

                CustomField customField = this.customFieldManager.createCustomField("Issue Type Mapping", "",
                        this.customFieldManager.getCustomFieldType("tr.com.almbase.plugin.cardif-jsd-engine:itmsl"),
                        this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:selectsearcher"),
                        contexts,
                        issueTypes);

                if (null != customField) {
                    lockCustomField(managedConfigurationItemService, customField);

                    FieldScreen remoteIssueLinkScreen = fieldScreenManager.getFieldScreen(11321L);
                    if (null != remoteIssueLinkScreen && !remoteIssueLinkScreen.containsField(customField.getId())) {
                        FieldScreenTab firstTab = remoteIssueLinkScreen.getTab(0);
                        firstTab.addFieldScreenLayoutItem(customField.getId(), 0);
                    }
                }
            }
*/

            CustomField rilvCustomField = null;
            List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
            if (null != customFields) {
                for (CustomField customField : customFields) {
                    if (null != customField) {
                        if (customField.getCustomFieldType().getKey().equalsIgnoreCase("tr.com.almbase.plugin.cardif-jsd-engine:rilv")) {
                            rilvCustomField = customField;
                        }
                    } else {
                        this.logger.error("destroy : Custom Field is null");
                    }
                }
            }

            if (null == rilvCustomField) {
                List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
                contexts.add(GlobalIssueContext.getInstance());

                List<IssueType> issueTypes = new ArrayList<>();
                issueTypes.add(null);

                CustomField customField = this.customFieldManager.createCustomField("Remote Issue Links", "",
                        this.customFieldManager.getCustomFieldType("tr.com.almbase.plugin.cardif-jsd-engine:rilv"),
                        this.customFieldManager.getCustomFieldSearcher("com.atlassian.jira.plugin.system.customfieldtypes:textsearcher"),
                        contexts,
                        issueTypes);

                if (null != customField) {
                    lockCustomField(managedConfigurationItemService, customField);

                    FieldScreen remoteIssueLinkScreen = fieldScreenManager.getFieldScreen(11321L);
                    if (null != remoteIssueLinkScreen && !remoteIssueLinkScreen.containsField(customField.getId())) {
                        FieldScreenTab firstTab = remoteIssueLinkScreen.getTab(0);
                        firstTab.addFieldScreenLayoutItem(customField.getId(), 0);
                    }
                }
            }


        } catch (Exception e) {
            this.logger.error("createCustomField error!");
        }
    }

    private void lockCustomField(ManagedConfigurationItemService managedConfigurationItemService, CustomField customField)
    {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(customField);
        managedConfigurationItem = managedConfigurationItem.newBuilder().setManaged(true).setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED).build();
        managedConfigurationItemService.updateManagedConfigurationItem(managedConfigurationItem);
    }

    public void onStop() {
        this.logger.debug("onStop started!");
        /*List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
        if (null != customFields) {
            for (CustomField customField : customFields) {
                if (null != customField) {
                    //this.logger.debug(customField.getFieldName());
                    if (customField.getCustomFieldType().getKey().equalsIgnoreCase("tr.com.almbase.plugin.cardif-jsd-engine:itmsl")) {
                        try {
                            customFieldManager.removeCustomField(customField);
                            this.logger.debug("customFieldManager.removeCustomField : Custom Field Name : " + customField.getFieldName() + " removed!");
                        } catch (Exception e) {
                            this.logger.error("customFieldManager.removeCustomField : Custom Field Name : " + customField.getFieldName());
                        }
                    }
                } else {
                    this.logger.error("destroy : Custom Field is null");
                }
            }
        }*/
        this.logger.debug("onStop ended!");
    }

    @Override
    public void destroy() throws Exception {
        this.logger.debug("destroy");
        this.unregisterListener();
    }

    private void onLifecycleEvent(LifecycleEvent event) {
        this.logger.debug("onLifecycleEvent: " + event);
        if (this.isLifecycleReady(event)) {
            unregisterListener();
            try {
                this.launch();
            } catch (Exception ex) {
                this.logger.error("Unexpected error during launch", ex);
            }
        }
    }

    synchronized private boolean isLifecycleReady(LifecycleEvent event) {
        this.logger.debug("isLifecycleReady");
        return this.lifecycleEvents.add(event) && this.lifecycleEvents.size() == LifecycleEvent.values().length;
    }

    private void launch() throws Exception {
        this.logger.debug("launch");
        this.serviceManager.refreshServiceByName("RemoteIssueUpdateService");
    }

    private void registerListener()
    {
        this.eventPublisher.register(this);
    }

    private void unregisterListener()
    {
        this.eventPublisher.unregister(this);
    }
}
