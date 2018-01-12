package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface RestrictedProject extends Entity {
    String getGroupName();
    void setGroupName(String groupName);
    String getProjectKey();
    void setProjectKey(String projectKey);
}
