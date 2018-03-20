package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class ComponentRelationControllerImpl implements ComponentRelationController{

    private static final Logger log = LoggerFactory.getLogger(ComponentRelationControllerImpl.class);
    private ActiveObjects activeObjects;

    public ComponentRelationControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public ComponentRelation getRecordFromAOTableById(String componentRelationId) {
        ComponentRelation componentRelation = null;
        try {
            ComponentRelation[] tempComponentRelation = activeObjects.find(ComponentRelation.class, "ID = ?", Integer.parseInt(componentRelationId));
            if (null != tempComponentRelation && tempComponentRelation.length > 0) {
                componentRelation = tempComponentRelation[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return componentRelation;
    }

    public ComponentRelation getRecordFromAOTableByComponentId(String componentId) {
        ComponentRelation componentRelation = null;
        try {
            ComponentRelation[] tempComponentRelation = activeObjects.find(ComponentRelation.class, "COMPONENT_ID = ?", componentId);
            if (null != tempComponentRelation && tempComponentRelation.length > 0) {
                componentRelation = tempComponentRelation[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return componentRelation;
    }

    public ComponentRelation[] getAllEntriesFromAOTable() {
        ComponentRelation[] componentRelations = null;
        try {
            componentRelations = activeObjects.find(ComponentRelation.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return componentRelations;
    }

    public ComponentRelation createRecordInAOTable(ComponentRelationObject componentRelationObject) {
        ComponentRelation returnComponentRelation = null;
        try {
            ComponentRelation found = getRecordFromAOTableByComponentId(componentRelationObject.getComponentId());

            if (null != found) {
                deleteRecordFromAOTable(found);
            }

            log.debug("New setting!");
            ComponentRelation componentRelationRecord = activeObjects.create(ComponentRelation.class);
            if (componentRelationRecord != null) {
                returnComponentRelation = setAOValuesAndReturnAsObject(componentRelationObject, componentRelationRecord);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnComponentRelation;
    }

    public void deleteRecordFromAOTable(ComponentRelation componentRelation) {
        try {
            activeObjects.delete(componentRelation);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private ComponentRelation setAOValuesAndReturnAsObject(ComponentRelationObject componentRelationObject, ComponentRelation componentRelationRecord) {
        try {
            componentRelationRecord.setComponentId(componentRelationObject.getComponentId());
            componentRelationRecord.setRilComponentId(componentRelationObject.getRilComponentId());
            componentRelationRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            componentRelationRecord = null;
        }
        return componentRelationRecord;
    }
}
