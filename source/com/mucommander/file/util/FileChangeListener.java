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
public interface FileChangeListener {

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