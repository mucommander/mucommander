/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.ui.dialog.file;

import com.mucommander.file.AbstractFile;
import com.mucommander.job.FileCollisionChecker;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.notifier.AbstractNotifier;
import com.mucommander.ui.notifier.NotificationTypes;
import com.mucommander.ui.text.FileLabel;

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
    public final static int RENAME_ACTION = 5;

    public final static String CANCEL_TEXT = Translator.get("cancel");
    public final static String SKIP_TEXT = Translator.get("skip");
    public final static String OVERWRITE_TEXT = Translator.get("overwrite");
    public final static String OVERWRITE_IF_OLDER_TEXT = Translator.get("overwrite_if_older");
    public final static String RESUME_TEXT = Translator.get("resume");
    public final static String RENAME_TEXT = Translator.get("rename");

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
     * @param allowRename if true, display an option to rename a file
     */
    public FileCollisionDialog(Dialog owner, Component locationRelative, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode, boolean allowRename) {
        super(owner, Translator.get("file_collision_dialog.title"), locationRelative);
		
        init(owner, collisionType, sourceFile, destFile, multipleFilesMode, allowRename);
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
     * @param allowRename if true, display an option to rename a file
     */
    public FileCollisionDialog(Frame owner, Component locationRelative, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode, boolean allowRename) {
        super(owner, Translator.get("file_collision_dialog.title"), locationRelative);

        init(owner, collisionType, sourceFile, destFile, multipleFilesMode, allowRename);
    }


    private void init(Container owner, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode, boolean allowRename) {

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

                if (allowRename) {
                    choicesTextV.add(RENAME_TEXT);
                    choicesActionsV.add(new Integer(RENAME_ACTION));
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

        XAlignedComponentPanel tfPanel = new XAlignedComponentPanel(10);

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
            addComponent(applyToAllCheckBox);
        }

        // Send a system notification if a notifier is available and enabled
        if(AbstractNotifier.isAvailable() && AbstractNotifier.getNotifier().isEnabled())
            AbstractNotifier.getNotifier().displayBackgroundNotification(NotificationTypes.NOTIFICATION_TYPE_JOB_ERROR, getTitle(), desc);
    }


    private void addFileDetails(XAlignedComponentPanel panel, AbstractFile file, String nameLabel) {
        panel.addRow(nameLabel+":", new FileLabel(file, false), 0);

        AbstractFile parent = file.getParentSilently();

        panel.addRow(Translator.get("location")+":", new FileLabel((parent==null?file:parent), true), 0);

        panel.addRow(Translator.get("size")+":", new JLabel(SizeFormat.format(file.getSize(), SizeFormat.DIGITS_FULL| SizeFormat.UNIT_LONG| SizeFormat.INCLUDE_SPACE)), 0);

        panel.addRow(Translator.get("date")+":", new JLabel(CustomDateFormat.format(new Date(file.getDate()))), 0);

        panel.addRow(Translator.get("permissions")+":", new JLabel(file.getPermissionsString()), 10);
    }



    /**
     * Returns true if the 'apply to all' checkbox has been selected.
     */
    public boolean applyToAllSelected() {
        return applyToAllCheckBox==null?false:applyToAllCheckBox.isSelected();
    }
	
}
