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

    public static final String REST_CREATE_ISSUE = "/rest/api/2/issue";
    public static final String REST_GET_ISSUE = "/rest/api/2/issue/ISSUEKEY";
    public static final String BROWSE_ISSUE = "/browse/";
    public static final String REST_GET_ALL_PROJECTS = "/rest/api/2/project";
    public static final String REST_GET_PROJECT = "/rest/api/2/project/PROJECTKEY";
    public static final String REST_GET_PROJECT_COMPONENTS = "/rest/api/2/project/PROJECTKEY/components";
    public static final String REST_GET_PROJECT_VERSIONS = "/rest/api/2/project/PROJECTKEY/versions";
    public static final String REST_GET_ISSUE_TYPES = "/rest/api/2/issuetype";
    public static final String REST_GET_FIELDS = "/rest/api/2/field";
    public static final String REST_GET_ISSUE_CREATE_METADATA = "/rest/api/2/issue/createmeta?expand=projects.issuetypes.fields&projectKeys=PROJECTKEY&issuetypeIds=ISSUETYPEID";

    public static final String BRANS_CUSTOMFIELD_ID = "customfield_10061";

    public static final String CATEGORY_CF_ID = getPropertyValue(getProperties(), "category.cf.id");
    public static final String SUB_CATEGORY_CF_ID = getPropertyValue(getProperties(), "sub.category.cf.id");
    public static final String CATEGORY_ITEM_CF_ID = getPropertyValue(getProperties(), "category.item.cf.id");
    public static final String CATEGORY_COMPONENT_CF_ID = getPropertyValue(getProperties(), "category.component.cf.id");

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
