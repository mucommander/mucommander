
package com.mucommander.ui;

import com.mucommander.job.ExtendedFileJob;
import com.mucommander.job.FileJob;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.text.DurationFormat;
import com.mucommander.ui.comp.button.ButtonChoicePanel;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.ui.comp.progress.OverlayProgressBar;
import com.mucommander.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * This dialog informs the user of the progress made by a FileJob.
 *
 * @author Maxence Bernard
 */
public class ProgressDialog extends FocusDialog implements Runnable, ActionListener {

    private JLabel infoLabel;
    private JLabel statsLabel;
    private OverlayProgressBar totalProgressBar;
    private OverlayProgressBar fileProgressBar;
    private JLabel elapsedTimeLabel;
    private JButton cancelButton;
    private JButton hideButton;

    private FileJob job;    
    private Thread repaintThread;
    /* True if the current job is a MulitipleFileJob */
    private boolean dualBar;

    // Dialog width is constrained to 320, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);	
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	

    /** How often should progress information be refreshed (in ms) */
    private final static int REFRESH_RATE = 500;

    private MainFrame mainFrame;

    private boolean firstTimeActivated = true;
	
    public ProgressDialog(MainFrame mainFrame, String title) {
        super(mainFrame, title, mainFrame);

        this.mainFrame = mainFrame;

        // Sets maximum and minimum dimensions for this dialog
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
    }
    
    
    private void initUI() {
        Container contentPane = getContentPane();

        totalProgressBar = new OverlayProgressBar();
        totalProgressBar.setAlignmentX(LEFT_ALIGNMENT);
        infoLabel = new JLabel(job.getStatusString());
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
		
        YBoxPanel tempPanel = new YBoxPanel();
        // 2 progress bars
        if (dualBar) {
            tempPanel.add(infoLabel);
            fileProgressBar = new OverlayProgressBar();
            tempPanel.add(fileProgressBar);
            tempPanel.addSpace(10);
		
            statsLabel = new JLabel(Translator.get("progress_bar.starting"));
            tempPanel.add(statsLabel);
			
            // Do not show total progress bar if there is only one file
            // (would show the exact same information as file progress bar)
            if(job.getNbFiles()>1)
                tempPanel.add(totalProgressBar);
        }	
        // Single progress bar
        else {
            tempPanel.add(infoLabel);
            tempPanel.add(totalProgressBar);
        }

        tempPanel.addSpace(10);
        elapsedTimeLabel = new JLabel(Translator.get("progress_bar.elapsed_time")+": ");
        tempPanel.add(elapsedTimeLabel);

        tempPanel.add(Box.createVerticalGlue());
        contentPane.add(tempPanel, BorderLayout.CENTER);
        
        cancelButton = new JButton(Translator.get("cancel"));
        cancelButton.addActionListener(this);
        hideButton = new JButton(Translator.get("progress_bar.hide"));
        hideButton.addActionListener(this);
        // Cancel button receives initial focus
        setInitialFocusComponent(cancelButton);
        // Enter triggers cancel button
        getRootPane().setDefaultButton(cancelButton);
        contentPane.add(new ButtonChoicePanel(new JButton[] {cancelButton, hideButton}, 0, getRootPane()), BorderLayout.SOUTH);
    }


    public void start(FileJob job) {
        this.job = job;
        this.dualBar = job instanceof ExtendedFileJob;
        initUI();
        
        repaintThread = new Thread(this, "com.mucommander.ui.ProgressDialog's Thread");
        repaintThread.start();

    	showDialog();
    }
    

    //////////////////////
    // Runnable methods //
    //////////////////////

