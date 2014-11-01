/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.bonjour;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;

/**
 * Collects and maintains a list of available Bonjour/Zeroconf services using the JmDNS library.
 * Newly discovered services are added to the list and removed as they become unavailable.
 *
 * <p>Use {@link #getServices()} to get a list of currently available Bonjour services.
 *
 * @author Maxence Bernard
 * @see BonjourMenu
 */
public class BonjourDirectory implements ServiceListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(BonjourDirectory.class);
	
    /** Singleton instance held to prevent garbage collection and also used for synchronization */
    private final static BonjourDirectory instance = new BonjourDirectory();
    /** Does all the hard work */
    private static JmDNS jmDNS;
    /** List of discovered and currently active Bonjour services */
    private static List<BonjourService> services = new Vector<BonjourService>();

    /** Known Bonjour/Zeroconf service types and their corresponding protocol */
    private final static String KNOWN_SERVICE_TYPES[][] = {
        {"_http._tcp.local.", FileProtocols.HTTP},
        {"_ftp._tcp.local.", FileProtocols.FTP},
        {"_ssh._tcp.local.", FileProtocols.SFTP},
        {"_smb._tcp.local.", FileProtocols.SMB}
    };

    /** Number of milliseconds to wait for service info resolution before giving up */
    private final static int SERVICE_RESOLUTION_TIMEOUT = 10000;


    /**
     * No-arg contructor made private so that only one instance can exist.
     */
    private BonjourDirectory() {
    }


    /**
     * Enables/disables Bonjour services discovery. If currently active and false is specified, current services
     * will be lost and {@link #getServices()} will return an empty array. If currently inactive and true is specified,
     * services discovery will be immediately started but it may take a while (a few seconds at least) to
     * collect services.
     * @param enabled whether Bonjour services discovery should be enabled.
     */
    public static void setActive(boolean enabled) {
        if(enabled && jmDNS==null) {
            // Start JmDNS
            try {
                jmDNS = JmDNS.create();

                // Listens to service events for known service types
                int nbServices = KNOWN_SERVICE_TYPES.length;
                for(int i=0; i<nbServices; i++)
                    jmDNS.addServiceListener(KNOWN_SERVICE_TYPES[i][0], instance);
            }
            catch(IOException e) {
            	LOGGER.warn("Could not instantiate jmDNS, Bonjour not enabled", e);
            }
        }
        else if(!enabled && jmDNS!=null) {
            // Shutdown JmDNS
            jmDNS.close();
            services.clear();
            jmDNS = null;
        }
    }

    /**
     * Returns <code>true</code> if Bonjour services discovery is currently running.
     * @return <code>true</code> if Bonjour services discovery is currently running, <code>false</code> otherwise.
     */
    public static boolean isActive() {
        return jmDNS!=null;
    }


    /**
     * Returns all currently available Bonjour services. The returned array may be empty but never null.
     * If BonjourDirectory is not currently active ({@link #isActive()}, an empty array will be returned.
     * @return all currently available Bonjour services
     */
    public static BonjourService[] getServices() {
        BonjourService servicesArray[] = new BonjourService[services.size()];
        services.toArray(servicesArray);
        return servicesArray;
    }


    /**
     * Wraps a Bonjour service into a {@link BonjourService} object and returns it. Returns <code>null</code> if
     * the service type doesn't correspond to any of the supported protocols, or if the service URL is malformed.
     *
     * @param serviceInfo the ServiceInfo to wrap into a BonjourService
     * @return a BonjourService instance corresponding to the given ServiceInfo
     */
    private static BonjourService createBonjourService(ServiceInfo serviceInfo) {
        try {
            String type = serviceInfo.getType();
            int nbServices = KNOWN_SERVICE_TYPES.length;
            // Looks for the file protocol corresponding to the service type
            for(int i=0; i<nbServices; i++) {
                if(KNOWN_SERVICE_TYPES[i][0].equals(type)) {
                    return new BonjourService(serviceInfo.getName(), FileURL.getFileURL(serviceInfo.getURL(KNOWN_SERVICE_TYPES[i][1])), serviceInfo.getQualifiedName());
                }
            }
        }
        catch(MalformedURLException e) {
            // Null will be returned
        }

        return null;
    }


    ////////////////////////////////////
    // ServiceListener implementation //
    ////////////////////////////////////

    public void serviceAdded(final ServiceEvent serviceEvent) {
    	LOGGER.trace("name="+serviceEvent.getName()+" type="+serviceEvent.getType());
        
        // Ignore if Bonjour has been disabled
        if(!isActive())
            return;

        // Resolve service info in a separate thread, serviceResolved() will be called once service info has been resolved.
        // Not spawning a thread often leads to service info loss (serviceResolved() not called).
        new Thread() {
            @Override
            public void run() {
                jmDNS.requestServiceInfo(serviceEvent.getType(), serviceEvent.getName(), SERVICE_RESOLUTION_TIMEOUT);
            }
        }.start();
    }

    public void serviceResolved(ServiceEvent serviceEvent) {
    	LOGGER.trace("name="+serviceEvent.getName()+" type="+serviceEvent.getType()+" info="+serviceEvent.getInfo());

        // Ignore if Bonjour has been disabled
        if(!isActive())
            return;

        // Creates a new BonjourService corresponding to the new service and add it to the list of current Bonjour services
        ServiceInfo serviceInfo = serviceEvent.getInfo();
        if(serviceInfo!=null) {
            if(serviceInfo.getInetAddress() instanceof Inet6Address) {
                // IPv6 addresses not supported at this time + they seem not to be correctly handled by ServiceInfo
            	LOGGER.debug("ignoring IPv6 service");
                return;
            }

            BonjourService bs = createBonjourService(serviceInfo);
            // Synchronized to properly handle duplicate calls
            synchronized(instance) {
                if(bs!=null && !services.contains(bs)) {
                	LOGGER.debug("BonjourService "+bs+" added");
                    services.add(bs);
                }
            }
        }
    }

    public void serviceRemoved(ServiceEvent serviceEvent) {
    	LOGGER.trace("name="+serviceEvent.getName()+" type="+serviceEvent.getType());

        // Ignore if Bonjour has been disabled
        if(!isActive())
            return;

        // Looks for an existing BonjourService instance corresponding to the service being removed and removes it from
        // the list of current Bonjour services.
        // ServiceInfo should be available in JmDNS's cache.
        ServiceInfo serviceInfo = jmDNS.getServiceInfo(serviceEvent.getType(), serviceEvent.getName()); 
        if(serviceInfo!=null) {
            if(serviceInfo.getInetAddress() instanceof Inet6Address) {
                // IPv6 addresses not supported at this time + they seem not to be correctly handled by ServiceInfo
            	LOGGER.debug("ignoring IPv6 service");
                return;
            }

            BonjourService bs = createBonjourService(serviceInfo);
            // Synchronized to properly handle duplicate calls
            synchronized(instance) {
                // Note: BonjourService#equals() uses the service's fully qualified name as the discriminator.
                if(bs!=null && services.contains(bs)) {
                	LOGGER.debug("BonjourService "+bs+" removed");
                    services.remove(bs);
                }
            }
        }
    }
}
