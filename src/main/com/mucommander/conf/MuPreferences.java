/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.conf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.mucommander.RuntimeConstants;
import com.mucommander.commons.conf.Configuration;
import com.mucommander.commons.conf.ConfigurationException;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.conf.ValueList;
import com.mucommander.commons.runtime.JavaVersion;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.icon.FileIcons;

/**
 * muCommander specific wrapper for the <code>com.mucommander.conf</code> API which is used to save 'static' configurations.
 * 'static' configurations refer to properties that can be changed from the preferences dialog only.
 * those properties do not change often.
 * 
 * @author Nicolas Rinaudo, Maxence Bernard, Arik Hadas
 */
public class MuPreferences implements MuPreferencesAPI {

	// - Misc. variables -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Whether or not to automatically check for updates on startup. */
	public static final String  CHECK_FOR_UPDATE                  = "check_for_updates_on_startup";
	/** Default automated update behavior. */
	public static final boolean DEFAULT_CHECK_FOR_UPDATE          = true;
	/** Description of the format dates should be displayed with. */
	public static final String  DATE_FORMAT                       = "date_format";
	/** Default date format. */
	public static final String  DEFAULT_DATE_FORMAT               = "MM/dd/yy";
	/** Character used to separate years, months and days in a date. */
	public static final String  DATE_SEPARATOR                    = "date_separator";
	/** Default date separator. */
	public static final String  DEFAULT_DATE_SEPARATOR            = "/";
	/** Description of the format timestamps should be displayed with.*/
	public static final String  TIME_FORMAT                       = "time_format";
	/** Default time format. */
	public static final String  DEFAULT_TIME_FORMAT               = "hh:mm a";
	/** Language muCommander should use when looking for text.. */
	public static final String  LANGUAGE                          = "language";
	/** Whether or not to display compact file sizes. */
	public static final String  DISPLAY_COMPACT_FILE_SIZE         = "display_compact_file_size";
	/** Default file size display behavior. */
	public static final boolean DEFAULT_DISPLAY_COMPACT_FILE_SIZE = true;
	/** Whether or not to ask the user for confirmation before quitting muCommander. */
	public static final String  CONFIRM_ON_QUIT                   = "quit_confirmation";
	/** Default quitting behavior. */
	public static final boolean DEFAULT_CONFIRM_ON_QUIT           = true;
	/** Whether or not to display splash screen when starting muCommander. */
	public static final String  SHOW_SPLASH_SCREEN                = "show_splash_screen";
	/** Default splash screen behavior. */
	public static final boolean DEFAULT_SHOW_SPLASH_SCREEN        = true;
	/** Look and feel used by muCommander. */
	public static final String  LOOK_AND_FEEL                     = "lookAndFeel";
	/** All registered custom Look and feels. */
	public static final String  CUSTOM_LOOK_AND_FEELS             = "custom_look_and_feels";
	/** Separator used to tokenise the custom look and feels variable. */
	public static final String  CUSTOM_LOOK_AND_FEELS_SEPARATOR   = ";";
	/** Controls whether system notifications are enabled. */
	public static final String  ENABLE_SYSTEM_NOTIFICATIONS       = "enable_system_notifications";
	/** System notifications are enabled by default on platforms where a notifier is available and works well enough.
	 * In particular, the system tray notifier is available under Linux+Java 1.6, but it doesn't work well so it is not
	 * enabled by default. */
	public static final boolean DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS = OsFamily.MAC_OS_X.isCurrent() ||
			(OsFamily.WINDOWS.isCurrent() && JavaVersion.JAVA_1_6.isCurrentOrHigher());
	/** List of encodings that are displayed in encoding selection components. */
	public static final String  PREFERRED_ENCODINGS               = "preferred_encodings";


	// - Log variables -------------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the log CONFIGURATION. */
	public static final String  LOG_SECTION                       = "log";
	/** Log level. */
	public static final String  LOG_LEVEL                         = LOG_SECTION + '.' + "level";
	/** Default log level. */
	public static final String  DEFAULT_LOG_LEVEL                 = "WARNING";
	/** Log buffer size, in number of messages. */
	public static final String  LOG_BUFFER_SIZE                   = LOG_SECTION + '.' + "buffer_size";
	/** Default log buffer size. Should be set to a low value to minimize memory usage, yet high enough to have most of
	 * the recent log messages. */
	public static final int     DEFAULT_LOG_BUFFER_SIZE           = 200;


