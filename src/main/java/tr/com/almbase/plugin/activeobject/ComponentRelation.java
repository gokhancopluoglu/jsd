package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface ComponentRelation extends Entity {
    String getComponentId();
    String getRilComponentId();
    void setComponentId(String componentId);
    void setRilComponentId(String rilComponentId);
}
