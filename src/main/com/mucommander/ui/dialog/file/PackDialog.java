/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archiver.Archiver;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.job.ArchiveJob;
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.PackAction;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;


/**
 * This dialog allows the user to pack marked files to an archive file of a selected format (Zip, TAR, ...)
 * and add an optional comment to the archive (for the formats that support it).
 *
 * @author Maxence Bernard
 */
public class PackDialog extends TransferDestinationDialog implements ItemListener {

    private JComboBox formatsComboBox;
    private int formats[];
	
    private JTextArea commentArea;

    /** Used to keep track of the last selected archive format. */
    private int lastFormatIndex;

    /** Last archive format used (Zip initially), selected by default when this dialog is created */
    private static int lastFormat = Archiver.ZIP_FORMAT;


    public PackDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files, ActionProperties.getActionLabel(PackAction.Descriptor.ACTION_ID), Translator.get("pack_dialog_description"), Translator.get("pack"), Translator.get("pack_dialog.error_title"), false);

        // Retrieve available formats for single file or many file archives
        int nbFiles = files.size();
        this.formats = Archiver.getFormats(nbFiles>1 || (nbFiles>0 && files.elementAt(0).isDirectory()));
        int nbFormats = formats.length;

        int initialFormat = formats[0];		// this value will only be used if last format is not available
        int initialFormatIndex = 0;			// this value will only be used if last format is not available
        for(int i=0; i<nbFormats; i++) {
            if(formats[i]==lastFormat) {
                initialFormat = formats[i];
                initialFormatIndex = i;
                break;
            }
        }
        lastFormat = initialFormat;
        lastFormatIndex = initialFormatIndex;

        // Archive formats combo box

        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(new JLabel(Translator.get("pack_dialog.archive_format")));		
        this.formatsComboBox = new JComboBox();
        for(int i=0; i<nbFormats; i++)
            formatsComboBox.addItem(Archiver.getFormatName(formats[i]));

        formatsComboBox.setSelectedIndex(lastFormatIndex);
		
        formatsComboBox.addItemListener(this);
        tempPanel.add(formatsComboBox);

        YBoxPanel mainPanel = getMainPanel();
        mainPanel.add(tempPanel);		
        mainPanel.addSpace(10);
		
        // Comment area, enabled only if selected archive format has comment support
		
        mainPanel.add(new JLabel(Translator.get("comment")));
        commentArea = new JTextArea();
        commentArea.setRows(4);
        mainPanel.add(commentArea);
    }
	

    //////////////////////////////////////////////
    // TransferDestinationDialog implementation //
    //////////////////////////////////////////////

    @Override
    protected PathFieldContent computeInitialPath(FileSet files) {
        String initialPath = mainFrame.getInactivePanel().getCurrentFolder().getAbsolutePath(true);
        AbstractFile file;
        String fileName;
        // Computes the archive's default name:
        // - if it only contains one file, uses that file's name.
        // - if it contains more than one file, uses the FileSet's parent folder's name.
        if(files.size() == 1) {
            file = files.elementAt(0);
            fileName = file.isDirectory() && !DesktopManager.isApplication(file)
                    ?file.getName()
                    :file.getNameWithoutExtension();
        }
        else {
            file = files.getBaseFolder();
            fileName = file.isRoot()?"":DesktopManager.isApplication(file)?file.getNameWithoutExtension():file.getName();
        }

        return new PathFieldContent(initialPath + fileName + "." + Archiver.getFormatExtension(lastFormat), initialPath.length(), initialPath.length() + fileName.length());
    }

    @Override
    protected TransferFileJob createTransferFileJob(ProgressDialog progressDialog, PathUtils.ResolvedDestination resolvedDest, int defaultFileExistsAction) {
        // Remember last format used, for next time this dialog is invoked
        lastFormat = formats[formatsComboBox.getSelectedIndex()];

        return new ArchiveJob(progressDialog, mainFrame, files, resolvedDest.getDestinationFile(), lastFormat, Archiver.formatSupportsComment(lastFormat)?commentArea.getText():null);
    }

    @Override
    protected String getProgressDialogTitle() {
        return Translator.get("pack_dialog.packing");
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected boolean isValidDestination(PathUtils.ResolvedDestination resolvedDest, String destPath) {
        if(resolvedDest==null)
            return false;

        int destType = resolvedDest.getDestinationType();
        return destType==PathUtils.ResolvedDestination.NEW_FILE || destType==PathUtils.ResolvedDestination.EXISTING_FILE;
    }


    //////////////////////////
    // ItemListener methods //
    //////////////////////////

    public void itemStateChanged(ItemEvent e) {
        int newFormatIndex;

        FilePathField pathField = getPathField();

        // Updates the GUI if, and only if, the format selection has changed.
        if(lastFormatIndex != (newFormatIndex = formatsComboBox.getSelectedIndex())) {

            String fileName = pathField.getText();  // Name of the destination archive file.
            String oldFormatExtension = Archiver.getFormatExtension(formats[lastFormatIndex]);	// Old/current format's extension
            if(fileName.endsWith("." + oldFormatExtension)) {
                int selectionStart;
                int selectionEnd;

                // Saves the old selection.
                selectionStart = pathField.getSelectionStart();
                selectionEnd   = pathField.getSelectionEnd();

                // Computes the new file name.
                fileName = fileName.substring(0, fileName.length() - oldFormatExtension.length()) +
                    Archiver.getFormatExtension(formats[newFormatIndex]);

                // Makes sure that the selection stays somewhat coherent.
                if(selectionEnd == pathField.getText().length())
                    selectionEnd = fileName.length();

                // Resets the file path field.
                pathField.setText(fileName);
                pathField.setSelectionStart(selectionStart);
                pathField.setSelectionEnd(selectionEnd);
            }

            commentArea.setEnabled(Archiver.formatSupportsComment(formats[formatsComboBox.getSelectedIndex()]));
            lastFormatIndex = newFormatIndex;
        }

        // Transfer focus back to the text field 
        pathField.requestFocus();
    }
}
