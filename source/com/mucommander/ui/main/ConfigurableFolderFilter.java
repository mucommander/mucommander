/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.filter.AndFileFilter;
import com.mucommander.file.filter.AttributeFileFilter;
import com.mucommander.file.filter.FileFilter;
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
    
    private FileFilter hiddenFileFilter = new AttributeFileFilter(AttributeFileFilter.HIDDEN, true);
    private FileFilter dsFileFilter = new DSStoreFileFilter();
    private FileFilter systemFileFilter = new SystemFileFilter();
    

    public ConfigurableFolderFilter() {
        configureFilters();
        MuConfiguration.addConfigurationListener(this);
    }

    private void configureFilters() {
        // Filters out hidden files, null when 'show hidden files' option is enabled
        if(!MuConfiguration.getVariable(MuConfiguration.SHOW_HIDDEN_FILES, MuConfiguration.DEFAULT_SHOW_HIDDEN_FILES)) {
            // This filter is inverted and matches non-hidden files            
            addFileFilter(hiddenFileFilter);
        }

        // Filters out Mac OS X .DS_Store files, null when 'show DS_Store files' option is enabled
        if(!MuConfiguration.getVariable(MuConfiguration.SHOW_DS_STORE_FILES, MuConfiguration.DEFAULT_SHOW_DS_STORE_FILES))
            addFileFilter(dsFileFilter);

        /** Filters out Mac OS X system folders, null when 'show system folders' option is enabled */
        if(!MuConfiguration.getVariable(MuConfiguration.SHOW_SYSTEM_FOLDERS, MuConfiguration.DEFAULT_SHOW_SYSTEM_FOLDERS))
            addFileFilter(systemFileFilter);
    }


    //////////////////////////////////////////
    // ConfigurationListener implementation //
    //////////////////////////////////////////

    /**
     * Adds or removes filters based on configuration changes.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Show or hide hidden files
        if (var.equals(MuConfiguration.SHOW_HIDDEN_FILES)) {
            if(event.getBooleanValue())
                removeFileFilter(hiddenFileFilter);
            else
                addFileFilter(hiddenFileFilter);
        }
        // Show or hide .DS_Store files (Mac OS X option)
        else if (var.equals(MuConfiguration.SHOW_DS_STORE_FILES)) {
            if(event.getBooleanValue())
                removeFileFilter(dsFileFilter);
            else
                addFileFilter(dsFileFilter);
        }
        // Show or hide system folders (Mac OS X option)
        else if (var.equals(MuConfiguration.SHOW_SYSTEM_FOLDERS)) {
            if(event.getBooleanValue())
                removeFileFilter(systemFileFilter);
            else
                addFileFilter(systemFileFilter);
        }

    }
}
