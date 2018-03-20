package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class FieldMappingObject {
    String integrationId;
    String itMappingId;
    String localFieldId;
    String remoteFieldId;

    public String getIntegrationId() {
        return integrationId;
    }

    public String getLocalFieldId() {
        return localFieldId;
    }

    public String getRemoteFieldId() {
        return remoteFieldId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public void setLocalFieldId(String localFieldId) {
        this.localFieldId = localFieldId;
    }

    public void setRemoteFieldId(String remoteFieldId) {
        this.remoteFieldId = remoteFieldId;
    }

    public String getItMappingId() {
        return itMappingId;
    }

    public void setItMappingId(String itMappingId) {
        this.itMappingId = itMappingId;
    }
}
