/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.lang.acl;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * This class contains a set of static methods that convert
 * to/from the Date Time format adopted by FIPA.
 * The FIPA format is based on ISO8601, with the addition of milliseconds.
 * Using the <code>java.text.SimpleDateFormat</code> notation, it is: 
 * <code>yyyyMMdd'T'HHmmssSSS'Z'</code>
 * , where the <code>'T'</code> serves to separate the Day from the Time, 
 * and the <code>'Z'</code> indicates that the time is in UTC.
 *
 * The FIPA specs permit either local or UTC time, however, they do 
 * express a preference for UTC time (this is particularly helpful when 
 * passing messages between agents running on machines in different timezones).
 * <UL>
 * <LI> Older versions of this code:
 *      <UL>
 *      <LI> read DateTime as local time
 *      <LI> wrote DateTime as local time
 *      </UL>
 * <LI> Current versions of this code:
 *      <UL>
 *      <LI> read DateTime in both local time and UTC time
 *      <LI> write DateTime as UTC time by default (can generate local time 
 *           if <code>toString(false)</code> is called). 
 *      </UL>
 * </UL> 
 *
 * @author Fabio Bellifemine - CSELT
 * @version $Date$ $Revision$
 * Modified by:
 * @author Craig Sayers, HP Labs, Palo Alto, California
 */
public class ISO8601 {

    
    private static SimpleDateFormat utcDateFormat;
    private static SimpleDateFormat localDateFormat;
 
    /* Initialize the date formats for later use in toDate and toString */
    static {
        utcDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        localDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
    }
    
  /**
   * This method converts a FIPA DateTime token to a <code>java.util.Date</code>.  
   * It will accept both local and UTC time formats.
   */
public static Date toDate(String dateTimeToken) throws Exception {
    if (dateTimeToken == null)
      return new Date();
    else if( dateTimeToken.startsWith("+") ) {
	// add current time for backwards compatability - does the FIPA spec
        // permit a DateTime token starting with '+'?
	int pos = 1;
	long millisec = Integer.parseInt(dateTimeToken.substring(pos, pos + 4))*365*24*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 4, pos + 6))*30*24*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 6, pos + 8))*24*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 9, pos +11))*60*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 11, pos + 13))*60*1000+
	  Integer.parseInt(dateTimeToken.substring(pos + 13, pos + 15))*1000;
	return(new Date((new Date()).getTime() + millisec));
    }        
    else if( dateTimeToken.endsWith("Z")) {
        // Preferred format is to pass UTC times, indicated by trailing 'Z'
        return utcDateFormat.parse(dateTimeToken);
    }
    else {
        // Alternate format is to use local times - no trailing 'Z'
        return localDateFormat.parse(dateTimeToken);
    }
}

  /**
   * This method converts a <code>java.util.Date</code> into a FIPA DateTime token.
   *
   * Note: the current default behaviour is to generate dates in UTC time.
   * see <code>ISO8601.useUTCtime</code> for details.
   * @param useUTCtime controls the style used by <code>toString</code>,
   *  'true' generates tokens using UTC time, 'false' using local time.
   * If you need to send messages to agents compiled with older versions 
   * of Jade, then set this to <code>false</code>.
   * @return a String, e.g. "19640625T073000000Z" to represent 7:30AM on the
   * 25th of June of 1964, UTC time.
   */
public static String toString(Date d, boolean useUTCtime){
    if( useUTCtime ) {
        // perferred style is to generate UTC times, indicated by trailing 'Z'
        return utcDateFormat.format(d);
    } else {
        // for backwards compatability, also support generating local times.
        return localDateFormat.format(d);
    }
}


  /**
   * This method converts a <code>java.util.Date</code> into a FIPA DateTime 
   * token by using the UTC time.
   * @return a String, e.g. "19640625T073000000Z" to represent 7:30AM on the
   * 25th of June of 1964, UTC time.
   */
public static String toString(Date d){
    return toString(d, true);
}

  /**
   * this method converts into a string in ISO8601 format representing
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
   * <code> java jade.lang.acl.ISO8601 <yourtoken> </code>
   */
public static void main(String argv[]) {
    System.out.println("USAGE: java ISO8601 DateTimetoken");
    System.out.println(argv[0]);
    try {
        System.out.println("Testing default behaviour (using UTC DateTime):");
        System.out.println("  ISO8601.toDate("+argv[0]+") returns:" + ISO8601.toDate(argv[0]));
        System.out.println("  converting that back to a string gives:" + ISO8601.toString(ISO8601.toDate(argv[0])));
        Date d1 = new Date();
        System.out.println("  ISO8601.toString( new Date() ) returns:" + ISO8601.toString(d1));
        System.out.println("  converting that back to a date gives:" + ISO8601.toDate(ISO8601.toString(d1)));
        
        System.out.println("Testing local time (for backwards compatability):");
        // ISO8601.useUTCtime = false;
        System.out.println("  ISO8601.toDate("+argv[0]+") returns:" + ISO8601.toDate(argv[0]));
        System.out.println("  converting that back to a string gives:" + ISO8601.toString(ISO8601.toDate(argv[0]), false));
        System.out.println("  ISO8601.toString( new Date(), false ) returns:" + ISO8601.toString(d1, false));
        System.out.println("  converting that back to a date gives:" + ISO8601.toDate(ISO8601.toString(d1, false)));
    } catch (Exception e) {
        e.printStackTrace();
    }
    
    try {
        System.out.println("ISO8601.toRelativeTimeString("+argv[0]+") returns:" + ISO8601.toRelativeTimeString(Long.parseLong(argv[0])));
        
        Date d = new Date(Integer.parseInt(argv[0]));
        System.out.println("ISO8601.toString("+d+", false) returns:" + ISO8601.toString(d, false));
    } catch (Exception e1) {
    }
    
}
}
