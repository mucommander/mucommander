/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.Debug;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.job.FileJob;
import com.mucommander.job.FileJobListener;
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.DurationFormat;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.button.ButtonChoicePanel;
import com.mucommander.ui.button.CollapseExpandButton;
import com.mucommander.ui.chooser.SizeChooser;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.StatusBar;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * This dialog informs the user of the progress made by a FileJob and allows to control it: pause/resume it, stop it,
 * limit transfer rate...
 *
 * @author Maxence Bernard
 */
public class ProgressDialog extends FocusDialog implements Runnable, ActionListener, ItemListener, ChangeListener, FileJobListener {

    private JLabel currentFileLabel;
    private JLabel totalTransferredLabel;

    private JProgressBar totalProgressBar;
    private JProgressBar currentFileProgressBar;

    private JLabel currentSpeedLabel;
    private JCheckBox limitSpeedCheckBox;
    private SizeChooser speedChooser;
    private JLabel elapsedTimeLabel;

    private SpeedGraph speedGraph;

    private CollapseExpandButton collapseExpandButton;
    private ButtonChoicePanel buttonsChoicePanel;
    private JButton pauseResumeButton;
    private JButton skipButton;
    private JButton stopButton;
    private JCheckBox closeWhenFinishedCheckBox;
//    private JButton hideButton;

    private FileJob job;
    private TransferFileJob transferFileJob;

    private Thread repaintThread;
    private boolean firstTimeActivated = true;

    // Button icons
    private final static String RESUME_ICON = "resume.png";
    private final static String PAUSE_ICON = "pause.png";
    private final static String SKIP_ICON = "skip.png";
    private final static String STOP_ICON = "stop.png";
    private final static String CURRENT_SPEED_ICON = "speed.png";

    // Dialog width is constrained to 320, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);

    /** Height allocated to the 'speed graph' */
    private final static int SPEED_GRAPH_HEIGHT = 100;

    /** Controls how often should progress information be refreshed (in ms) */
    private final static int MAIN_REFRESH_RATE = 1000;

    /** Controls how often should current file label be refreshed */
    private final static int CURRENT_FILE_LABEL_REFRESH_RATE = 100;

    static {
        // Disable JProgressBar animation which is a real CPU hog under Mac OS X
        UIManager.put("ProgressBar.repaintInterval", new Integer(Integer.MAX_VALUE));
    }


    public ProgressDialog(MainFrame mainFrame, String title) {
        super(mainFrame, title, mainFrame);

        // Sets maximum and minimum dimensions for this dialog
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);