    public void run() {
        // Used for dual progress bars only
        ExtendedFileJob extendedJob = null;
        if(dualBar)
            extendedJob = (ExtendedFileJob)job;

        String progressText;
        while(repaintThread!=null && !job.hasFinished()) {
            // Do not refresh progress information is job is paused, simply sleep
            if(!job.isPaused()) {
                long currentFileRemainingTime = 0;
                long totalRemainingTime;

                long effectiveJobTime = job.getEffectiveJobTime();
                if(effectiveJobTime==0)
                    effectiveJobTime = 1;   // To avoid potential zero divisions

                if (dualBar) {
                    long bytesTotal = extendedJob.getTotalByteCounter().getByteCount();
                    long bytesPerSec = (long)(bytesTotal/(((float)effectiveJobTime)/1000));

                    // Update current file progress bar
                    float filePercentFloat = extendedJob.getFilePercentDone();
                    int filePercentInt = (int)(100*filePercentFloat);
                    fileProgressBar.setValue(filePercentInt);

                    progressText = filePercentInt+"% - ";

                    // Add estimated remaining time (ETA) for current file
                    long currentFileSize = extendedJob.getCurrentFileSize();
                    // If current file size is not available, ETA cannot be calculated
                    if(currentFileSize==-1)
                        progressText += "?";
                    // Avoid potential divisions by zero
                    else if(bytesPerSec==0) {
                        currentFileRemainingTime = -1;
                        progressText += DurationFormat.getInfiniteSymbol();
                    }
                    else {
                        currentFileRemainingTime = (long)((1000*(currentFileSize-extendedJob.getCurrentFileByteCounter().getByteCount()))/(float)bytesPerSec);
                        progressText += DurationFormat.format(currentFileRemainingTime);
                    }

                    fileProgressBar.setTextOverlay(progressText);

                    // Update stats label
                    statsLabel.setText(
                       Translator.get("progress_bar.transferred",
                                      SizeFormat.format(bytesTotal, SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_LONG| SizeFormat.ROUND_TO_KB),
                                      SizeFormat.format(bytesPerSec, SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT| SizeFormat.ROUND_TO_KB))
                    );
                }

                // Update total progress bar
                // Total job percent is based on the *number* of files remaining, not their actual size.
                // So this is very approximate.
                float totalPercentFloat = job.getTotalPercentDone();
                int totalPercentInt = (int)(100*totalPercentFloat);

                totalProgressBar.setValue(totalPercentInt);

                progressText = totalPercentInt+"% - ";

                // Add a rough estimate of the total remaining time (ETA):
                // total remaining time is based on the total job percent completed which itself is based on the *number*
                // of files remaining, not their actual size. So this is very approximate.

                // Avoid potential divisions by zero
                if(totalPercentFloat==0)
                    progressText += "?";
                else {
                    // Make sure that total ETA is never smaller than current file ETA
                    totalRemainingTime = (long)((1-totalPercentFloat)*(effectiveJobTime/totalPercentFloat));
                    totalRemainingTime = Math.max(totalRemainingTime, currentFileRemainingTime);
                    progressText += DurationFormat.format(totalRemainingTime);
                }

                totalProgressBar.setTextOverlay(progressText);

                // Update info label
                infoLabel.setText(job.getStatusString());

                // Update elapsed time label
                elapsedTimeLabel.setText(Translator.get("progress_bar.elapsed_time")+": "+DurationFormat.format(effectiveJobTime));
            }

            // Sleep for a while
            try { Thread.sleep(REFRESH_RATE); }
            catch(InterruptedException e) {}
        }
	
        dispose();
    }

    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
    	
        if (source==cancelButton) {
            // Cancel button pressed, dispose dialog and stop job immediately
            // (job will be stopped a second time in windowClosed() but that will just be a no-op)
            dispose();
            job.stop();
        }
        else if(source==hideButton) {
            mainFrame.setState(Frame.ICONIFIED);
        }
    }


    ///////////////////////////////////////
    // Overridden WindowListener methods // 
    ///////////////////////////////////////

    public void windowActivated(WindowEvent e) {
        // This method is called each time the dialog is activated
        super.windowActivated(e);
        if(firstTimeActivated) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("first time activated, starting job!");
            firstTimeActivated = false;
            this.job.start();
        }
    }

    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);

        // Stop threads
        repaintThread = null;
        // Job may have already been stopped if cancel button was pressed
        job.stop();
    }
}
