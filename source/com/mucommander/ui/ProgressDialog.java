
package com.mucommander.ui;

import com.mucommander.job.FileJob;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.progress.ValueProgressBar;
import com.mucommander.ui.comp.button.ButtonChoicePanel;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.dialog.EscapeKeyAdapter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This dialog informs the user of the progress made by a Job.
 */
public class ProgressDialog extends FocusDialog implements Runnable, ActionListener, KeyListener {
    private JLabel progressLabel;
    private JProgressBar totalProgressBar;
    private JProgressBar fileProgressBar;
    private JButton cancelButton;
    private JButton hideButton;

    private FileJob job;
    private Thread repaintThread;
	private boolean dualBar;

	// Dialog width is constrained to 320, height is not an issue (always the same)
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);	
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	

private MainFrame mainFrame;

    public ProgressDialog(MainFrame mainFrame, String title, boolean dualBar) {
        super(mainFrame, title, mainFrame);

this.mainFrame = mainFrame;

		// Sets maximum and minimum dimensions for this dialog
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
				
		this.dualBar = dualBar;
		
        Container contentPane = getContentPane();
        progressLabel = new JLabel();
//        progressLabel = new JLabel(job.getCurrentInfo());
        totalProgressBar = new ValueProgressBar();
        
		Panel tempPanel;
		if (dualBar) {
			tempPanel = new Panel(new GridLayout(2,1));
        	
			Panel tempPanel2 = new Panel(new BorderLayout());
        	tempPanel2.add(progressLabel, BorderLayout.NORTH);
        	fileProgressBar = new ValueProgressBar();
			tempPanel2.add(fileProgressBar, BorderLayout.CENTER);
			tempPanel.add(tempPanel2);
			
			tempPanel2 = new Panel(new BorderLayout());
			tempPanel2.add(new JLabel("Total:"), BorderLayout.NORTH);
			tempPanel2.add(totalProgressBar, BorderLayout.CENTER);
			tempPanel.add(tempPanel2);
		}	
		else {
			tempPanel = new Panel(new BorderLayout());
			tempPanel.add(progressLabel, BorderLayout.NORTH);
			tempPanel.add(totalProgressBar, BorderLayout.CENTER);
		}
		contentPane.add(tempPanel, BorderLayout.CENTER);
        
//        tempPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
//        tempPanel = new Panel(new BorderLayout());
		cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.addKeyListener(this);
		hideButton = new JButton("Hide");
		hideButton.addActionListener(this);
		hideButton.addKeyListener(this);
		// Cancel button receives initial focus
		setInitialFocusComponent(cancelButton);
		// Enter triggers cancel button
		getRootPane().setDefaultButton(cancelButton);
		contentPane.add(new ButtonChoicePanel(new JButton[] {cancelButton, hideButton}, 0, getRootPane()), BorderLayout.SOUTH);
    }

	/**
	 * This method MUST be called before start().
	 */
	public void setFileJob(FileJob job) {
		this.job = job;
	}

    public void start() {
	    progressLabel.setText(job.getCurrentInfo());
		
        repaintThread = new Thread(this);
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
        
        totalPercent = lastTotalPercent = job.getTotalPercentDone();
        filePercent = lastFilePercent = job.getFilePercentDone();
		currentInfo = lastInfo = job.getCurrentInfo();

        while(repaintThread!=null && !job.hasFinished()) {
	        // Updates totalProgressBar if necessary
	        if (dualBar) {
				filePercent = job.getFilePercentDone(); 
		        if(lastFilePercent!=filePercent) {
		            fileProgressBar.setValue(filePercent);
		            fileProgressBar.repaint();
		            lastFilePercent = filePercent;
		        }
			}
        	
		
            // Updates totalProgressBar if necessary
            totalPercent = job.getTotalPercentDone(); 
			if(lastTotalPercent!=totalPercent) {
                totalProgressBar.setValue(totalPercent);
                totalProgressBar.repaint();
                lastTotalPercent = totalPercent;
            }
            
            // Updates progressLabel if necessary 
            currentInfo = job.getCurrentInfo();
            if(!lastInfo.equals(currentInfo)) {
                progressLabel.setText(currentInfo);
                progressLabel.repaint();
                repaint();
                lastInfo = currentInfo;
            }

            try { Thread.sleep(100); }
            catch(InterruptedException e) {}
        }
	
        dispose();
	}

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


    /***********************
     * KeyListener methods *
     ***********************/

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
}