	// - Shell variables -----------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the shell CONFIGURATION. */
	public static final String  SHELL_SECTION                     = "shell";
	/** Shell invocation command (in case muCommander is not using the default one). */
	public static final String  CUSTOM_SHELL                      = SHELL_SECTION + '.' + "custom_command";
	/** Whether or not to use a custom shell invocation command. */
	public static final String  USE_CUSTOM_SHELL                  = SHELL_SECTION + '.' + "use_custom";
	/** Default custom shell behavior. */
	public static final boolean DEFAULT_USE_CUSTOM_SHELL          = false;
	/** Maximum number of items that should be present in the shell history. */
	public static final String  SHELL_HISTORY_SIZE                = SHELL_SECTION + '.' + "history_size";
	/** Default maximum shell history size. */
	public static final int     DEFAULT_SHELL_HISTORY_SIZE        = 100;
	/** Encoding used to read the shell output. */
	public static final String  SHELL_ENCODING                    = SHELL_SECTION + '.' + "encoding";
	/** Whether or not shell encoding should be auto-detected. */
	public static final String  AUTODETECT_SHELL_ENCODING         = SHELL_SECTION + '.' + "autodect_encoding";
	/** Default shell encoding auto-detection behaviour. */
	public static final boolean DEFAULT_AUTODETECT_SHELL_ENCODING = true;



	// - Mail variables ------------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing mail CONFIGURATION. */
	public static final String MAIL_SECTION                       = "mail";
	/** Address of the SMTP server that should be used when sending mails. */
	public static final String SMTP_SERVER                        = MAIL_SECTION + '.' + "smtp_server";
	/** Outgoing TCP port to the SMTP server. */
	public static final String SMTP_PORT                          = MAIL_SECTION + '.' + "smtp_port";
	/** Default outgoing TCP port to the SMTP server. */
	public static final int    DEFAULT_SMTP_PORT                   = 25;
	/** Name under which mails sent by muCommander should appear. */
	public static final String MAIL_SENDER_NAME                   = MAIL_SECTION + '.' + "sender_name";
	/** Address which mails sent by muCommander should be replied to. */
	public static final String MAIL_SENDER_ADDRESS                = MAIL_SECTION + '.' + "sender_address";



	// - Command bar variables -----------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the command bar CONFIGURATION. */
	public static final String  COMMAND_BAR_SECTION               = "command_bar";
	/** Whether or not the command bar is visible. */
	public static final String  COMMAND_BAR_VISIBLE               = COMMAND_BAR_SECTION + '.' + "visible";
	/** Default command bar visibility. */
	public static final boolean DEFAULT_COMMAND_BAR_VISIBLE       = true;
	/** Scale factor of commandbar icons. */
	public static final String  COMMAND_BAR_ICON_SCALE            = COMMAND_BAR_SECTION + '.' + "icon_scale";
	/** Default scale factor of commandbar icons. */
	public static final float   DEFAULT_COMMAND_BAR_ICON_SCALE    = 1.0f;



	// - Status bar variables ------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the status bar CONFIGURATION. */
	public static final String STATUS_BAR_SECTION                 = "status_bar";
	/** Whether or not the status bar is visible. */
	public static final String STATUS_BAR_VISIBLE                 = STATUS_BAR_SECTION + '.' + "visible";
	/** Default status bar visibility. */
	public static final boolean DEFAULT_STATUS_BAR_VISIBLE        = true;



	// - Toolbar variables ---------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the toolbar CONFIGURATION. */
	public static final String TOOLBAR_SECTION                    = "toolbar";
	/** Whether or not the toolbar is visible. */
	public static final String TOOLBAR_VISIBLE                    = TOOLBAR_SECTION + '.' + "visible";
	/** Default toolbar visibility. */
	public static final boolean DEFAULT_TOOLBAR_VISIBLE           = true;
	/** Scale factor of toolbar icons. */
	public static final String  TOOLBAR_ICON_SCALE                = TOOLBAR_SECTION + '.' + "icon_scale";
	/** Default scale factor of toolbar icons. */
	public static final float   DEFAULT_TOOLBAR_ICON_SCALE        = 1.0f;


	// - Volume list ---------------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the volume list CONFIGURATION. */
	public static final String VOLUME_LIST_SECTION                 = "volume_list";
	/** Regexp that allows volumes to be excluded from the list. */
	public static final String VOLUME_EXCLUDE_REGEXP               = VOLUME_LIST_SECTION + '.' + "exclude_regexp";


