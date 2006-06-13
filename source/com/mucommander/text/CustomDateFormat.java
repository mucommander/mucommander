
package com.mucommander.text;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This class allows custom date formatting, according to the date format stored
 * in preferences file.
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
                                    replaceDateSeparator(ConfigurationManager.getVariable("prefs.date_format", "MM/dd/yy"), ConfigurationManager.getVariable("prefs.date_separator", "/"))
                                    +" "+ConfigurationManager.getVariable("prefs.time_format", "hh:mm a"));
    }
	
	
    /**
     * Formats the given with custom date format and returns a formatted date string. 
     *
     * @return a formatted string representing the given date.
     */
    public static String format(Date date) {
        return dateFormat.format(date);
    }
	

    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////
	
    /**
     * Listens to some configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("variable changed : "+var+"="+event.getValue());
        		
        if (var.equals("prefs.time_format") || var.equals("prefs.date_format") || var.equals("prefs.date_separator"))
            dateFormat = createDateFormat();

        return true;
    }
}
