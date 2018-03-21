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
    String getType();
    void setHost(String host);
    void setPort(String port);
    void setType(String type);
}
