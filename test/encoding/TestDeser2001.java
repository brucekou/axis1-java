package test.encoding;

import org.apache.axis.Constants;
import org.apache.axis.encoding.Hex;

import java.util.Calendar;
import java.util.HashMap;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.xml.rpc.namespace.QName;

/** 
 * Test deserialization of SOAP responses
 */
public class TestDeser2001 extends TestDeser {

    public TestDeser2001(String name) {
        super(name, Constants.NS_URI_2001_SCHEMA_XSI, 
                    Constants.NS_URI_2001_SCHEMA_XSD);
    }

    /** 
     * Test deserialization of Date responses
     */
    public void testMinDate() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(1999, 04, 31, 0, 0, 0);
        date.set(Calendar.MILLISECOND,0);
        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        deserialize("<result xsi:type=\"xsd:date\">" + 
                       "1999-05-31" + 
                     "</result>",
                     date.getTime());
    }

    /** 
     * Test deserialization of dateTime (Calendar) responses
     */
    public void testMinDateTime() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(1999,04,31, 12, 01, 30);
        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        date.set(Calendar.MILLISECOND,0);
        deserialize("<result xsi:type=\"xsd:dateTime\">" + 
                       "1999-05-31T12:01:30Z" + 
                     "</result>",
                     date);
    }

    public void testDateTimeZ() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(1999,04,31,12,01,30);
        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        date.set(Calendar.MILLISECOND,150);
        deserialize("<result xsi:type=\"xsd:dateTime\">" + 
                       "1999-05-31T12:01:30.150Z" + 
                     "</result>",
                     date);
    }

    public void testDateTZ() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(1999, 04, 31, 0, 0, 0);
        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        date.set(Calendar.MILLISECOND,0);
        deserialize("<result xsi:type=\"xsd:date\">" + 
                       "1999-05-31" + 
                     "</result>",
                     date.getTime());
    }

    public void testDateTimeTZ() throws Exception {
        Calendar date = Calendar.getInstance();
        date.set(1999,04,31,12,01,30);
        date.set(Calendar.MILLISECOND,150);
        deserialize("<result xsi:type=\"xsd:dateTime\">" + 
                       "1999-05-31T12:01:30.150" + calcGMTOffset(date) + 
                     "</result>",
                     date);
    }

    private final int msecsInMinute = 60000;
    private final int msecsInHour = 60 * msecsInMinute;

    private String calcGMTOffset(Calendar cal) {
        int msecOffset = cal.get(Calendar.ZONE_OFFSET) +
                cal.get(Calendar.DST_OFFSET);
        int hourOffset = Math.abs(msecOffset / msecsInHour);
        String offsetString = msecOffset > 0 ? "+" : "-";
        offsetString += hourOffset >= 10 ? "" + hourOffset : "0" + hourOffset;
        offsetString += ":";
        int minOffset = Math.abs(msecOffset % msecsInHour);
        if (minOffset == 0) {
            offsetString += "00";
        }
        else {
            offsetString += minOffset >= 10 ? "" + minOffset : "0" + minOffset;
        }
        return offsetString;
    }

    public void testBase64() throws Exception {
        deserialize("<result xsi:type=\"xsd:base64Binary\">QmFzZTY0</result>",
                    "Base64".getBytes());
    }

    public void testBase64Null() throws Exception {
        deserialize("<result xsi:type=\"xsd:base64Binary\"></result>",
                    new byte[0]);
    }

    public void testHex() throws Exception {
        deserialize("<result xsi:type=\"xsd:hexBinary\">50A9</result>",
                    new Hex("50A9"),true);
    }

    public void testHexNull() throws Exception {
        deserialize("<result xsi:type=\"xsd:hexBinary\"></result>",
                    new Hex(""),true);
    }

    public void testQName() throws Exception {
        deserialize("<result xsi:type=\"xsd:QName\" xmlns:qns=\"namespace\">qns:localPart</result>", new QName("namespace", "localPart"), true);
    }

    public void testMapWithNils() throws Exception {
        HashMap m = new HashMap();
        m.put(null, new Boolean("false"));
        m.put("hi", null);
        deserialize("<result xsi:type=\"xmlsoap:Map\" " +
                    "xmlns:xmlsoap=\"http://xml.apache.org/xml-soap\"> " +
                      "<item>" +
                       "<key xsi:nil=\"true\"/>" +
                       "<value xsi:type=\"xsd:boolean\">false</value>" + 
                      "</item><item>" +
                       "<key xsi:type=\"string\">hi</key>" +
                       "<value xsi:nil=\"true\"/>" +
                      "</item>" +
                    "</result>",
                    m);
    }

}
