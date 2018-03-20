package tr.com.almbase.plugin.activeobject;

import net.java.ao.Entity;
import net.java.ao.Preload;

/**
 * Created by kivanc.ahat@almbase.com
 */

@Preload
public interface Integration extends Entity {
    String getName();
    String getUrl();
    String getUsername();
    String getPassword();
    void setName(String name);
    void setUrl(String url);
    void setUsername(String username);
    void setPassword(String password);
}
