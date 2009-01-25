/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.file.filter;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;

/**
 * Manages file filters configured on preferences dialog. 
 *
 */
public class FileFilterHelper implements ConfigurationListener {
    
    private AndFileFilter chainedFileFilter;
    
    private FileFilter hiddenFileFilter = new AttributeFileFilter(AttributeFileFilter.HIDDEN, true);
    private FileFilter dsFileFilter = new DSStoreFileFilter();
    private FileFilter systemFileFilter = new SystemFileFilter();
    

    public FileFilterHelper(AndFileFilter chainedFileFilter) {
        this.chainedFileFilter = chainedFileFilter;
        configureFileFilter();
        MuConfiguration.addConfigurationListener(this);
    }
    
    private void configureFileFilter() {
        // Filters out hidden files, null when 'show hidden files' option is enabled
        if(!MuConfiguration.getVariable(MuConfiguration.SHOW_HIDDEN_FILES, MuConfiguration.DEFAULT_SHOW_HIDDEN_FILES)) {
            // This filter is inverted and matches non-hidden files            
            chainedFileFilter.addFileFilter(hiddenFileFilter);
        }

        // Filters out Mac OS X .DS_Store files, null when 'show DS_Store files' option is enabled
        if(!MuConfiguration.getVariable(MuConfiguration.SHOW_DS_STORE_FILES, MuConfiguration.DEFAULT_SHOW_DS_STORE_FILES))
            chainedFileFilter.addFileFilter(dsFileFilter);

        /** Filters out Mac OS X system folders, null when 'show system folders' option is enabled */
        if(!MuConfiguration.getVariable(MuConfiguration.SHOW_SYSTEM_FOLDERS, MuConfiguration.DEFAULT_SHOW_SYSTEM_FOLDERS))
            chainedFileFilter.addFileFilter(systemFileFilter);
    }
    
    /**
     * Adds or removes filters based on configuration changes.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Show or hide hidden files
        if (var.equals(MuConfiguration.SHOW_HIDDEN_FILES)) {
            if(event.getBooleanValue())
                chainedFileFilter.removeFileFilter(hiddenFileFilter);
            else
                chainedFileFilter.addFileFilter(hiddenFileFilter);
        }
        // Show or hide .DS_Store files (Mac OS X option)
        else if (var.equals(MuConfiguration.SHOW_DS_STORE_FILES)) {
            if(event.getBooleanValue())
                chainedFileFilter.removeFileFilter(dsFileFilter);
            else
                chainedFileFilter.addFileFilter(dsFileFilter);
        }
        // Show or hide system folders (Mac OS X option)
        else if (var.equals(MuConfiguration.SHOW_SYSTEM_FOLDERS)) {
            if(event.getBooleanValue())
                chainedFileFilter.removeFileFilter(systemFileFilter);
            else
                chainedFileFilter.addFileFilter(systemFileFilter);
        }

    }
    

}
