package tr.com.almbase.plugin.activeobject;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by kivanc.ahat@almbase.com
 */

public class ProxyControllerImpl implements ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyControllerImpl.class);
    private ActiveObjects activeObjects;

    public ProxyControllerImpl(ActiveObjects activeObjects)
    {
        this.activeObjects = activeObjects;
    }

    public Proxy[] getAllEntriesFromAOTable() {
        Proxy[] proxies = null;
        try {
            proxies = activeObjects.find(Proxy.class);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            log.error("Couldn't find any record!");
        }
        return proxies;
    }

    public Proxy createRecordInAOTable(ProxyObject proxyObject) {
        Proxy returnProxy = null;
        try {
            deleteAllRecordFromAOTable();

            Proxy proxyRecord = activeObjects.create(Proxy.class);
            if (proxyRecord != null)
            {
                returnProxy = setAOValuesAndReturnAsObject(proxyRecord, proxyObject);
            } else {
                log.error("An error occured while creating empty object!");
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return returnProxy;
    }

    public void deleteRecordFromAOTable(Proxy proxyRecord) {
        try {
            activeObjects.delete(proxyRecord);
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    public void deleteAllRecordFromAOTable() {
        try {
            Proxy[] proxies = getAllEntriesFromAOTable();
            for (Proxy proxy : proxies) {
                deleteRecordFromAOTable(proxy);
            }
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
    }

    private Proxy setAOValuesAndReturnAsObject(Proxy proxyRecord, ProxyObject proxyObject) {
        try {
            proxyRecord.setHost(proxyObject.getHost());
            proxyRecord.setPort(proxyObject.getPort());
            proxyRecord.setUsername(proxyObject.getUsername());
            proxyRecord.setPassword(proxyObject.getPassword());
            proxyRecord.save();
        } catch (Exception e) {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
            proxyRecord = null;
        }
        return proxyRecord;
    }
}
