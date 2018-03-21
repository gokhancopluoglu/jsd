package tr.com.almbase.plugin.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by kivanc.ahat@almbase.com on 19/12/2017.
 */
public class Test {

    public static void main (String [] args) throws Exception {
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
