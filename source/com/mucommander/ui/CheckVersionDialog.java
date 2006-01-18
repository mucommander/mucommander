
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.text.Translator;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.VersionChecker;
import com.mucommander.PlatformManager;
import com.mucommander.Debug;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * This class takes care of retrieving information about the latest muCommander
 * version from a remote server and displaying the result to the end user.
 *
 * @author Maxence Bernard
 */
public class CheckVersionDialog extends QuestionDialog implements Runnable {

	/** Parent MainFrame instance */
    private MainFrame mainFrame;

	/** true if the user manually clicked on the 'Check for updates' menu item,
	 * false if the update check was automatically triggered on startup */
    private boolean userInitiated;

	/** Dialog's width has to be at least 240 */
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	

	private final static int OK_ACTION = 0;
	private final static int DOWNLOAD_ACTION = 1;
	
	
	/**
     * Checks for updates and notifies the user of the outcome. The check itself is performed in a separate thread
	 * to prevent the app from waiting for the request's result.
     *
     * @param userInitiated true if the user manually clicked on the 'Check for updates' menu item,
	 * false if the update check was automatically triggered on startup. If the check was automatically triggered,
	 * the user won't be notified if there is no new version (current version is the latest).
     */
    public CheckVersionDialog(MainFrame mainFrame, boolean userInitiated) {
		super(mainFrame, "", mainFrame);
		this.mainFrame = mainFrame;
        this.userInitiated = userInitiated;

        // Do all the hard work in a separate thread
        new Thread(this, "com.mucommander.ui.CheckVersionDialog's Thread").start();
    }
	
    
	/**
	 * Checks for updates and notifies the user of the outcome.
	 */
    public void run() {    
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
        
        String text;
        String title;
		String downloadURL = null;
        boolean downloadOption = false;
        try {
            if(Debug.ON) Debug.trace("Checking for new version...");            
            
            // Retrieves version information
            VersionChecker.getVersionInformation();
            
            // A newer version is available
            if(VersionChecker.newVersionAvailable()) {
                if(Debug.ON) Debug.trace("A new version is available!");            

                title = Translator.get("version_dialog.new_version_title");

                // Checks if the current platform can open a new browser window
                downloadOption = PlatformManager.canOpenURL();
                downloadURL = VersionChecker.getDownloadURL();
                
                // If the platform is not capable of opening a new browser window,
                // display the download URL.
                if(downloadOption) {
					text = Translator.get("version_dialog.new_version");
				}
				else {
					text = Translator.get("version_dialog.new_version_url", downloadURL);
				}
			}
            // We're already running latest version
			else {
                if(Debug.ON) Debug.trace("No new version.");            

                // If the version check was not iniated by the user (i.e. was automatic),
                // we do not need to inform the user that he already has the latest version
                if(!userInitiated) {
                    dispose();
					return;
				}
                
				title = Translator.get("version_dialog.no_new_version_title");
                text = Translator.get("version_dialog.no_new_version");
			}
        }
        // Check failed
        catch(Exception e) {
            // If the version check was not iniated by the user (i.e. was automatic),
            // we do not need to inform the user that the check failed
            if(!userInitiated) {
                dispose();
				return;
			}

            title = Translator.get("version_dialog.not_available_title");
            text = Translator.get("version_dialog.not_available");
		}

		// Set title
        setTitle(title);
        
		String okText = Translator.get("ok");
		init(mainFrame, new JLabel(text), 
			downloadOption?new String[]{Translator.get("version_dialog.download"), okText}:new String[]{okText},
			downloadOption?new int[]{DOWNLOAD_ACTION, OK_ACTION}:new int[]{OK_ACTION},
			0);
			
		JCheckBox showNextTimeCheckBox = new JCheckBox(Translator.get("prefs_dialog.check_for_updates_on_startup"), ConfigurationManager.getVariable("prefs.check_for_updates_on_startup", "true").equals("true"));
		addCheckBox(showNextTimeCheckBox);

		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		
		// Show dialog and get user action
		if(getActionValue()==DOWNLOAD_ACTION)
			PlatformManager.open(downloadURL, mainFrame.getLastActiveTable().getCurrentFolder());	// Open URL in browser
		
		// Remember user preference
		ConfigurationManager.setVariable("prefs.check_for_updates_on_startup", ""+showNextTimeCheckBox.isSelected());
	}
}