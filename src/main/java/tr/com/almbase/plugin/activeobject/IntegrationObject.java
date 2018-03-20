package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class IntegrationObject {
    String name;
    String url;
    String username;
    String password;

    public IntegrationObject(Integration integration) {
        this.name = integration.getName();
        this.url = integration.getUrl();
        this.username = integration.getUsername();
        this.password = integration.getPassword();
    }

    public IntegrationObject(String name, String url, String username, String password) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
