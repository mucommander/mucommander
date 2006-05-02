
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.job.DeleteJob;
import com.mucommander.text.Translator;
import com.mucommander.file.FileSet;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Dialog invoked when the user wants to create a new folder (F7).
 *
 * @author Maxence Bernard
 */
public class DeleteDialog extends QuestionDialog {

    private final static int DELETE_ACTION = 0;
    private final static int CANCEL_ACTION = 1;
	
    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	

	
    public DeleteDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, 
              Translator.get("delete_dialog.delete"),
              Translator.get("delete_dialog.confirmation"),
              mainFrame,
              new String[] {Translator.get("delete_dialog.delete"), Translator.get("cancel")},
              new int[] {DELETE_ACTION, CANCEL_ACTION},
              0);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        if(getActionValue()==DELETE_ACTION) {
            // Starts deleting files
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("delete_dialog.deleting"));
            DeleteJob deleteJob = new DeleteJob(progressDialog, mainFrame, files);
            progressDialog.start(deleteJob);
        }
    }
}
