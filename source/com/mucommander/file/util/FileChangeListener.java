/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;

/**
 * Interface to be implemented by classes that wish to be notified when changes are made to files monitored by
 * {@link FileMonitor}.
 *
 * <p>FileChangeListener instances must register themselves with FileMonitor using
 * {@link FileMonitor#addFileChangeListener(FileChangeListener)}, in order for {@link #fileChanged(AbstractFile, int)}
 * to be called whenever a file monitored by a FileMonitor has changed.
 *
 * @see FileMonitor
 * @author Maxence Bernard
 */
public interface FileChangeListener extends FileMonitorConstants {

    /**
     * This method is called whenever a change in one or several attributes of the given file has changed. The
     * <code>changedAttributes</code> parameter may contain several attributes, use the binary AND operator with
     * {@link FileMonitor} constant attribute fields to read them.
     *
     * @param file the AbstractFile for which an attribute change has been detected
     * @param changedAttributes a set of attributes that have changed, see FileMonitor constant fields for possible values 
     */
    public void fileChanged(AbstractFile file, int changedAttributes);
}