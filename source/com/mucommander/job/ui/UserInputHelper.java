/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.job.ui;

import com.mucommander.AppLogger;
import com.mucommander.job.FileJob;

import javax.swing.SwingUtilities;

/**
 * This class is used to show a dialog for user and get a response from
 * this dialog. It is used by {@link FileJob} class.
 * 
 * @author Mariusz Jakubowski
 */
public class UserInputHelper {
    private Object userInput;
    private DialogResult dialog;

    public UserInputHelper(FileJob job, DialogResult dialog) {
        this.dialog = dialog;
    }
    
    
    public Object getUserInput() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    userInput = dialog.getUserInput();
                }
            });
        } catch (Exception e) {
            AppLogger.fine("Caught exception", e);
        }
        return userInput;
    }

}