	// - FileTable variables ---------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the folders view CONFIGURATION. */
	public static final String  FILE_TABLE_SECTION                 = "file_table";
	/** Identifier of the left file table. */
	public static final String  LEFT                               = "left";
	/** Identifier of the right file table. */
	public static final String  RIGHT                              = "right";
	/** Whether or not to display hidden files. */
	public static final String  SHOW_HIDDEN_FILES                  = FILE_TABLE_SECTION + '.' + "show_hidden_files";
	/** Default hidden files visibility. */
	public static final boolean DEFAULT_SHOW_HIDDEN_FILES          = true;
	/** Whether or not to display OS X .DS_Store files. */
	public static final String  SHOW_DS_STORE_FILES                = FILE_TABLE_SECTION + '.' + "show_ds_store_files";
	/** Default .DS_Store files visibility. */
	public static final boolean DEFAULT_SHOW_DS_STORE_FILES        = true;
	/** Whether or not to display system folders. */
	public static final String  SHOW_SYSTEM_FOLDERS                = FILE_TABLE_SECTION + '.' + "show_system_folders";
	/** Default system folders visibility. */
	public static final boolean DEFAULT_SHOW_SYSTEM_FOLDERS        = true;
	/** Scale factor of file table icons. */
	public static final String  TABLE_ICON_SCALE                   = FILE_TABLE_SECTION + '.' + "icon_scale";
	/** Default scale factor of file table icons. */
	public static final float   DEFAULT_TABLE_ICON_SCALE           = 1.0f;
	/** Whether or not columns should resize themselves automatically. */
	public static final String  AUTO_SIZE_COLUMNS                  = FILE_TABLE_SECTION + '.' + "auto_size_columns";
	/** Default columns auto-resizing behavior. */
	public static final boolean DEFAULT_AUTO_SIZE_COLUMNS          = true;
	/** Controls if and when system file icons should be used instead of custom file icons. */
	public static final String  USE_SYSTEM_FILE_ICONS              = FILE_TABLE_SECTION + '.' + "use_system_file_icons";
	/** Default system file icons policy. */
	public static final String  DEFAULT_USE_SYSTEM_FILE_ICONS      = FileIcons.USE_SYSTEM_ICONS_APPLICATIONS;
	/** Controls whether folders are displayed first in the FileTable or mixed with regular files. */
	public static final String  SHOW_FOLDERS_FIRST                 = FILE_TABLE_SECTION + '.' + "show_folders_first";
	/** Default value for 'Show folders first' option. */
	public static final boolean DEFAULT_SHOW_FOLDERS_FIRST         = true;
	/** Controls whether symlinks should be followed when changing directory. */
	public static final String  CD_FOLLOWS_SYMLINKS                = FILE_TABLE_SECTION + '.' + "cd_follows_symlinks";
	/** Default value for 'Follow symlinks when changing directory' option. */
	public static final boolean DEFAULT_CD_FOLLOWS_SYMLINKS        = false;
	/** Whether to always show the header of a single tab or not */
	public static final String SHOW_SINGLE_TAB_HEADER			   = FILE_TABLE_SECTION + '.' + "show_single_tab_header";
	/** Default value for 'Always show single tab header" */
	public static final boolean DEFAULT_SHOW_TAB_HEADER	   = false;

	/** Name of the root element's attribute that contains the version of muCommander used to write the CONFIGURATION file. */
	static final String VERSION_ATTRIBUTE = "version";



	// - Mac OS X variables --------------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing muCommander's Mac OS X integration. */
	public static final String  MAC_OSX_SECTION                   = "macosx";
	/** Whether or not to use the brushed metal look. */
	public static final String  USE_BRUSHED_METAL                 = MAC_OSX_SECTION + '.' + "brushed_metal_look";
	/** Default brushed metal look behavior. */
	// At the time of writing, the 'brushed metal' look causes the JVM to crash randomly under Leopard (10.5)
	// so we disable brushed metal on that OS version but leave it for earlier versions where it works fine.
	// See http://www.mucommander.com/forums/viewtopic.php?f=4&t=746 for more info about this issue.
	public static final boolean DEFAULT_USE_BRUSHED_METAL         = false;
	/** Whether or not to use a Mac OS X style menu bar. */
	public static final String  USE_SCREEN_MENU_BAR               = MAC_OSX_SECTION + '.' + "screen_menu_bar";
	/** Default menu bar type. */
	public static final boolean DEFAULT_USE_SCREEN_MENU_BAR       = true;



