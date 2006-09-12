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
     * Registers a LocationListener to receive LocationEvents whenever the current folder
     * of this FolderPanel has or is being changed.
     *
     * <p>Listeners are stored as weak references so {@link #removeLocationListener(LocationListener) removeLocationListener()}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     */
    public synchronized void addLocationListener(LocationListener listener) {
        locationListeners.put(listener, null);
    }

    /**
     * Unsubscribes the LocationListener as to not receive LocationEvents anymore.
     */
    public synchronized void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that current folder is being changed on this FolderPanel.
     */
    public synchronized void fireLocationChanging() {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationChanging(new LocationEvent(folderPanel));
    }

    /**
     * Notifies all registered listeners that current folder has changed on this FolderPanel.
     */
    public synchronized void fireLocationChanged() {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationChanged(new LocationEvent(folderPanel));
    }

    /**
     * Notifies all registered listeners that folder change has been cancelled.
     */
    public synchronized void fireLocationCancelled() {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationCancelled(new LocationEvent(folderPanel));
    }

}
