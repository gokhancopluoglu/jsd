package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class FieldMappingControllerImpl implements FieldMappingController {

    private static final Logger log = LoggerFactory.getLogger(FieldMappingControllerImpl.class);
    private ActiveObjects activeObjects;

    public FieldMappingControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }
    
    public FieldMapping getRecordFromAOTableById(String fieldMappingId) {
        FieldMapping fieldMapping = null;
        try {
            FieldMapping[] tempFieldMaps = activeObjects.find(FieldMapping.class, "ID = ?", Integer.parseInt(fieldMappingId));
            if (null != tempFieldMaps && tempFieldMaps.length > 0) {
                fieldMapping = tempFieldMaps[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return fieldMapping;
    }

    public FieldMapping[] getRecordFromAOTableByIntegrationId(String integrationId) {
        FieldMapping[] fieldMappings = null;
        try {
            fieldMappings = activeObjects.find(FieldMapping.class, "INTEGRATION_ID = ?", integrationId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return fieldMappings;
    }

    public FieldMapping[] getRecordFromAOTableByIssueTypeMappingId(String issueTypeMappingId) {
        FieldMapping[] fieldMappings = null;
        try {
            fieldMappings = activeObjects.find(FieldMapping.class, "IT_MAPPING_ID = ?", issueTypeMappingId);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return fieldMappings;
    }

    public FieldMapping[] getAllEntriesFromAOTable() {
        FieldMapping[] fieldMappings = null;
        try {
            fieldMappings = activeObjects.find(FieldMapping.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return fieldMappings;
    }

    public FieldMapping createRecordInAOTable(FieldMappingObject fieldMappingObject) {
        FieldMapping returnFieldMapping = null;
        try {
            log.debug("New setting!");
            FieldMapping fieldMappingRecord = activeObjects.create(FieldMapping.class);
            if (fieldMappingRecord != null)
            {
                returnFieldMapping = setAOValuesAndReturnAsObject(fieldMappingRecord, fieldMappingObject);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnFieldMapping;
    }

    public void deleteRecordFromAOTable(FieldMapping fieldMappingRecord) {
        try {
            activeObjects.delete(fieldMappingRecord);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    public FieldMapping updateRecordFromAOTable(FieldMapping fieldMappingRecord, FieldMappingObject fieldMappingObject) {
        try {
            fieldMappingRecord = setAOValuesAndReturnAsObject(fieldMappingRecord, fieldMappingObject);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }

        return fieldMappingRecord;
    }

    private FieldMapping setAOValuesAndReturnAsObject(FieldMapping fieldMappingRecord, FieldMappingObject fieldMappingObject) {
        try {
            fieldMappingRecord.setIntegrationId(fieldMappingObject.getIntegrationId());
            fieldMappingRecord.setItMappingId(fieldMappingObject.getItMappingId());
            fieldMappingRecord.setLocalFieldId(fieldMappingObject.getLocalFieldId());
            fieldMappingRecord.setRemoteFieldId(fieldMappingObject.getRemoteFieldId());
            fieldMappingRecord.setRemoteFieldId(fieldMappingObject.getRemoteFieldId());
            fieldMappingRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            fieldMappingRecord = null;
        }
        return fieldMappingRecord;
    }
}
