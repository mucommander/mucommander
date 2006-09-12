
package com.mucommander.ui.event;


/**
 * Interface to be implemented by classes that wish to be notified of location changes on a particular
 * FolderPanel. Those classes need to be registered to receive those events, this can be done by calling
 * {@link com.mucommander.ui.FolderPanel#addLocationListener(LocationListener) FolderPanel.addLocationListener()}.
 *
 * @see com.mucommander.ui.FolderPanel
 * @author Maxence Bernard
 */
public interface LocationListener {
	
    /**
     * This method is invoked when the current folder on the given FolderPanel is being changed.
     */
    public void locationChanging(LocationEvent e);


    /**
     * This method is invoked when the current folder on the given FolderPanel has changed.
     */
    public void locationChanged(LocationEvent e);


    /**
     * This method is invoked when the current folder on the given FolderPanel has been cancelled,
     * either because of an error, or as a result of a user action.
     */
    public void locationCancelled(LocationEvent e);

}
