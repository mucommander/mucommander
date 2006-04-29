package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;

import com.mucommander.text.Translator;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.file.FileToolkit;

import com.mucommander.job.ArchiveJob;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * This dialog allows the user to zip selected files to a specified file
 * and add a comment in the zip file.
 *
 * @author Maxence Bernard
 */
public class ZipDialog extends FocusDialog implements ActionListener {

    private MainFrame mainFrame;

    /** Files to zip */
    private FileSet files;
	
    private JTextField filePathField;
    private JTextArea commentArea;
    private JButton okButton;
    private JButton cancelButton;

    // Dialog's width has to be at least 240
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	

    // Dialog's width has to be at most 320
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	


    public ZipDialog(MainFrame mainFrame, FileSet files, boolean isShiftDown) {
        super(mainFrame, Translator.get("zip_dialog.title"), mainFrame);

        this.mainFrame = mainFrame;
        this.files = files;
		
        Container contentPane = getContentPane();
		
        YBoxPanel mainPanel = new YBoxPanel(5);
        JLabel label = new JLabel(Translator.get("zip_dialog_description")+" :");
        mainPanel.add(label);

        FileTable activeTable = mainFrame.getUnactiveTable();
        String initialPath = (isShiftDown?"":activeTable.getCurrentFolder().getAbsolutePath(true));

        // Computes the archive's default name:
        // - if it only contains one file, uses that file's name.
        // - if it contains more than one file, uses the FileSet's parent folder's name.
        if(files.size() == 1)
            initialPath += files.fileAt(0).getName();
        else if(files.getBaseFolder().getParent() != null)
            initialPath += files.getBaseFolder().getName();

        initialPath += ".zip";
        filePathField = new JTextField(initialPath);
        filePathField.setCaretPosition(initialPath.length()-4);
        mainPanel.add(filePathField);
		
        mainPanel.addSpace(10);
		
        label = new JLabel(Translator.get("zip_dialog.comment"));
        mainPanel.add(label);
        commentArea = new JTextArea();
        commentArea.setRows(4);
        mainPanel.add(commentArea);

        mainPanel.addSpace(10);

        contentPane.add(mainPanel, BorderLayout.NORTH);
				
        okButton = new JButton(Translator.get("zip_dialog.zip"));
        cancelButton = new JButton(Translator.get("cancel"));
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
	
	
    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        if (source==okButton)  {
            // Starts by disposing the dialog
            dispose();

            // Checks that destination file can be resolved 
            String filePath = filePathField.getText();
            Object dest[] = FileToolkit.resolvePath(filePath, mainFrame.getLastActiveTable().getCurrentFolder());
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
            ArchiveJob archiveJob = new ArchiveJob(progressDialog, mainFrame, files, commentArea.getText(), destFile);
            progressDialog.start(archiveJob);
        }
        else if (source==cancelButton)  {
            dispose();			
        }
    }


}
