
package com.mucommander.ui;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.text.Translator;
import com.mucommander.conf.ConfigurationManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


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

    // Dialog's width has to be at least 240
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	

    private final static int QUIT_ACTION = 0;
    private final static int CANCEL_ACTION = 1;
	
    private boolean quitConfirmed;
	
    /**
     * Creates a new instance of QuitDialog, displays the dialog and waits for a user's choice. This dialog
     * doesn't quit the application when 'Quit' is confirmed, it is up to the method that invoked this dialog
     * to perform that task, only if {@link #quitConfirmed()} returns <code>true</code>.
     *
     * <p>If 'Quit' is selected and the 'Show next time' checkbox is unchecked, the preference will be saved and
     * {@link #askConfirmation()} will return <code>true</code>.
     *
     * @param mainFrame the parent MainFrame
     */
    public QuitDialog(MainFrame mainFrame) {
        super(mainFrame, 
              Translator.get("quit_dialog.title"),
              Translator.get("quit_dialog.desc"),
              mainFrame,
              new String[] {Translator.get("quit_dialog.quit"), Translator.get("cancel")},
              new int[] {QUIT_ACTION, CANCEL_ACTION},
              0);
		
        JCheckBox showNextTimeCheckBox = new JCheckBox(Translator.get("quit_dialog.show_next_time"), true);
        addCheckBox(showNextTimeCheckBox);
		
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);

        this.quitConfirmed = getActionValue()==QUIT_ACTION;
        if(quitConfirmed) {
            // Remember user preference
            ConfigurationManager.setVariableBoolean("prefs.quit_confirmation", showNextTimeCheckBox.isSelected());
        }
    }
    
    
    /**
     * Returns <code>true</code> if the user confirmed and pressed the Quit button. 
     */
    public boolean quitConfirmed() {
        return quitConfirmed;
    }
    
    
    /**
     * Shows up a QuitDialog asking the user for confirmation to quit, and returns <code>true</code> if user confirmed
     * the operation. The dialog will not be shown if it quit confirmation has been disabled in the preferences.
     * In this case, <code>true</code> will simply be returned.
     */
    public static boolean confirmQuit() {
        // Show confirmation dialog only if it hasn't been disabled in the preferences
        if(ConfigurationManager.getVariableBoolean("prefs.quit_confirmation", true)) {
            QuitDialog quitDialog = new QuitDialog(WindowManager.getCurrentMainFrame());
            // Return true if user confirmed quit
            return quitDialog.quitConfirmed();
        }
        
        return true;
    }
}
