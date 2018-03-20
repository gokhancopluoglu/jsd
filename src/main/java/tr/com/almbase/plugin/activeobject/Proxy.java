package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface Proxy extends Entity {
    String getHost();
    String getPort();
    String getUsername();
    String getPassword();
    void setHost(String host);
    void setPort(String port);
    void setUsername(String username);
    void setPassword(String password);
}
