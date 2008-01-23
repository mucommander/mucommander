/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

/**
 * MuteProxyAction is an implementation of {@link ProxyAction} where {@link #actionPerformed(java.awt.event.ActionEvent)}
 * does nothing. 
 *
 * @author Maxence Bernard
 */
public class MuteProxyAction extends ProxyAction {

    public MuteProxyAction(Action proxiedAction) {
        super(proxiedAction);
    }

    /**
     * This method is a No-op, i.e. does absolutely nothing. 
     */
    public void actionPerformed(ActionEvent actionEvent) {
    }
}
