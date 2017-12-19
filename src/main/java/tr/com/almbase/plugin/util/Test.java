package tr.com.almbase.plugin.util;

/**
 * Created by kivanc.ahat@almbase.com on 19/12/2017.
 */
public class Test {

    public static void main (String [] args) {
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
