package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class IntegrationControllerImpl implements IntegrationController {

    private static final Logger log = LoggerFactory.getLogger(IntegrationControllerImpl.class);
    private ActiveObjects activeObjects;

    public IntegrationControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public Integration getRecordFromAOTableByName(String integrationName) {
        Integration integration = null;
        try {
            Integration[] tempIntegrations = activeObjects.find(Integration.class, "NAME = ?", integrationName);
            if (null != tempIntegrations && tempIntegrations.length > 0) {
                integration = tempIntegrations[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return integration;
    }

    public Integration getRecordFromAOTableById(String integrationId) {
        Integration integration = null;
        try {
            Integration[] tempIntegrations = activeObjects.find(Integration.class, "ID = ?", Integer.parseInt(integrationId));
            if (null != tempIntegrations && tempIntegrations.length > 0) {
                integration = tempIntegrations[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return integration;
    }

    public Integration[] getAllEntriesFromAOTable() {
        Integration[] integrations = null;
        try {
            integrations = activeObjects.find(Integration.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return integrations;
    }

    public Integration createRecordInAOTable(IntegrationObject integrationObject) {
        Integration returnIntegration = null;
        try {
            log.debug("New setting!");
            Integration integrationRecord = activeObjects.create(Integration.class);
            if (integrationRecord != null)
            {
                returnIntegration = setAOValuesAndReturnAsObject(integrationRecord, integrationObject);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnIntegration;
    }

    public void deleteRecordFromAOTable(Integration integrationRecord) {
        try {
            activeObjects.delete(integrationRecord);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    public Integration updateRecordFromAOTable(Integration integrationRecord, IntegrationObject integrationObject) {
        try {
            integrationRecord = setAOValuesAndReturnAsObject(integrationRecord, integrationObject);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }

        return integrationRecord;
    }

    private Integration setAOValuesAndReturnAsObject(Integration integrationRecord, IntegrationObject integrationObject) {
        try {
            integrationRecord.setName(integrationObject.getName());
            integrationRecord.setUrl(integrationObject.getUrl());
            integrationRecord.setUsername(integrationObject.getUsername());
            integrationRecord.setPassword(integrationObject.getPassword());
            integrationRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            integrationRecord = null;
        }
        return integrationRecord;
    }
}
