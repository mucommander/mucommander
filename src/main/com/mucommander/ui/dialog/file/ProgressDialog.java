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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.job.FileJob;
import com.mucommander.job.FileJobListener;
import com.mucommander.job.TransferFileJob;
import com.mucommander.job.progress.JobProgress;
import com.mucommander.job.progress.JobProgressListener;
import com.mucommander.job.progress.JobProgressMonitor;
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

/**
 * This dialog informs the user of the progress made by a FileJob and allows to control it: pause/resume it, stop it,
 * limit transfer rate...
 *
 * @author Maxence Bernard
 */
public class ProgressDialog extends FocusDialog implements ActionListener, ItemListener, ChangeListener, FileJobListener, JobProgressListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgressDialog.class);
	
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
    private final static int SPEED_GRAPH_HEIGHT = 80;

    static {
        // Disable JProgressBar animation which is a real CPU hog under Mac OS X
        UIManager.put("ProgressBar.repaintInterval", Integer.MAX_VALUE);
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

            this.speedGraph = new SpeedGraph();
            speedGraph.setPreferredSize(new Dimension(0, SPEED_GRAPH_HEIGHT));
            advancedPanel.add(speedGraph);

            advancedPanel.addSpace(5);

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

            this.collapseExpandButton = new CollapseExpandButton(Translator.get("progress_dialog.advanced"), advancedPanel, true);
            collapseExpandButton.setExpandedState(MuConfigurations.getPreferences().getVariable(MuPreference.PROGRESS_DIALOG_EXPANDED,
                                                                                   MuPreferences.DEFAULT_PROGRESS_DIALOG_EXPANDED));
            tempPanel.add(collapseExpandButton, BorderLayout.EAST);

            yPanel.add(tempPanel);
            yPanel.addSpace(5);

            yPanel.add(advancedPanel);
        }

        closeWhenFinishedCheckBox = new JCheckBox(Translator.get("progress_dialog.close_when_finished"));
        closeWhenFinishedCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.PROGRESS_DIALOG_CLOSE_WHEN_FINISHED,
                                                                               MuPreferences.DEFAULT_PROGRESS_DIALOG_CLOSE_WHEN_FINISHED));
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
        
		JobProgressMonitor.getInstance().addJob(job);
        JobProgressMonitor.getInstance().addJobProgressListener(this);

        showDialog();
    }


    /**
     * Stops repaint thread.
     */
    public void stop() {
    	JobProgressMonitor.getInstance().removeJobProgressListener(this);
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
        LOGGER.debug("currentThread="+Thread.currentThread()+" oldState="+oldState+" newState="+newState);

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

    
    // Refresh current file label in a separate thread, more frequently than other components to give a sense
    // of speed when small files are being transferred.
    // This 'pull' approach allows to throttle the number label updates which have a cost VS updating the label
    // for each file being processed (job notifications) which can hog the CPU when lots of small files
    // are being transferred.
    private void updateProgressLabel(JobProgress progress) {
    	currentFileLabel.setText(progress.getJobStatusString());
    }
    
    private void updateProgressUI(JobProgress progress) {
        if (progress.isTransferFileJob()) {
            currentFileProgressBar.setValue(progress.getFilePercentInt());
            currentFileProgressBar.setString(progress.getFileProgressText());

            // Update total transferred label
            totalTransferredLabel.setText(
               Translator.get("progress_dialog.transferred",
                              SizeFormat.format(progress.getBytesTotal(), SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_LONG| SizeFormat.ROUND_TO_KB),
                              SizeFormat.format(progress.getTotalBps(), SizeFormat.UNIT_SPEED| SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT| SizeFormat.ROUND_TO_KB))
            );
            
            // Add new immediate bytes per second speed sample to speed graph and label and repaint it
            // Skip this sample if job was paused and resumed, speed would not be accurate
            if (progress.getLastTime()>progress.getJobPauseStartDate()) {
                speedGraph.addSample(progress.getCurrentBps());
                updateCurrentSpeedLabel(SizeFormat.format(progress.getCurrentBps(), SizeFormat.UNIT_SPEED| SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT));
            }
            
        }
        
        totalProgressBar.setValue(progress.getTotalPercentInt());
        totalProgressBar.setString(progress.getTotalProgressText());

        // Update elapsed time label
        elapsedTimeLabel.setText(Translator.get("progress_dialog.elapsed_time")+": "+DurationFormat.format(progress.getEffectiveJobTime()));
    	
    }
    
    /////////////////////////////
    // JobProgress listener    //
    /////////////////////////////
    

	public void jobAdded(FileJob source, int idx) {
		// nothing here		
	}


	public void jobRemoved(FileJob source, int idx) {
		// nothing here		
	}

	public void jobProgress(FileJob source, int idx, boolean fullUpdate) {
		if (job.equals(source)) {
			updateProgressLabel(source.getJobProgress());
			if (fullUpdate) {
				updateProgressUI(source.getJobProgress());
			}
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

    @Override
    public void windowActivated(WindowEvent e) {
        // This method is called each time the dialog is activated
        super.windowActivated(e);
        if(firstTimeActivated) {
            firstTimeActivated = false;
            this.job.start();
        }
    }

    @Override
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
        	MuConfigurations.getPreferences().setVariable(MuPreference.PROGRESS_DIALOG_EXPANDED, collapseExpandButton.getExpandedState());

        // Remember 'close window when finished' option state
        MuConfigurations.getPreferences().setVariable(MuPreference.PROGRESS_DIALOG_CLOSE_WHEN_FINISHED, closeWhenFinishedCheckBox.isSelected());        
    }


    /**
     * Transfer speed graph.
     */
    private class SpeedGraph extends JPanel {

        private final Color GRAPH_OUTLINE_COLOR = new Color(70, 70, 70);

        private final Color GRAPH_FILL_COLOR = new Color(215, 215, 215);

        private final Color BPS_LIMIT_COLOR = new Color(204, 0, 0);

        private static final int LINE_SPACING = 6;

        private static final int NB_SAMPLES_MAX = 320;

        private static final int STROKE_WIDTH = 1;

        private java.util.List<Long> samples = new Vector<Long>(NB_SAMPLES_MAX);

        private Stroke lineStroke = new BasicStroke(STROKE_WIDTH);


        private SpeedGraph() {
        }


        private void addSample(long bytesPerSecond) {
            synchronized(samples) {     // Ensures that paint() is not currently accessing the Vector
                // Capacity reached, remove first sample
                if(samples.size()==NB_SAMPLES_MAX)
                    samples.remove(0);

                // Add sample to the vector
                samples.add(bytesPerSecond);
            }

            repaint();
        }


        @Override
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;

            // Enable antialiasing, looks way better
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Fill the background with the panel's background color
            g.setColor(getBackground());
            g.fillRect(0, 0, width, height);

            g2d.setStroke(lineStroke);

            synchronized(samples) {     // Ensures that addSample() is not currently accessing the Vector
                // Number of collected sample
                int nbSamples = samples.size();
                // Number of displayable samples based on their spacing
                int nbDisplayableSamples = (width-2*STROKE_WIDTH)/LINE_SPACING;
                // Index of the first sample
                int firstSample = nbSamples>nbDisplayableSamples?nbSamples-nbDisplayableSamples:0;
                // Number of lines to be drawn
                int nbLines = Math.min(nbSamples, nbDisplayableSamples);

                // Calculate the maximum bytes per second of all the samples to be displayed
                long maxBps = 0;
                for(int i=firstSample; i<firstSample+nbLines; i++) {
                    long sample = samples.get(i);
                    if(sample>maxBps)
                        maxBps = sample;
                }

                // Y-scale projection ratio, leave some space on both sides of the graph
                float yRatio = maxBps/((float)height-2*STROKE_WIDTH);

                // Draw throughput limit as an horizontal line, only if there is a limit
                long bpsLimit = transferFileJob.getThroughputLimit();
                if(bpsLimit>0) {
                    g.setColor(BPS_LIMIT_COLOR);
                    int y = height-STROKE_WIDTH-(int)(bpsLimit/yRatio);
                    g.drawLine(0, y, width, y);
                }

                // Fill the graph
                g.setColor(GRAPH_FILL_COLOR);
                int x = STROKE_WIDTH;
                Polygon p = new Polygon();
                int sampleOffset = firstSample;
                for(int l=0; l<nbLines; l++) {
                    p.addPoint(x, height-STROKE_WIDTH-(int)((Long) samples.get(sampleOffset++) /yRatio));
                    x+=LINE_SPACING;
                }
                p.addPoint(x-LINE_SPACING, height-1);
                p.addPoint(0, height-1);
                g.fillPolygon(p);

                // Draw the graph outline in a darker color
                g.setColor(GRAPH_OUTLINE_COLOR);
                x = STROKE_WIDTH;
                sampleOffset = firstSample;
                for(int l=0; l<nbLines-1; l++) {
                    g.drawLine(x, height-STROKE_WIDTH-(int)((Long) samples.get(sampleOffset) /yRatio),
                              (x+=LINE_SPACING), height-STROKE_WIDTH-(int)((Long) samples.get(++sampleOffset) /yRatio));
                }

                // Draw an horizontal line at the bottom of the graph
                g.drawLine(0, height-1,
                        width-1, height-1);

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
