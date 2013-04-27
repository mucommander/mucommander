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

/**
 * 
 * @author Arik Hadas
 */
public enum MuPreference {
	CHECK_FOR_UPDATE(MuPreferences.CHECK_FOR_UPDATE),
	DATE_FORMAT(MuPreferences.DATE_FORMAT),
	DATE_SEPARATOR(MuPreferences.DATE_SEPARATOR),
	TIME_FORMAT(MuPreferences.TIME_FORMAT),
	LANGUAGE(MuPreferences.LANGUAGE),
	DISPLAY_COMPACT_FILE_SIZE(MuPreferences.DISPLAY_COMPACT_FILE_SIZE),
	CONFIRM_ON_QUIT(MuPreferences.CONFIRM_ON_QUIT),
	SHOW_SPLASH_SCREEN(MuPreferences.SHOW_SPLASH_SCREEN),
	LOOK_AND_FEEL(MuPreferences.LOOK_AND_FEEL),
	CUSTOM_LOOK_AND_FEELS(MuPreferences.CUSTOM_LOOK_AND_FEELS),
	ENABLE_SYSTEM_NOTIFICATIONS(MuPreferences.ENABLE_SYSTEM_NOTIFICATIONS),
	PREFERRED_ENCODINGS(MuPreferences.PREFERRED_ENCODINGS),
	LOG_LEVEL(MuPreferences.LOG_LEVEL),
	LOG_BUFFER_SIZE(MuPreferences.LOG_BUFFER_SIZE),
	CUSTOM_SHELL(MuPreferences.CUSTOM_SHELL),
	USE_CUSTOM_SHELL(MuPreferences.USE_CUSTOM_SHELL),
	SHELL_HISTORY_SIZE(MuPreferences.SHELL_HISTORY_SIZE),
	SHELL_ENCODING(MuPreferences.SHELL_ENCODING),
	AUTODETECT_SHELL_ENCODING(MuPreferences.AUTODETECT_SHELL_ENCODING),
	SMTP_SERVER(MuPreferences.SMTP_SERVER),
	SMTP_PORT(MuPreferences.SMTP_PORT),
	MAIL_SENDER_NAME(MuPreferences.MAIL_SENDER_NAME),
	MAIL_SENDER_ADDRESS(MuPreferences.MAIL_SENDER_ADDRESS),
	COMMAND_BAR_VISIBLE(MuPreferences.COMMAND_BAR_VISIBLE),
	COMMAND_BAR_ICON_SCALE(MuPreferences.COMMAND_BAR_ICON_SCALE),
	STATUS_BAR_VISIBLE(MuPreferences.STATUS_BAR_VISIBLE),
	TOOLBAR_VISIBLE(MuPreferences.TOOLBAR_VISIBLE),
	TOOLBAR_ICON_SCALE(MuPreferences.TOOLBAR_ICON_SCALE),
	VOLUME_EXCLUDE_REGEXP(MuPreferences.VOLUME_EXCLUDE_REGEXP),
	SHOW_HIDDEN_FILES(MuPreferences.SHOW_HIDDEN_FILES),
	SHOW_DS_STORE_FILES(MuPreferences.SHOW_DS_STORE_FILES),
	SHOW_SYSTEM_FOLDERS(MuPreferences.SHOW_SYSTEM_FOLDERS),
	TABLE_ICON_SCALE(MuPreferences.TABLE_ICON_SCALE),
	AUTO_SIZE_COLUMNS(MuPreferences.AUTO_SIZE_COLUMNS),
	USE_SYSTEM_FILE_ICONS(MuPreferences.USE_SYSTEM_FILE_ICONS),
	SHOW_FOLDERS_FIRST(MuPreferences.SHOW_FOLDERS_FIRST),
	CD_FOLLOWS_SYMLINKS(MuPreferences.CD_FOLLOWS_SYMLINKS),
	USE_BRUSHED_METAL(MuPreferences.USE_BRUSHED_METAL),
	USE_SCREEN_MENU_BAR(MuPreferences.USE_SCREEN_MENU_BAR),
	STARTUP_FOLDERS(MuPreferences.STARTUP_FOLDERS),
	LEFT_CUSTOM_FOLDER(MuPreferences.LEFT_CUSTOM_FOLDER),
	RIGHT_CUSTOM_FOLDER(MuPreferences.RIGHT_CUSTOM_FOLDER),
	REFRESH_CHECK_PERIOD(MuPreferences.REFRESH_CHECK_PERIOD),
	WAIT_AFTER_REFRESH(MuPreferences.WAIT_AFTER_REFRESH),
	PROGRESS_DIALOG_EXPANDED(MuPreferences.PROGRESS_DIALOG_EXPANDED),
	PROGRESS_DIALOG_CLOSE_WHEN_FINISHED(MuPreferences.PROGRESS_DIALOG_CLOSE_WHEN_FINISHED),
	THEME_TYPE(MuPreferences.THEME_TYPE),
	THEME_NAME(MuPreferences.THEME_NAME),
	ENABLE_BONJOUR_DISCOVERY(MuPreferences.ENABLE_BONJOUR_DISCOVERY),
	LIST_HIDDEN_FILES(MuPreferences.LIST_HIDDEN_FILES),
	SMB_LM_COMPATIBILITY(MuPreferences.SMB_LM_COMPATIBILITY),
	SMB_USE_EXTENDED_SECURITY(MuPreferences.SMB_USE_EXTENDED_SECURITY),
	SHOW_TAB_HEADER(MuPreferences.SHOW_SINGLE_TAB_HEADER);
	
	private String label;
	
	private MuPreference(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
