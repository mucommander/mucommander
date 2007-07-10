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

package com.mucommander.ui.action;

import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Brings all MainFrame windows to front, from the last window index to the first, except for the current
 * (or last active) MainFrame which is brought to the front last. .
 * After this action has been performed, minimized windows will return to a normal state and windows will be stacked
 * in the following order:
 * <ul>
 *  <li>Current MainFrame
 *  <li>MainFrame #1
 *  <li>MainFrame #2
 *  <li>...
 *  <li>MainFrame #N
 * </ul>
 *
 * @author Maxence Bernard
 */
public class BringAllToFrontAction extends MucoAction {

    public BringAllToFrontAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        Vector mainFrames = WindowManager.getMainFrames();
        MainFrame currentMainFrame = WindowManager.getCurrentMainFrame();

        int nbMainFrames = mainFrames.size();
        MainFrame mainFrame;
        for(int i=nbMainFrames-1; i>=0; i--) {
            mainFrame = (MainFrame)mainFrames.elementAt(i);
            if(mainFrame!=currentMainFrame) {
                mainFrame.toFront();
            }
        }

        currentMainFrame.toFront();
    }
}
