package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.ZipJob;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

public class ZipDialog extends FocusDialog implements ActionListener {

	// Dialog's width has to be at least 240
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	

	// Dialog's width has to be at most 320
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(320,10000);	

	private MainFrame mainFrame;

	private JTextField filePathField;
	private JTextArea commentArea;
	private JButton okButton;
	private JButton cancelButton;

	private final static int CANCEL_ACTION = 0;
	private final static int OVERWRITE_ACTION = 1;
	private final static int APPEND_ACTION = 2;

	private final static String CANCEL_TEXT = "Cancel";
	private final static String OVERWRITE_TEXT = "Overwrite";
	private final static String APPEND_TEXT = "Add to zip file";


	public ZipDialog(MainFrame mainFrame, boolean isShiftDown) {
		super(mainFrame, "Add to zip", mainFrame);
		this.mainFrame = mainFrame;
		
		Container contentPane = getContentPane();
		
		YBoxPanel mainPanel = new YBoxPanel(5);
		JLabel label = new JLabel("Add selected files to:");
		mainPanel.add(label);

		FileTable activeTable = mainFrame.getUnactiveTable();
		String initialPath = (isShiftDown?"":activeTable.getCurrentFolder().getAbsolutePath(true))+".zip";
		filePathField = new JTextField(initialPath);
		filePathField.setCaretPosition(initialPath.length()-4);
		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
		filePathField.addKeyListener(escapeKeyAdapter);
		mainPanel.add(filePathField);
		
		mainPanel.addSpace(10);
		
		label = new JLabel("Comment (optional)");
		mainPanel.add(label);
		commentArea = new JTextArea();
		commentArea.setRows(4);
		commentArea.addKeyListener(escapeKeyAdapter);
		mainPanel.add(commentArea);

		mainPanel.addSpace(10);

		contentPane.add(mainPanel, BorderLayout.NORTH);
				
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		// Escape key disposes dialog
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton.addKeyListener(escapeKeyAdapter);
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

			// Figures out which files to zip
			Vector filesToZip = mainFrame.getLastActiveTable().getSelectedFiles();
			
			boolean append = false;
			
			String filePath = filePathField.getText();
			Object dest[] = mainFrame.resolvePath(filePath);
			if (dest==null || dest[1]==null) {
				// Incorrect destination
				QuestionDialog dialog = new QuestionDialog(mainFrame, "Incorrect destination", filePath+" is not a valid file path.", mainFrame,
					new String[] {"OK"},
					new int[]  {0},
					0);
				dialog.getActionValue();
				return;
			}
			
			AbstractFile destFile = AbstractFile.getAbstractFile(((AbstractFile)dest[0]).getAbsolutePath(true)+(String)dest[1]);
			if (destFile.exists()) {
				// File already exists: cancel, append or overwrite?
				QuestionDialog dialog = new QuestionDialog(mainFrame, "Zip warning", "A file with the same name already exists.", mainFrame,
					new String[] {CANCEL_TEXT, OVERWRITE_TEXT},
					new int[]  {CANCEL_ACTION, OVERWRITE_ACTION},
					0);
				int ret = dialog.getActionValue();
				
				// User cancelled
				if(ret==-1 || ret==CANCEL_ACTION)		// CANCEL_ACTION or close dialog
					return;
			}

				
			java.io.OutputStream destOut = null;
			// Tries to open zip/destination file
			try {
				destOut = destFile.getOutputStream(false);
			}
			catch(Exception ex) {
				QuestionDialog dialog = new QuestionDialog(mainFrame, "Zip error", "Cannot write to destination file.", mainFrame,
					new String[] {"OK"},
					new int[]  {0},
					0);
				dialog.getActionValue();
				return;
			}
			
			// Starts zipping
			ProgressDialog progressDialog = new ProgressDialog(mainFrame, "Zipping files");
			ZipJob zipJob = new ZipJob(mainFrame, progressDialog, filesToZip, commentArea.getText(), destOut, destFile.getParent());
			zipJob.start();
			progressDialog.start(zipJob);
		}
		else if (source==cancelButton)  {
			dispose();			
		}
	}


}