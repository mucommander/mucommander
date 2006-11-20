package com.mucommander.ui.event;

import com.mucommander.ui.FolderPanel;

import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * @author Maxence Bernard
 */
public class LocationManager {

    /** Contains all registered location listeners, stored as weak references */
    private WeakHashMap locationListeners = new WeakHashMap();

    /** The FolderPanel instance this LocationManager manages location events for */
    private FolderPanel folderPanel;


    /**
     * Creates a new LocationManager that manages location events listeners and broadcasts for the specified FolderPanel.
     *
     * @param folderPanel the FolderPanel instance this LocationManager manages location events for
     */
    public LocationManager(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
    }


    /**
     * Registers a LocationListener to receive notifications whenever the current folder of the associated FolderPanel
     * has or is being changed.
     *
     * <p>Listeners are stored as weak references so {@link #removeLocationListener(LocationListener) removeLocationListener()}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the LocationListener to register
     */
    public synchronized void addLocationListener(LocationListener listener) {
        locationListeners.put(listener, null);
    }

    /**
     * Removes the LocationListener from the list of listeners that notifications when the current folder of the
     * associated FolderPanel has or is being changed.
     *
     * @param listener the LocationListener to remove
     */
    public synchronized void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the current folder is being changed on the associated FolderPanel.
     *
     * @param folderPath path to the folder that will become the new location if the folder change is succesfull
     */
    public synchronized void fireLocationChanging(String folderPath) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationChanging(new LocationEvent(folderPanel, folderPath));
    }

    /**
     * Notifies all registered listeners that the current folder has changed on associated FolderPanel.
     *
     * @param folderPath path to the new current folder in the associated FolderPanel
     */
    public synchronized void fireLocationChanged(String folderPath) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationChanged(new LocationEvent(folderPanel, folderPath));
    }

    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(String)}
     * has been cancelled by the user.
     *
     * @param folderPath path to the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationCancelled(String folderPath) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationCancelled(new LocationEvent(folderPanel, folderPath));
    }


    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(String)}
     * could not be changed, as a result of the folder not existing or failing to list its contents.
     *
     * @param folderPath path to the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationFailed(String folderPath) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationFailed(new LocationEvent(folderPanel, folderPath));
    }
}
