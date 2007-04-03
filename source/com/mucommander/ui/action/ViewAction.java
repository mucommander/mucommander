package com.mucommander.ui.action;

import com.mucommander.file.*;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.text.Translator;
import com.mucommander.job.TempOpenWithJob;
import com.mucommander.ui.MainFrame;
import com.mucommander.conf.*;
import com.mucommander.command.*;
import com.mucommander.process.*;

import java.util.Hashtable;

/**
 * Customisable version of {@link InternalViewAction}.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class ViewAction extends InternalViewAction implements ConfigurationListener {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Custom viewer defined in the configuration. */
    private Command customViewer;
    /** Whether or not to use the custom viewer. */
    private boolean useCustomViewer;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ViewAction</code>.
     */
    public ViewAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Initialises configuration.
        useCustomViewer = ConfigurationManager.getVariableBoolean(ConfigurationVariables.USE_CUSTOM_VIEWER, ConfigurationVariables.DEFAULT_USE_CUSTOM_VIEWER);
        setCustomViewer(ConfigurationManager.getVariable(ConfigurationVariables.CUSTOM_VIEWER));
    }



    // - Action execution ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Views the currently selected file.
     */
    public synchronized void performAction() {
        // If we're using a custom viewer...
        if(useCustomViewer) {
            AbstractFile file;

            file = mainFrame.getActiveTable().getSelectedFile();
            // If the file is viewable...
            if(file != null && !(file.isDirectory() || file.isSymlink())) {
                // If it's local, run the custom viewer on it.
                if(file.getURL().getProtocol().equals(FileProtocols.FILE) && (file instanceof LocalFile)) {
                    try {ProcessRunner.execute(customViewer.getTokens(file), file);}
                    catch(Exception e) {}
                }
                // If it's distant, copies it locally before running the custom viewer on it.
                else {
                    ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                    TempOpenWithJob job = new TempOpenWithJob(progressDialog, mainFrame, file, FileFactory.getTemporaryFile(file.getName(), true), customViewer);
                    progressDialog.start(job);
                }
            }
        }
        // If we're not using a custom viewer, this action behaves exactly like its parent.
        else
            super.performAction();
    }



    // - Configuration management --------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Reacts to configuration changed events.
     * @param event describes the configuration change.
     */
    public synchronized boolean configurationChanged(ConfigurationEvent event) {
        // Updates useCustomViewer.
        if(event.getVariable().equals(ConfigurationVariables.USE_CUSTOM_VIEWER))
            useCustomViewer = event.getBooleanValue();
        // Updates customViewer.
        else if(event.getVariable().equals(ConfigurationVariables.CUSTOM_VIEWER))
            setCustomViewer(event.getValue());
        return true;
    }

    /**
     * Sets the custom viewer to the specified command.
     * @param command command to use as a custom viewer.
     */
    private void setCustomViewer(String command) {
        if(command == null)
            customViewer = null;
        else
            customViewer = CommandParser.getCommand("view", command);
    }
}
