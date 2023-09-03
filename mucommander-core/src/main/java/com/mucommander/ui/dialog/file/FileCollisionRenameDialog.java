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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.job.impl.CopyJob;
import com.mucommander.job.ui.DialogResult;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

/**
 * Dialog invoked when the user wants to change a file name after a collision has been detected
 * while copying or moving files.
 *
 * @see CopyJob
 * @author Mariusz Jakubowski
 */
public class FileCollisionRenameDialog extends FocusDialog implements ActionListener, DialogResult {
	
    private JTextField edtNewName;

    private JButton okButton;

	private String newName;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);


    /**
     * Creates a new rename file dialog.
     *
     * @param mainFrame the parent MainFrame 
     * @param file the file to rename.
     */
    public FileCollisionRenameDialog(MainFrame mainFrame, AbstractFile file) {
        super(mainFrame.getJFrame(), Translator.get("rename"), mainFrame.getJFrame());

        Container contentPane = getContentPane();

        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get("rename_dialog.new_name") + ":"));
        edtNewName = new JTextField();
        edtNewName.addActionListener(this);

        // Sets the initial selection.
        AbstractCopyDialog.selectDestinationFilename(file, file.getName(), 0).feedToPathField(edtNewName);
        mainPanel.add(edtNewName);
   
        mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton(Translator.get("rename"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(edtNewName);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // OK Button
        if(source == okButton || source == edtNewName) {
        	newName = edtNewName.getText();
        } else {
            newName = null;
        }
        dispose();
    }

    public Object getUserInput() {
        showDialog();
        return newName;
    }
    
}
