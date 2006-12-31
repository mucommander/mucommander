package com.mucommander.job;

/**
 * Interface to be implemented by classes that wish to be notified of state changes on a particular
 * {@link FileJob}. Those classes need to be registered to receive those events, this can be done by calling
 * {@link FileJob#addFileJobListener(FileJobListener)}.
 *
 * @author Maxence Bernard
 */
public interface FileJobListener {

    /**
     * Called when the state of the specified FileJob has changed.
     *
     * @param source the FileJob which state has changed
     * @param oldState the FileJob's state prior to the change, see FileJob's constant fields for possible values
     * @param newState the new FileJob's state, see FileJob's constant fields for possible values
     */
    public abstract void jobStateChanged(FileJob source, int oldState, int newState);

}
