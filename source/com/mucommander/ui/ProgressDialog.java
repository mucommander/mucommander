
package com.mucommander.ui;

import com.mucommander.job.TransferFileJob;
import com.mucommander.job.FileJob;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.text.DurationFormat;
import com.mucommander.ui.comp.button.ButtonChoicePanel;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.ui.comp.dialog.CollapseExpandButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.conf.ConfigurationManager;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * This dialog informs the user of the progress made by a FileJob.
 *
 * @author Maxence Bernard
 */
public class ProgressDialog extends FocusDialog implements Runnable, ActionListener, ItemListener, ChangeListener {

    private JLabel currentFileLabel;
    private JLabel totalTransferredLabel;

    private JProgressBar totalProgressBar;
    private JProgressBar currentFileProgressBar;

    private JLabel currentSpeedLabel;
    private JCheckBox limitSpeedCheckBox;
    private JSpinner limitSpeedSpinner;
    private JComboBox speedUnitComboBox;
    private JLabel elapsedTimeLabel;

    private SpeedGraph speedGraph;

    private CollapseExpandButton collapseExpandButton;
    private ButtonChoicePanel buttonsChoicePanel;
    private JButton pauseResumeButton;
    private JButton stopButton;
//    private JButton hideButton;

    private FileJob job;
    private TransferFileJob transferFileJob;

    private Thread repaintThread;

    private MainFrame mainFrame;

    private boolean firstTimeActivated = true;

    // Button icons
    private final static String RESUME_ICON = "resume.png";
    private final static String PAUSE_ICON = "pause.png";
    private final static String STOP_ICON = "stop.png";
    private final static String CURRENT_SPEED_ICON = "speed.png";

    // Dialog width is constrained to 320, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);

    /** Height allocated to the 'speed graph' */
    private final static int SPEED_GRAPH_HEIGHT = 100;

    /** How often should progress information be refreshed (in ms) */
    private final static int REFRESH_RATE = 1000;

    private final static String EXPANDED_CONF_VAR = "prefs.progress_dialog.expanded";


    static {
        // Disable JProgressBar animation which is a real CPU hog under Mac OS X
        UIManager.put("ProgressBar.repaintInterval", new Integer(Integer.MAX_VALUE));
    }


    public ProgressDialog(MainFrame mainFrame, String title) {
        super(mainFrame, title, mainFrame);

        this.mainFrame = mainFrame;

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

        if(transferFileJob !=null) {
            JPanel tempPanel = new JPanel(new BorderLayout());

            this.currentSpeedLabel = new JLabel();
            updateCurrentSpeedLabel("");
            currentSpeedLabel.setIcon(IconManager.getIcon(IconManager.PROGRESS_ICON_SET, CURRENT_SPEED_ICON));
            tempPanel.add(currentSpeedLabel, BorderLayout.WEST);

            YBoxPanel advancedPanel = new YBoxPanel();

            JPanel tempPanel2 = new JPanel(new BorderLayout());
            this.limitSpeedCheckBox = new JCheckBox(Translator.get("progress_dialog.limit_speed")+":", false);
            limitSpeedCheckBox.setFocusable(false);
            limitSpeedCheckBox.addItemListener(this);

            tempPanel2.add(limitSpeedCheckBox, BorderLayout.WEST);
            this.limitSpeedSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 100));
            limitSpeedSpinner.setEnabled(false);
            limitSpeedSpinner.addChangeListener(this);
//            limitSpeedSpinner.addKeyListener(
//                new KeyAdapter() {
//                    public void keyPressed(KeyEvent e) {
//if(Debug.ON) Debug.trace("called, e="+e);
//                        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
//                            e.consume();
//                        }
//                    }
//                }
//            );

            JPanel tempPanel3 = new JPanel(new FlowLayout(FlowLayout.LEADING));
            tempPanel3.add(limitSpeedSpinner);
            speedUnitComboBox = new JComboBox();
            for(int i=SizeFormat.BYTE_UNIT; i<SizeFormat.GIGA_BYTE_UNIT; i++)
                speedUnitComboBox.addItem(SizeFormat.getUnitString(i, true));
            speedUnitComboBox.setSelectedIndex(SizeFormat.KILO_BYTE_UNIT);
            speedUnitComboBox.setEnabled(false);
            speedUnitComboBox.addItemListener(this);
            tempPanel3.add(speedUnitComboBox);

