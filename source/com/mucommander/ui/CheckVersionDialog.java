
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * This class takes care of checking on a remote server information about the latest muCommander
 * version and displaying the result to the end user.
 *
 * @author Maxence Bernard
 */
public class CheckVersionDialog extends FocusDialog implements ActionListener, Runnable {

	// Dialog's width has to be at least 240
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	

	private MainFrame mainFrame;
    private boolean userInitiated;

	private JButton okButton;
	private JButton downloadButton;

    private String downloadURL;

	/**
     * Checks for updates and displays the result
     *
     * @param userInitiated if <code>true</code>, the user manually asked to check for updates,
     * if not then this check is performed automatically by the application. This parameter is used
     * to not display check results when
     */
    public CheckVersionDialog(MainFrame mainFrame, boolean userInitiated) {
		super(mainFrame, "", mainFrame);
		this.mainFrame = mainFrame;
        this.userInitiated = userInitiated;

        // Do all the hard work in a separate thread
        new Thread(this).start();
    }
	
    
    public void run() {    
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
        
        String text;
        String title;
        boolean downloadOption = false;
        try {
            if(Debug.ON)
                System.out.println("Checking for new version...");            
            
            // Retrieves version information
            VersionChecker.getVersionInformation();
            
            String version = VersionChecker.getLatestVersion();
            // We're already running latest version
            if(version.equals(Launcher.MUCOMMANDER_VERSION)) {
                if(Debug.ON)
                    System.out.println("No new version.");            

                // If the version check was not iniated by the user (i.e. was automatic),
                // we do not need to inform the user that he already has the latest version
                if(!userInitiated)
                    return;
                
                title = "No new version";
                text = "Congratulations, you already have the latest version.";
            }
            // A newer version is available
            else {
                if(Debug.ON)
                    System.out.println("A new version is available!");            

                title = "New version available";

                // Checks if the current platform can open a new browser window
                downloadOption = PlatformManager.canOpenURL();
                downloadURL = VersionChecker.getDownloadURL();
                
                // If the platform is not capable of opening a new browser window,
                // display the download URL.
                 text = "There is a new version of muCommander available"
                    +(downloadOption?".":" at "+downloadURL);
            }
        }
        // Check failed
        catch(Exception e) {
            // If the version check was not iniated by the user (i.e. was automatic),
            // we do not need to inform the user that the check failed
            if(!userInitiated)
                return;

            title = "Not available";
            text = "Unable to get version information from server.";
        }

        setTitle(title);
        
		// Text message
		YBoxPanel mainPanel = new YBoxPanel();
        JLabel label = new JLabel(text);
        mainPanel.add(label);
		mainPanel.addSpace(10);
		contentPane.add(mainPanel, BorderLayout.CENTER);

        // Add buttons
        JButton okButton = new JButton("OK");
        JButton buttons[];
        if(downloadOption) {
            this.downloadButton = new JButton("Download");
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
        showDialog();
    }


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

        // Starts by disposing the dialog
        dispose();

		if (source==downloadButton)  {
                PlatformManager.open(downloadURL, mainFrame.getLastActiveTable().getCurrentFolder());
		}
	}
}