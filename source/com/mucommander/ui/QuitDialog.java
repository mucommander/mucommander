
package com.mucommander.ui;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.text.Translator;
import com.mucommander.conf.ConfigurationManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class QuitDialog extends FocusDialog implements ActionListener {

	private JCheckBox showNextTimeCheckBox;
	private JButton quitButton;
	
	// Dialog's width has to be at least 240
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	

	
	public QuitDialog(MainFrame mainFrame) {
		super(mainFrame, Translator.get("quit_dialog.title"), mainFrame);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// Text label and 'show next time' checkbox
		YBoxPanel mainPanel = new YBoxPanel(5);
        mainPanel.add(new JLabel(Translator.get("quit_dialog.desc")));
		mainPanel.addSpace(5);
		this.showNextTimeCheckBox = new JCheckBox(Translator.get("quit_dialog.show_next_time"), true);
		// Insert a 10-pixel gap before checkbox
		mainPanel.add(showNextTimeCheckBox, 10);
		mainPanel.addSpace(10);
		contentPane.add(mainPanel, BorderLayout.CENTER);

		// Quit and cancel buttons
		this.quitButton = new JButton(Translator.get("quit_dialog.quit"));
		JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createButtonPanel(new JButton[]{quitButton, cancelButton}, this), BorderLayout.SOUTH);

		// Selects Quit when enter is pressed
		getRootPane().setDefaultButton(quitButton);
//		FocusRequester.requestFocus(quitButton);
		
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setResizable(false);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		dispose();

		if(source==quitButton) {
			// Remember user preference
			ConfigurationManager.setVariable("prefs.quit_confirmation", ""+showNextTimeCheckBox.isSelected());

			// Quit
			WindowManager.getInstance().quit();
		}
	}
	
}