	// - Startup folder variables --------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing muCommander's startup folders. */
	public static final String  STARTUP_FOLDER_SECTION            = "startup_folder";
	/** Startup folder type (for the two panels) */
	public static final String  STARTUP_FOLDERS                   = STARTUP_FOLDER_SECTION + '.' + "on_startup";
	/** The custom folder should be used on startup. */
	public static final String  STARTUP_FOLDERS_CUSTOM            = "customFolders";
	/** The last visited folder should be used on startup. */
	public static final String  STARTUP_FOLDERS_LAST              = "lastFolders";
	/** Default startup folder type. */
	public static final String  DEFAULT_STARTUP_FOLDERS           = STARTUP_FOLDERS_LAST;
	/** Section describing custom folders that were set for the two panel. */
	public static final String  CUSTOM_FOLDERS_SECTION            = STARTUP_FOLDER_SECTION + '.' + "custom_folders";
	/** Path to a custom startup folders Section describing the right panel's startup folder. */
	public static final String  RIGHT_CUSTOM_FOLDER      		  = CUSTOM_FOLDERS_SECTION + '.' + RIGHT;
	/** Section describing the left panel's startup folder. */
	public static final String  LEFT_CUSTOM_FOLDER			      = CUSTOM_FOLDERS_SECTION + '.' + LEFT;
	


	// - Folder monitoring variables -----------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the automatic folder refresh behavior. */
	public static final String REFRESH_SECTION                    = "auto_refresh";
	/** Frequency at which the current folder is checked for updates, -1 to disable auto refresh. */
	public static final String REFRESH_CHECK_PERIOD               = REFRESH_SECTION + '.' + "check_period";
	/** Default folder refresh frequency. */
	public static final long   DEFAULT_REFRESH_CHECK_PERIOD       = 3000;
	/** Minimum amount of time a folder should be checked for updates after it's been refreshed. */
	public static final String WAIT_AFTER_REFRESH                 = REFRESH_SECTION + '.' + "wait_after_refresh";
	/** Default minimum amount of time between two refreshes. */
	public static final long   DEFAULT_WAIT_AFTER_REFRESH         = 10000;



	// - Progress dialog variables -------------------------------------------
	// -----------------------------------------------------------------------
	/** Section describing the behavior of the progress dialog. */
	public static final String  PROGRESS_DIALOG_SECTION           = "progress_dialog";
	/** Whether the progress dialog is expanded or not. */
	public static final String  PROGRESS_DIALOG_EXPANDED          = PROGRESS_DIALOG_SECTION + '.' + "expanded";
	/** Default progress dialog expanded state. */
	public static final boolean DEFAULT_PROGRESS_DIALOG_EXPANDED  = true;
	/** Controls whether or not the progress dialog should be closed when the job is finished. */
	public static final String PROGRESS_DIALOG_CLOSE_WHEN_FINISHED = PROGRESS_DIALOG_SECTION + '.' + "close_when_finished";
	/** Default progress dialog behavior when the job is finished. */
	public static final boolean DEFAULT_PROGRESS_DIALOG_CLOSE_WHEN_FINISHED  = true;



	// - Variables used for themes -------------------------------------------
	// -----------------------------------------------------------------------
	/** Section controlling which theme should be applied to muCommander. */
	public static final String THEME_SECTION                      = "theme";
	/** Current theme type (custom, predefined or user defined). */
	public static final String THEME_TYPE                         = THEME_SECTION + '.' + "type";
	/** Describes predefined themes. */
	public static final String THEME_PREDEFINED                   = "predefined";
	/** Describes custom themes. */
	public static final String THEME_CUSTOM                       = "custom";
	/** Describes the user theme. */
	public static final String THEME_USER                         = "user";
	/** Default theme type. */
	public static final String DEFAULT_THEME_TYPE                 = THEME_PREDEFINED;
	/** Name of the current theme. */
	public static final String THEME_NAME                         = THEME_SECTION + '.' + "path";
	/** Default current theme name. */
	public static final String DEFAULT_THEME_NAME                 = RuntimeConstants.DEFAULT_THEME;



