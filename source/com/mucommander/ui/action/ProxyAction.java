/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.action;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * ProxyAction is a proxy for an Action instance. All Action methods are proxied except for <code>actionPerformed()</code>.
 * That means all properties of the proxied Action are preserved but the proxied Action is not performed.
 *
 * <p>ProxyAction is useful to keep the visual properties of an Action instance in a component (JButton for instance)
 * but perform a different action.
 *
 * <p>This class is abstract, leaving <code>actionPerformed()</code> unimplemented. {@link MuteProxyAction} provides an
 * implementation where <code>actionPerformed()</code> does nothing.
 * 
 * @author Maxence Bernard
 */
public abstract class ProxyAction implements Action {

    /** Proxied action */
    protected Action proxiedAction;

    
    /**
     * Creates a new ProxyAction that acts as a proxy to the provided Action instance.
     *
     * @param proxiedAction the action to proxy
     */
    public ProxyAction(Action proxiedAction) {
        this.proxiedAction = proxiedAction;
    }


    /**
     * Returns the Action instance that this ProxyAction proxies. 
     */
    public Action getProxiedAction() {
        return proxiedAction;
    }

    /////////////////////
    // Proxied methods //
    /////////////////////

    public Object getValue(String key) {
        return proxiedAction.getValue(key);
    }

    public void putValue(String key, Object value) {
        proxiedAction.putValue(key, value);
    }

    public void setEnabled(boolean b) {
        proxiedAction.setEnabled(b);
    }

    public boolean isEnabled() {
        return proxiedAction.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        proxiedAction.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        proxiedAction.removePropertyChangeListener(propertyChangeListener);
    }
}