        setResizable(false);
    }
    
    
    private void initUI() {
        Container contentPane = getContentPane();

        totalProgressBar = new JProgressBar();
        totalProgressBar.setStringPainted(true);
        totalProgressBar.setAlignmentX(LEFT_ALIGNMENT);
        currentFileLabel = new JLabel(job.getStatusString());
        currentFileLabel.setAlignmentX(LEFT_ALIGNMENT);
		
        YBoxPanel yPanel = new YBoxPanel();
        // 2 progress bars
        if (transferFileJob !=null) {
            yPanel.add(currentFileLabel);
            currentFileProgressBar = new JProgressBar();
            currentFileProgressBar.setStringPainted(true);
            yPanel.add(currentFileProgressBar);
            yPanel.addSpace(10);
		
            totalTransferredLabel = new JLabel(Translator.get("progress_dialog.starting"));
            yPanel.add(totalTransferredLabel);
			
            yPanel.add(totalProgressBar);
        }
        // Single progress bar
        else {
            yPanel.add(currentFileLabel);
            yPanel.add(totalProgressBar);
        }

        yPanel.addSpace(10);
        elapsedTimeLabel = new JLabel(Translator.get("progress_dialog.elapsed_time")+": ");
        elapsedTimeLabel.setIcon(IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, StatusBar.WAITING_ICON));
        yPanel.add(elapsedTimeLabel);

        if(transferFileJob!=null) {
            JPanel tempPanel = new JPanel(new BorderLayout());

            this.currentSpeedLabel = new JLabel();
            updateCurrentSpeedLabel("");
            currentSpeedLabel.setIcon(IconManager.getIcon(IconManager.PROGRESS_ICON_SET, CURRENT_SPEED_ICON));
            tempPanel.add(currentSpeedLabel, BorderLayout.WEST);

            YBoxPanel advancedPanel = new YBoxPanel();

            JPanel tempPanel2 = new JPanel(new BorderLayout());
            this.limitSpeedCheckBox = new JCheckBox(Translator.get("progress_dialog.limit_speed")+":", false);
            limitSpeedCheckBox.addItemListener(this);

            tempPanel2.add(limitSpeedCheckBox, BorderLayout.WEST);

            speedChooser = new SizeChooser(true);
            speedChooser.setEnabled(false);
            speedChooser.addChangeListener(this);

            tempPanel2.add(speedChooser, BorderLayout.EAST);
            advancedPanel.add(tempPanel2);
            advancedPanel.addSpace(5);

            this.speedGraph = new SpeedGraph();
            speedGraph.setPreferredSize(new Dimension(0, SPEED_GRAPH_HEIGHT));
            advancedPanel.add(speedGraph);

            this.collapseExpandButton = new CollapseExpandButton(Translator.get("progress_dialog.advanced"), advancedPanel, true);
            collapseExpandButton.setExpandedState(MuConfiguration.getVariable(MuConfiguration.PROGRESS_DIALOG_EXPANDED,
                                                                                   MuConfiguration.DEFAULT_PROGRESS_DIALOG_EXPANDED));
            tempPanel.add(collapseExpandButton, BorderLayout.EAST);

            yPanel.add(tempPanel);
            yPanel.addSpace(5);

            yPanel.add(advancedPanel);
        }

        closeWhenFinishedCheckBox = new JCheckBox(Translator.get("progress_dialog.close_when_finished"));
        closeWhenFinishedCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.PROGRESS_DIALOG_CLOSE_WHEN_FINISHED,
                                                                               MuConfiguration.DEFAULT_PROGRESS_DIALOG_CLOSE_WHEN_FINISHED));
        yPanel.add(closeWhenFinishedCheckBox);

        yPanel.add(Box.createVerticalGlue());
        contentPane.add(yPanel, BorderLayout.CENTER);

        pauseResumeButton = new JButton(Translator.get("pause"), IconManager.getIcon(IconManager.PROGRESS_ICON_SET, PAUSE_ICON));
        pauseResumeButton.addActionListener(this);

        if(transferFileJob!=null) {
            skipButton = new JButton(Translator.get("skip"), IconManager.getIcon(IconManager.PROGRESS_ICON_SET, SKIP_ICON));
            skipButton.addActionListener(this);
        }

        stopButton = new JButton(Translator.get("stop"), IconManager.getIcon(IconManager.PROGRESS_ICON_SET, STOP_ICON));
        stopButton.addActionListener(this);

//        hideButton = new JButton(Translator.get("progress_dialog.hide"));
//        hideButton.addActionListener(this);

        this.buttonsChoicePanel = new ButtonChoicePanel(
                skipButton==null?new JButton[] {pauseResumeButton, stopButton}:new JButton[] {pauseResumeButton, skipButton, stopButton},
                0, getRootPane());
        contentPane.add(buttonsChoicePanel, BorderLayout.SOUTH);

        // Cancel button receives initial focus
        setInitialFocusComponent(stopButton);

        // Enter triggers cancel button
        getRootPane().setDefaultButton(stopButton);
    }


    public void start(FileJob job) {
        this.job = job;

        // Listen to job state changes
        job.addFileJobListener(this);

        if(job instanceof TransferFileJob)
            this.transferFileJob = (TransferFileJob)job;

        initUI();
        
        repaintThread = new Thread(this, getClass().getName());
        repaintThread.start();

        showDialog();
    }


    /**
     * Stops repaint thread.
     */
    public void stop() {
        repaintThread = null;
    }


