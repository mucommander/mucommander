
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

		if(getActionValue()==QUIT_ACTION) {
			// Remember user preference
			ConfigurationManager.setVariableBoolean("prefs.quit_confirmation", showNextTimeCheckBox.isSelected());

			// Quit
			WindowManager.getInstance().quit();
		}
	}
}
