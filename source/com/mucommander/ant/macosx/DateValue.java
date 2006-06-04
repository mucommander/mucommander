package com.mucommander.ant.macosx;

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;
import java.util.Calendar;

/**
 * Represents the value part of a date property.
 * @author Nicolas Rinaudo
 */
public class DateValue implements InfoElement {
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
    public void setYear(int i) {year = new Integer(i);}
    public void setMonth(int i) {month = new Integer(i);}
    public void setDay(int i) {day = new Integer(i);}
    public void setHours(int i) {hours = new Integer(i);}
    public void setMinutes(int i) {minutes = new Integer(i);}
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
        if(year == null)
            year = new Integer(now.get(Calendar.YEAR));
        else if(year.intValue() < 0)
            throw new BuildException("Illegal year value: " + year);

        // Makes sure month is initialised properly.
        if(month == null)
            month = new Integer(getMonthNumber(now.get(Calendar.MONTH)));
        else if(month.intValue() < 1 || month.intValue() > 12)
            throw new BuildException("Illegal month value: " + month);

        // Makes sure day is initialised properly.
        if(day == null)
            day = new Integer(now.get(Calendar.DAY_OF_MONTH));
        else if(day.intValue() < 0 || !checkDay(year.intValue(), month.intValue(), day.intValue()))
            throw new BuildException("Illegal day value: " + day);

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
        out.writeCData("-");
        writeValue(out, month, 2);
        out.writeCData("-");
        writeValue(out, day, 2);
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
    private static final void writeValue(XmlWriter out, Integer value, int digits) {
        String buffer;

        buffer = value.toString();
        for(int i = digits - buffer.length(); i > 0; i--)
            out.writeCData("0");
        out.writeCData(buffer);
    }

    private static final int getMonthNumber(int month) throws BuildException {
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

    private static final boolean checkDay(int year, int month, int day) {
        Calendar date;

        date = Calendar.getInstance();
        date.set(year, month, 1);

        return day <= date.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
