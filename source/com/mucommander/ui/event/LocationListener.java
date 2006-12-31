
package com.mucommander.ui.event;


/**
 * Interface to be implemented by classes that wish to be notified of location changes on a particular
 * FolderPanel. Those classes need to be registered to receive those events, this can be done by calling
 * {@link LocationManager#addLocationListener(LocationListener)}.
 *
 * @see com.mucommander.ui.FolderPanel
 * @author Maxence Bernard
 */
public interface LocationListener {
	
    /**
     * This method is invoked when the current folder is being changed.
     *
     * <p>A call to either {@link #locationChanged(LocationEvent)}, {@link #locationCancelled(LocationEvent)} or
     * {@link #locationFailed(LocationEvent)} will always follow to indicate the outcome of the folder change. 
     *
     * @param locationEvent describes the location change event
     */
    public void locationChanging(LocationEvent locationEvent);


    /**
     * This method is invoked when the current folder has changed.
     *
     * @param locationEvent describes the location change event
     */
    public void locationChanged(LocationEvent locationEvent);


    /**
     * This method is invoked when the current folder has been cancelled by the user.
     *
     * @param locationEvent describes the location change event
     */
    public void locationCancelled(LocationEvent locationEvent);


    /**
     * This method is invoked when the current folder could not be changed, as a result
     * of the folder not existing or failing to list its contents.
     *
     * @param locationEvent describes the location change event
     */
    public void locationFailed(LocationEvent locationEvent);

}
