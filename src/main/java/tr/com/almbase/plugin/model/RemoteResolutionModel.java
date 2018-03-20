package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteResolutionModel {
    private String resolutionId;
    private String resolutionName;

    public RemoteResolutionModel(String resolutionId, String resolutionName) {
        this.resolutionId = resolutionId;
        this.resolutionName = resolutionName;
    }

    public String getResolutionId() {
        return resolutionId;
    }

    public String getResolutionName() {
        return resolutionName;
    }

    public void setResolutionId(String resolutionId) {
        this.resolutionId = resolutionId;
    }

    public void setResolutionName(String resolutionName) {
        this.resolutionName = resolutionName;
    }
}
