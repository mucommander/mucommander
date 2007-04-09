
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.job.FileCollisionChecker;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.dialog.TextFieldsPanel;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.ui.macosx.GrowlSupport;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.Vector;


/**
 * Dialog used to inform the user that a file collision has been detected and ask him how to resolve the conflict.
 * Prior to invoking this dialog, {@link com.mucommander.job.FileCollisionChecker} can be used to check for file collisions. 
 *
 * @see com.mucommander.job.FileCollisionChecker
 * @author Maxence Bernard
 */
public class FileCollisionDialog extends QuestionDialog {

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
     * Creates a new FileCollisionDialog.
     *
     * @param owner the Frame that owns this dialog
     * @param locationRelative component the location of this dialog will be based on
     * @param collisionType the type of collision as returned by {@link com.mucommander.job.FileCollisionChecker}
     * @param sourceFile the source file that 'conflicts' with the destination file, can be null.
     * @param destFile the destination file which already exists
     * @param multipleFilesMode if true, options that apply to multiple files will be displayed (skip, apply to all)
     */
    public FileCollisionDialog(Dialog owner, Component locationRelative, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode) {
        super(owner, Translator.get("file_collision_dialog.title"), locationRelative);
		
        init(owner, collisionType, sourceFile, destFile, multipleFilesMode);
    }

    /**
     * Creates a new FileCollisionDialog.
     *
     * @param owner the Frame that owns this dialog
     * @param locationRelative component the location of this dialog will be based on
     * @param collisionType the type of collision as returned by {@link com.mucommander.job.FileCollisionChecker}
     * @param sourceFile the source file that 'conflicts' with the destination file, can be null.
     * @param destFile the destination file which already exists
     * @param multipleFilesMode if true, options that apply to multiple files will be displayed (skip, apply to all)
     */
    public FileCollisionDialog(Frame owner, Component locationRelative, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode) {
        super(owner, Translator.get("file_collision_dialog.title"), locationRelative);

        init(owner, collisionType, sourceFile, destFile, multipleFilesMode);
    }


    private void init(Container owner, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode) {

        // Init choices

        Vector choicesTextV = new Vector();
        Vector choicesActionsV = new Vector();

        choicesTextV.add(CANCEL_TEXT);
        choicesActionsV.add(new Integer(CANCEL_ACTION));

        if(multipleFilesMode) {
            choicesTextV.add(SKIP_TEXT);
            choicesActionsV.add(new Integer(SKIP_ACTION));
        }

        // Add 'overwrite' / 'overwrite if older' / 'resume' actions only for 'destination file already exists' collision type
        if(collisionType==FileCollisionChecker.DESTINATION_FILE_ALREADY_EXISTS && !destFile.isDirectory()) {
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

        // Convert choice vectors into arrays
        int nbChoices = choicesActionsV.size();

        String choicesText[] = new String[nbChoices];
        choicesTextV.toArray(choicesText);

        int choicesActions[] = new int[nbChoices];
        for(int i=0; i<nbChoices; i++)
            choicesActions[i] = ((Integer)choicesActionsV.elementAt(i)).intValue();


        // Init UI

        String desc;

        if(collisionType==FileCollisionChecker.DESTINATION_FILE_ALREADY_EXISTS)
            desc = Translator.get("file_exists_in_destination");
        else if(collisionType==FileCollisionChecker.SAME_SOURCE_AND_DESTINATION)
            desc = Translator.get("same_source_destination");
        else if(collisionType==FileCollisionChecker.SOURCE_PARENT_OF_DESTINATION)
            desc = Translator.get("source_parent_of_destination");
        else
            desc = null;

        YBoxPanel yPanel = new YBoxPanel();

        if(desc!=null) {
            yPanel.add(new JLabel(desc+": "));
            yPanel.addSpace(10);
        }

        TextFieldsPanel tfPanel = new TextFieldsPanel(10);

        // If collision type is 'same source and destination' no need to show both source and destination 
        if(collisionType==FileCollisionChecker.SAME_SOURCE_AND_DESTINATION) {
            addFileDetails(tfPanel, sourceFile, Translator.get("name"));
        }
        else {
            if(sourceFile!=null)
                addFileDetails(tfPanel, sourceFile, Translator.get("source"));

            addFileDetails(tfPanel, destFile, Translator.get("destination"));
        }

        yPanel.add(tfPanel);

        init(owner, yPanel,
             choicesText,
             choicesActions,
             3);

        // 'Apply to all' is available only for 'destination file already exists' collision type
        if(multipleFilesMode && collisionType==FileCollisionChecker.DESTINATION_FILE_ALREADY_EXISTS) {
            applyToAllCheckBox = new JCheckBox(Translator.get("apply_to_all"));
            addCheckBox(applyToAllCheckBox);
        }

        // Send a growl notification
        if(GrowlSupport.isGrowlAvailable())
            GrowlSupport.sendNotification(this, GrowlSupport.NOTIFICATION_TYPE_JOB_ERROR, getTitle(), desc);
    }


    private void addFileDetails(TextFieldsPanel panel, AbstractFile file, String nameLabel) {
        panel.addTextFieldRow(nameLabel+":", new FilenameLabel(file), 0);

        String parentLocation = file.getParent().getCanonicalPath();
        JLabel label = new JLabel(parentLocation);
        label.setToolTipText(parentLocation);
        panel.addTextFieldRow(Translator.get("location")+":", label, 0);

        panel.addTextFieldRow(Translator.get("size")+":", new JLabel(SizeFormat.format(file.getSize(), SizeFormat.DIGITS_FULL| SizeFormat.UNIT_LONG| SizeFormat.INCLUDE_SPACE)), 0);

        panel.addTextFieldRow(Translator.get("date")+":", new JLabel(CustomDateFormat.format(new Date(file.getDate()))), 0);

        panel.addTextFieldRow(Translator.get("permissions")+":", new JLabel(file.getPermissionsString()), 10);
    }



    /**
     * Returns true if the 'apply to all' checkbox has been selected.
     */
    public boolean applyToAllSelected() {
        return applyToAllCheckBox==null?false:applyToAllCheckBox.isSelected();
    }
	
}
