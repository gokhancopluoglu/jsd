package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface GroupUser extends Entity {
    String getIssueType();
    void setIssueType(String issueType);
    String getGroupName();
    void setGroupName(String groupName);
    String getUserName();
    void setUserName(String userName);
}
