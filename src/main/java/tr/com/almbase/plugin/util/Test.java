package tr.com.almbase.plugin.util;

import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kivanc.ahat@almbase.com on 19/12/2017.
 */
public class Test {

    public static void main (String [] args) throws Exception {

        JsonObject parentObject = new JsonObject();
        System.out.print(parentObject.toString());
    }


    public static void main4 (String [] args) throws Exception {
        Map<String, String> cascadingSelectList = new HashMap<>();
        cascadingSelectList.put("parent", "green");
        cascadingSelectList.put("child", "blue");
        JsonObject parentObject = new JsonObject();
        JsonObject childObject = new JsonObject();
        childObject.addProperty("value", cascadingSelectList.get("child"));
        parentObject.addProperty("value", cascadingSelectList.get("parent"));
        parentObject.add("child", childObject);
        System.out.print(parentObject.toString());
    }

    public static void main3 (String [] args) throws Exception {
        String value = "2018-03-20T15:30:35.000+0200";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String lastUpdatedDate = sdf.format(format.parse(value));
        System.out.print(lastUpdatedDate);
    }

    public static void main2 (String [] args) {
        String categoryCFVal = "<content>     " +
                "   <value>Deneme Value</value>    ++++" +
                "   < /content>    ";
        if (null != categoryCFVal) {
            if (categoryCFVal.contains("<value>") && categoryCFVal.contains("</value>")) {
                categoryCFVal = categoryCFVal.substring(categoryCFVal.indexOf("<value>") + 7, categoryCFVal.indexOf("</value>"));
            }
        }

        System.out.print(categoryCFVal);
    }


}
