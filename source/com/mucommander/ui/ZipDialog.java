package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.ZipJob;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;


/**
 * This dialog allows the user to zip selected files to a specified file
 * and add a comment in the zip file.
 *
 * @author Maxence Bernard
 */
public class ZipDialog extends FocusDialog implements ActionListener {

	private MainFrame mainFrame;

	/** Files to zip */
	private Vector files;
	
	private JTextField filePathField;
	private JTextArea commentArea;
	private JButton okButton;
	private JButton cancelButton;

	private final static int CANCEL_ACTION = 0;
	private final static int REPLACE_ACTION = 1;

	// Dialog's width has to be at least 240
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	

	// Dialog's width has to be at most 320
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);	


	public ZipDialog(MainFrame mainFrame, Vector files, boolean isShiftDown) {
		super(mainFrame, Translator.get("zip_dialog.title"), mainFrame);

		this.mainFrame = mainFrame;
		this.files = files;
		
		Container contentPane = getContentPane();
		
		YBoxPanel mainPanel = new YBoxPanel(5);
		JLabel label = new JLabel(Translator.get("zip_dialog_description")+" :");
		mainPanel.add(label);

		FileTable activeTable = mainFrame.getUnactiveTable();
		String initialPath = (isShiftDown?"":activeTable.getCurrentFolder().getAbsolutePath(true))+".zip";
		filePathField = new JTextField(initialPath);
		filePathField.setCaretPosition(initialPath.length()-4);
//		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
//		filePathField.addKeyListener(escapeKeyAdapter);
		mainPanel.add(filePathField);
		
		mainPanel.addSpace(10);
		
		label = new JLabel(Translator.get("zip_dialog.comment"));
		mainPanel.add(label);
		commentArea = new JTextArea();
		commentArea.setRows(4);
//		commentArea.addKeyListener(escapeKeyAdapter);
		mainPanel.add(commentArea);

		mainPanel.addSpace(10);

		contentPane.add(mainPanel, BorderLayout.NORTH);
				
		okButton = new JButton(Translator.get("ok"));
		cancelButton = new JButton(Translator.get("cancel"));
		// Escape key disposes dialog
//		okButton.addKeyListener(escapeKeyAdapter);
//		cancelButton.addKeyListener(escapeKeyAdapter);
		contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

		// text field will receive initial focus
		setInitialFocusComponent(filePathField);		
		
		// Selects OK when enter is pressed
		getRootPane().setDefaultButton(okButton);

		// Packs dialog
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
	
		showDialog();
	}
	

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source==okButton)  {
			// Starts by disposing the dialog
			dispose();

			// Checks that destination file can be resolved 
			String filePath = filePathField.getText();
			Object dest[] = mainFrame.resolvePath(filePath);
			if (dest==null || dest[1]==null) {
				// Incorrect destination
				QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("zip_dialog.error_title"), Translator.get("this_folder_does_not_exist", filePath), mainFrame,
					new String[] {Translator.get("ok")},
					new int[]  {0},
					0);
				dialog.getActionValue();
				return;
			}

			AbstractFile destFile = AbstractFile.getAbstractFile(((AbstractFile)dest[0]).getAbsolutePath(true)+(String)dest[1]);

			// Starts zipping
			ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("zip_dialog.zipping"));
			ZipJob zipJob = new ZipJob(progressDialog, mainFrame, files, commentArea.getText(), destFile);
			progressDialog.start(zipJob);
		}
		else if (source==cancelButton)  {
			dispose();			
		}
	}


}