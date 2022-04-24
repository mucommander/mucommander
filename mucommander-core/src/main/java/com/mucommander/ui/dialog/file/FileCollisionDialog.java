/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.commons.util.ui.text.FontUtils;
import com.mucommander.job.FileCollisionChecker;
import com.mucommander.os.notifier.NotificationType;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogAction;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.notifier.NotifierProvider;
import com.mucommander.ui.text.FileLabel;


/**
 * Dialog used to inform the user that a file collision has been detected and ask him how to resolve the conflict.
 * Prior to invoking this dialog, {@link com.mucommander.job.FileCollisionChecker} can be used to check for file collisions.
 *
 * @author Maxence Bernard
 * @see com.mucommander.job.FileCollisionChecker
 */
public class FileCollisionDialog extends QuestionDialog {

    public enum FileCollisionAction implements DialogAction {

        ASK("ask"),
        CANCEL("cancel"),
        SKIP("skip"),
        OVERWRITE("overwrite"),
        OVERWRITE_IF_OLDER("overwrite_if_older"),
        OVERWRITE_IF_SIZE_DIFFERS("overwrite_if_size_differs"),
        RESUME("resume"),
        RENAME("rename");

        private final String actionName;

        FileCollisionAction(String actionKey) {
            // here or when in #getActionName
            this.actionName = Translator.get(actionKey);
        }

        public String getActionName() {
            return actionName;
        }

        // Required by JComboBox to properly display text when an object is set instead of String (sad)
        @Override
        public String toString() {
            return getActionName();
        }
    }

    private JCheckBox applyToAllCheckBox;

    /**
     * Creates a new FileCollisionDialog.
     *
     * @param owner             the Frame that owns this dialog
     * @param locationRelative  component the location of this dialog will be based on
     * @param collisionType     the type of collision as returned by {@link com.mucommander.job.FileCollisionChecker}
     * @param sourceFile        the source file that 'conflicts' with the destination file, can be null.
     * @param destFile          the destination file which already exists
     * @param multipleFilesMode if true, options that apply to multiple files will be displayed (skip, apply to all)
     * @param allowRename       if true, display an option to rename a file
     */
    public FileCollisionDialog(Dialog owner, Component locationRelative, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode, boolean allowRename) {
        super(owner, Translator.get("file_collision_dialog.title"), locationRelative);

        init(collisionType, sourceFile, destFile, multipleFilesMode, allowRename);
    }

    /**
     * Creates a new FileCollisionDialog.
     *
     * @param owner             the Frame that owns this dialog
     * @param locationRelative  component the location of this dialog will be based on
     * @param collisionType     the type of collision as returned by {@link com.mucommander.job.FileCollisionChecker}
     * @param sourceFile        the source file that 'conflicts' with the destination file, can be null.
     * @param destFile          the destination file which already exists
     * @param multipleFilesMode if true, options that apply to multiple files will be displayed (skip, apply to all)
     * @param allowRename       if true, display an option to rename a file
     */
    public FileCollisionDialog(Frame owner, Component locationRelative, int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode, boolean allowRename) {
        super(owner, Translator.get("file_collision_dialog.title"), locationRelative);

        init(collisionType, sourceFile, destFile, multipleFilesMode, allowRename);
    }


