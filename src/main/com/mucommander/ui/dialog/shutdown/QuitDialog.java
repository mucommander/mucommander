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

package com.mucommander.ui.dialog.shutdown;

import javax.swing.JCheckBox;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.QuitAction;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * Quit confirmation dialog invoked when the user asked the application to quit, which gives the user a chance
 * to cancel the operatoin in case the quit shortcut was hit by mistake. 
 * 
 * <p>A checkbox allows the user to disable this confirmation dialog for the next the application is quit. It can
 * later be re-enabled in the application preferences.</p>
 *
 * @author Maxence Bernard
 */
public class QuitDialog extends QuestionDialog {

    /** True when quit confirmation button has been pressed by the user */
    private boolean quitConfirmed;
	
    private final static int QUIT_ACTION = 0;
    private final static int CANCEL_ACTION = 1;

    
    /**
     * Creates a new instance of QuitDialog, displays the dialog and waits for a user's choice. This dialog
     * doesn't quit the application when 'Quit' is confirmed, it is up to the method that invoked this dialog
     * to perform that task, only if {@link #quitConfirmed()} returns <code>true</code>.
     *
     * <p>If 'Quit' is selected and the 'Show next time' checkbox is unchecked, the preference will be saved and
     * {@link #confirmQuit()} will return <code>true</code>.
     *
     * @param mainFrame the parent MainFrame
     */
    public QuitDialog(MainFrame mainFrame) {
        super(mainFrame, 
              Translator.get("quit_dialog.title"),
              Translator.get("quit_dialog.desc", ""+WindowManager.getMainFrames().size()),
              mainFrame,
              new String[] {ActionProperties.getActionLabel(QuitAction.Descriptor.ACTION_ID), Translator.get("cancel")},
              new int[] {QUIT_ACTION, CANCEL_ACTION},
              0);
		
        JCheckBox showNextTimeCheckBox = new JCheckBox(Translator.get("quit_dialog.show_next_time"), true);
        addComponent(showNextTimeCheckBox);
		
        this.quitConfirmed = getActionValue()==QUIT_ACTION;
        if(quitConfirmed) {
            // Remember user preference
        	MuConfigurations.getPreferences().setVariable(MuPreference.CONFIRM_ON_QUIT, showNextTimeCheckBox.isSelected());
        }
    }
    
    
    /**
     * Returns <code>true</code> if the user confirmed and pressed the Quit button.
     *
     * @return <code>true</code> if the user confirmed and pressed the Quit button
     */
    public boolean quitConfirmed() {
        return quitConfirmed;
    }
    
    
    /**
     * Returns <code>true</code> if quit confirmation hasn't been disabled in the preferences, and if there is at least
     * one window to close.
     *
     * @return <code>true</code> if quit confirmation hasn't been disabled in the preferences
     */
    public static boolean confirmationRequired() {
        return  WindowManager.getMainFrames().size() > 0     // May happen after an uncaught exception in the startup sequence
             && MuConfigurations.getPreferences().getVariable(MuPreference.CONFIRM_ON_QUIT, MuPreferences.DEFAULT_CONFIRM_ON_QUIT);
    }
    
    
    /**
     * Shows up a QuitDialog asking the user for confirmation to quit, and returns <code>true</code> if user confirmed
     * the operation. The dialog will not be shown if quit confirmation has been disabled in the preferences.
     * In this case, <code>true</code> will simply be returned.
     *
     * @return <code>true</code> if user confirmed the quit operation
     */
    public static boolean confirmQuit() {
        // Show confirmation dialog only if it hasn't been disabled in the preferences
        if(confirmationRequired()) {
            QuitDialog quitDialog = new QuitDialog(WindowManager.getCurrentMainFrame());
            // Return true if user confirmed quit
            return quitDialog.quitConfirmed();
        }
        
        return true;
    }
}