            tempPanel2.add(tempPanel3, BorderLayout.EAST);
            advancedPanel.add(tempPanel2);
            advancedPanel.addSpace(5);

            this.speedGraph = new SpeedGraph();
            speedGraph.setPreferredSize(new Dimension(0, SPEED_GRAPH_HEIGHT));
            advancedPanel.add(speedGraph);

            this.collapseExpandButton = new CollapseExpandButton(Translator.get("progress_dialog.advanced"), advancedPanel, this, true);
            collapseExpandButton.setExpandedState(ConfigurationManager.getVariableBoolean(EXPANDED_CONF_VAR, true));
            tempPanel.add(collapseExpandButton, BorderLayout.EAST);

            yPanel.add(tempPanel);
            yPanel.addSpace(5);

            yPanel.add(advancedPanel);
        }

        yPanel.add(Box.createVerticalGlue());
        contentPane.add(yPanel, BorderLayout.CENTER);

        pauseResumeButton = new JButton(Translator.get("pause"), IconManager.getIcon(IconManager.PROGRESS_ICON_SET, PAUSE_ICON));
        pauseResumeButton.addActionListener(this);

        stopButton = new JButton(Translator.get("stop"), IconManager.getIcon(IconManager.PROGRESS_ICON_SET, STOP_ICON));
        stopButton.addActionListener(this);

//        hideButton = new JButton(Translator.get("progress_dialog.hide"));
//        hideButton.addActionListener(this);

//        this.buttonsChoicePanel = new ButtonChoicePanel(new JButton[] {pauseResumeButton, stopButton, hideButton}, 0, getRootPane());
        this.buttonsChoicePanel = new ButtonChoicePanel(new JButton[] {pauseResumeButton, stopButton}, 0, getRootPane());
        contentPane.add(buttonsChoicePanel, BorderLayout.SOUTH);

        // Cancel button receives initial focus
        setInitialFocusComponent(stopButton);

        // Enter triggers cancel button
        getRootPane().setDefaultButton(stopButton);
    }


    public void start(FileJob job) {
        this.job = job;
        if(job instanceof TransferFileJob)
            this.transferFileJob = (TransferFileJob)job;

        initUI();
        
        repaintThread = new Thread(this, "com.mucommander.ui.ProgressDialog's Thread");
        repaintThread.start();

        showDialog();
    }


    private void updateThroughputLimit() {
        transferFileJob.setThroughputLimit(limitSpeedCheckBox.isSelected()?SizeFormat.getUnitBytes(speedUnitComboBox.getSelectedIndex())*(((Integer)limitSpeedSpinner.getValue())).intValue():-1);
    }

    private void updateCurrentSpeedLabel(String value) {
        currentSpeedLabel.setText(Translator.get("progress_dialog.current_speed")+": "+value);
    }


    //////////////////////
    // Runnable methods //
    //////////////////////

    public void run() {
        String progressText;
        long lastBytesTotal = 0;
        long lastTime = System.currentTimeMillis();

        while(repaintThread!=null && !job.hasFinished()) {
            long now = System.currentTimeMillis();

            // Do not refresh progress information is job is paused, simply sleep
            if(!job.isPaused()) {
                long currentFileRemainingTime = 0;
                long totalRemainingTime;

                long effectiveJobTime = job.getEffectiveJobTime();
                if(effectiveJobTime==0)
                    effectiveJobTime = 1;   // To avoid potential zero divisions

                if (transferFileJob !=null) {
                    long bytesTotal = transferFileJob.getTotalByteCounter().getByteCount();
                    long totalBps = (long)(bytesTotal*1000d/effectiveJobTime);
                    long currentBps = (long)((bytesTotal-lastBytesTotal)*1000d/(now-lastTime));

                    // Update current file progress bar
                    float filePercentFloat = transferFileJob.getFilePercentDone();
                    int filePercentInt = (int)(100*filePercentFloat);
                    currentFileProgressBar.setValue(filePercentInt);

                    progressText = filePercentInt+"% - ";

                    // Add estimated remaining time (ETA) for current file
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
                        currentFileRemainingTime = (long)((1000*(currentFileSize- transferFileJob.getCurrentFileByteCounter().getByteCount()))/(float)totalBps);
                        progressText += DurationFormat.format(currentFileRemainingTime);
                    }

//                    fileProgressBar.setTextOverlay(progressText);
                    currentFileProgressBar.setString(progressText);

                    // Update total transferred label
                    totalTransferredLabel.setText(
                       Translator.get("progress_dialog.transferred",
                                      SizeFormat.format(bytesTotal, SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_LONG| SizeFormat.ROUND_TO_KB),
                                      SizeFormat.format(totalBps, SizeFormat.UNIT_SPEED| SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT| SizeFormat.ROUND_TO_KB))
                    );

                    // Add new immediate bytes per second speed sample to speed graph and label and repaint it
                    // Skip this sample if job was paused and resumed, speed would not be accurate
                    if(lastTime>job.getPauseStartDate()) {
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

//                totalProgressBar.setTextOverlay(progressText);
                totalProgressBar.setString(progressText);

                // Update current file label
                currentFileLabel.setText(job.getStatusString());

                // Update elapsed time label
                elapsedTimeLabel.setText(Translator.get("progress_dialog.elapsed_time")+": "+DurationFormat.format(effectiveJobTime));
            }

            // Sleep for a while
            try {
//                if(Debug.ON) Debug.trace("sleeping "+(REFRESH_RATE-(System.currentTimeMillis()-now)));
                Thread.sleep(REFRESH_RATE-(System.currentTimeMillis()-now));
            }
            catch(InterruptedException e) {}
        }
	
        dispose();
    }

    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source==stopButton) {
            // Cancel button pressed, dispose dialog and stop job immediately
            // (job will be stopped a second time in windowClosed() but that will just be a no-op)
            dispose();
            job.stop();
        }
        else if(source==pauseResumeButton) {
            boolean isPaused = job.isPaused();

            // Resume the job and change the button's label and icon to 'pause'
            if(isPaused) {
                pauseResumeButton.setText(Translator.get("pause"));
                pauseResumeButton.setIcon(IconManager.getIcon(IconManager.PROGRESS_ICON_SET, PAUSE_ICON));
            }
            // Pause the job and change the button's label and icon to 'resume'
            else {
                pauseResumeButton.setText(Translator.get("resume"));
                pauseResumeButton.setIcon(IconManager.getIcon(IconManager.PROGRESS_ICON_SET, RESUME_ICON));

                if(transferFileJob!=null)
                    updateCurrentSpeedLabel("N/A");
            }

            // Update buttons mnemonics
            buttonsChoicePanel.updateMnemonics();
            // Pause/resume job
            job.setPaused(!isPaused);
        }
