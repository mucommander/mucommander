
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CheckVersionDialog extends FocusDialog implements ActionListener {

	// Dialog's width has to be at least 240
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	

	private MainFrame mainFrame;

	private JButton okButton;
	private JButton downloadButton;

    private final static String DOWNLOAD_URL = "http:/www.mucommander.com/#download";

	public CheckVersionDialog(MainFrame mainFrame) {
		super(mainFrame, "", mainFrame);
		this.mainFrame = mainFrame;
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
        
        String text;
        String title;
        boolean downloadOption = false;
        try {
            String version = new VersionChecker().getLatestVersion();
            if(version.equals(Launcher.VERSION)) {
                title = "No new version";
                text = "Congratulations, you already have the latest version.";
            }
            else {
                title = "New version available";
                text = "There is a new "+version+" version available for download.";
                downloadOption = PlatformManager.canOpenURL();
            }
        }
        catch(Exception e) {
            title = "No version information";
            text = "Unable to retrieve version information from server";
        }
        
        JLabel label = new JLabel(text);
        contentPane.add(label, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        JButton buttons[];
        if(downloadOption) {
            JButton downloadButton = new JButton("Download");
            buttons = new JButton[]{downloadButton, okButton};
        }
        else {
            buttons = new JButton[]{okButton};
        }

        JPanel buttonPanel = DialogToolkit.createButtonPanel(buttons, this);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

		// Selects OK when enter is pressed
		getRootPane().setDefaultButton(okButton);

		// Packs dialog
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		pack();
	}
	

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

        // Starts by disposing the dialog
        dispose();
		
		if (source==downloadButton)  {
                PlatformManager.open(DOWNLOAD_URL, mainFrame.getLastActiveTable().getCurrentFolder());
		}
	}
}