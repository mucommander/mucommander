
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.job.PropertiesJob;
import com.mucommander.text.Translator;
import com.mucommander.text.SizeFormatter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.NumberFormat;

import java.util.Vector;


/**
 * This dialog shows properties of a file or a group of files : number of files, file kind,
 * combined size and location.
 *
 * @author Maxence Bernard
 */
public class PropertiesDialog extends FocusDialog implements Runnable, ActionListener {
	private MainFrame mainFrame;
	private PropertiesJob job;
	private Thread repaintThread;
	
	private JLabel counterLabel;
	private JLabel sizeLabel;

	private JButton okButton;

	// Dialog width is constrained to 320, height is not an issue (always the same)
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	

	/** How often should progress information be refreshed (in ms) */
	private final static int REFRESH_RATE = 500;
	
	/* Window title without status */
	private String title;

	
	public PropertiesDialog(MainFrame mainFrame, Vector files) {
		super(mainFrame, "", mainFrame);
		this.mainFrame = mainFrame;

		AbstractFile firstFile = (AbstractFile)files.elementAt(0);
		this.title = files.size()==1?Translator.get("properties_dialog.file_properties", firstFile.getName()):Translator.get("properties_dialog.properties");
		setTitle(title+" ("+Translator.get("properties_dialog.calculating")+")");
		
		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		this.job = new PropertiesJob(files, mainFrame);
		
		Container contentPane = getContentPane();
	
/*	
		JPanel gridPanel = new JPanel(new GridLayout(0,1));
		
		// Contents (set later)
		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel("<html><b>"+Translator.get("properties_dialog.contents")+": </b></html>"));
		counterLabel = new JLabel("");
		tempPanel.add(counterLabel);
		gridPanel.add(tempPanel);

		// Location (set here)
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		String folderString = firstFile.getParent().getAbsolutePath(true);
		JLabel locationLabel = new JLabel("<html><b>"+Translator.get("properties_dialog.location")+": </b>"+folderString+"</html>");
		locationLabel.setToolTipText(folderString);
		tempPanel.add(locationLabel);
		gridPanel.add(tempPanel);

		// Size (set later)
		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel("<html><b>"+Translator.get("properties_dialog.size")+": </b></html>"));
		sizeLabel = new JLabel("");
		tempPanel.add(sizeLabel);
		gridPanel.add(tempPanel);

		updateLabels();
		contentPane.add(gridPanel, BorderLayout.NORTH);
*/

		TextFieldsPanel mainPanel = new TextFieldsPanel(10);
		
		// Contents (set later)
		counterLabel = new JLabel("");
		mainPanel.addTextFieldRow(Translator.get("properties_dialog.contents")+":", counterLabel, 10);

		// Location (set here)
		String location = firstFile.getParent().getAbsolutePath(true);
		JLabel locationLabel = new JLabel(location);
		locationLabel.setToolTipText(location);
		mainPanel.addTextFieldRow(Translator.get("properties_dialog.location")+":", locationLabel, 10);

		// Size (set later)
		sizeLabel = new JLabel("");
		mainPanel.addTextFieldRow(Translator.get("properties_dialog.size")+":", sizeLabel, 5);

		updateLabels();
		YBoxPanel yPanel = new YBoxPanel(5);
		yPanel.add(mainPanel);
		contentPane.add(yPanel, BorderLayout.NORTH);
		
		okButton = new JButton(Translator.get("ok"));
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
		int nbFiles = job.getNbFilesRecurse();
		int nbFolders = job.getNbFolders();
		counterLabel.setText("<html>"
			+(nbFiles>0?Translator.get("properties_dialog.nb_files", ""+nbFiles):"")
			+(nbFiles>0&&nbFolders>0?", ":"")
			+(nbFolders>0?Translator.get("properties_dialog.nb_folders", ""+nbFolders):"")+"</html>");

//		sizeLabel.setText("<html>"+Translator.get("properties_dialog.nb_bytes", NumberFormat.getInstance().format(job.getTotalBytes()))+"</html>");
		sizeLabel.setText("<html>"+SizeFormatter.format(job.getTotalBytes(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)+"</html>");
		
		counterLabel.repaint(REFRESH_RATE);
		sizeLabel.repaint(REFRESH_RATE);
	}


	public void start() {
		job.start();
		
		repaintThread = new Thread(this, "com.mucommander.ui.PropertiesDialog's Thread");
		repaintThread.start();
	}

	
	public void stop() {
		job.stop();
		repaintThread = null;
	}
	

	public void run() {
		
		while(repaintThread!=null && !job.hasFinished()) {
			updateLabels();
			
			try { Thread.sleep(REFRESH_RATE); }
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