//        else if(source==hideButton) {
//            mainFrame.setState(Frame.ICONIFIED);
//        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
//if(Debug.ON) Debug.trace("called, source="+e.getSource()+" isEnabled="+limitSpeedCheckBox.isEnabled());
        Object source = e.getSource();
        if(source==limitSpeedCheckBox) {
            boolean isEnabled = limitSpeedCheckBox.isSelected();
            limitSpeedSpinner.setEnabled(isEnabled);
            speedUnitComboBox.setEnabled(isEnabled);
            updateThroughputLimit();
        }
        else if(source==speedUnitComboBox) {
            updateThroughputLimit();
        }
    }


    ///////////////////////////////////
    // ChangeListener implementation //
    ///////////////////////////////////

    public void stateChanged(ChangeEvent e) {
        if(e.getSource()==limitSpeedSpinner) {
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

        // Stop threads
        repaintThread = null;
        // Job may have already been stopped if cancel button was pressed
        job.stop();
        // Remember 'advanced panel' expanded state
        if(collapseExpandButton!=null)
            ConfigurationManager.setVariableBoolean(EXPANDED_CONF_VAR, collapseExpandButton.getExpandedState());
    }



    private class SpeedGraph extends JPanel {

        private final Color BACKGROUND_COLOR = ConfigurationManager.getVariableColor("prefs.colors.background", null);

        private final Color GRAPH_COLOR = ConfigurationManager.getVariableColor("prefs.colors.selectionBackground", null);

        private final Color BPS_LIMIT_COLOR = ConfigurationManager.getVariableColor("prefs.colors.marked", null);

        private final int LINE_SPACING = 6;

        private final int NB_SAMPLES_MAX = 320;

        private final int STROKE_WIDTH = 2;

        private Vector samples = new Vector(NB_SAMPLES_MAX);

        private Stroke lineStroke = new BasicStroke(STROKE_WIDTH);


        private SpeedGraph() {
        }


        private void addSample(long bytesPerSecond) {
            // Capacity reached, remove first sample
            if(samples.size()==NB_SAMPLES_MAX)
                samples.removeElementAt(0);

            // Add sample to the vector
            samples.add(new Long(bytesPerSecond));

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
