

package com.mucommander.ui;

import com.mucommander.file.*;
import com.mucommander.ui.table.*;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.job.*;
import com.mucommander.Launcher;
import com.mucommander.ui.viewer.*;
import com.mucommander.ui.editor.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

/**
 * Probably the dirtiest class of all...
 */
public class CommandBarPanel extends JPanel implements ActionListener {
    private MainFrame mainFrame;
    
	private FileTable table1;
    private FileTable table2;

	private JButton viewButton;
	private JButton editButton;
	private JButton copyButton;
	private JButton moveButton;
	private JButton mkdirButton;
	private JButton deleteButton;
	private JButton refreshButton;
	private JButton exitButton;
	
    private JButton okButton;
    private JButton cancelButton;
    
    private FocusDialog dialog;
	private JTextField mkdirPathField;
    private JTextField movePathField;
    private JTextField copyPathField;
	
	private final static String VIEW_CAPTION = "[F3] View";
	private final static String EDIT_CAPTION = "[F4] Edit";
	private final static String COPY_CAPTION = "[F5] Copy";
	private final static String MOVE_CAPTION = "[F6] Move";
	private final static String MKDIR_CAPTION = "[F7] Make dir";
	private final static String DELETE_CAPTION = "[F8] Delete";
	private final static String REFRESH_CAPTION = "[F9] Refresh";
	private final static String EXIT_CAPTION = "[F10] Exit";
	
    private int dialogType;
	private boolean unzipDialog;
    private final static int NO_DIALOG = 0;
	private final static int COPY_DIALOG = 1;
    private final static int MOVE_DIALOG = 2;
    private final static int MKDIR_DIALOG = 3;
    private final static int DELETE_DIALOG = 4;

    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	
    
	public CommandBarPanel(MainFrame mainFrame, FileTable table1, FileTable table2) {
		super(new GridLayout(0,8));

        this.mainFrame = mainFrame;
		this.table1 = table1;
		this.table2 = table2;

		viewButton = addButton(VIEW_CAPTION);
		editButton = addButton(EDIT_CAPTION);
		copyButton = addButton(COPY_CAPTION);
		moveButton = addButton(MOVE_CAPTION);
		mkdirButton = addButton(MKDIR_CAPTION);
		deleteButton = addButton(DELETE_CAPTION);
		refreshButton = addButton(REFRESH_CAPTION);
		exitButton = addButton(EXIT_CAPTION);
	}

