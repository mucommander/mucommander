
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.job.PropertiesJob;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.NumberFormat;

import java.util.Vector;

public class PropertiesDialog extends FocusDialog implements Runnable, ActionListener {
	private MainFrame mainFrame;
	private PropertiesJob job;
	private Thread repaintThread;
	
	private JLabel counterLabel;
	private JLabel sizeLabel;

	private JButton okButton;

	// Dialog width is constrained to 320, height is not an issue (always the same)
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);	
	
	// Window title without status
	private String title;
	
	public PropertiesDialog(MainFrame mainFrame, Vector files) {
		super(mainFrame, "", mainFrame);
		this.mainFrame = mainFrame;

		AbstractFile firstFile = (AbstractFile)files.elementAt(0);
		setTitle((title=files.size()==1?firstFile.getName()+" Properties":"Properties"));
		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		this.job = new PropertiesJob(files);
		
		Container contentPane = getContentPane();
		
		JPanel gridPanel = new JPanel(new GridLayout(0,1));
		
		// Contents (set later)
		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel("<html><b>Contents: </b></html>"));
		counterLabel = new JLabel("");
		tempPanel.add(counterLabel);
		gridPanel.add(tempPanel);

		// Location (set here)
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String folderString = firstFile.getParent().getAbsolutePath()+firstFile.getSeparator();
		JLabel locationLabel = new JLabel("<html><b>Location:</b> "+folderString+"</html>");
		locationLabel.setToolTipText(folderString);
		tempPanel.add(locationLabel);
		gridPanel.add(tempPanel);

		// Size (set later)
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel("<html><b>Size: </b></html>"));
		sizeLabel = new JLabel("");
		tempPanel.add(sizeLabel);
		gridPanel.add(tempPanel);

		updateLabels();
		contentPane.add(gridPanel, BorderLayout.NORTH);
		
		okButton = new JButton("OK");
		// Escape key disposes dialog
		okButton.addKeyListener(new EscapeKeyAdapter(this));
		contentPane.add(DialogToolkit.createOKPanel(okButton, this), BorderLayout.SOUTH);

		// OK button will receive initial focus
		setInitialFocusComponent(okButton);		
		
		// Selects OK when enter is pressed
		getRootPane().setDefaultButton(okButton);

		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
		start();
	}


	private void updateLabels() {
		int nbFiles = job.getNbFiles();
		int nbFolders = job.getNbFolders();
		counterLabel.setText("<html>"
			+(nbFiles>0?nbFiles+" file"+(nbFiles>1?"s":""):"")
			+(nbFiles>0&&nbFolders>0?", ":"")
			+(nbFolders>0?nbFolders+ " folder"+(nbFolders>1?"s":""):"")+"</html>");

		sizeLabel.setText("<html>"+NumberFormat.getInstance().format(job.getTotalBytes())+" bytes</html>");

		repaint();
	}


	public void start() {
		job.start();
		
		repaintThread = new Thread(this);
		repaintThread.start();
	}

	
	public void stop() {
		job.stop();
		repaintThread = null;
	}
	

	public void run() {
		while(repaintThread!=null && !job.hasFinished()) {
			updateLabels();
			
			try { Thread.sleep(100); }
			catch(InterruptedException e) {}
		}

		updateLabels();
		setTitle(title);
		mainFrame.setCursor(Cursor.getDefaultCursor());
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==okButton) {
			stop();
			dispose();
		}
	}
}