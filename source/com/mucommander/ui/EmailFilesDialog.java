
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveFile;
import com.mucommander.job.SendMailJob;
import com.mucommander.text.SizeFormatter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.IOException;

import java.util.Vector;

public class EmailFilesDialog extends FocusDialog implements ActionListener, ItemListener {
	private MainFrame mainFrame;
	
	private Vector files;
	
	private JTextField toField;
	private JTextField subjectField;
	private JTextArea bodyArea;
	private JLabel infoLabel;
	private JCheckBox fileCheckboxes[];

	private static String lastTo = "";
	private static String lastSubject = "";
	private static String lastBody = "";

	private JButton okButton;
	private JButton cancelButton;

	// Dialog size constrains
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(480,400);	
	
	
	public EmailFilesDialog(MainFrame mainFrame) {
		super(mainFrame, "Email files", mainFrame);
		this.mainFrame = mainFrame;

		try {
			// Figures out which files to send and calculates the number of files and the number of bytes
			Vector selectedFiles = mainFrame.getLastActiveTable().getSelectedFiles();
			files = getFlattenedFiles(selectedFiles);
			
			Container contentPane = getContentPane();
			EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	
			mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
	
			// To (recipients) field
			JPanel tempPanel = new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
			tempPanel.add(new JLabel("To"));
			tempPanel.add(Box.createRigidArea(new Dimension(5, 0)));		
			toField = new JTextField(lastTo);
			toField.addKeyListener(escapeKeyAdapter);
			tempPanel.add(toField);
			tempPanel.setAlignmentX(LEFT_ALIGNMENT);
			mainPanel.add(tempPanel);
	
			mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			
			// Subject field
			tempPanel = new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
			tempPanel.add(new JLabel("Subject"));
			tempPanel.add(Box.createRigidArea(new Dimension(5, 0)));		
			subjectField = new JTextField(lastSubject);
			subjectField.addKeyListener(escapeKeyAdapter);
			tempPanel.add(subjectField);
			tempPanel.setAlignmentX(LEFT_ALIGNMENT);
			mainPanel.add(tempPanel);		
	
			mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
	
			// Body area
			bodyArea = new JTextArea(lastBody);
			bodyArea.addKeyListener(escapeKeyAdapter);
			bodyArea.setRows(6); 
			JScrollPane scrollPane = new JScrollPane(bodyArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setAlignmentX(LEFT_ALIGNMENT);
			mainPanel.add(scrollPane);
			
			mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
	
			
			// Label showing the number of files and total size
			infoLabel = new JLabel();
			infoLabel.setAlignmentX(LEFT_ALIGNMENT);
			mainPanel.add(infoLabel);			
			
			// checkbox showing all files that are to be sent, allowing them to be unselected
			tempPanel = new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
			int nbFiles = files.size();
			fileCheckboxes = new JCheckBox[nbFiles];
			for(int i=0; i<nbFiles; i++) {
				fileCheckboxes[i] = new JCheckBox(((AbstractFile)files.elementAt(i)).getName(), true);
				fileCheckboxes[i].addItemListener(this);
				fileCheckboxes[i].addKeyListener(escapeKeyAdapter);
				tempPanel.add(fileCheckboxes[i]);
			}
			scrollPane = new JScrollPane(tempPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setAlignmentX(LEFT_ALIGNMENT);
			updateInfoLabel();
			contentPane.add(scrollPane, BorderLayout.CENTER);
	
			mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	
			contentPane.add(mainPanel, BorderLayout.NORTH);
			
			// OK / Cancel buttons panel
			okButton = new JButton("OK");
			cancelButton = new JButton("Cancel");
			// Escape key disposes dialog
			okButton.addKeyListener(escapeKeyAdapter);
			cancelButton.addKeyListener(escapeKeyAdapter);
			contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);
	
			// 'To' field will receive initial focus
			setInitialFocusComponent(toField);		
			
			// Selects OK when enter is pressed
			getRootPane().setDefaultButton(okButton);
	
			// Packs dialog
			setResizable(true);
			setMinimumSize(MINIMUM_DIALOG_DIMENSION);
			setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(this, "Unable to read files in subfolders.", "Email files error", JOptionPane.ERROR_MESSAGE);	
		}
	}


	/**
	 * Updates the number of selected files and their total size.
	 */
	private void updateInfoLabel() {
		int nbFiles = fileCheckboxes.length;
		int nbSelected = 0;
		int bytesTotal = 0;
		for(int i=0; i<nbFiles; i++) {
			if(fileCheckboxes[i].isSelected()) {
				bytesTotal += ((AbstractFile)files.elementAt(i)).getSize();
				nbSelected++;
			}
		}
		String text = nbSelected
			+(nbSelected>1?" files":" file")
			+(nbSelected==0?"":" ("+SizeFormatter.format(bytesTotal, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_LONG)+")");
		infoLabel.setText(text);
		infoLabel.repaint(100);
	}


	/**
	 * Returns a Vector of *files* (as opposed to folders) that have been found either in the given 
	 * Vector or in one of the subfolders. 
	 *
	 * @param originalFiles files as selected by the user which may contain folders
	 */
	private Vector getFlattenedFiles(Vector originalFiles) throws IOException {
		int nbFiles = originalFiles.size();
		Vector flattenedFiles = new Vector();
		for(int i=0; i<nbFiles; i++)
			recurseOnFolder((AbstractFile)originalFiles.elementAt(i), flattenedFiles);

		return flattenedFiles;
	}

	/**
	 * Adds the given file to the Vector if it's not a folder, recurses otherwise.
	 */
	private void recurseOnFolder(AbstractFile file, Vector flattenedFiles) throws IOException {
		if(file.isFolder() && !(file instanceof ArchiveFile)) {
			AbstractFile children[] = file.ls();
			for(int i=0; i<children.length; i++)
				recurseOnFolder(children[i], flattenedFiles);
		}
		else {
			flattenedFiles.add(file);
		}
	}
	
	

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		// OK Button
		if (source==okButton)  {
			String to = toField.getText().trim();
			String subject = subjectField.getText();
			String body = bodyArea.getText();
			if(!to.equals("")) {
				lastTo = to;
				lastSubject = subject;
				lastBody = body;
				
				// Starts by disposing the dialog
				dispose();

				// Creates new Vector with files that have been selected
				Vector filesToSend = new Vector();
				int nbFiles = fileCheckboxes.length;
				for(int i=0; i<nbFiles; i++)
					if(fileCheckboxes[i].isSelected())
						filesToSend.add(files.elementAt(i));

				// Starts sending files
				ProgressDialog progressDialog = new ProgressDialog(mainFrame, "Sending files");
				SendMailJob mailJob = new SendMailJob(mainFrame, progressDialog, filesToSend, to, subject, body);
				mailJob.start();
				progressDialog.start(mailJob);
			}
		}
		// Cancel button
		else if (source==cancelButton)  {
			dispose();			
		}
	}
	
	
	/**
	 * Updates label text whenever a checkbox has been checked or unchecked.
	 */
	public void itemStateChanged(ItemEvent e) {	
		updateInfoLabel();
	}


}