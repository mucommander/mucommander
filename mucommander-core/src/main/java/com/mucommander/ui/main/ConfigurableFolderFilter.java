/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.ui.main;

import java.util.function.Consumer;

import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.filter.AndFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.main.tree.FoldersTreePanel;

/**
 * Filters out files that are unwanted when displaying a folder, based on user preferences.
 * <p>
 * This class is used for displaying the {@link FolderPanel} main folder panel and the
 * {@link FoldersTreePanel folder tree view}.
 * </p>
 *
 * @author Maxence Bernard, Mariusz Jakubowski
 */
public class ConfigurableFolderFilter extends AndFileFilter implements ConfigurationListener {
    
    private FileFilter hiddenFileFilter = new AttributeFileFilter(FileAttribute.HIDDEN, true);
    private FileFilter dsFileFilter = new DSStoreFileFilter();
    /** Filter used to filter out system files and folders that should not be displayed to inexperienced users. */
    private FileFilter systemFileFilter = new AttributeFileFilter(FileAttribute.SYSTEM, true);
    

    public ConfigurableFolderFilter() {
        configureFilters();
        MuConfigurations.addPreferencesListener(this);
    }

    private void configureFilters() {
        // Filters out hidden files, null when 'show hidden files' option is enabled
        if(!MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES)) {
            // This filter is inverted and matches non-hidden files            
            addFileFilter(hiddenFileFilter);
        }

        // Filters out Mac OS X .DS_Store files, null when 'show DS_Store files' option is enabled
        if(!MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_DS_STORE_FILES, MuPreferences.DEFAULT_SHOW_DS_STORE_FILES))
            addFileFilter(dsFileFilter);

        /** Filters out Mac OS X system folders, null when 'show system folders' option is enabled */
        if(!MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SYSTEM_FOLDERS, MuPreferences.DEFAULT_SHOW_SYSTEM_FOLDERS))
            addFileFilter(systemFileFilter);
    }


    //////////////////////////////////////////
    // ConfigurationListener implementation //
    //////////////////////////////////////////

    /**
     * Adds or removes filters based on configuration changes.
     */
    public void configurationChanged(ConfigurationEvent event) {
        FileFilter fileFilter = null;
        switch(event.getVariable()) {
        // Show or hide hidden files
        case MuPreferences.SHOW_HIDDEN_FILES:
            fileFilter = hiddenFileFilter;
            break;
            // Show or hide .DS_Store files (macOS option)
        case MuPreferences.SHOW_DS_STORE_FILES:
            fileFilter = dsFileFilter;
            break;
            // Show or hide system folders (macOS option)
        case MuPreferences.SHOW_SYSTEM_FOLDERS:
            fileFilter = systemFileFilter;
            break;
        }
        if (fileFilter != null) {
            Consumer<FileFilter> func = event.getBooleanValue() ? this::removeFileFilter : this::addFileFilter;
            func.accept(fileFilter);
        }
    }
}
