package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteStatusModel {
    private String statusId;
    private String statusName;
    private String statusColor;

    public RemoteStatusModel(String statusId, String statusName, String statusColor) {
        this.statusId = statusId;
        this.statusName = statusName;
        this.statusColor = statusColor;
    }

    public String getStatusId() {
        return statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
