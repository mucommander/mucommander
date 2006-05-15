package com.mucommander.ant.util;

import org.apache.tools.ant.*;
import java.util.*;

/**
 * Ant task used to make the current date available to the rest of the project.
 * <p>
 * This task will simply create a propery with the specified name and the current
 * date as a value, using the YYYYMMDD format.
 * </p>
 * @author Nicolas Rinaudo
 */
public class DateTask extends Task {
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Name of the property in which to store the current date. */
    private String propertyName;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates a new instance of the date task.
     */
    public DateTask() {}



    // - Misc. -----------------------------------------------------------
    // -------------------------------------------------------------------
    public static String getDate() {
        StringBuffer date;
        Calendar     now;

        date = new StringBuffer();
        now  = Calendar.getInstance();

        // Appends the year.
        date.append(now.get(Calendar.YEAR));

        // Appends the month.
        switch(now.get(Calendar.MONTH)) {
        case Calendar.JANUARY:
            date.append("01");
            break;
        case Calendar.FEBRUARY:
            date.append("02");
            break;
        case Calendar.MARCH:
            date.append("03");
            break;
        case Calendar.APRIL:
            date.append("04");
            break;
        case Calendar.MAY:
            date.append("05");
            break;
        case Calendar.JUNE:
            date.append("06");
            break;
        case Calendar.JULY:
            date.append("07");
            break;
        case Calendar.AUGUST:
            date.append("08");
            break;
        case Calendar.SEPTEMBER:
            date.append("09");
            break;
        case Calendar.OCTOBER:
            date.append("10");
            break;
        case Calendar.NOVEMBER:
            date.append("11");
            break;
        case Calendar.DECEMBER:
            date.append("12");
            break;
        }

        int day;
        if((day = now.get(Calendar.DAY_OF_MONTH)) < 10)
            date.append('0');
        date.append(day);

        return date.toString();
    }



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the name of the property in which to store the current date.
     * @param s name of the property in which to store the current date.
     */
    public void setName(String s) {propertyName = s;}

    /**
     * Executes the ant task.
     * @exception BuildException thrown if any error occurs.
     */
    public void execute() throws BuildException {
        // Makes sure the task has been properly initialised.
        if(propertyName == null)
            throw new BuildException("Unspecified name - please fill in the name attribute.");

        getProject().setProperty(propertyName, getDate());

        // Flushes the instance.
        propertyName = null;
    }
}
