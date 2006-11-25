
package com.mucommander.text;

import com.mucommander.conf.*;
import com.mucommander.Debug;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * CustomDateFormat allows custom date formatting, according to the date format stored in the preferences.
 *
 * @author Maxence Bernard
 */
public class CustomDateFormat implements ConfigurationListener {

    /** Singleton instance */
    private static CustomDateFormat singleton;

    /** Custom SimpleDateFormat instance */
    private static SimpleDateFormat dateFormat;


    /**
     * Creates a new CustomDateFormat instance.
     */
    private CustomDateFormat() {}


    /**
     * Forces static fields to be initialized
     */
    public static void init() {
        // Create a singleton instance and keep it as a static member of this class.
        // Not doing it so would cause the garbage collector to GC it as ConfigurationManager holds
        // weak references of its listeners.
        singleton = new CustomDateFormat();
        ConfigurationManager.addConfigurationListener(singleton);

        dateFormat = createDateFormat();
    }


    /**
     * Replace the default '/' separator in the given format string, by the given custom separator.
     *
     * @return the given format string with '/' separator characters replaced by the given separator character.
     */
    public static String replaceDateSeparator(String dateFormatString, String separator) {
        if(separator==null || separator.equals("/"))
            return dateFormatString;
		
        StringBuffer dateFormatStringSB = new StringBuffer();
        int pos1 = 0;
        int pos2;
        while((pos2=dateFormatString.indexOf('/', pos1))>-1) {
            dateFormatStringSB.append(dateFormatString.substring(pos1, pos2)+separator);
            pos1 = pos2+1;
        }
        dateFormatStringSB.append(dateFormatString.substring(pos1, dateFormatString.length()));
        return dateFormatStringSB.toString();
    }


    /**
     * Creates and returns a SimpleDateFormat instance using date format stored in preferences.
     */
    private static SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat(
            replaceDateSeparator(
                ConfigurationManager.getVariable(ConfigurationVariables.DATE_FORMAT, ConfigurationVariables.DEFAULT_DATE_FORMAT),
                ConfigurationManager.getVariable(ConfigurationVariables.DATE_SEPARATOR, ConfigurationVariables.DEFAULT_DATE_SEPARATOR))
            + " " + ConfigurationManager.getVariable(ConfigurationVariables.TIME_FORMAT, ConfigurationVariables.DEFAULT_TIME_FORMAT));
    }
	
	
    /**
     * Formats the given with custom date format and returns a formatted date string. 
     *
     * @return a formatted string representing the given date.
     */
    public static String format(Date date) {
try {
        return dateFormat.format(date);
}
catch(Exception e) {
    e.printStackTrace();
    if(Debug.ON) Debug.trace("date="+date+" dateFormat="+dateFormat);
    return null;
}
    }
	

    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////
	
    /**
     * Listens to some configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        if (var.equals(ConfigurationVariables.TIME_FORMAT) || var.equals(ConfigurationVariables.DATE_FORMAT) || var.equals(ConfigurationVariables.DATE_SEPARATOR))
            dateFormat = createDateFormat();

        return true;
    }
}
