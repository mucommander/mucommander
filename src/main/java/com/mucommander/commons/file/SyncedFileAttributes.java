/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file;

/**
 * SyncedFileAttributes is a FileAttributes implementation which allows attribute values to be automatically
 * updated when accessed after a certain amount of time (the 'time to live') since their last update. The update
 * is performed by the abstract {@link #updateAttributes()} method and is triggered by any of the attribute getters.
 * A typical usage for this class is for remote file systems that need to keep file attributes in sync with a server,
 * {@link #updateAttributes()} can retrieve a fresh copy of the attributes on the server.
 *
 * <p>Attributes can also be manually updated using attribute setters. The {@link #updateExpirationDate()} method allows
 * to reset the expiration date and consider the attributes as 'fresh'.</p>
 *
 * <p>An initial value for the attributes 'time to live' is specified in the constructor and can later be changed using
 * {@link #setTtl(long)}. If the 'time to live' is set to -1, attributes are no longer automatically updated, this class
 * then simply acts as {@link com.mucommander.commons.file.SimpleFileAttributes}.</p>
 *
 * @author Maxence Bernard
 */
public abstract class SyncedFileAttributes extends SimpleFileAttributes {

    /** The attributes' 'time to live', negative values disable automatic attributes updates */
    private long ttl;

    /** The attributes' expiration timestamp/date */
    private long expirationDate;

    /** True when attributes are being updated */
    private boolean isUpdating;


    /**
     * Creates a new SyncedFileAttributes using the specifies 'time to live' value.
     *
     * @param ttl the attributes' 'time to live', in milliseconds
     * @param updateAttributesNow if <code>true</code>, attributes are automatically updated
     */
    public SyncedFileAttributes(long ttl, boolean updateAttributesNow) {
        setTtl(ttl);    // also sets the expiration date

       if(updateAttributesNow)
            checkForExpiration(true);   // force attributes update
    }

    /**
     * Returns the attributes' 'time to live', i.e. the amount of time since the last update after which attributes will
     * be automatically updated when any of the getter method is called.
     *
     * @return the attributes' 'time to live', in milliseconds
     */
    public long getTtl() {
        return ttl;
    }

    /**
     * Sets the attributes' 'time to live', , i.e. the amount of time since the last update after which attributes will
     * be automatically updated when any of the getter method is called.
     * Note that setting the 'time to live' causes the expiration date to be updated with {@link #updateExpirationDate()}.
     *
     * @param ttl the attributes' 'time to live', in milliseconds
     */
    public void setTtl(long ttl) {
        this.ttl = ttl;

        // update the expiration date
        updateExpirationDate();
    }

    /**
     * Returns the attributes' expiration timestamp/date, the date after which attributes values will be automatically
     * updated when any of the getter method is called.
     *
     * @return the attributes' expiration timestamp/date
     */
    public long getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the attributes' expiration timestamp/date, the date after which attributes values will be automatically
     * updated when they are accessed using any of the getter methods.
     *
     * @param expirationDate the attributes expiration timestamp/date
     */
    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Updates the attributes' expiration date to 'now' + 'ttl' (as returned by {@link #getTtl()}.
     * This method is called after attributes have been automatically updated. It can also be called after attribute
     * values have been manually updated using the setter methods.
     */
    public void updateExpirationDate() {
        setExpirationDate(
            ttl<0
                ?Long.MAX_VALUE
                :System.currentTimeMillis()+getTtl());
    }

    /**
     * Returns <code>true</code> if attributes have expired, i.e. the {@link #getExpirationDate()} expiration date} has
     * passed, <code>false</code> if attributes are still 'fresh'. This method also returns <code>false</code> if
     * automatic attributes' update has been disabled ('time to live' set to a negative value), or if attributes are
     * currently being updated.
     *  
     * @return <code>true</code> if attributes have expired
     */
    public boolean hasExpired() {
        return ttl>=0           // prevents automatic updates if ttl is set to a negative value
            && !isUpdating()    // causes getters to return the current value while attributes are being updated
            && System.currentTimeMillis()>expirationDate;
    }

    /**
     * Returns <code>true</code> if attributes are currently being updated.
     *
     * @return <code>true</code> if attributes are currently being updated
     */
    private synchronized boolean isUpdating() {
        return isUpdating;
    }

    /**
     * Sets whether attributes are currently being updated.
     *
     * @param isUpdating <code>true</code> if attributes are currently being updated
     */
    private synchronized void setUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }

    /**
     * Checks if the attributes have expired and if they have, calls {@link #updateAttributes()} to refresh their
     * values.
     *
     * @param forceUpdate if true, attributes will systematically be updated, without checking the expiration date
     */
    protected void checkForExpiration(boolean forceUpdate) {
        if(forceUpdate || hasExpired()) {
            // After this method is called, hasExpired() returns false so that implementations of updateAttributes()
            // can query attribute getters without entering a loop of death.
            setUpdating(true);

            // Updates attribute values
            updateAttributes();

            // Update expiration date after the attribute have actually been updated, note that it may take a while
            // for remote file protocols to retrieve attributes.
            updateExpirationDate();

            // OK we're done
            setUpdating(false);
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public String getPath() {
        checkForExpiration(false);

        return super.getPath();
    }

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public boolean exists() {
        checkForExpiration(false);

        return super.exists();
    }

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public long getDate() {
        checkForExpiration(false);

        return super.getDate();
    }

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public long getSize() {
        checkForExpiration(false);

        return super.getSize();
    }

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public boolean isDirectory() {
        checkForExpiration(false);

        return super.isDirectory();
    }

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public FilePermissions getPermissions() {
        checkForExpiration(false);

        return super.getPermissions();
    }

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public String getOwner() {
        checkForExpiration(false);

        return super.getOwner();
    }

    /**
     * Overridden to trigger attributes update if the expiration date has been reached.
     */
    @Override
    public String getGroup() {
        checkForExpiration(false);

        return super.getGroup();
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Updates the attribute values. This method is automatically called when attributes are expired and one of the
     * attribute getters is called. The implementation may choose to update only certain attributes, or skip updates
     * under certain conditions.
     */
    public abstract void updateAttributes();
}
