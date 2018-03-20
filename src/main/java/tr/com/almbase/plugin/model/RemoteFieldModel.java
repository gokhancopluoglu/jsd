package tr.com.almbase.plugin.model;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class RemoteFieldModel {
    private String id;
    private String name;
    private String type;
    private String system;
    private String custom;

    public RemoteFieldModel(String id, String name, String type, String system, String custom) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.system = system;
        this.custom = custom;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSystem() {
        return system;
    }

    public String getCustom() {
        return custom;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }
}
