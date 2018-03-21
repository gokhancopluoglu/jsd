package tr.com.almbase.plugin.activeobject;

/**
 * Created by kivanc.ahat@almbase.com on 13/03/2018.
 */
public class IntegrationObject {
    String id;
    String name;
    String url;
    String username;
    String password;
    Proxy proxy;

    public IntegrationObject(Integration integration) {
        this.name = integration.getName();
        this.url = integration.getUrl();
        this.username = integration.getUsername();
        this.password = integration.getPassword();
    }

    public IntegrationObject(String name, String url, String username, String password) {
        setName(name);
        setUrl(url);
        setUsername(username);
        setPassword(password);
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

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
