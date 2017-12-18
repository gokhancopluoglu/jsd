package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class GroupUserControllerImpl implements GroupUserController{

    private static final Logger log = LoggerFactory.getLogger(GroupUserControllerImpl.class);
    private ActiveObjects activeObjects;

    public GroupUserControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public GroupUser getRecordFromAOTableByGroupName(String groupName) {
        GroupUser groupUser = null;
        try {
            GroupUser[] tempGroupUser = activeObjects.find(GroupUser.class, "GROUP_NAME = ?", groupName);
            if (null != tempGroupUser && tempGroupUser.length > 0)
                groupUser = tempGroupUser[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return groupUser;
    }

    public GroupUser getRecordFromAOTableByUserName(String userName) {
        GroupUser groupUser = null;
        try {
            GroupUser[] tempGroupUser = activeObjects.find(GroupUser.class, "USER_NAME = ?", userName);
            if (null != tempGroupUser && tempGroupUser.length > 0)
                groupUser = tempGroupUser[0];
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return groupUser;
    }

    public GroupUser getRecordFromAOTableById(String groupUserId) {
        GroupUser groupUser = null;
        try {
            GroupUser[] tempGroupUser = activeObjects.find(GroupUser.class, "ID = ?", Integer.parseInt(groupUserId));
            if (null != tempGroupUser && tempGroupUser.length > 0) {
                groupUser = tempGroupUser[0];
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return groupUser;
    }

    public GroupUser[] getAllEntriesFromAOTable() {
        GroupUser[] groupUsers = null;
        try {
            groupUsers = activeObjects.find(GroupUser.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return groupUsers;
    }

    public GroupUser createRecordInAOTable(GroupUserObject groupUserObject) {
        GroupUser returnGroupUser = null;
        try {
            GroupUser foundAO = getRecordFromAOTableByGroupName(groupUserObject.getGroupName());
            if (foundAO != null)
            {
                deleteRecordFromAOTable(foundAO);

                GroupUser groupUser = activeObjects.create(GroupUser.class);
                if (groupUser != null)
                {
                    returnGroupUser = setAOValuesAndReturnAsObject(groupUserObject, groupUser);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            } else {
                log.debug("New setting!");
                GroupUser groupUser = activeObjects.create(GroupUser.class);
                if (groupUser != null)
                {
                    returnGroupUser = setAOValuesAndReturnAsObject(groupUserObject, groupUser);
                } else {
                    log.error("An error occured while creating empty object!");
                }
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnGroupUser;
    }

    public GroupUser updateRecordInAOTable(GroupUser groupUser, GroupUserObject groupUserObject) {
        try {
            if (groupUser != null) {
                groupUser.setUserName(groupUserObject.getUserName());
                groupUser.save();
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return groupUser;
    }

    public void deleteRecordFromAOTable(GroupUser groupUser) {
        try {
            activeObjects.delete(groupUser);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private GroupUser setAOValuesAndReturnAsObject(GroupUserObject groupUserObject, GroupUser groupUserRecord) {
        try {
            groupUserRecord.setGroupName(groupUserObject.getGroupName());
            groupUserRecord.setUserName(groupUserObject.getUserName());
            groupUserRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            groupUserRecord = null;
        }
        return groupUserRecord;
    }
}
