package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class RestrictedProjectControllerImpl implements RestrictedProjectController{

    private static final Logger log = LoggerFactory.getLogger(RestrictedProjectControllerImpl.class);
    private ActiveObjects activeObjects;

    public RestrictedProjectControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public RestrictedProject getRecordFromAOTableByGroupName(String groupName) {
        RestrictedProject restrictedProject = null;
        try {
            RestrictedProject[] tempRestrictedProject = activeObjects.find(RestrictedProject.class, "GROUP_NAME = ?", groupName);
            if (null != tempRestrictedProject && tempRestrictedProject.length > 0)
                restrictedProject = tempRestrictedProject[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return restrictedProject;
    }

    public RestrictedProject [] getRecordFromAOTableByProjectKey(String projectKey) {
        RestrictedProject [] restrictedProjects = null;
        try {
            restrictedProjects = activeObjects.find(RestrictedProject.class, "PROJECT_KEY = ?", projectKey);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return restrictedProjects;
    }

    public RestrictedProject getRecordFromAOTableById(String restrictedProjectId) {
        RestrictedProject restrictedProject = null;
        try {
            RestrictedProject[] tempRestrictedProject = activeObjects.find(RestrictedProject.class, "ID = ?", Integer.parseInt(restrictedProjectId));
            if (null != tempRestrictedProject && tempRestrictedProject.length > 0) {
                restrictedProject = tempRestrictedProject[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return restrictedProject;
    }

    public RestrictedProject[] getAllEntriesFromAOTable() {
        RestrictedProject[] restrictedProjects = null;
        try {
            restrictedProjects = activeObjects.find(RestrictedProject.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return restrictedProjects;
    }

    public RestrictedProject createRecordInAOTable(RestrictedProjectObject restrictedProjectObject) {
        RestrictedProject returnRestrictedProject = null;
        try {
            RestrictedProject foundAO = null;
            RestrictedProject[] foundAOs = getRecordFromAOTableByProjectKey(restrictedProjectObject.getProjectKey());
            for (RestrictedProject restrictedProject : foundAOs) {
                if (restrictedProject.getGroupName() != null
                    && restrictedProjectObject.getGroupName() != null
                        && restrictedProject.getGroupName().equalsIgnoreCase(restrictedProjectObject.getGroupName())) {
                    foundAO = restrictedProject;
                    break;
                }
            }
            if (foundAO != null)
            {
                deleteRecordFromAOTable(foundAO);

                RestrictedProject restrictedProject = activeObjects.create(RestrictedProject.class);
                if (restrictedProject != null)
                {
                    returnRestrictedProject = setAOValuesAndReturnAsObject(restrictedProjectObject, restrictedProject);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            } else {
                log.debug("New setting!");
                RestrictedProject restrictedProject = activeObjects.create(RestrictedProject.class);
                if (restrictedProject != null)
                {
                    returnRestrictedProject = setAOValuesAndReturnAsObject(restrictedProjectObject, restrictedProject);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnRestrictedProject;
    }

    public void deleteRecordFromAOTable(RestrictedProject restrictedProject) {
        try {
            activeObjects.delete(restrictedProject);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private RestrictedProject setAOValuesAndReturnAsObject(RestrictedProjectObject restrictedProjectObject, RestrictedProject restrictedProject) {
        try {
            restrictedProject.setProjectKey(restrictedProjectObject.getProjectKey());
            restrictedProject.setGroupName(restrictedProjectObject.getGroupName());
            restrictedProject.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            restrictedProject = null;
        }
        return restrictedProject;
    }
}
