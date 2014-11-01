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

package com.mucommander.ui.action;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AWTActionProxy acts as a proxy between a given <code>java.awt.event.ActionListener</code> and
 * <code>java.swing.Action</code>, transferring received action events to the Action's <code>actionPerformed</code>
 * method.
 * This class provides an easy way to use <code>java.swing.Action</code> instances in AWT components.
 *
 * <p>
 * Usage: after creating an <code>AWTActionProxy</code> instance, the <code>addActionListener</oode> method must be
 * called on the AWT component which action events are to be proxied, using the <code>AWTActionProxy</code> instance as
 * a parameter.
 * </p>
 *
 * @author Maxence Bernard
 */
public class AWTActionProxy implements ActionListener {

    /** Proxied Action */
    private Action proxiedAction;

    /**
     * Creates a new AWTActionProxy instance that will transfer ActionEvents caught by {@link #actionPerformed(java.awt.event.ActionEvent)}
     * to the specified <code>Action</code>.
     *
     * @param action the Action instance to transfer the ActionEvents to.
     */
    public AWTActionProxy(Action action) {
        this.proxiedAction = action;
    }

    /**
     * Returns the <code>Action</code> instance to which the ActionEvents received by {@link #actionPerformed(java.awt.event.ActionEvent)}
     * are transferred. 
     */
    public Action getProxiedAction() {
        return proxiedAction;
    }

    /**
     * Forwards the specified ActionEvent to the proxied Action.
     */
    public void actionPerformed(ActionEvent actionEvent) {
        proxiedAction.actionPerformed(actionEvent);
    }
}