	// - Variables used by Bonjour/Zeroconf support --------------------------
	// -----------------------------------------------------------------------
	/** Section controlling parameters related to Bonjour/Zeroconf support. */
	public static final String  BONJOUR_SECTION                   = "bonjour";
	/** Used do determine whether discovery of Bonjour services should be activated or not. */
	public static final String  ENABLE_BONJOUR_DISCOVERY          = BONJOUR_SECTION + '.' + "discovery_enabled";
	/** Default Bonjour discovery activation used on startup. */
	public static final boolean DEFAULT_ENABLE_BONJOUR_DISCOVERY  = OsFamily.MAC_OS_X.isCurrent() ? false : true;



	// - Variables used for FTP ----------------------------------------------
	// -----------------------------------------------------------------------
	/** Section containing all FTP variables. */
	public static final String FTP_SECTION                        = "ftp";
	/** Controls whether hidden files should be listed by the client (LIST -al instead of LIST -l). */
	public static final String LIST_HIDDEN_FILES                  = FTP_SECTION + '.' + "list_hidden_files";
	/** Default value for {@link #LIST_HIDDEN_FILES}. */
	public static final boolean DEFAULT_LIST_HIDDEN_FILES         = false;


	// - Variables used for SMB ----------------------------------------------
	// -----------------------------------------------------------------------
	/** Section containing all SMB variables. */
	public static final String SMB_SECTION                        = "smb";
	/** Controls the authentication protocol to use when connecting to SMB servers. */
	public static final String SMB_LM_COMPATIBILITY               = SMB_SECTION + '.' + "lm_compatibility";
	/** Default value for {@link #SMB_LM_COMPATIBILITY}. */
	public static final int DEFAULT_SMB_LM_COMPATIBILITY          = 0;
	/** Controls the authentication protocol to use when connecting to SMB servers. */
	public static final String SMB_USE_EXTENDED_SECURITY          = SMB_SECTION + '.' + "use_extended_security";
	/** Default value for {@link #SMB_USE_EXTENDED_SECURITY}. */
	public static final boolean DEFAULT_SMB_USE_EXTENDED_SECURITY = false;

	
	private static final String ROOT_ELEMENT = "preferences";

	// - Instance fields -----------------------------------------------------
	// -----------------------------------------------------------------------
	private Configuration configuration;
	
	private String configurationVersion;

	/**
	 * Prevents instantiation of this class from outside of this package.
	 */
	MuPreferences() {
		configuration = new Configuration(MuPreferencesFile.getPreferencesFile(), new VersionedXmlConfigurationReaderFactory(),
				new VersionedXmlConfigurationWriterFactory(ROOT_ELEMENT));
	}

	// - Configuration reading / writing -------------------------------------
	// -----------------------------------------------------------------------
	/**
	 * Loads the muCommander CONFIGURATION.
	 * @throws IOException            if an I/O error occurs.
	 * @throws ConfigurationException if a CONFIGURATION related error occurs.
	 */
	void read() throws IOException, ConfigurationException {
		VersionedXmlConfigurationReader reader = new VersionedXmlConfigurationReader();
		configuration.read(reader);

		// Ensure backward compatibility
		configurationVersion = reader.getVersion();
		if(configurationVersion == null || !configurationVersion.equals(RuntimeConstants.VERSION)) {
			// Rename preferences that have changed (from v0.8.5)
			configuration.renameVariable("show_hidden_files", SHOW_HIDDEN_FILES);
			configuration.renameVariable("auto_size_columns", AUTO_SIZE_COLUMNS);
			configuration.renameVariable("show_toolbar",      TOOLBAR_VISIBLE);
			configuration.renameVariable("show_status_bar",   STATUS_BAR_VISIBLE);
			configuration.renameVariable("show_command_bar",  COMMAND_BAR_VISIBLE);
		}

		// Initializes MAC OS X specific values
		if(OsFamily.MAC_OS_X.isCurrent()) {
			if(configuration.getVariable(SHELL_ENCODING) == null) {
				configuration.setVariable(SHELL_ENCODING, "UTF-8");
				configuration.setVariable(AUTODETECT_SHELL_ENCODING, false);
			}
		}
	}

	/**
	 * Saves the muCommander CONFIGURATION.
	 * @throws IOException            if an I/O error occurs.
	 * @throws ConfigurationException if a CONFIGURATION related error occurs.
	 */
	void write() throws IOException, ConfigurationException {
		if(configurationVersion != null && !configurationVersion.equals(RuntimeConstants.VERSION)) {
			// Clear the configuration before saving to drop preferences which are unused anymore
			Configuration conf = new Configuration(MuPreferencesFile.getPreferencesFile(), new VersionedXmlConfigurationReaderFactory(),
					new VersionedXmlConfigurationWriterFactory(ROOT_ELEMENT));

			for (MuPreference preference : MuPreference.values())
				conf.setVariable(preference.toString(), configuration.getVariable(preference.toString()));
			
			// Remove preferences which are not relevant if we're not using MAC
			if (!OsFamily.MAC_OS_X.isCurrent()) {
				conf.removeVariable(USE_BRUSHED_METAL);
				conf.removeVariable(USE_SCREEN_MENU_BAR);
			}

			configuration = conf;
		}

		configuration.write();
	}