//    /**
//     * This method is called by the registered FileJob starts each time a new file is being processed.
//     */
//    public void notifyCurrentFileChanged() {
//        // Update current file label
//        currentFileLabel.setText(job.getStatusString());
//    }

    private void updateThroughputLimit() {
        transferFileJob.setThroughputLimit(limitSpeedCheckBox.isSelected()?speedChooser.getValue():-1);
    }

    private void updateCurrentSpeedLabel(String value) {
        currentSpeedLabel.setText(Translator.get("progress_dialog.current_speed")+": "+value);
    }


    ////////////////////////////////////
    // FileJobListener implementation //
    ////////////////////////////////////

    public void jobStateChanged(FileJob source, int oldState, int newState) {
        if(Debug.ON) Debug.trace("currentThread="+Thread.currentThread()+" oldState="+oldState+" newState="+newState);

        if(newState==FileJob.INTERRUPTED) {
            // Stop repaint thread and dispose dialog
            stop();
            dispose();
        }
        else if(newState==FileJob.FINISHED) {
            //  Dispose dialog only if 'Close when finished option' is selected
            if(closeWhenFinishedCheckBox.isSelected()) {
                // Stop repaint thread and dispose dialog
                stop();
                dispose();
            // If not, disable components to indicate that the job is finished and leave the dialog open
            }
            else {
                // Repaint thread should not be stopped now, it will die naturally after updating labels and progress
                // bars to indicate that the job is finished

                // Change 'Stop' button's label to 'Close'
                stopButton.setText(Translator.get("close"));

                // Disable components
                pauseResumeButton.setEnabled(false);

                if(transferFileJob!=null) {
                    skipButton.setEnabled(false);
                    limitSpeedCheckBox.setEnabled(false);
                    speedChooser.setEnabled(false);
                }
            }
        }
        else if(newState==FileJob.PAUSED) {
            pauseResumeButton.setText(Translator.get("resume"));
            pauseResumeButton.setIcon(IconManager.getIcon(IconManager.PROGRESS_ICON_SET, RESUME_ICON));

            // Update buttons mnemonics
            buttonsChoicePanel.updateMnemonics();
            
            if(transferFileJob!=null)
                updateCurrentSpeedLabel("N/A");
        }
        else if(newState==FileJob.RUNNING) {
            pauseResumeButton.setText(Translator.get("pause"));
            pauseResumeButton.setIcon(IconManager.getIcon(IconManager.PROGRESS_ICON_SET, PAUSE_ICON));

            // Update buttons mnemonics
            buttonsChoicePanel.updateMnemonics();
        }
    }


    /////////////////////////////
    // Runnable implementation //
    /////////////////////////////

    public void run() {
        String progressText;
        long lastBytesTotal = 0;
        long lastTime = System.currentTimeMillis();
        boolean lastLoop = false;

        // Refresh current file label in a separate thread, more frequently than other components to give a sense
        // of speed when small files are being transferred.
        // This 'pull' approach allows to throttle the number label updates which have a cost VS updating the label
        // for each file being processed (job notifications) which can hog the CPU when lots of small files
        // are being transferred.
        new Thread() {
            public void run() {
                // This thread will naturally die when the main repaint thread is terminated
                while(repaintThread!=null) {
                    int jobState = job.getState();

                    if(jobState==FileJob.FINISHED || jobState==FileJob.INTERRUPTED) {
                        currentFileLabel.setText(Translator.get("progress_dialog.job_finished"));
                        return;
                    }

                    currentFileLabel.setText(job.getStatusString());

                    // Sleep for a while
                    try {
                        Thread.sleep(CURRENT_FILE_LABEL_REFRESH_RATE);
                    }
                    catch(InterruptedException e) {}
                }
            }
        }.start();

        while(repaintThread!=null) {
            // Now is updated with current time, or job end date if job has finished already.
            long now = job.getEndDate();
            if(now==0)  // job hasn't finished yet
                now = System.currentTimeMillis(); 

            // Do not refresh progress information is job is paused, simply sleep
            if(job.getState()!=FileJob.PAUSED) {
                long currentFileRemainingTime = 0;
                long totalRemainingTime;

                long effectiveJobTime = job.getEffectiveJobTime();
                if(effectiveJobTime==0)
                    effectiveJobTime = 1;   // To avoid potential zero divisions

                if (transferFileJob !=null) {
                    // Do not count bytes that are skipped when files are resumed
                    long bytesTotal = transferFileJob.getTotalByteCounter().getByteCount() - transferFileJob.getTotalSkippedByteCounter().getByteCount();
                    long totalBps = (long)(bytesTotal*1000d/effectiveJobTime);
                    long currentBps;

                    if(now-lastTime>0)  // To avoid divisions by zero
                        currentBps = (long)((bytesTotal-lastBytesTotal)*1000d/(now-lastTime));
                    else
                        currentBps = 0;

                    // Update current file progress bar
                    float filePercentFloat = transferFileJob.getFilePercentDone();
                    int filePercentInt = (int)(100*filePercentFloat);
                    currentFileProgressBar.setValue(filePercentInt);

                    progressText = filePercentInt+"%";
                    // Append estimated remaining time (ETA) if current file transfer is not already finished (100%)
                    if(filePercentFloat<1) {
                        progressText += " - ";

                        long currentFileSize = transferFileJob.getCurrentFileSize();
                        // If current file size is not available, ETA cannot be calculated
                        if(currentFileSize==-1)
                            progressText += "?";
                        // Avoid potential divisions by zero
                        else if(totalBps==0) {
                            currentFileRemainingTime = -1;
                            progressText += DurationFormat.getInfiniteSymbol();
                        }
                        else {
                            currentFileRemainingTime = (long)((1000*(currentFileSize - transferFileJob.getCurrentFileByteCounter().getByteCount()))/(float)totalBps);
                            progressText += DurationFormat.format(currentFileRemainingTime);
                        }
                    }

                    currentFileProgressBar.setString(progressText);

                    // Update total transferred label
                    totalTransferredLabel.setText(
                       Translator.get("progress_dialog.transferred",
                                      SizeFormat.format(bytesTotal, SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_LONG| SizeFormat.ROUND_TO_KB),
                                      SizeFormat.format(totalBps, SizeFormat.UNIT_SPEED| SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT| SizeFormat.ROUND_TO_KB))
                    );

                    // Add new immediate bytes per second speed sample to speed graph and label and repaint it
                    // Skip this sample if job was paused and resumed, speed would not be accurate
                    if(lastTime>job.getPauseStartDate() && !lastLoop) {
                        speedGraph.addSample(currentBps);
                        updateCurrentSpeedLabel(SizeFormat.format(currentBps, SizeFormat.UNIT_SPEED| SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT));
                    }

                    lastBytesTotal = bytesTotal;
                    lastTime = now;
                }

                // Update total progress bar
                // Total job percent is based on the *number* of files remaining, not their actual size.
                // So this is very approximate.
                float totalPercentFloat = job.getTotalPercentDone();
                int totalPercentInt = (int)(100*totalPercentFloat);

                totalProgressBar.setValue(totalPercentInt);

                progressText = totalPercentInt+"%";

                // Add a rough estimate of the total remaining time (ETA):
                // total remaining time is based on the total job percent completed which itself is based on the *number*
                // of files remaining, not their actual size. So this is very approximate.
                // Do not add ETA if job is already finished (100%)
                if(totalPercentFloat<1) {
                    progressText += " - ";

                    // Avoid potential divisions by zero
                    if(totalPercentFloat==0)
                        progressText += "?";
                    else {
                        // Make sure that total ETA is never smaller than current file ETA
                        totalRemainingTime = (long)((1-totalPercentFloat)*(effectiveJobTime/totalPercentFloat));
                        totalRemainingTime = Math.max(totalRemainingTime, currentFileRemainingTime);
                        progressText += DurationFormat.format(totalRemainingTime);
                    }
                }
                totalProgressBar.setString(progressText);

//                // Update current file label
//                currentFileLabel.setText(job.getStatusString());

                // Update elapsed time label
                elapsedTimeLabel.setText(Translator.get("progress_dialog.elapsed_time")+": "+DurationFormat.format(effectiveJobTime));
            }

            if(lastLoop) {
                break;
            }
            else if(job.getState()==FileJob.FINISHED) {
                // Job just finished, let's loop one more time to ensure that components (progress bar in particular)
                // reflect job completion
                lastLoop = true;
            }

            // Sleep for a while
            try {
                Thread.sleep(Math.max(MAIN_REFRESH_RATE -(System.currentTimeMillis()-now), 0));
            }
            catch(InterruptedException e) {}
        }
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source==stopButton) {
            int jobState = job.getState();
            // Case when 'Close when finished' isn't selected, stop button's action becomes 'Close'
            if(jobState==FileJob.FINISHED || jobState==FileJob.INTERRUPTED)
                dispose();
            else
                job.interrupt();
        }
        else if(source==skipButton) {
            transferFileJob.skipCurrentFile();
        }
        else if(source==pauseResumeButton) {
            // Pause/resume job
            job.setPaused(job.getState()!=FileJob.PAUSED);
        }
