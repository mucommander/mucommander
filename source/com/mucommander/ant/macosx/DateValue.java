/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ant.macosx;

import com.mucommander.xml.writer.XmlWriter;
import org.apache.tools.ant.BuildException;

import java.util.Calendar;

/**
 * Represents the value part of a date property.
 * @author Nicolas Rinaudo
 */
class DateValue implements InfoElement {
    // - Class fields ----------------------------------------------------
    // -------------------------------------------------------------------
    /** Label of the 'date' XML element. */
    private static final String ELEMENT_DATE = "date";



    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Year represented by this date instance. */
    private Integer year;
    /** Month represented by this date instance. */
    private Integer month;
    /** Day represented by this date instance. */
    private Integer day;
    /** Hours represented by this date instance. */
    private Integer hours;
    /** Minutes represented by this date instance. */
    private Integer minutes;
    /** Seconds represented by this date instance. */
    private Integer seconds;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates an empty date value.
     */
    public DateValue() {}



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Allows Ant to set the date's year.
     * <p>
     * If not specified, the current year will be used.
     * </p>
     * @param i date's year.
     */
    public void setYear(int i) {year = new Integer(i);}

    /**
     * Allows Ant to set the date's month.
     * <p>
     * If not specified and <code>year</code> was, the current month will be used.
     * </p>
     * @param i date's month.
     */
    public void setMonth(int i) {month = new Integer(i);}

    /**
     * Allows Ant to set the date's day.
     * <p>
     * If not specified and <code>year</code> and <code>month</code> were,
     * the current day will be used.
     * </p>
     * @param i date's month.
     */
    public void setDay(int i) {day = new Integer(i);}

    /**
     * Allows Ant to set the date's hours.
     * <p>
     * This value is non-compulsory. If not specified, any minute and second
     * data will be discarded.
     * </p>
     * @param i date's hour.
     */
    public void setHours(int i) {hours = new Integer(i);}

    /**
     * Allows Ant to set the date's minutes.
     * <p>
     * This value is non-compulsory. If not specified, any seconds data
     * will be discarded.
     * </p>
     * <p>
     * This will be ignored if no hours information was set.
     * </p>
     * @param i date's minutes.
     */
    public void setMinutes(int i) {minutes = new Integer(i);}

    /**
     * Allows Ant to set the date's seconds.
     * <p>
     * This will be ignored if no hours and minutes information was set.
     * </p>
     * @param i date's seconds.
     */
    public void setSeconds(int i) {seconds = new Integer(i);}



    // - XML output ------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the XML representation of this date value.
     * @param     out            where to write the date's value to.
     * @exception BuildException thrown if anything wrong happens.
     */
    public void write(XmlWriter out) throws BuildException {
        Calendar now; // Used to fill in default values.

        now = Calendar.getInstance();

        // Makes sure year is initialised properly.
        if(year == null) {
            // Uses default values where applicable.
            year = new Integer(now.get(Calendar.YEAR));
            if(month == null) {
                month = new Integer(getMonthNumber(now.get(Calendar.MONTH)));
                if(day == null)
                    day = new Integer(now.get(Calendar.DAY_OF_MONTH));
            }
        }
        else if(year.intValue() < 0)
            throw new BuildException("Illegal year value: " + year);

        // Makes sure month is initialised properly.
        if(month != null) {
            if(month.intValue() < 1 || month.intValue() > 12)
                throw new BuildException("Illegal month value: " + month);
            // Makes sure day is initialised properly.
            if(day != null && (day.intValue() < 0 || !checkDay(year.intValue(), month.intValue(), day.intValue())))
                throw new BuildException("Illegal day value: " + day);
        }

        // Makes sure time is initialised properly.
        if(hours != null) {
            if(hours.intValue() < 0 || hours.intValue() > 24)
                throw new BuildException("Illegal hours value: " + hours);
            if(minutes != null) {
                if(minutes.intValue() < 0 || minutes.intValue() > 60)
                    throw new BuildException("Illegal minutes value: " + minutes);
                if(seconds != null && (seconds.intValue() < 0 || seconds.intValue() > 60))
                    throw new BuildException("Illegal seconds value: " + seconds);
            }
        }

        // Writes the date value.
        out.startElement(ELEMENT_DATE);
        writeValue(out, year, 4);

        // Writes the month and day information if they were specified.
        if(month != null) {
            out.writeCData("-");
            writeValue(out, month, 2);
            if(day != null) {
                out.writeCData("-");
                writeValue(out, day, 2);
            }
        }

        // Writes the time information if it was specified.
        if(hours != null) {
            out.writeCData("T");
            writeValue(out, hours, 2);
            if(minutes != null) {
                out.writeCData(":");
                writeValue(out, minutes, 2);
                if(seconds != null) {
                    out.writeCData(":");
                    writeValue(out, seconds, 2);
                }
            }
        }
        out.writeCData("Z");
        out.endElement(ELEMENT_DATE);
    }


    // - Helper methods --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes a 0-padded integer value to the specified XML output stream.
     * @param out    where to write the integer value.
     * @param value  value to write to the XML output stream.
     * @param digits minimum number of digits the value must have.
     */
    private static void writeValue(XmlWriter out, Integer value, int digits) {
        String buffer;

        buffer = value.toString();

        // Padds the value.
        for(int i = digits - buffer.length(); i > 0; i--)
            out.writeCData("0");

        out.writeCData(buffer);
    }

    /**
     * Retrieves the proper number of the specified month.
     * <p>
     * This method is necessary as:<br/>
     * - the Java month constants start from 0, not 1.<br/>
     * - there is no guaranty that the value will not change in fugure VMs.
     * </p>
     * @param month identifier of the month as defined in {@link java.util.Calendar}.
     * @return      the number of the month in the year.
     */
    private static int getMonthNumber(int month) throws BuildException {
        switch(month) {
        case Calendar.JANUARY:
            return 1;
        case Calendar.FEBRUARY:
            return 2;
        case Calendar.MARCH:
            return 3;
        case Calendar.APRIL:
            return 4;
        case Calendar.MAY:
            return 5;
        case Calendar.JUNE:
            return 6;
        case Calendar.JULY:
            return 7;
        case Calendar.AUGUST:
            return 8;
        case Calendar.SEPTEMBER:
            return 9;
        case Calendar.OCTOBER:
            return 10;
        case Calendar.NOVEMBER:
            return 11;
        case Calendar.DECEMBER:
            return 12;
        }
        throw new BuildException("System error: cannot compute the current month.");
    }

    /**
     * Makes sure that the specified day is valid for the given year and month.
     * @param  year  year in which the day is located.
     * @param  month month in which the day is located.
     * @param  day   day whose validity must be checked.
     * @return       <code>true</code> if the day is valid, <code>false</code> otherwise.
     */
    private static boolean checkDay(int year, int month, int day) {
        Calendar date;

        date = Calendar.getInstance();
        date.set(year, month, 1);

        return day <= date.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
