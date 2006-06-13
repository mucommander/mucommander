
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.dialog.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Date;


/**
 * Dialog invoked to ask the user what to do when a file is to be transferred in a folder
 * where a file with the same name already exists.
 *
 * @author Maxence Bernard
 */
public class FileExistsDialog extends QuestionDialog {

    /** This value is used by some FileJob classes */
    public final static int ASK_ACTION = -1;
	
    public final static int CANCEL_ACTION = 0;
    public final static int SKIP_ACTION = 1;
    public final static int OVERWRITE_ACTION = 2;
    public final static int OVERWRITE_IF_OLDER_ACTION = 3;
    public final static int RESUME_ACTION = 4;

    public final static String CANCEL_TEXT = Translator.get("cancel");
    public final static String SKIP_TEXT = Translator.get("skip");
    public final static String OVERWRITE_TEXT = Translator.get("overwrite");
    public final static String OVERWRITE_IF_OLDER_TEXT = Translator.get("overwrite_if_older");
    public final static String RESUME_TEXT = Translator.get("append");

    private String choicesText[];
    private int choicesActions[];
	
    private JCheckBox applyToAllCheckBox;

	
    /**
     * Creates a new FileExistsDialog.
     *
     * @param parent parent dialog
     * @param locationRelative component the location of this dialog will be based on
     * @param sourceFile the source file that 'conflicts' with the destination file, can be null.
     * @param destFile the destination file which already exists
     * @param multipleFilesMode if true, options for multiple files processing will be enabled (skip, apply to all)
     */
    public FileExistsDialog(Dialog parent, Component locationRelative, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode) {
        super(parent, Translator.get("file_exists_dialog.title"), locationRelative);
		
        setChoices(sourceFile, destFile, multipleFilesMode);
        init(sourceFile, destFile, multipleFilesMode);
    }

    /**
     * Creates a new FileExistsDialog.
     *
     * @param parent parent
     * @param locationRelative component the location of this dialog will be based on
     * @param sourceFile the source file that 'conflicts' with the destination file, can be null.
     * @param destFile the destination file which already exists
     * @param multipleFilesMode if true, options for multiple files processing will be enabled (skip, apply to all)
     */
    public FileExistsDialog(Frame parent, Component locationRelative, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode) {
        super(parent, Translator.get("file_exists_dialog.title"), locationRelative);

        setChoices(sourceFile, destFile, multipleFilesMode);
        init(sourceFile, destFile, multipleFilesMode);
    }


    private void setChoices(AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode) {
        boolean resumeOption = false;
        if(sourceFile!=null) {
            // Give resume option only if destination file is smaller than source file
            long destSize = destFile.getSize();
            long sourceSize = sourceFile.getSize();
            resumeOption = destSize!=-1 && (sourceSize==-1 || destSize<sourceSize);
        }

        if(multipleFilesMode) {
            if(resumeOption) {
                choicesText = new String[]{CANCEL_TEXT, SKIP_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT, RESUME_TEXT};
                choicesActions = new int[]{CANCEL_ACTION, SKIP_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION, RESUME_ACTION};
            }
            else {
                if(sourceFile!=null) {
                    choicesText = new String[]{CANCEL_TEXT, SKIP_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT};
                    choicesActions = new int[]{CANCEL_ACTION, SKIP_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION};
                }
                else {
                    choicesText = new String[]{CANCEL_TEXT, SKIP_TEXT, OVERWRITE_TEXT};
                    choicesActions = new int[]{CANCEL_ACTION, SKIP_ACTION, OVERWRITE_ACTION};
                }
            }
        }
        else {
            if(resumeOption) {
                choicesText = new String[]{CANCEL_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT, RESUME_TEXT};
                choicesActions = new int[]{CANCEL_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION, RESUME_ACTION};
            }
            else {
                if(sourceFile!=null) {
                    choicesText = new String[]{CANCEL_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT};
                    choicesActions = new int[]{CANCEL_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION};
                }
                else {
                    choicesText = new String[]{CANCEL_TEXT, OVERWRITE_TEXT};
                    choicesActions = new int[]{CANCEL_ACTION, OVERWRITE_ACTION};
                }
            }
        }
    }

	
    private void init(AbstractFile sourceFile, AbstractFile destFile, boolean applyToAllOption) {
        YBoxPanel panel = new YBoxPanel();

        if(sourceFile!=null) {
            panel.add(new JLabel("Source: "+sourceFile.getAbsolutePath()));
            panel.add(new JLabel("  "+SizeFormatter.format(sourceFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
                                 +", "+CustomDateFormat.format(new Date(sourceFile.getDate()))));
            panel.addSpace(10);
        }
		
        // Use canonical path for destination, to get rid of '..', '.' and '~'
    	panel.add(new JLabel("Destination: "+destFile.getCanonicalPath()));
    	panel.add(new JLabel("  "+SizeFormatter.format(destFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
                             +", "+CustomDateFormat.format(new Date(destFile.getDate()))));

    	init(parent, panel,
             choicesText,
             choicesActions,
             3);
		
        if(applyToAllOption) {
            applyToAllCheckBox = new JCheckBox(Translator.get("apply_to_all"));
            addCheckBox(applyToAllCheckBox);
        }
    }


    /**
     * Returns <code>true</code> if the 'apply to all' checkbox has been selected.
     */
    public boolean applyToAllSelected() {
        return applyToAllCheckBox==null?false:applyToAllCheckBox.isSelected();
    }
	
}
