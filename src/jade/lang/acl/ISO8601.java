/*
  $Log$
  Revision 1.1  1999/02/22 09:29:38  rimassa
  This class handles FIPA-mandated ISO 8601 time format.

*/

package jade.lang.acl;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * This class contains a set of static methods that allow to convert
 * to/from the Date Time format specified by ISO8601 and adopted by FIPA.
 */
public class ISO8601 {

  /**
   * This method converts a String, that represents a Date Time Token
   * in IS8601 format, to a java.util.Date object.
   */
public static Date toDate(String dateTimeToken) throws Exception {
    if (dateTimeToken == null)
      return new Date();
    else {
      int pos;
      if(dateTimeToken.substring(0, 1).equals("+")) {
	// add current time
	pos = 1;
	long millisec = Integer.parseInt(dateTimeToken.substring(pos, pos + 4))*365*24*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 4, pos + 6))*30*24*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 6, pos + 8))*24*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 9, pos +11))*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 11, pos + 13))*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 13, pos + 15))*1000;
	return(new Date((new Date()).getTime() + millisec));
      } else {
    	pos = 0;
	GregorianCalendar cal = new GregorianCalendar(
		Integer.parseInt(dateTimeToken.substring(pos, pos + 4)),
		Integer.parseInt(dateTimeToken.substring(pos + 4, pos + 6))-1,
		Integer.parseInt(dateTimeToken.substring(pos + 6, pos + 8)),
		Integer.parseInt(dateTimeToken.substring(pos + 9, pos +11)),
		Integer.parseInt(dateTimeToken.substring(pos + 11, pos + 13)),
		Integer.parseInt(dateTimeToken.substring(pos + 13, pos + 15))
		);
	return cal.getTime();
      }
    }
}

  /**
   * This method converts a java.util.Date into a String in ISO8601 format.
   * @return a String, e.g. "19640625T073000000" to represent the 7:30 of the
   * 25th of June of 1964.
   */
public static String toString(Date d){
   SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
   return formatter.format(d);
}

  /**
   * this method converts into a String in ISO8601 format representing
   * relative time from the current time
   * @param millisec is the number of milliseconds from now
   * @return a String, e.g. "+00000000T010000000" to represent one hour
   * from now
   */
public static String toRelativeTimeString(long millisec) {
  if (millisec > 0) { //FIXME
    long tmp = millisec/1000;
    long msec = millisec - tmp*1000;
    millisec = tmp;

    tmp = millisec/60;
    long sec = millisec - tmp*60;
    millisec = tmp;

    tmp = millisec/60;
    long min = millisec - tmp*60;
    millisec = tmp;

    tmp = millisec/24;
    long h = millisec - tmp*24;
    millisec = tmp;
    
    tmp = millisec/30;
    long day = millisec - tmp*30;
    millisec = tmp;

    tmp = millisec/12;
    long mon = millisec - tmp*12;
    millisec = tmp;

    long year = millisec;
    return "+"+zeroPaddingNumber(year,4)+zeroPaddingNumber(mon,2)+
      zeroPaddingNumber(day,2)+"T"+zeroPaddingNumber(h,2)+
      zeroPaddingNumber(min,2)+zeroPaddingNumber(sec,2)+
      zeroPaddingNumber(msec,3);
  }
  else
    return "+00000000T000000000";
}


private static String zeroPaddingNumber(long value, int digits) {
  String s = Long.toString(value);
  int n=digits-s.length();
  for (int i=0; i<n; i++)
      s="0"+s;
  return s;
}



  /**
   * The main is here only for debugging.
   * You can test your conversion by executing the following command:
   * <p>
   * <code> java Support.ISO8601 <yourtoken> </code>
   */
public static void main(String argv[]) {
  System.out.println(argv[0]);
  System.out.println("USAGE: java pacselt.ISO8601 DateTimetoken");
  try {
    System.out.println("ISO8601.toDate("+argv[0]+") returns:" + ISO8601.toDate(argv[0]));
  } catch (Exception e) {
    e.printStackTrace();
  }
  
  try {
    System.out.println("ISO8601.toRelativeTimeString("+argv[0]+") returns:" + ISO8601.toRelativeTimeString(Long.parseLong(argv[0])));
  
    Date d = new Date(Integer.parseInt(argv[0]));
    System.out.println("ISO8601.toString("+d+") returns:" + ISO8601.toString(d));
  } catch (Exception e1) {
  }
  
  Date d1 = new Date();
  System.out.println("ISO8601.toString("+d1+") returns:" + ISO8601.toString(d1));
}
}