    private void init(int collisionType, AbstractFile sourceFile, AbstractFile destFile, boolean multipleFilesMode, boolean allowRename) {

        // Init choices

        List<DialogAction> actionChoices = new ArrayList<>();

        actionChoices.add(FileCollisionAction.CANCEL);

        if (multipleFilesMode) {
            actionChoices.add(FileCollisionAction.SKIP);
        }

        // Add 'overwrite' / 'overwrite if older' / 'resume' actions only for 'destination file already exists' collision type
        if (collisionType == FileCollisionChecker.DESTINATION_FILE_ALREADY_EXISTS && !destFile.isDirectory()) {
            actionChoices.add(FileCollisionAction.OVERWRITE);

            if (sourceFile != null) {
                actionChoices.add(FileCollisionAction.OVERWRITE_IF_OLDER);
                actionChoices.add(FileCollisionAction.OVERWRITE_IF_SIZE_DIFFERS);

                // Give resume option only if destination file is smaller than source file
                long destSize = destFile.getSize();
                long sourceSize = sourceFile.getSize();
                if (destSize != -1 && (sourceSize == -1 || destSize < sourceSize)) {
                    actionChoices.add(FileCollisionAction.RESUME);
                }

                if (allowRename) {
                    actionChoices.add(FileCollisionAction.RENAME);
                }
            }

        }

        // Init UI
        String desc = null;

        switch (collisionType) {
            case FileCollisionChecker.DESTINATION_FILE_ALREADY_EXISTS:
                desc = Translator.get("file_exists_in_destination");
                break;
            case FileCollisionChecker.SAME_SOURCE_AND_DESTINATION:
                desc = Translator.get("same_source_destination");
                break;
            case FileCollisionChecker.SOURCE_PARENT_OF_DESTINATION:
                desc = Translator.get("source_parent_of_destination");
                break;
        }

        YBoxPanel yPanel = new YBoxPanel();

        if (desc != null) {
            yPanel.add(new InformationPane(desc, null, Font.PLAIN, InformationPane.QUESTION_ICON));
            yPanel.addSpace(10);
        }

        // Add a separator before file details
        yPanel.add(new JSeparator());

        XAlignedComponentPanel tfPanel = new XAlignedComponentPanel(10);

        // If collision type is 'same source and destination' no need to show both source and destination 
        if (collisionType == FileCollisionChecker.SAME_SOURCE_AND_DESTINATION) {
            addFileDetails(tfPanel, sourceFile, Translator.get("name"));
        } else {
            if (sourceFile != null) {
                addFileDetails(tfPanel, sourceFile, Translator.get("source"));
            }

            addFileDetails(tfPanel, destFile, Translator.get("destination"));
        }

        yPanel.add(tfPanel);

        // Add a separator after file details
        yPanel.add(new JSeparator());

        init(yPanel, actionChoices, 3);
        // TODO below there's workaround to accommodate texts within buttons - any idea to how to make it better?
        // override to avoid FocusDialog#pack making the dialog box too small for some buttons
        // so they won't display full texts (observe when Spanish lang pack is chosen - a lot of them have ellipsis)
        setMinimumSize(null);
        setMaximumSize(null);

        // 'Apply to all' is available only for 'destination file already exists' collision type
        if (multipleFilesMode && collisionType == FileCollisionChecker.DESTINATION_FILE_ALREADY_EXISTS) {
            applyToAllCheckBox = new JCheckBox(Translator.get("apply_to_all"));
            addComponent(applyToAllCheckBox);
        }

        // Send a system notification if a notifier is available and enabled
        if (NotifierProvider.isAvailable() && NotifierProvider.getNotifier().isEnabled()) {
            NotifierProvider.displayBackgroundNotification(NotificationType.JOB_ERROR, getTitle(), desc);
        }
    }


    private void addFileDetails(XAlignedComponentPanel panel, AbstractFile file, String nameLabel) {
        addFileDetailsRow(panel, nameLabel + ":", new FileLabel(file, false), 0);

        AbstractFile parent = file.getParent();

        addFileDetailsRow(panel, Translator.get("location") + ":", new FileLabel(parent == null ? file : parent, true), 0);

        addFileDetailsRow(panel, Translator.get("size") + ":", new JLabel(SizeFormat.format(file.getSize(), SizeFormat.DIGITS_FULL | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE)), 0);

        addFileDetailsRow(panel, Translator.get("date") + ":", new JLabel(CustomDateFormat.format(new Date(file.getDate()))), 0);

        addFileDetailsRow(panel, Translator.get("permissions") + ":", new JLabel(file.getPermissionsString()), 10);
    }

    private void addFileDetailsRow(XAlignedComponentPanel panel, String label, JComponent comp, int ySpaceAfter) {
        panel.addRow(FontUtils.makeMini(new JLabel(label)), FontUtils.makeMini(comp), ySpaceAfter);
    }

    /**
     * Returns <code>true</code> if the 'apply to all' checkbox has been selected.
     *
     * @return <code>true</code> if the 'apply to all' checkbox has been selected.
     */
    public boolean applyToAllSelected() {
        return applyToAllCheckBox != null && applyToAllCheckBox.isSelected();
    }

}
