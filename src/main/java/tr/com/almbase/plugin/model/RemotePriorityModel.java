package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemotePriorityModel {
    private String priorityId;
    private String priorityName;

    public RemotePriorityModel(String priorityId, String priorityName) {
        this.priorityId = priorityId;
        this.priorityName = priorityName;
    }

    public String getPriorityId() {
        return priorityId;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public void setPriorityId(String priorityId) {
        this.priorityId = priorityId;
    }

    public void setPriorityName(String priorityName) {
        this.priorityName = priorityName;
    }
}
