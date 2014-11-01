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


package com.mucommander.ui.dialog.file;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;


/**
 * This class is an abstract dialog which allows the user to enter the destination of a transfer in a text field
 * and control some options such as the default action to perform when a file already exists in the destination, or
 * if the files should be checked for integrity.
 * <p>
 * The initial path displayed in the text field is the one returned by {@link #computeInitialPath(FileSet)}.
 * When the dialog is confirmed by the user, either by pressing the 'OK' button or the 'Enter' key, the destination
 * path is resolved and checked with {@link #isValidDestination(PathUtils.ResolvedDestination, String)}. If the
 * path is a valid destination, a job instance is created using
 * {@link #createTransferFileJob(ProgressDialog, PathUtils.ResolvedDestination, int)} and started. If it isn't,
 * the user is notified with an error message.
 * </p>
 *
 * @author Maxence Bernard
 */
public abstract class TransferDestinationDialog extends JobDialog implements ActionListener, DocumentListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransferDestinationDialog.class);
	
    protected String errorDialogTitle;
    private boolean enableTransferOptions;

    private YBoxPanel mainPanel;
    private FilePathField pathField;
    private SpinningDial spinningDial;

    private JComboBox fileExistsActionComboBox;
    private JCheckBox skipErrorsCheckBox;
    private JCheckBox verifyIntegrityCheckBox;
    private JButton okButton;

    /** Background thread that is currently being executed, <code>null</code> if there is none. */
    private Thread thread;

    // Dialog size constraints
    protected final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(360,0);
    // Dialog width should not exceed 360, height is not an issue (always the same)
    protected final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	

	
    private final static int DEFAULT_ACTIONS[] = {
        FileCollisionDialog.CANCEL_ACTION,
        FileCollisionDialog.SKIP_ACTION,
        FileCollisionDialog.OVERWRITE_ACTION,
        FileCollisionDialog.OVERWRITE_IF_OLDER_ACTION,
        FileCollisionDialog.RESUME_ACTION,
        FileCollisionDialog.RENAME_ACTION
    };

    private final static String DEFAULT_ACTIONS_TEXT[] = {
        FileCollisionDialog.CANCEL_TEXT,
        FileCollisionDialog.SKIP_TEXT,
        FileCollisionDialog.OVERWRITE_TEXT,
        FileCollisionDialog.OVERWRITE_IF_OLDER_TEXT,
        FileCollisionDialog.RESUME_TEXT,
        FileCollisionDialog.RENAME_TEXT
    };


    public TransferDestinationDialog(MainFrame mainFrame, FileSet files, String title, String labelText, String okText, String errorDialogTitle, boolean enableTransferOptions) {
        super(mainFrame, title, files);

        this.errorDialogTitle = errorDialogTitle;
        this.enableTransferOptions = enableTransferOptions;

        mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(labelText+" :"));

        // Create a path field with auto-completion capabilities
        pathField = new FilePathField();

        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(pathField, BorderLayout.CENTER);
        // Spinning dial displayed while I/O-bound operations are being performed in a separate thread
        spinningDial = new SpinningDial(false);
        borderPanel.add(new JLabel(spinningDial), BorderLayout.EAST);
        mainPanel.add(borderPanel);
        mainPanel.addSpace(10);
        pathField.getDocument().addDocumentListener(this);

        // Path field will receive initial focus
        setInitialFocusComponent(pathField);		

        if(enableTransferOptions) {
            // Combo box that allows the user to choose the default action when a file already exists in destination
            mainPanel.add(new JLabel(Translator.get("destination_dialog.file_exists_action")+" :"));
            fileExistsActionComboBox = new JComboBox();
            fileExistsActionComboBox.addItem(Translator.get("ask"));
            int nbChoices = DEFAULT_ACTIONS_TEXT.length;
            for(int i=0; i<nbChoices; i++)
                fileExistsActionComboBox.addItem(DEFAULT_ACTIONS_TEXT[i]);
            mainPanel.add(fileExistsActionComboBox);

            skipErrorsCheckBox = new JCheckBox(Translator.get("destination_dialog.skip_errors"));
            mainPanel.add(skipErrorsCheckBox);

            verifyIntegrityCheckBox = new JCheckBox(Translator.get("destination_dialog.verify_integrity"));
            mainPanel.add(verifyIntegrityCheckBox);

            mainPanel.addSpace(10);
        }

        getContentPane().add(mainPanel, BorderLayout.NORTH);

        // Create file details button and OK/cancel buttons and lay them out a single row
        JPanel fileDetailsPanel = createFileDetailsPanel();

        okButton = new JButton(okText);
        // Prevent the dialog from being validated while the initial path is being set.
        okButton.setEnabled(false);
        JButton cancelButton = new JButton(Translator.get("cancel"));

        YBoxPanel buttonsPanel = new YBoxPanel();
        buttonsPanel.add(createButtonsPanel(createFileDetailsButton(fileDetailsPanel),
                DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this)));
        buttonsPanel.add(fileDetailsPanel);

        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        // Set minimum/maximum dimension
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        // Dispose this dialog when the close window button is pressed
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Spawn a new thread that retrieves the initial path (I/O-bound) and sets the path field accordingly
                startThread(new InitialPathRetriever());
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // Interrupt any ongoing thread when the dialog has been closed, regardless of how it has been closed.
                interruptOngoingThread();
            }
        });
    }

    /**
     * Returns the main panel that contains the path field.
     *
     * @return the main panel that contains the path field.
     */
    protected YBoxPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Returns the field where the destination path has to be entered.
     *
     * @return the field where the destination path has to be entered.
     */
    protected FilePathField getPathField() {
        return pathField;
    }

    /**
     * Interrupts any ongoing thread and starts the given one. The spinning dial is set to 'animated'.
     *
     * @param thread the thread to start
     */
    private synchronized void startThread(Thread thread) {
        // Interrupt any ongoing thread
        interruptOngoingThread();

        // Spin the dial
        spinningDial.setAnimated(true);

        // Start the thread
        this.thread = thread;
        thread.start();
    }

    /**
     * Interrupts the ongoing thread if there is one, does nothing otherwise.
     */
    private synchronized void interruptOngoingThread() {
        if(thread!=null) {
            LOGGER.trace("Calling interrupt() on "+thread);
            thread.interrupt();
            // Set the current thread to null
            thread = null;
        }
    }

    /**
     * This method checks that the given resolved destination is valid. This implementation returns <code>true</code>
     * if the resolved destination is not <code>null</code> and, in case there is more than one file to process, if the
     * destination is a folder that exists. This method can safely be overridden by subclasses to change the behavior.
     * <p>
     * Returning <code>true</code> will cause the job to go ahead and be started. Returning <code>false</code> will
     * pop up an error dialog that notifies the user that the path is incorrect.
     * </p>
     * <p>
     * This method is called from a dedicated thread so that it can safely perform I/O operations without any chance
     * of locking the event thread.
     * </p>
     *
     * @param resolvedDest the resolved destination
     * @param destPath the path, as it was entered in the path field
     * @return <code>true</code> if the given resolved destination is valid
     */
	protected boolean isValidDestination(PathUtils.ResolvedDestination resolvedDest, String destPath) {
        return (resolvedDest!=null && (files.size()==1 || resolvedDest.getDestinationType()==PathUtils.ResolvedDestination.EXISTING_FOLDER));
	}

    /**
     * This method is called after the destination has been validated to start the job, with the resolved destination
     * that has been validated by {@link #isValidDestination(PathUtils.ResolvedDestination, String)}.
     *
     * @param resolvedDest the resolved destination
     */
    private void startJob(PathUtils.ResolvedDestination resolvedDest) {
        int defaultFileExistsAction;
        boolean skipErrors;
        boolean verifyIntegrity;
        if(enableTransferOptions) {
            // Retrieve default action when a file exists in destination, default choice
            // (if not specified by the user) is 'Ask'
            defaultFileExistsAction = fileExistsActionComboBox.getSelectedIndex();
            if(defaultFileExistsAction==0)
                defaultFileExistsAction = FileCollisionDialog.ASK_ACTION;
            else
                defaultFileExistsAction = DEFAULT_ACTIONS[defaultFileExistsAction-1];
            // Note: we don't remember default action on purpose: we want the user to specify it each time,
            // it would be too dangerous otherwise.

            skipErrors = skipErrorsCheckBox.isSelected();
            verifyIntegrity = verifyIntegrityCheckBox.isSelected();
        }
        else {
            defaultFileExistsAction = FileCollisionDialog.ASK_ACTION;
            skipErrors = false;
            verifyIntegrity = false;
        }

        ProgressDialog progressDialog = new ProgressDialog(mainFrame, getProgressDialogTitle());
        TransferFileJob job = createTransferFileJob(progressDialog, resolvedDest, defaultFileExistsAction);

        if(job!=null) {
            job.setAutoSkipErrors(skipErrors);
            job.setIntegrityCheckEnabled(verifyIntegrity);
            progressDialog.start(job);
        }
    }

    /**
     * Called when the path has changed while {@link InitialPathRetriever} is running.
     */
    private void textUpdated() {
        synchronized(this) {
            if(thread!=null && thread instanceof InitialPathRetriever) {
                // Interrupt InitialPathRetriever
                interruptOngoingThread();

                // Enable
                okButton.setEnabled(true);

                pathField.getDocument().removeDocumentListener(this);
            }
        }
    }


    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////

    public void insertUpdate(DocumentEvent e) {
        textUpdated();
    }

    public void removeUpdate(DocumentEvent e) {
        textUpdated();
    }

    public void changedUpdate(DocumentEvent e) {
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == okButton) {
            // Disable the OK button and path field while the current path is being resolved
            okButton.setEnabled(false);
            pathField.setEnabled(false);

            // Start resolving the path
            startThread(new PathResolver());
        }
        else {              // Cancel button
            dispose();
        }
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Called when the dialog has just been created to compute the initial path, based on the user file selection.
     *
     * <p>This method is called from a dedicated thread so that it can safely perform I/O operations without any chance
     * of locking the event thread.</p>
     *
     * @param files files that were selected/marked by the user
     * @return a {@link PathFieldContent} containing the initial path to set in the path field
     */
    protected abstract PathFieldContent computeInitialPath(FileSet files);

    /**
     * Called after the dialog has been confirmed by the user and the resolved destination has been
     * {@link #isValidDestination(PathUtils.ResolvedDestination, String) validated} to create the
     * {@link TransferFileJob} instance that will subsequently be started.
     *
     * <p>This method is called from a dedicated thread so that it can safely perform I/O operations without any chance
     * of locking the event thread.</p>
     *
     * @param progressDialog the progress dialog that will show the job's progression
     * @param resolvedDest the resolved and validated destination
     * @param defaultFileExistsAction the value of the 'default action when file exists' choice
     * @return the {@link TransferFileJob} instance that will subsequently be started
     */
    protected abstract TransferFileJob createTransferFileJob(ProgressDialog progressDialog, PathUtils.ResolvedDestination resolvedDest, int defaultFileExistsAction);

    /**
     * Returns the title to be used in the progress dialog.
     *
     * @return the title to be used in the progress dialog.
     */
    protected abstract String getProgressDialogTitle();


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Retrieves the initial path to be set in the path field by calling {@link TransferDestinationDialog#computeInitialPath(FileSet)}.
     * Since this operation can be I/O-bound, it is performed in a separate thread.
     */
    private class InitialPathRetriever extends Thread {

        /** True if the thread has been interrupted */
        private boolean interrupted;

        @Override
        public void run() {
            final PathFieldContent pathFieldContent = computeInitialPath(files);

            // Perform UI tasks in the AWT event thread
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        spinningDial.setAnimated(false);

                        if(!interrupted) {
                            // Document change events are no longer needed
                            pathField.getDocument().removeDocumentListener(TransferDestinationDialog.this);

                            // Set the path field's text and selection 
                            pathFieldContent.feedToPathField(pathField);

                            okButton.setEnabled(true);
                        }
                    }
                });
            }
            catch(InterruptedException e) {
                LOGGER.trace("Interrupted", e);
            }
            catch(InvocationTargetException e) {
                LOGGER.debug("Caught exception", e);
            }

            // Set the current thread to null
            synchronized(TransferDestinationDialog.this) {
                if(thread==this)        // This thread may have been interrupted already
                    thread = null;
            }
        }

        /**
         * Overridden to trap interruptions ({@link #isInterrupted()} doesn't seem to be working as advertised).
         */
        @Override
        public void interrupt() {
            super.interrupt();
            this.interrupted = true;
        }
    }

    /**
     * Resolves the path entered in the path field into a {@link PathUtils.ResolvedDestination} instance and validates
     * it using {@link TransferDestinationDialog#isValidDestination(PathUtils.ResolvedDestination, String)}.
     * Since both of those operations can be I/O-bound, they are performed in a separate thread.
     * <p>
     * If the destination is valid, the job is started using {@link TransferDestinationDialog#startJob(PathUtils.ResolvedDestination)}
     * and this dialog is disposed. Otherwise, a error dialog is displayed to notify the user that the path he has
     * entered is invalid and invite him to try again.
     * </p>
     */
    private class PathResolver extends Thread {

        /** True if the thread has been interrupted */
        private boolean interrupted;

        @Override
        public void run() {
            spinningDial.setAnimated(false);

            final String destPath = pathField.getText();
            // Resolves destination folder (I/O bound)
            final PathUtils.ResolvedDestination resolvedDest = PathUtils.resolveDestination(destPath, mainFrame.getActivePanel().getCurrentFolder());
            // Resolves destination folder (I/O bound)
            final boolean isValid = isValidDestination(resolvedDest, destPath);

            // Perform UI tasks in the AWT event thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(interrupted) {
                        dispose();
                    }
                    else if(isValid) {
                        dispose();
                        startJob(resolvedDest);
                    }
                    else {
                        showErrorDialog(Translator.get("invalid_path", destPath), errorDialogTitle);
                        // Re-enable the OK button and path field so that a new path can be entered
                        okButton.setEnabled(true);
                        pathField.setEnabled(true);
                    }
                }
            });

            // Set the current thread to null
            synchronized(TransferDestinationDialog.this) {
                if(thread==this)        // This thread may have been interrupted already
                    thread = null;
            }
        }

        /**
         * Overridden to trap interruptions ({@link #isInterrupted()} doesn't seem to be working as advertised).
         */
        @Override
        public void interrupt() {
            super.interrupt();
            this.interrupted = true;
        }
    }
}
