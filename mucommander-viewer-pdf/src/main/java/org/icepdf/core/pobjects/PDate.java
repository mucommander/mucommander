/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.pobjects;

import org.icepdf.core.pobjects.security.SecurityManager;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>This class defines a standard PDF date.  The class will try its best
 * to parse the date format into its component parts.  If a date cannot
 * be parsed, a non-standard flag is set to true.  In this instance, any
 * of the data accessor methods will return the unparsed string.</p>
 * <p/>
 * <p>PDF defines a standard date format, which closely follows that of the
 * international standard ASN.1 (Abstract Syntax Notation One), defined in
 * ISO/IEC 8824. A date is a string of the form (D:YYYYMMDDHHmmSSOHH'mm')
 * where:
 * <ul>
 * <li>YYYY is the year</li>
 * <li>MM is the month </li>
 * <li>DD is the day (01-31)</li>
 * <li>HH is the hour (00-23) </li>
 * <li>mm is the minute (00-59) </li>
 * <li>SS is the second (00-59) </li>
 * <li>O is the relationship of local time to Universal Time (UT), denoted by
 * one of the characters +, ?, or Z (see below) </li>
 * <li>HH followed by ' is the absolute value of the offset from UT in hours
 * (00-23)mm followed by ' is the absolute value of the offset from UT
 * in minutes (00-59)</li>
 * </ul>
 * <p/>
 * <p>The apostrophe character (') after HH and mm is part of the syntax. All
 * fields after the year are optional. (The prefix D:, although also optional,
 * is strongly recommended.) The default values for MM and DD are both 01; all
 * other numerical fields default to zero values. A plus sign (+) as the value
 * of the O field signifies that local time is later than UT, a minus sign (-)
 * that local time is earlier than UT, and the letter Z that local time is equal
 * to UT. If no UT information is specified, the relationship of the specified
 * time to UT is considered to be unknown. Whether or not the time zone is
 * known, the rest of the date should be specified in local time. For example,
 * December 23, 1998, at 7:52 PM, U.S. Pacific Standard Time, is represented by
 * the string D:199812231952?08'00'</p>
 *
 * @since 1.1
 */
public class PDate {

    protected static final SimpleDateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("'D:'yyyyMMddHHmmss");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    // offset value for year, YYYY
    private static final int OFFSET_YYYY = 4;
    // offset value for month, MM
    private static final int OFFSET_MM = 2;
    // offset value for day, DD
    private static final int OFFSET_DD = 2;
    // offset value for hour, HH
    private static final int OFFSET_HH = 2;
    // offset value for minute, mm
    private static final int OFFSET_mm = 2;
    // offset value for second, SS
    private static final int OFFSET_SS = 2;
    // offset value for east or west of GMT, 0
    private static final int OFFSET_0 = 1;

    // Optional prefix for a date
    private static final String DATE_PREFIX = "D:";

    // Month Names, 1 indexed based
    private static String[] monthNames = {"",
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"};


    // instance values related to time parts
    private String year = "";
    private String month = "";
    private String day = "";
    private String hour = "";
    private String minute = "";
    private String second = "";
    private String timeZoneOffset = "";
    private String timeZoneHour = "";
    private String timeZoneMinute = "";

    // set to true when an non standard data is encountered
    private boolean notStandardFormat = false;


    /**
     * Create a new Date object.
     *
     * @param date date ASCII data.
     */
    public PDate(SecurityManager securityManager, String date) {
        // parse the the date string
        if (date != null) {
            parseDate(date);
        }
    }

    /**
     * Gets the year value of the date.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return year value.
     */
    public String getYear() {
        return year;
    }

    /**
     * Gets the month value of the date.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return month value.
     */
    public String getMonth() {
        return month;
    }

    /**
     * Gets the day value of the date.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return day value.
     */
    public String getDay() {
        return day;
    }

    /**
     * Gets the hour value of the date.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return hour value.
     */
    public String getHour() {
        return hour;
    }

    /**
     * Gets the minute value of the date.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return minute value.
     */
    public String getMinute() {
        return minute;
    }

    /**
     * Gets the second value of the date.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return second value.
     */
    public String getSecond() {
        return second;
    }

    /**
     * Gets the time zone offset hour from GMT.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return hour value.
     */
    public String getTimeZoneHour() {
        return timeZoneHour;
    }

    /**
     * Gets the time zone offset minute from GMT.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return minute value.
     */
    public String getTimeZoneMinute() {
        return timeZoneMinute;
    }

    /**
     * Gets the time zone offset fromm GMT.  If the offset is negative
     * true is returned, false otherwise.
     * <br /><b>Note</b><br />
     * If the original date value cannot be parsed, this method returns
     * the unparsed string.
     *
     * @return time offset value.
     */
    public boolean getTimeZoneOffset() {
        return !"-".equals(timeZoneOffset);
    }

    /**
     * Returns a decoded string representation of the date object.
     * If the date object could not be parsed, the orginal date value is
     * returned.
     *
     * @return date value.
     */
    public String toString() {
        if (!notStandardFormat) {
            StringBuilder sb = new StringBuilder(40);
            if (getMonth(month).length() > 0)
                sb.append(getMonth(month));
            if (day.length() > 0)
                sb.append(" ").append(day);
            if (year.length() > 0)
                sb.append(", ").append(year);
            if (hour.length() > 0)
                sb.append(" ").append(hour);
            if (minute.length() > 0)
                sb.append(":").append(minute);
            if (second.length() > 0)
                sb.append(":").append(second);
            if (timeZoneOffset.length() > 0) {
                if (timeZoneOffset.equalsIgnoreCase("Z"))
                    sb.append(" (UTC)");
                else {
                    sb.append(" (UTC ").append(timeZoneOffset);
                    if (timeZoneHour.length() > 0)
                        sb.append("").append(timeZoneHour);
                    if (timeZoneMinute.length() > 0)
                        sb.append(":").append(timeZoneMinute);
                    sb.append(")");
                }
            }
            return sb.toString();
        } else {
            // return coded date
            return day;
        }
    }

    /**
     * Utility method for parsing PDF docs date format
     * (D:YYYYMMDDHHmmSSOHH'mm').  If the date is not in a know format
     * all instances variables are assigned the date value.
     *
     * @param date string representing a PDF date object.
     */
    private void parseDate(String date) {
        // get ride of "D:" prefix
        if (date.indexOf(DATE_PREFIX) >= 0) {
            date = date.substring(2);
            parseAdobeDate(date);
        }
        // have none standard form, Ghostscript, 5/26/2004 13:25:11
        else if (date.indexOf("/") >= 0) {
            parseGhostScriptDate(date);
        }
        //try adobe format but with out D:
        else {
            year = date;
            month = date;
            day = date;
            hour = date;
            minute = date;
            second = date;
            timeZoneOffset = date;
            timeZoneHour = date;
            timeZoneMinute = date;
            notStandardFormat = true;
        }

    }

    /**
     * Utility method for parsing a ghostscript date formate, 5/26/2004 13:25:11.
     *
     * @param date string representing a PDF date object.
     */
    private void parseGhostScriptDate(String date) {
        // month/day/year hour:minute:second

        // break the string on the date/time space
        StringTokenizer dateTime = new StringTokenizer(date);
        // tokenize the date using "/"
        StringTokenizer dateToken =
                new StringTokenizer(dateTime.nextToken(), "/");
        // tokenize the time using ":"
        StringTokenizer timeToken =
                new StringTokenizer(dateTime.nextToken(), ":");

        // get date vars
        month = dateToken.nextToken();
        day = dateToken.nextToken();
        year = dateToken.nextToken();

        // get time vars
        hour = timeToken.nextToken();
        minute = timeToken.nextToken();
        second = timeToken.nextToken();
    }

    /**
     * Utility mehtod for parsing Adobe standard date format,
     * (D:YYYYMMDDHHmmSSOHH'mm').
     *
     * @param date string representing a PDF date object.
     */
    private void parseAdobeDate(String date) {

        // total offset count
        int totalOffset = 0;
        int currentOffset = 0;

        // start peeling of values from string
        if (totalOffset + OFFSET_YYYY <= date.length()) {
            currentOffset = (totalOffset + OFFSET_YYYY);
            year = date.substring(totalOffset, currentOffset);
            totalOffset += currentOffset;
        }
        if (totalOffset + OFFSET_MM <= date.length()) {
            currentOffset = (totalOffset + OFFSET_MM);
            month = date.substring(totalOffset, currentOffset);
            totalOffset += OFFSET_MM;
        }
        if (totalOffset + OFFSET_DD <= date.length()) {
            currentOffset = (totalOffset + OFFSET_DD);
            day = date.substring(totalOffset, currentOffset);
            totalOffset += OFFSET_DD;
        }
        if (totalOffset + OFFSET_HH <= date.length()) {
            currentOffset = (totalOffset + OFFSET_HH);
            hour = date.substring(totalOffset, currentOffset);
            totalOffset += OFFSET_HH;
        }
        if (totalOffset + OFFSET_mm <= date.length()) {
            currentOffset = (totalOffset + OFFSET_mm);
            minute = date.substring(totalOffset, currentOffset);
            totalOffset += OFFSET_mm;
        }
        if (totalOffset + OFFSET_SS <= date.length()) {
            currentOffset = (totalOffset + OFFSET_SS);
            second = date.substring(totalOffset, currentOffset);
            totalOffset += OFFSET_SS;
        }
        if (totalOffset + OFFSET_0 <= date.length()) {
            currentOffset = (totalOffset + OFFSET_0);
            timeZoneOffset = date.substring(totalOffset, currentOffset);
            totalOffset += OFFSET_0;
        }
        if (totalOffset + OFFSET_HH <= date.length()) {
            currentOffset = (totalOffset + OFFSET_HH);
            timeZoneHour = date.substring(totalOffset, currentOffset);
            totalOffset += OFFSET_HH;
        }
        // lastly pull off 'MM' for timezone minutes
        if (totalOffset + 4 <= date.length()) {
            // compensate for the ' in 'MM'
            timeZoneMinute = date.substring(totalOffset + 1, totalOffset + 3);
            //System.out.println(timeZoneMinute);
        }
    }

    /**
     * Utility method for finding month names.
     */
    private String getMonth(String month) {
        int monthIndex = 0;
        try {
            monthIndex = Integer.parseInt(month);
        }
        // eat any problems
        catch (NumberFormatException e) {
        }

        return monthNames[monthIndex];
    }

    /**
     * Formats a date/time according to the PDF specification
     * (D:YYYYMMDDHHmmSSOHH'mm').
     *
     * @param time date/time value to format
     * @param tz   the time zone
     * @return the requested String representation
     */
    public static String formatDateTime(Date time, TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz, Locale.ENGLISH);
        cal.setTime(time);

        int offset = cal.get(Calendar.ZONE_OFFSET);
        offset += cal.get(Calendar.DST_OFFSET);

        //DateFormat is operating on GMT so adjust for time zone offset
        Date dt1 = new Date(time.getTime() + offset);
        StringBuffer sb = new StringBuffer();
        sb.append(DATE_FORMAT.format(dt1));

        offset /= (1000 * 60); //Convert to minutes

        if (offset == 0) {
            sb.append('Z');
        } else {
            if (offset > 0) {
                sb.append('+');
            } else {
                sb.append('-');
            }
            int offsetHour = Math.abs(offset / 60);
            int offsetMinutes = Math.abs(offset % 60);
            if (offsetHour < 10) {
                sb.append('0');
            }
            sb.append(Integer.toString(offsetHour));
            sb.append('\'');
            if (offsetMinutes < 10) {
                sb.append('0');
            }
            sb.append(Integer.toString(offsetMinutes));
            sb.append('\'');
        }
        return sb.toString();
    }

    /**
     * Formats a date/time according to the PDF specification.
     * (D:YYYYMMDDHHmmSSOHH'mm').
     *
     * @param time date/time value to format
     * @return the requested String representation
     */
    public static String formatDateTime(Date time) {
        return formatDateTime(time, TimeZone.getDefault());
    }

    public static PDate createDate(Date date) {
        return new PDate(null, formatDateTime(date));
    }

}