	// - Configuration listening -----------------------------------------------
	// -------------------------------------------------------------------------
	/**
	 * Adds the specified object to the list of registered CONFIGURATION listeners.
	 * @param listener object to register as a CONFIGURATION listener.
	 * @see            #removeConfigurationListener(ConfigurationListener)
	 */
	void addConfigurationListener(ConfigurationListener listener) {configuration.addConfigurationListener(listener);}

	/**
	 * Removes the specified object from the list of registered CONFIGURATION listeners.
	 * @param listener object to remove from the list of registered CONFIGURATION listeners.
	 * @see            #addConfigurationListener(ConfigurationListener)
	 */
	void removeConfigurationListener(ConfigurationListener listener) {configuration.removeConfigurationListener(listener);}


	// - Configuration source --------------------------------------------------
	// -------------------------------------------------------------------------
	/**
	 * Sets the path to the CONFIGURATION file.
	 * @param  file                  path to the file that should be used for CONFIGURATION storage.
	 * @throws FileNotFoundException if the specified file is not a valid file.
	 * @see                          #getConfigurationFile()
	 */
	void setConfigurationFile(String file) throws FileNotFoundException {
		configuration.setSource(MuPreferencesFile.getPreferencesFile(file));
	}

	/**
	 * Check whether the preferences file exists
	 * @return             true if the preferences file exits, false otherwise.
	 * @throws IOException if an error occured.
	 */
	boolean isFileExists() throws IOException {
		return configuration.getSource().isExists();
	}
	
	/////////////////////////////////////
	// MuPreferencesAPI implementation //
	/////////////////////////////////////

	public boolean setVariable(MuPreference preference, String value) {
		return configuration.setVariable(preference.toString(), value);
	}

	public boolean setVariable(MuPreference preference, int value) {
		return configuration.setVariable(preference.toString(), value);
	}

	public boolean setVariable(MuPreference preference, List<String> value,
			String separator) {
		return configuration.setVariable(preference.toString(), value, separator);
	}

	public boolean setVariable(MuPreference preference, float value) {
		return configuration.setVariable(preference.toString(), value);
	}

	public boolean setVariable(MuPreference preference, boolean value) {
		return configuration.setVariable(preference.toString(), value);
	}

	public boolean setVariable(MuPreference preference, long value) {
		return configuration.setVariable(preference.toString(), value);
	}

	public boolean setVariable(MuPreference preference, double value) {
		return configuration.setVariable(preference.toString(), value);
	}

	public String getVariable(MuPreference preference) {
		return configuration.getVariable(preference.toString());
	}
	
	public String getVariable(MuPreference preference, String value) {
		return configuration.getVariable(preference.toString(), value);
	}

	public int getVariable(MuPreference preference, int value) {
		return configuration.getVariable(preference.toString(), value);
	}

	public List<String> getVariable(MuPreference preference, List<String> value, String separator) {
		return configuration.getVariable(preference.toString(), value, separator);
	}

	public float getVariable(MuPreference preference, float value) {
		return configuration.getVariable(preference.toString(), value);
	}

	public boolean getVariable(MuPreference preference, boolean value) { 
		return configuration.getVariable(preference.toString(), value);
	}

	public long getVariable(MuPreference preference, long value) {
		return configuration.getVariable(preference.toString(), value);
	}

	public double getVariable(MuPreference preference, double value) {
		return configuration.getVariable(preference.toString(), value);
	}
	
	public ValueList getListVariable(MuPreference preference, String separator) {
		return configuration.getListVariable(preference.toString(), separator);
	}
	
	public boolean getBooleanVariable(String name) {
		return configuration.getBooleanVariable(name);
	}
	
	public String  getVariable(String name) {
		return configuration.getVariable(name);
	}
	
	public boolean isVariableSet(MuPreference preference) {
		return configuration.isVariableSet(preference.toString());
	}
	
	public String removeVariable(String name) {
		return configuration.removeVariable(name);
	}
}