	private JButton addButton(String caption) {
		JButton button = new JButton(caption);
		button.setMargin(new Insets(1,1,1,1));
		// For Mac OS X whose minimum width for buttons is enormous
		button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getWidth()));
		button.addActionListener(this);
		//button.setBorder(BorderFactory.createRaisedBevelBorder());
		add(button);
		return button;
	}

	private void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);

		// FileTable lost focus
		mainFrame.getLastActiveTable().requestFocus();
	}

    public void showCopyDialog(boolean unzipDialog) {
	    this.unzipDialog = unzipDialog;
		
		FileTable activeTable = mainFrame.getLastActiveTable();
    	Vector selectedFiles = activeTable.getSelectedFiles();
		if(selectedFiles.size()==0)
    		return;
        
        dialogType = COPY_DIALOG;
        
		AbstractFile destFolder = (activeTable==table1?table2:table1).getCurrentFolder();
        String fieldText = destFolder.getAbsolutePath()+destFolder.getSeparator();
        if(selectedFiles.size()==1 && !unzipDialog)
			fieldText += ((AbstractFile)selectedFiles.elementAt(0)).getName();
		
		copyPathField = new JTextField(fieldText);
        // Text is selected so that user can directly type and replace path
        copyPathField.setSelectionStart(0);
        copyPathField.setSelectionEnd(fieldText.length());
        copyPathField.addActionListener(this);

        dialog = new FocusDialog(mainFrame, (unzipDialog?"Unzip":"Copy"), mainFrame, copyPathField);
		Container contentPane = dialog.getContentPane();

        Panel tempPanel = new Panel(new BorderLayout());
        tempPanel.add(new JLabel((unzipDialog?"Unzip":"Copy")+" selected file(s) to"), BorderLayout.NORTH);

		tempPanel.add(copyPathField, BorderLayout.SOUTH);
        contentPane.add(tempPanel, BorderLayout.CENTER);
        
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(dialog);
		copyPathField.addKeyListener(escapeKeyAdapter);
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        dialog.getRootPane().setDefaultButton(okButton);
        
		dialog.setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		dialog.showDialog();
	}
    
    public void showMoveDialog() {
        FileTable activeTable = mainFrame.getLastActiveTable();
       	Vector selectedFiles = activeTable.getSelectedFiles();
       	if(selectedFiles.size()==0)
        	return;
        
        dialogType = MOVE_DIALOG;
        
		AbstractFile destFolder = (activeTable==table1?table2:table1).getCurrentFolder();
        String fieldText = destFolder.getAbsolutePath()+destFolder.getSeparator();
        if(selectedFiles.size()==1)
        	fieldText += ((AbstractFile)selectedFiles.elementAt(0)).getName();

        movePathField = new JTextField(fieldText);
        // Text is selected so that user can directly type and replace path
        movePathField.setSelectionStart(0);
        movePathField.setSelectionEnd(fieldText.length());
        movePathField.addActionListener(this);

        dialog = new FocusDialog(mainFrame, "Move/Rename", mainFrame, movePathField);
        Container contentPane = dialog.getContentPane();
        
        Panel tempPanel = new Panel(new BorderLayout());
        tempPanel.add(new JLabel("Move/Rename to"), BorderLayout.NORTH);

		tempPanel.add(movePathField, BorderLayout.SOUTH);
        contentPane.add(tempPanel, BorderLayout.CENTER);

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
        EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(dialog);
        movePathField.addKeyListener(escapeKeyAdapter);
        okButton.addKeyListener(escapeKeyAdapter);
        cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        dialog.getRootPane().setDefaultButton(okButton);

        dialog.setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		dialog.showDialog();
	}

    public void showMkdirDialog() {
	    FileTable activeTable = mainFrame.getLastActiveTable();

        dialogType = MKDIR_DIALOG;
        
        Panel tempPanel = new Panel(new BorderLayout());
        tempPanel.add(new JLabel("Create directory"), BorderLayout.NORTH);
        mkdirPathField = new JTextField();
        mkdirPathField.addActionListener(this);

		dialog = new FocusDialog(mainFrame, "Make dir", mainFrame, mkdirPathField);
		mkdirPathField.addKeyListener(new EscapeKeyAdapter(dialog));

		Container contentPane = dialog.getContentPane();

        tempPanel.add(mkdirPathField, BorderLayout.SOUTH);
        contentPane.add(tempPanel, BorderLayout.CENTER);
        
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
        EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(dialog);
        mkdirPathField.addKeyListener(escapeKeyAdapter);
        okButton.addKeyListener(escapeKeyAdapter);
        cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        dialog.getRootPane().setDefaultButton(okButton);
        
		dialog.setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		dialog.showDialog();
    }

    public void showDeleteDialog() {
        FileTable activeTable = mainFrame.getLastActiveTable();
        if(activeTable.getSelectedFiles().size()==0)
        	return;

        dialogType = DELETE_DIALOG;

        okButton = new JButton("OK");
        dialog = new FocusDialog(mainFrame, "Delete", mainFrame, okButton);
        Container contentPane = dialog.getContentPane();
        
        Panel tempPanel = new Panel(new BorderLayout());
        contentPane.add(new JLabel("Permanently delete selected file(s) ?"), BorderLayout.CENTER);
        
		cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);
        
		// Escape key disposes dialog
        EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(dialog);
        okButton.addKeyListener(escapeKeyAdapter);
        cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        dialog.getRootPane().setDefaultButton(okButton);
        
		dialog.setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		dialog.showDialog();
	}


	public void doView() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
		if(file.isFolder() && !(file instanceof ArchiveFile))
			return;
		
		try {
//System.out.println(Runtime.getRuntime().freeMemory());
			FileViewer viewer = ViewerRegistrar.getViewer(file);
			// Default file viewer
			if (viewer==null) {
				// we should ask the user if he wants to try the default viewer
				viewer = new TextViewer();
			}

//System.out.println(file.getSize()+" "+viewer.getMaxRecommendedSize());
			// Tests if file is too large to be viewed and warns user
			long max = viewer.getMaxRecommendedSize();
			if (max!=-1 && file.getSize()>max) {
				QuestionDialog dialog = new QuestionDialog(mainFrame, null, "This file may be too large to be viewed, try anyway?", mainFrame, 
					new String[] {"Yes", "No"},
					new int[]  {0, 1},
					new int[]  {KeyEvent.VK_Y, KeyEvent.VK_N},
					0);
				int ret = dialog.getActionValue();
				
				if (ret==1 || ret==-1)
					return;
			}
			
			// All OK, start viewing
			viewer.startViewing(file, true);
			
			JFrame frame = new ViewerFrame(file.getAbsolutePath());
			
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(viewer, BorderLayout.CENTER);
			
			// Sets panel to preferred size, without exceeding a maximum size and with a minumum size
			frame.pack();
			Dimension d = frame.getSize();
			Dimension screend = Toolkit.getDefaultToolkit().getScreenSize();
			
			// width is 800 max and 480 min
			int width = Math.max(480, Math.min(d.width, Math.min(screend.width-44, 800-44)));
			
			// height is 3/4 of width
			frame.setSize(
				width, 
				(int)(width*3/((float)4))
			);

			frame.setResizable(true);
			frame.setVisible(true);
//System.out.println(Runtime.getRuntime().freeMemory());
		}
		catch(IOException e) {
			showErrorDialog("Unable to view file.", "View error");
		}
	}
	
	
	public void doEdit() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
		if(file.isFolder() && !(file instanceof ArchiveFile))
			return;

		try {
			FileEditor editor = EditorRegistrar.getEditor(file);
			// Default file editor
			if (editor==null) {
				// we should ask the user if he wants to try the default editor
				editor = new TextEditor();
			}

			// Tests if file is too large to be edited and warns user
			long max = editor.getMaxRecommendedSize();
			if (max!=-1 && file.getSize()>max) {
				QuestionDialog dialog = new QuestionDialog(mainFrame, null, "This file may be too large to be edited, try anyway?", mainFrame, 
					new String[] {"Yes", "No"},
					new int[]  {0, 1},
					new int[]  {KeyEvent.VK_Y, KeyEvent.VK_N},
					0);
				int ret = dialog.getActionValue();
				
				if (ret==1 || ret==-1)
					return;
			}

			// All OK, start editing
			editor.startEditing(file);
			
			// Sets dialog to preferred size, without exceeding a maximum size and with a minumum size
			editor.pack();
			Dimension d = editor.getSize();
			Dimension screend = Toolkit.getDefaultToolkit().getScreenSize();
			
			// Frame width is 800 max and 480 min
			int width = Math.max(480, Math.min(d.width, Math.min(screend.width-44, 800-44)));
			
			// Frame height is 3/4 of width
			editor.setSize(
				width, 
				(int)(width*3/((float)4))
			);

			
			editor.setVisible(true);
		}
		catch(IOException e) {
			showErrorDialog("Unable to edit file.", "Edit error");
		}
	}
	
	
	public void doCopy(String destPath, boolean unzip) {
		FileTable activeTable = mainFrame.getLastActiveTable();

		// Figures out which files to copy
		Vector filesToCopy = activeTable.getSelectedFiles();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null || ((filesToCopy.size()>1 || unzip) && ret[1]!=null)) {
			showErrorDialog("Folder "+destPath+" doesn't exist.", (unzip?"Unzip":"Copy")+" error");
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];

		if (!unzip && newName==null && activeTable.getCurrentFolder().equals(destFolder)) {
			showErrorDialog("Source and destination are the same.", (unzip?"Unzip":"Copy")+" error");
			return;
		}

		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, (unzip?"Unzipping":"Copying")+" files", true);
		CopyJob copyJob = new CopyJob(mainFrame, progressDialog, filesToCopy, newName, destFolder, unzip);
		copyJob.start();
		progressDialog.setFileJob(copyJob);
		progressDialog.start();
	}


	private void doMove(String destPath) {
	    FileTable activeTable = mainFrame.getLastActiveTable();

	    // Figures out which files to move
	    Vector filesToMove = activeTable.getSelectedFiles();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null || (filesToMove.size()>1 && ret[1]!=null)) {
			showErrorDialog("Folder "+destPath+" doesn't exist.", "Move error");
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];

		if (newName==null && activeTable.getCurrentFolder().equals(destFolder)) {
			showErrorDialog("Source and destination are the same.", "Move error");
			return;
		}

		if (filesToMove.contains(destFolder)) {
			showErrorDialog("Cannot move destination folder to itself.", "Move error");
			return;
		}
		
		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, "Moving files", true);
		MoveJob moveJob = new MoveJob(mainFrame, progressDialog, filesToMove, newName, destFolder);
	    moveJob.start();
	    progressDialog.setFileJob(moveJob);
	    progressDialog.start();
	}


	private void doMkdir(String dirPath) {
		FileTable activeTable = mainFrame.getLastActiveTable();

	    try {
		    // Resolves destination folder
		    Object ret[] = mainFrame.resolvePath(dirPath);
		    // The path entered doesn't correspond to any existing folder
		    if (ret==null) {
		    	showErrorDialog("Incorrect path "+dirPath, "mkdir error");
		    	return;
		    }

			if(ret[1]==null) {
		    	showErrorDialog("Directory "+dirPath+" already exists.", "mkdir error");
		    	return;
		    }

		    AbstractFile folder = (AbstractFile)ret[0];
		    String newName = (String)ret[1];

// System.out.println(folder.getAbsolutePath()+" "+newName);

	        folder.mkdir(newName);
	        activeTable.refresh();
						
			// Finds the row corresponding to the newly created folder
			// and makes it the current row.
			if (activeTable.getCurrentFolder().equals(folder)) {
				AbstractFile createdFolder = AbstractFile.getAbstractFile(folder.getAbsolutePath()+folder.getSeparator()+newName);
				activeTable.selectFile(createdFolder);
			}
		}
	    catch(IOException ex) {
//System.out.println(ex);            
	        showErrorDialog("Unable to create directory "+dirPath, "mkdir error");
	    }    

	    FileTable unactiveTable = activeTable==table1?table2:table1;
	    if (unactiveTable.getCurrentFolder().equals(activeTable.getCurrentFolder()))
	    	try {
	    		unactiveTable.refresh();
			}
		    catch(IOException e) {
		    	// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
		    	// like switching to a root folder        
		    }

	
		activeTable.requestFocus();
	}


    public void doDelete() {
	    FileTable activeTable = mainFrame.getLastActiveTable();

        // Figures out which files to delete
        Vector filesToDelete = activeTable.getSelectedFiles();
        if(filesToDelete.size()==0)
        	return;
                    
        // Starts deleting files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, "Deleting files", false);
		DeleteJob deleteJob = new DeleteJob(mainFrame, progressDialog, filesToDelete);
        deleteJob.start();
	    progressDialog.setFileJob(deleteJob);
    	progressDialog.start();
    }


	public void doRefresh()  {
		try  {
			mainFrame.getLastActiveTable().refresh();
		}
		catch(IOException e)  {
		}
	}
	

	public void doExit()  {
		Launcher.getLauncher().disposeMainFrame(mainFrame);
//		System.exit(0);
	}
	

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if(source == viewButton) {
        	doView();
		}
        if(source == editButton) {
        	doEdit();
		}
        if(source == copyButton) {
            showCopyDialog(false);
        }
        else if(source == moveButton) {
            showMoveDialog();
        }
        else if(source == mkdirButton) {
            showMkdirDialog();
        }
        else if(source == deleteButton) {
            showDeleteDialog();
        }
        else if(source == refreshButton) {
			doRefresh();
		}
        else if(source == exitButton) {
        	doExit();
		}

        // Dialog
		else if(dialogType == COPY_DIALOG) {
			dialog.dispose();
			dialog = null;
			dialogType = NO_DIALOG;

			if(source == okButton || source == copyPathField) {
                doCopy(copyPathField.getText(), unzipDialog);
            }
            copyPathField = null;
		}
        else if(dialogType == MOVE_DIALOG) {
            dialog.dispose();
            dialog = null;
            dialogType = NO_DIALOG;

            if(source == okButton || source == movePathField) {
                doMove(movePathField.getText());
            }
            movePathField = null;
        }
        else if(dialogType == MKDIR_DIALOG) {
	        dialog.dispose();
	        dialog = null;
	        dialogType = NO_DIALOG;

            if(source == okButton || source == mkdirPathField) {
                doMkdir(mkdirPathField.getText());
            }
            mkdirPathField = null;
        }
        else if(dialogType == DELETE_DIALOG) {
            dialog.dispose();
            dialog = null;
            dialogType = NO_DIALOG;

            if(source == okButton)
                doDelete();
        }
        
        // FileTable lost focus since a button was clicked
        mainFrame.getLastActiveTable().requestFocus();
    }
}