/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.lang.acl;

import java.util.*;

/**
 * This class contains a set of static methods that allow to convert
 * to/from the Date Time format specified by ISO8601 and adopted by FIPA.
 * 
 * @author Fabio Bellifemine - CSELT
 * @version $Date$ $Revision$
 */
public class ISO8601 {

    /**
     * This method converts a String, that represents a Date Time Token
     * in IS8601 format, to a java.util.Date object.
     */
    public static Date toDate(String dateTimeToken) throws Exception {
        if (dateTimeToken == null) {
            return new Date();
        } 
        else {
            int pos;

            if (dateTimeToken.substring(0, 1).equals("+")) {

                // add current time
                pos = 1;

                long millisec = 
                    Integer.parseInt(dateTimeToken.substring(pos, pos + 4)) 
                    * 365 * 24 * 60 * 60 * 1000 
                                                + Integer.parseInt(dateTimeToken.substring(pos + 4, pos + 6)) 
                                                * 30 * 24 * 60 * 60 * 1000 
                                                + Integer.parseInt(dateTimeToken.substring(pos + 6, pos + 8)) 
                                                * 24 * 60 * 60 * 1000 
                                                + Integer.parseInt(dateTimeToken.substring(pos + 9, pos + 11)) 
                                                * 60 * 60 * 1000 
                                                + Integer.parseInt(dateTimeToken.substring(pos + 11, pos + 13)) 
                                                * 60 * 1000 
                                                + Integer.parseInt(dateTimeToken.substring(pos + 13, pos + 15)) 
                                                  * 1000;

                return (new Date((new Date()).getTime() + millisec));
            } 
            else {
                pos = 0;

                Calendar cal = Calendar.getInstance();

                cal.set(Calendar.YEAR, 
                        Integer.parseInt(dateTimeToken.substring(pos, 
                        pos + 4)));
                cal.set(Calendar.MONTH, 
                        Integer.parseInt(dateTimeToken.substring(pos + 4, pos + 6)) 
                        - 1);
                cal.set(Calendar.DATE, 
                        Integer.parseInt(dateTimeToken.substring(pos + 6, 
                        pos + 8)));
                cal.set(Calendar.HOUR, 
                        Integer.parseInt(dateTimeToken.substring(pos + 9, 
                        pos + 11)));
                cal.set(Calendar.MINUTE, 
                        Integer.parseInt(dateTimeToken.substring(pos + 11, 
                        pos + 13)));
                cal.set(Calendar.SECOND, 
                        Integer.parseInt(dateTimeToken.substring(pos + 13, 
                        pos + 15)));
                cal.set(Calendar.MILLISECOND, 
                        Integer.parseInt(dateTimeToken.substring(pos + 15, 
                        pos + 18)));

                return cal.getTime();
            } 
        } 
    } 

    /**
     * This method converts a <code>java.util.Date</code> into a String
     * in ISO8601 format (<quote>yyyyMMdd'T'hhmmssSSS</quote>).
     * @return a String, e.g. "19640625T073000000" to represent the 7:30 of the
     * 25th of June of 1964.
     */
    public static String toString(Date d) {

        // Initialize time
        Calendar cal = Calendar.getInstance();

        cal.setTime(d);

        // Format time
        String formatedDate = new String();

        formatedDate += zeroPaddingNumber(cal.get(Calendar.YEAR), 4);
        formatedDate += zeroPaddingNumber(cal.get(Calendar.MONTH) + 1, 2);
        formatedDate += zeroPaddingNumber(cal.get(Calendar.DATE), 2);
        formatedDate += "T";
        formatedDate += zeroPaddingNumber(cal.get(Calendar.HOUR), 2);
        formatedDate += zeroPaddingNumber(cal.get(Calendar.MINUTE), 2);
        formatedDate += zeroPaddingNumber(cal.get(Calendar.SECOND), 2);
        formatedDate += zeroPaddingNumber(cal.get(Calendar.MILLISECOND), 3);

        return formatedDate;
    } 

    /**
     * this method converts into a string in ISO8601 format representing
     * relative time from the current time
     * @param millisec is the number of milliseconds from now
     * @return a String, e.g. "+00000000T010000000" to represent one hour
     * from now
     */
    public static String toRelativeTimeString(long millisec) {
        if (millisec > 0) {    // FIXME
            long tmp = millisec / 1000;
            long msec = millisec - tmp * 1000;

            millisec = tmp;
            tmp = millisec / 60;

            long sec = millisec - tmp * 60;

            millisec = tmp;
            tmp = millisec / 60;

            long min = millisec - tmp * 60;

            millisec = tmp;
            tmp = millisec / 24;

            long h = millisec - tmp * 24;

            millisec = tmp;
            tmp = millisec / 30;

            long day = millisec - tmp * 30;

            millisec = tmp;
            tmp = millisec / 12;

            long mon = millisec - tmp * 12;

            millisec = tmp;

            long year = millisec;

            return "+" + zeroPaddingNumber(year, 4) 
                   + zeroPaddingNumber(mon, 2) + zeroPaddingNumber(day, 2) 
                   + "T" + zeroPaddingNumber(h, 2) 
                   + zeroPaddingNumber(min, 2) + zeroPaddingNumber(sec, 2) 
                   + zeroPaddingNumber(msec, 3);
        } 
        else {
            return "+00000000T000000000";
        }
    } 

    /**
     * Method declaration
     *
     * @param value
     * @param digits
     *
     * @return
     *
     * @see
     */
    private static String zeroPaddingNumber(long value, int digits) {
        String s = Long.toString(value);
        int    n = digits - s.length();

        for (int i = 0; i < n; i++) {
            s = "0" + s;
        }

        return s;
    } 

    /**
     * The main is here only for debugging.
     * You can test your conversion by executing the following command:
     * <p>
     * <code> java jade.cl.acl.ISO8601 <yourtoken> </code>
     */
    public static void main(String argv[]) {
        System.out.println(argv[0]);
        System.out.println("USAGE: java pacselt.ISO8601 DateTimetoken");

        try {
            System.out.println("ISO8601.toDate(" + argv[0] + ") returns:" 
                               + ISO8601.toDate(argv[0]));
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 

        try {
            System.out.println("ISO8601.toRelativeTimeString(" + argv[0] 
                               + ") returns:" 
                               + ISO8601.toRelativeTimeString(Long.parseLong(argv[0])));

            Date d = new Date(Integer.parseInt(argv[0]));

            System.out.println("ISO8601.toString(" + d + ") returns:" 
                               + ISO8601.toString(d));
        } 
        catch (Exception e1) {}

        Date d1 = new Date();

        System.out.println("ISO8601.toString(" + d1 + ") returns:" 
                           + ISO8601.toString(d1));
    } 

}

