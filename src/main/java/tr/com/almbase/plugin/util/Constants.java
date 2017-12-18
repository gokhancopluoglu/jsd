package tr.com.almbase.plugin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Created by kivanc.ahat@almbase.com on 17/12/2017.
 */
public class Constants {
    private static Date lastUpdateDate;
    private static Properties properties;
    private static final String PROPERTIES_FILE_PATH = "/pluginconfig.properties";

    public static final String CATEGORY_CF_ID = getPropertyValue(getProperties(), "category.cf.id");
    public static final String SUB_CATEGORY_CF_ID = getPropertyValue(getProperties(), "sub.category.cf.id");
    public static final String CATEGORY_ITEM_CF_ID = getPropertyValue(getProperties(), "category.item.cf.id");

    private static final Logger log = LoggerFactory.getLogger(Constants.class);

    private static Properties getProperties()
    {
        Date now = Calendar.getInstance().getTime();
        if (lastUpdateDate == null || (now.getTime() - lastUpdateDate.getTime()) > new Long(3600000))
        {
            setProperties(getPropertiesFromPath());
            lastUpdateDate = now;
        }
        return properties;
    }

    private static void setProperties(Properties properties)
    {
        Constants.properties = properties;
    }

    private static String getPropertyValue(Properties properties, String key)
    {
        String propValue = null;
        try
        {
            propValue = properties.getProperty(key);
        }
        catch (Exception e)
        {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return propValue;
    }

    private static Properties getPropertiesFromPath()
    {
        Properties props = new Properties();
        try
        {
            InputStream inputStream = Constants.class.getResourceAsStream(PROPERTIES_FILE_PATH);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            props.load(reader);
            inputStream.close();
        }
        catch (Exception e)
        {
            StringWriter stack = new StringWriter();
            e.printStackTrace(new PrintWriter(stack));
            log.error(stack.toString());
        }
        return props;
    }
}
