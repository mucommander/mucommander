
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.pref.PreferencesDialog;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveFile;
import com.mucommander.file.FileSet;
import com.mucommander.job.SendMailJob;
import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;
import com.mucommander.conf.ConfigurationManager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.IOException;


/**
 * Dialog allowing the user to email files to someone.
 *
 * <p>One or several recipients, as well as a mail subject and body can be input.
 * The dialog also allows the user to review the files that have been marked,
 * select/unselect some, and displays the total file size.</p>
 *
 * @author Maxence Bernard
 */
public class EmailFilesDialog extends FocusDialog implements ActionListener, ItemListener {
	private MainFrame mainFrame;
	
	private FileSet files;
	
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

	// Dialog size constraints
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(400,0);	
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(550,400);	
	
	
	public EmailFilesDialog(MainFrame mainFrame, FileSet files) {
		super(mainFrame, Translator.get("email_dialog.title"), mainFrame);
		this.mainFrame = mainFrame;

		// Notifies the user that mail preferences are not set and brings the preferences dialog 
		if(!SendMailJob.mailPreferencesSet()) {
			JOptionPane.showMessageDialog(mainFrame, Translator.get("email_dialog.prefs_not_set"), Translator.get("email_dialog.prefs_not_set_title"), JOptionPane.INFORMATION_MESSAGE);
	
			PreferencesDialog preferencesDialog = new PreferencesDialog(mainFrame);
			preferencesDialog.setActiveTab(PreferencesDialog.MAIL_TAB);
			preferencesDialog.showDialog();
			
			return;
		}
		
		
		try {
			// Figures out which files to send and calculates the number of files and the number of bytes
			this.files = getFlattenedFiles(files);
			
			Container contentPane = getContentPane();
//			EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
			
			YBoxPanel mainPanel = new YBoxPanel(5);
	
			// Text fields panel
			TextFieldsPanel textFieldsPanel = new TextFieldsPanel(5);
			textFieldsPanel.setAlignmentX(LEFT_ALIGNMENT);

			// From (sender) field, non editable
			JLabel fromLabel = new JLabel(ConfigurationManager.getVariable("prefs.mail.sender_name")
				+" <"+ConfigurationManager.getVariable("prefs.mail.sender_address")+">");
//			fromField.setEditable(false);
			textFieldsPanel.addTextFieldRow(Translator.get("email_dialog.from")+":", fromLabel, 10);
			
			// To (recipients) field
			toField = new JTextField(lastTo);
//			toField.addKeyListener(escapeKeyAdapter);
			textFieldsPanel.addTextFieldRow(Translator.get("email_dialog.to")+":", toField, 10);
			
			// Subject field
			subjectField = new JTextField(lastSubject);
//			subjectField.addKeyListener(escapeKeyAdapter);
			textFieldsPanel.addTextFieldRow(Translator.get("email_dialog.subject")+":", subjectField, 15);

			mainPanel.add(textFieldsPanel);		
	
			// Body area
			bodyArea = new JTextArea(lastBody);
//			bodyArea.addKeyListener(escapeKeyAdapter);
			bodyArea.setRows(6);
			bodyArea.setLineWrap(true);
			JScrollPane scrollPane = new JScrollPane(bodyArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			mainPanel.add(scrollPane);
			
			mainPanel.addSpace(15);
			
			// Label showing the number of files and total size
			infoLabel = new JLabel();
			mainPanel.add(infoLabel);			

			contentPane.add(mainPanel, BorderLayout.NORTH);
			
			// checkbox showing all files that are to be sent, allowing them to be unselected
			int nbFiles = files.size();
			fileCheckboxes = new JCheckBox[nbFiles];
			if(nbFiles>0) {
				YBoxPanel tempPanel2 = new YBoxPanel();
				AbstractFile file;
				for(int i=0; i<nbFiles; i++) {
					file = (AbstractFile)files.elementAt(i);
					fileCheckboxes[i] = new JCheckBox(file.getName()
						+" ("+SizeFormatter.format(file.getSize(), SizeFormatter.DIGITS_SHORT|SizeFormatter.UNIT_SHORT|SizeFormatter.INCLUDE_SPACE|SizeFormatter.ROUND_TO_KB)+")", true);
					fileCheckboxes[i].addItemListener(this);
//					fileCheckboxes[i].addKeyListener(escapeKeyAdapter);
					tempPanel2.add(fileCheckboxes[i]);
				}
				scrollPane = new JScrollPane(tempPanel2, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				contentPane.add(scrollPane, BorderLayout.CENTER);
			}
			updateInfoLabel();
				
			// OK / Cancel buttons panel
			okButton = new JButton(Translator.get("email_dialog.send"));
			cancelButton = new JButton(Translator.get("cancel"));
			// Escape key disposes dialog
//			okButton.addKeyListener(escapeKeyAdapter);
//			cancelButton.addKeyListener(escapeKeyAdapter);
			contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);
	
			// 'To' field will receive initial focus
			setInitialFocusComponent(toField);		
			
			// Selects OK when enter is pressed
			getRootPane().setDefaultButton(okButton);
	
			// Packs dialog
			setMinimumSize(MINIMUM_DIALOG_DIMENSION);
			setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
			showDialog();
		}
		catch(IOException e) {
			JOptionPane.showMessageDialog(this, Translator.get("email_dialog.read_error"), Translator.get("email_dialog.error_title"), JOptionPane.ERROR_MESSAGE);	
		}
	}


	/**
	 * Updates the number of selected files and their total size.
	 */
	private void updateInfoLabel() {
		int nbFiles = fileCheckboxes.length;
		int nbSelected = 0;
		int bytesTotal = 0;
		long fileSize;
		for(int i=0; i<nbFiles; i++) {
			if(fileCheckboxes[i].isSelected()) {
				fileSize = ((AbstractFile)files.elementAt(i)).getSize();
				if(fileSize>0)
					bytesTotal += fileSize;
				nbSelected++;
			}
		}
		String text = 
//			nbSelected + (nbSelected>1?" files":" file")
			Translator.get("email_dialog.nb_files", ""+nbSelected)
			+(nbSelected==0?"":" ("+SizeFormatter.format(bytesTotal, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_LONG|SizeFormatter.ROUND_TO_KB)+")");
		infoLabel.setText(text);
		infoLabel.repaint(100);
	}


	/**
	 * Returns a FileSet of *files* (as opposed to folders) that have been found either in the given 
	 * FileSet or in one of the subfolders. 
	 *
	 * @param originalFiles files as selected by the user which may contain folders
	 */
	private FileSet getFlattenedFiles(FileSet originalFiles) throws IOException {
		int nbFiles = originalFiles.size();
		FileSet flattenedFiles = new FileSet(originalFiles.getBaseFolder());
		for(int i=0; i<nbFiles; i++)
			recurseOnFolder((AbstractFile)originalFiles.elementAt(i), flattenedFiles);

		return flattenedFiles;
	}

	/**
	 * Adds the given file to the FileSet if it's not a folder, recurses otherwise.
	 */
	private void recurseOnFolder(AbstractFile file, FileSet flattenedFiles) throws IOException {
//		if(file.isFolder() && !(file instanceof ArchiveFile)) {
		if(file.isDirectory() && !file.isSymlink()) {
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

				// Creates new FileSet with files that have been selected
				FileSet filesToSend = new FileSet(files.getBaseFolder());
				int nbFiles = fileCheckboxes.length;
				for(int i=0; i<nbFiles; i++)
					if(fileCheckboxes[i].isSelected())
						filesToSend.add(files.elementAt(i));

				// Starts sending files
				ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("email_dialog.sending"));
				SendMailJob mailJob = new SendMailJob(progressDialog, mainFrame, filesToSend, to, subject, body);
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