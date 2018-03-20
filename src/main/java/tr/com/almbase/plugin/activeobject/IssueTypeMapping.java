package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface IssueTypeMapping extends Entity {
    String getIntegrationId();
    String getName();
    String getLocalProjectId();
    String getLocalIssueTypeId();
    String getRemoteProjectId();
    String getRemoteIssueTypeId();
    void setIntegrationId(String integrationId);
    void setName(String name);
    void setLocalProjectId(String localProjectId);
    void setLocalIssueTypeId(String localIssueTypeId);
    void setRemoteProjectId(String remoteProjectId);
    void setRemoteIssueTypeId(String remoteIssueTypeId);
}
