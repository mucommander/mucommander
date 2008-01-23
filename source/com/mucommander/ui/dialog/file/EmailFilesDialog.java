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

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.SendMailJob;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.pref.general.GeneralPreferencesDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

/**
 * Dialog allowing the user to email files to someone.
 *
 * <p>One or several recipients, as well as a mail subject and body can be input.
 * The dialog also allows the user to review the files that have been marked,
 * select/unselect some, and displays the total file size.</p>
 *
 * @author Maxence Bernard
 */
public class EmailFilesDialog extends JobDialog implements ActionListener, ItemListener {

    private FileSet flattenedFiles;
	
    private JTextField toField;
    private JTextField subjectField;
    private JTextArea bodyArea;
    private JLabel infoLabel;
    private JCheckBox fileCheckboxes[];

    private static String lastTo = "";
    private static String lastSubject = "";
    private static String lastBody = "";

    private JButton okButton;
    private JButton cancelButton;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(400,0);	
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(550,400);	
	
	
    public EmailFilesDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, Translator.get("email_dialog.title"), files);

        // Notifies the user that mail preferences are not set and brings the preferences dialog 
        if(!SendMailJob.mailPreferencesSet()) {
            showErrorDialog(Translator.get("email_dialog.prefs_not_set"), Translator.get("email_dialog.prefs_not_set_title"));
	
            GeneralPreferencesDialog preferencesDialog = GeneralPreferencesDialog.getDialog();
            preferencesDialog.setActiveTab(GeneralPreferencesDialog.MAIL_TAB);
            preferencesDialog.showDialog();
			
            return;
        }
		
		
        try {
            // Figures out which files to send and calculates the number of files and the number of bytes
            this.flattenedFiles = getFlattenedFiles(files);
			
            Container contentPane = getContentPane();
			
            YBoxPanel mainPanel = new YBoxPanel(5);
	
            // Text fields panel
            XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

            // From (sender) field, non editable
            JLabel fromLabel = new JLabel(MuConfiguration.getVariable(MuConfiguration.MAIL_SENDER_NAME)
                                          +" <"+MuConfiguration.getVariable(MuConfiguration.MAIL_SENDER_ADDRESS)+">");
            //			fromField.setEditable(false);
            compPanel.addRow(Translator.get("email_dialog.from")+":", fromLabel, 10);
			
            // To (recipients) field
            toField = new JTextField(lastTo);
            compPanel.addRow(Translator.get("email_dialog.to")+":", toField, 10);
			
            // Subject field
            subjectField = new JTextField(lastSubject);
            compPanel.addRow(Translator.get("email_dialog.subject")+":", subjectField, 15);

            mainPanel.add(compPanel);		
	
            // Body area
            bodyArea = new JTextArea(lastBody);
            bodyArea.setRows(6);
            bodyArea.setLineWrap(true);
            JScrollPane scrollPane = new JScrollPane(bodyArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            mainPanel.add(scrollPane);
			
            mainPanel.addSpace(15);
			
            // Label showing the number of files and total size
            infoLabel = new JLabel();
            mainPanel.add(infoLabel);			

            contentPane.add(mainPanel, BorderLayout.NORTH);
			
            // checkbox showing all files that are to be sent, allowing them to be unselected
            int nbFiles = flattenedFiles.size();
            fileCheckboxes = new JCheckBox[nbFiles];
            if(nbFiles>0) {
                YBoxPanel tempPanel2 = new YBoxPanel();
                AbstractFile file;
                for(int i=0; i<nbFiles; i++) {
                    file = (AbstractFile)flattenedFiles.elementAt(i);
                    fileCheckboxes[i] = new JCheckBox(file.getName()
                                                      +" ("+ SizeFormat.format(file.getSize(), SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_SHORT| SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB)+")", true);
                    fileCheckboxes[i].addItemListener(this);
                    tempPanel2.add(fileCheckboxes[i]);
                }
                scrollPane = new JScrollPane(tempPanel2, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                contentPane.add(scrollPane, BorderLayout.CENTER);
            }
            updateInfoLabel();
				
            // OK / Cancel buttons panel
            okButton = new JButton(Translator.get("email_dialog.send"));
            cancelButton = new JButton(Translator.get("cancel"));
            contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);
	
            // 'To' field will receive initial focus
            setInitialFocusComponent(toField);		
			
            // Packs dialog
            setMinimumSize(MINIMUM_DIALOG_DIMENSION);
            setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
            showDialog();
        }
        catch(IOException e) {
            showErrorDialog(Translator.get("email_dialog.read_error"), Translator.get("email_dialog.error_title"));
        }
    }


    /**
     * Updates the number of selected files and their total size.
     */
    private void updateInfoLabel() {
        int nbFiles = fileCheckboxes.length;
        int nbSelected = 0;
        int bytesTotal = 0;
        long fileSize;
        for(int i=0; i<nbFiles; i++) {
            if(fileCheckboxes[i].isSelected()) {
                fileSize = ((AbstractFile)flattenedFiles.elementAt(i)).getSize();
                if(fileSize>0)
                    bytesTotal += fileSize;
                nbSelected++;
            }
        }
        String text = 
            Translator.get("nb_files", ""+nbSelected)
            +(nbSelected==0?"":" ("+ SizeFormat.format(bytesTotal, SizeFormat.DIGITS_MEDIUM| SizeFormat.UNIT_LONG| SizeFormat.ROUND_TO_KB)+")");
        infoLabel.setText(text);
        infoLabel.repaint(100);
    }


    /**
     * Returns a FileSet of *files* (as opposed to folders) that have been found either in the given 
     * FileSet or in one of the subfolders. 
     *
     * @param originalFiles files as selected by the user which may contain folders
     */
    private FileSet getFlattenedFiles(FileSet originalFiles) throws IOException {
        int nbFiles = originalFiles.size();
        FileSet flattenedFiles = new FileSet(originalFiles.getBaseFolder());
        for(int i=0; i<nbFiles; i++)
            recurseOnFolder((AbstractFile)originalFiles.elementAt(i), flattenedFiles);

        return flattenedFiles;
    }

    /**
     * Adds the given file to the FileSet if it's not a folder, recurses otherwise.
     */
    private void recurseOnFolder(AbstractFile file, FileSet flattenedFiles) throws IOException {
        if(file.isDirectory() && !file.isSymlink()) {
            AbstractFile children[] = file.ls();
            for(int i=0; i<children.length; i++)
                recurseOnFolder(children[i], flattenedFiles);
        }
        else {
            flattenedFiles.add(file);
        }
    }
	
	
    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // OK Button
        if (source==okButton)  {
            String to = toField.getText().trim();
            String subject = subjectField.getText();
            String body = bodyArea.getText();
            if(!to.equals("")) {
                lastTo = to;
                lastSubject = subject;
                lastBody = body;
				
                // Starts by disposing the dialog
                dispose();

                // Creates new FileSet with files that have been selected
                FileSet filesToSend = new FileSet(flattenedFiles.getBaseFolder());
                int nbFiles = fileCheckboxes.length;
                for(int i=0; i<nbFiles; i++)
                    if(fileCheckboxes[i].isSelected())
                        filesToSend.add(flattenedFiles.elementAt(i));

                // Starts sending files
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("email_dialog.sending"));
                SendMailJob mailJob = new SendMailJob(progressDialog, mainFrame, filesToSend, to, subject, body);
                progressDialog.start(mailJob);
            }
        }
        // Cancel button
        else if (source==cancelButton)  {
            dispose();			
        }
    }
	
    //////////////////////////
    // ItemListener methods //
    //////////////////////////
		
    /**
     * Updates label text whenever a checkbox has been checked or unchecked.
     */
    public void itemStateChanged(ItemEvent e) {	
        updateInfoLabel();
    }


}
