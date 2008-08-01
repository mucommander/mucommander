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

import com.mucommander.runtime.OsFamilies;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.util.Hashtable;

/**
 * Minimizes the {@link MainFrame} this action is associated with.
 *
 * @author Maxence Bernard
 * @see com.mucommander.ui.action.MaximizeWindowAction
 */
public class MinimizeWindowAction extends MuAction {

    public MinimizeWindowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, !OsFamilies.MAC_OS_X.isCurrent());

        if(OsFamilies.MAC_OS_X.isCurrent())
            setLabel(Translator.get(MinimizeWindowAction.class.getName()+".label"+".mac_os_x"));
    }

    public void performAction() {
        mainFrame.setExtendedState(JFrame.ICONIFIED);
    }
}
