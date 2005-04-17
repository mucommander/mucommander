
package com.mucommander.text;

import com.mucommander.conf.*;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This class allows custom date formatting, according to the date format stored
 * in preferences file.
 *
 * @author Maxence Bernard
 */
public class CustomDateFormat implements ConfigurationListener {

	/** Single instance which allows methods to be static */
	private final static CustomDateFormat instance = new CustomDateFormat();

	/** Custom SimpleDateFormat instance */
	private static SimpleDateFormat dateFormat = createDateFormat();


	static {
		// Monitors some configuration variables
		ConfigurationManager.addConfigurationListener(instance);
	}

	
	/**
	 * Creates a new CustomDateFormat instance.
	 */
	private CustomDateFormat() {
	}


	/**
	 * Forces static fields to be initialized
	 */
	public static void init() {
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
		
		if (var.equals("prefs.time_format") || var.equals("prefs.date_format") || var.equals("prefs.date_separator"))
			dateFormat = createDateFormat();

		return true;
	}
}