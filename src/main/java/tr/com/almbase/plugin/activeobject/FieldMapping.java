package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface FieldMapping extends Entity {
    String getIntegrationId();
    String getLocalFieldId();
    String getRemoteFieldId();
    void setIntegrationId(String integrationId);
    void setLocalFieldId(String localFieldId);
    void setRemoteFieldId(String remoteFieldId);
    String getItMappingId();
    void setItMappingId(String itMappingId);
}