//        else if(source==hideButton) {
//            mainFrame.setState(Frame.ICONIFIED);
//        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if(source==limitSpeedCheckBox) {
            boolean isEnabled = limitSpeedCheckBox.isSelected();
            speedChooser.setEnabled(isEnabled);
            updateThroughputLimit();
        }
    }


    ///////////////////////////////////
    // ChangeListener implementation //
    ///////////////////////////////////

    public void stateChanged(ChangeEvent e) {
        if(e.getSource()==speedChooser) {
            updateThroughputLimit();
        }
    }


    ///////////////////////////////////////
    // Overridden WindowListener methods // 
    ///////////////////////////////////////

    public void windowActivated(WindowEvent e) {
        // This method is called each time the dialog is activated
        super.windowActivated(e);
        if(firstTimeActivated) {
            firstTimeActivated = false;
            this.job.start();
        }
    }

    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
        
        // Stop repaint thread if it isn't already
        stop();

        // Stop job if it isn't already
        int jobState = job.getState();
        if(!(jobState==FileJob.FINISHED || jobState==FileJob.INTERRUPTED))
            job.interrupt();

        // Remember 'advanced panel' expanded state
        if(collapseExpandButton!=null)
            MuConfiguration.setVariable(MuConfiguration.PROGRESS_DIALOG_EXPANDED, collapseExpandButton.getExpandedState());

        // Remember 'close window when finished' option state
        MuConfiguration.setVariable(MuConfiguration.PROGRESS_DIALOG_CLOSE_WHEN_FINISHED, closeWhenFinishedCheckBox.isSelected());        
    }


    /**
     * Transfer speed graph.
     */
    private class SpeedGraph extends JPanel {

        private final Color BACKGROUND_COLOR = ThemeManager.getCurrentColor(Theme.FILE_TABLE_BACKGROUND_COLOR);

        private final Color GRAPH_COLOR = ThemeManager.getCurrentColor(Theme.FILE_TABLE_SELECTED_BACKGROUND_COLOR);

        private final Color BPS_LIMIT_COLOR = ThemeManager.getCurrentColor(Theme.MARKED_FOREGROUND_COLOR);

        private static final int LINE_SPACING = 6;

        private static final int NB_SAMPLES_MAX = 320;

        private static final int STROKE_WIDTH = 2;

        private Vector samples = new Vector(NB_SAMPLES_MAX);

        private Stroke lineStroke = new BasicStroke(STROKE_WIDTH);


        private SpeedGraph() {
        }


        private void addSample(long bytesPerSecond) {
            synchronized(samples) {     // Ensures that paint() is not currently accessing the Vector
                // Capacity reached, remove first sample
                if(samples.size()==NB_SAMPLES_MAX)
                    samples.removeElementAt(0);

                // Add sample to the vector
                samples.add(new Long(bytesPerSecond));
            }

            repaint();
        }


        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;

            // Enable antialiasing, looks way better
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Fill background
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, width, height);

            g.setColor(ThemeManager.getCurrentColor(Theme.FILE_TABLE_BORDER_COLOR));
            g.drawRect(0, 0, width - 1, height - 1);


            synchronized(samples) {     // Ensures that addSample() is not currently accessing the Vector
                // Number of collected sample
                int nbSamples = samples.size();
                // Number of displayable samples based on their spacing
                int nbDisplayableSamples = (width-2*STROKE_WIDTH)/LINE_SPACING;
                // Index of the first sample
                int sampleOffset = nbSamples>nbDisplayableSamples?nbSamples-nbDisplayableSamples:0;
                // Number of lines to be drawn
                int nbLines = Math.min(nbSamples, nbDisplayableSamples);

                // Calculate the maximum bytes per second of all the samples to be displayed
                long maxBps = 0;
                for(int i= sampleOffset; i<sampleOffset+nbLines; i++) {
                    long sample = ((Long)samples.elementAt(i)).longValue();
                    if(sample>maxBps)
                        maxBps = sample;
                }

                // Y-scale projection ratio, leave some space on both sides of the graph
                float yRatio = maxBps/((float)height-2*STROKE_WIDTH);

                // Draw throughput limit as an horizontal line, only if there is a limit
                long bpsLimit = transferFileJob.getThroughputLimit();
                g.setColor(BPS_LIMIT_COLOR);
                if(bpsLimit>0) {
                    int y = height-STROKE_WIDTH-(int)(bpsLimit/yRatio);
                    g.drawLine(0, y, width, y);
                }

                // Set custom line stroke and color
                g.setColor(GRAPH_COLOR);
                g2d.setStroke(lineStroke);

                // Draw the graph based on the collected samples
                int x = STROKE_WIDTH;
                for(int l=0; l<nbLines-1; l++) {
                    g.drawLine(x, height-STROKE_WIDTH-(int)(((Long)samples.elementAt(sampleOffset)).longValue()/yRatio),
                              (x+=LINE_SPACING), height-STROKE_WIDTH-(int)(((Long)samples.elementAt(++sampleOffset)).longValue()/yRatio));
                }

                // Unsuccessful rendering test using curves
    //            GeneralPath gp = new GeneralPath();
    //            int x = STROKE_WIDTH;
    //            gp.moveTo(x, height-STROKE_WIDTH-(int)(((Long)samples.elementAt(sampleOffset++)).longValue()/yRatio));
    //            x += LINE_SPACING;
    //            for(int l=1; l<nbLines-2; l+=2) {
    //                gp.quadTo(
    //                    x, height-STROKE_WIDTH-(int)(((Long)samples.elementAt(sampleOffset)).longValue()/yRatio),
    //                    x+LINE_SPACING, height-STROKE_WIDTH-(int)(((Long)samples.elementAt(sampleOffset+1)).longValue()/yRatio)
    //                );
    //
    //                sampleOffset += 2;
    //                x += 2*LINE_SPACING;
    //
    //                g2d.draw(gp);
    //            }
            }
        }
    }
}
