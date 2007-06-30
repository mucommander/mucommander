/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.event;

import com.mucommander.file.FileURL;
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
     * <p>Listeners are stored as weak references so {@link #removeLocationListener(LocationListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the LocationListener to register
     */
    public synchronized void addLocationListener(LocationListener listener) {
        locationListeners.put(listener, null);
    }

    /**
     * Removes the LocationListener from the list of listeners that receive notifications when the current folder of the
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
     * @param folderURL url of the folder that will become the new location if the folder change is successful
     */
    public synchronized void fireLocationChanging(FileURL folderURL) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationChanging(new LocationEvent(folderPanel, folderURL));
    }

    /**
     * Notifies all registered listeners that the current folder has changed on associated FolderPanel.
     *
     * @param folderURL url of the new current folder in the associated FolderPanel
     */
    public synchronized void fireLocationChanged(FileURL folderURL) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationChanged(new LocationEvent(folderPanel, folderURL));
    }

    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(FileURL)}
     * has been cancelled by the user.
     *
     * @param folderURL url of the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationCancelled(FileURL folderURL) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationCancelled(new LocationEvent(folderPanel, folderURL));
    }


    /**
     * Notifies all registered listeners that the folder change as notified by {@link #fireLocationChanging(FileURL)}
     * could not be changed, as a result of the folder not existing or failing to list its contents.
     *
     * @param folderURL url of the folder for which a failed attempt was made to make it the current folder
     */
    public synchronized void fireLocationFailed(FileURL folderURL) {
        Iterator iterator = locationListeners.keySet().iterator();
        while(iterator.hasNext())
            ((LocationListener)iterator.next()).locationFailed(new LocationEvent(folderPanel, folderURL));
    }
}
