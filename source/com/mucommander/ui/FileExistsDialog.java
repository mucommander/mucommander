
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.dialog.TextFieldsPanel;
import com.mucommander.ui.comp.dialog.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.Vector;


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
    public final static String RESUME_TEXT = Translator.get("resume");

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
        super(parent, null, locationRelative);
		
        init(parent, sourceFile, destFile, multipleFilesMode);
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
        super(parent, null, locationRelative);

        init(parent, sourceFile, destFile, multipleFilesMode);
    }


    private void init(Container parent, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode) {

        // Init choices

        Vector choicesTextV = new Vector();
        Vector choicesActionsV = new Vector();

        choicesTextV.add(CANCEL_TEXT);
        choicesActionsV.add(new Integer(CANCEL_ACTION));

        if(multipleFilesMode) {
            choicesTextV.add(SKIP_TEXT);
            choicesActionsV.add(new Integer(SKIP_ACTION));
        }

        boolean sameSourceAndDestination = destFile.equals(sourceFile);
        if(!sameSourceAndDestination) {
            choicesTextV.add(OVERWRITE_TEXT);
            choicesActionsV.add(new Integer(OVERWRITE_ACTION));

            if(sourceFile!=null) {
                choicesTextV.add(OVERWRITE_IF_OLDER_TEXT);
                choicesActionsV.add(new Integer(OVERWRITE_IF_OLDER_ACTION));

                // Give resume option only if destination file is smaller than source file
                long destSize = destFile.getSize();
                long sourceSize = sourceFile.getSize();
                if(destSize!=-1 && (sourceSize==-1 || destSize<sourceSize)) {
                    choicesTextV.add(RESUME_TEXT);
                    choicesActionsV.add(new Integer(RESUME_ACTION));
                }
            }
        }

        int nbChoices = choicesActionsV.size();

        String choicesText[] = new String[nbChoices];
        choicesTextV.toArray(choicesText);

        int choicesActions[] = new int[nbChoices];
        for(int i=0; i<nbChoices; i++)
            choicesActions[i] = ((Integer)choicesActionsV.elementAt(i)).intValue();


        // Init UI

        String title = sameSourceAndDestination?
            Translator.get("same_source_destination")
            :Translator.get("file_exists_dialog.title");

        setTitle(title);

        YBoxPanel yPanel = new YBoxPanel();
        yPanel.add(new JLabel(title+": "));
        yPanel.addSpace(10);

        TextFieldsPanel tfPanel = new TextFieldsPanel(10);

        if(sourceFile!=null)
            addFileDetails(tfPanel, sourceFile, Translator.get("source"));

        addFileDetails(tfPanel, destFile, Translator.get("destination"));

        yPanel.add(tfPanel);

        init(parent, yPanel,
             choicesText,
             choicesActions,
             3);

        if(multipleFilesMode && !sameSourceAndDestination) {
            applyToAllCheckBox = new JCheckBox(Translator.get("apply_to_all"));
            addCheckBox(applyToAllCheckBox);
        }


//        if(multipleFilesMode) {
//            if(resumeOption) {
//                choicesText = new String[]{CANCEL_TEXT, SKIP_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT, RESUME_TEXT};
//                choicesActions = new int[]{CANCEL_ACTION, SKIP_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION, RESUME_ACTION};
//            }
//            else {
//                if(sourceFile!=null) {
//                    choicesText = new String[]{CANCEL_TEXT, SKIP_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT};
//                    choicesActions = new int[]{CANCEL_ACTION, SKIP_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION};
//                }
//                else {
//                    choicesText = new String[]{CANCEL_TEXT, SKIP_TEXT, OVERWRITE_TEXT};
//                    choicesActions = new int[]{CANCEL_ACTION, SKIP_ACTION, OVERWRITE_ACTION};
//                }
//            }
//        }
//        else {
//            if(resumeOption) {
//                choicesText = new String[]{CANCEL_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT, RESUME_TEXT};
//                choicesActions = new int[]{CANCEL_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION, RESUME_ACTION};
//            }
//            else {
//                if(sourceFile!=null) {
//                    choicesText = new String[]{CANCEL_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT};
//                    choicesActions = new int[]{CANCEL_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION};
//                }
//                else {
//                    choicesText = new String[]{CANCEL_TEXT, OVERWRITE_TEXT};
//                    choicesActions = new int[]{CANCEL_ACTION, OVERWRITE_ACTION};
//                }
//            }
//        }
    }

	
    private void addFileDetails(TextFieldsPanel panel, AbstractFile file, String nameLabel) {
        panel.addTextFieldRow(nameLabel+":", new FilenameLabel(file), 0);

        String parentLocation = file.getParent().getCanonicalPath();
        JLabel label = new JLabel(parentLocation);
        label.setToolTipText(parentLocation);
        panel.addTextFieldRow(Translator.get("location")+":", label, 0);

        panel.addTextFieldRow(Translator.get("size")+":", new JLabel(SizeFormat.format(file.getSize(), SizeFormat.DIGITS_FULL| SizeFormat.UNIT_LONG| SizeFormat.INCLUDE_SPACE)), 0);

        panel.addTextFieldRow(Translator.get("date")+":", new JLabel(CustomDateFormat.format(new Date(file.getDate()))), 10);
    }



    /**
     * Returns <code>true</code> if the 'apply to all' checkbox has been selected.
     */
    public boolean applyToAllSelected() {
        return applyToAllCheckBox==null?false:applyToAllCheckBox.isSelected();
    }
	
}
