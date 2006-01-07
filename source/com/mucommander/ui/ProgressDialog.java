
package com.mucommander.ui;

import com.mucommander.job.FileJob;
import com.mucommander.job.ExtendedFileJob;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.progress.OverlayProgressBar;
import com.mucommander.ui.comp.button.ButtonChoicePanel;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This dialog informs the user of the progress made by a Job.
 *
 * @author Maxence Bernard
 */
public class ProgressDialog extends FocusDialog implements Runnable, ActionListener, KeyListener, WindowListener {
    private JLabel infoLabel;
    private JLabel statsLabel;
    private OverlayProgressBar totalProgressBar;
    private OverlayProgressBar fileProgressBar;
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
    
//addWindowListener(this);
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
		tempPanel.add(Box.createVerticalGlue());
		contentPane.add(tempPanel, BorderLayout.CENTER);
        
		cancelButton = new JButton(Translator.get("cancel"));
        cancelButton.addActionListener(this);
        cancelButton.addKeyListener(this);
		hideButton = new JButton(Translator.get("progress_bar.hide"));
		hideButton.addActionListener(this);
		hideButton.addKeyListener(this);
		// Cancel button receives initial focus
		setInitialFocusComponent(cancelButton);
		// Enter triggers cancel button
		getRootPane().setDefaultButton(cancelButton);
		contentPane.add(new ButtonChoicePanel(new JButton[] {cancelButton, hideButton}, 0, getRootPane()), BorderLayout.SOUTH);
    }


    //////////////////////
	// Runnable methods //
    //////////////////////

    public void start(FileJob job) {
        this.job = job;
        this.dualBar = job instanceof ExtendedFileJob;
        initUI();
        
        repaintThread = new Thread(this, "com.mucommander.ui.ProgressDialog's Thread");
        repaintThread.start();

    	showDialog();
	}
    
    public void run() {
	    // Used for dual bars
		int filePercent;
    	int lastFilePercent;

        int totalPercent;
        int lastTotalPercent;

        String currentInfo;
        String lastInfo;

		long nbBytesTotal;
		long lastBytesTotal = 0;
        
        totalPercent = lastTotalPercent = -1;
        filePercent = lastFilePercent = -1;
        currentInfo = lastInfo = "";

        ExtendedFileJob extendedJob = null;
        if(dualBar)
            extendedJob = (ExtendedFileJob)job;

		long speed;
//		long startTime = job.getStartTime();
		// Start time will only be available after this dialog has been activated
		long startTime;
		long now;
		long pausedTime;
		while(repaintThread!=null && !job.hasFinished()) {
	        if (dualBar) {
				// Updates fileProgressBar if necessary
				filePercent = extendedJob.getFilePercentDone(); 
		        if(lastFilePercent!=filePercent) {
		            // Updates file progress bar
                    fileProgressBar.setValue(filePercent);
                    fileProgressBar.setTextOverlay(filePercent+"%");
		            fileProgressBar.repaint(REFRESH_RATE);

		            lastFilePercent = filePercent;
                }

				// Update stats if necessary
				nbBytesTotal = job.getTotalBytesProcessed();
//				nbBytesSkipped = job.getTotalBytesSkipped();
				pausedTime = job.getPausedTime();
				startTime = job.getStartTime();
				if(lastBytesTotal!=nbBytesTotal) {
					now = System.currentTimeMillis();
//					speed = (long)((nbBytesTotal-nbBytesSkipped)/((now-startTime-pausedTime)/1000d));
					speed = (long)(nbBytesTotal/((now-startTime-pausedTime)/1000d));
//if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("nbBytesTotal="+nbBytesTotal+"nbBytesSkipped="+nbBytesSkipped+" startTime="+startTime+" pausedTime="+pausedTime+" now="+now+" speed="+speed+" speedStr="+SizeFormatter.format(speed, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB));
					statsLabel.setText(
						Translator.get("progress_bar.transferred",
							SizeFormatter.format(nbBytesTotal, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_LONG|SizeFormatter.ROUND_TO_KB),
							SizeFormatter.format(speed, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)));
					statsLabel.repaint(REFRESH_RATE);
	
					lastBytesTotal = nbBytesTotal;
				}
			}
        	
		
            // Updates totalProgressBar if necessary
            if(totalProgressBar!=null) {
				totalPercent = job.getTotalPercentDone();
				if(lastTotalPercent!=totalPercent) {
					// Updates total progress bar 
					totalProgressBar.setValue(totalPercent);
					totalProgressBar.setTextOverlay(totalPercent+"% ");
					totalProgressBar.repaint(REFRESH_RATE);
									
					lastTotalPercent = totalPercent;
				}
			}
            
            // Updates infoLabel if necessary 
            currentInfo = job.getStatusString();
            if(!lastInfo.equals(currentInfo)) {
                infoLabel.setText(currentInfo);
                infoLabel.repaint(REFRESH_RATE);
                lastInfo = currentInfo;
            }

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
			// Cancel button pressed, let's stop deleting
	        job.stop();
	        repaintThread = null;
	        dispose();
		}
		else if(source==hideButton) {
			mainFrame.setState(Frame.ICONIFIED);
		}
    }


    /////////////////////////
	// KeyListener methods //
    /////////////////////////

     public void keyPressed(KeyEvent e) {
     	int keyCode = e.getKeyCode();
		
     	// Disposes the dialog on escape key
     	if (keyCode==KeyEvent.VK_ESCAPE) {
     		job.stop();
			dispose();
     	}
     }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }


	////////////////////////////
	// WindowListener methods // 
	////////////////////////////

	public void windowOpened(WindowEvent e) {
		// (this method is called first time the dialog is made visible)
		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
		super.windowOpened(e);
	}

	public void windowActivated(WindowEvent e) {
		// (this method is called each time the dialog is activated)
		super.windowActivated(e);
		if(firstTimeActivated) {
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("first time activated, starting job!");
			firstTimeActivated = false;
			this.job.start();
		}
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

}
