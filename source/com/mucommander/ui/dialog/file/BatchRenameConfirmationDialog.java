/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.BatchRenameAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BatchRenameConfirmationDialog extends FocusDialog implements ActionListener {

    private JButton btnRename;
    
    private boolean proceedWithRename = false;
 
    public BatchRenameConfirmationDialog(MainFrame mainFrame, FileSet files, int changed, int unchanged) {
        super(mainFrame, ActionProperties.getActionLabel(BatchRenameAction.Descriptor.ACTION_ID), mainFrame);

        YBoxPanel mainPanel = new YBoxPanel();
        String msg = Translator.get("batch_rename_dialog.proceed_renaming", Integer.toString(changed), Integer.toString(unchanged));
        mainPanel.add(new InformationPane(msg,
                Translator.get("this_operation_cannot_be_undone"),
                Font.BOLD, InformationPane.getPredefinedIcon(InformationPane.WARNING_ICON)));
        mainPanel.addSpace(10);
        btnRename = new JButton(Translator.get("rename"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        mainPanel.add(DialogToolkit.createOKCancelPanel(btnRename, cancelButton, getRootPane(), this));
        getContentPane().add(mainPanel);
        setInitialFocusComponent(btnRename);

        // Call dispose() when dialog is closed
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Size dialog and show it to the screen
        setResizable(false);
        showDialog();
    }
    
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==btnRename) {
            proceedWithRename = true;
        }
        dispose();
    }
    
    public boolean isProceedWithRename() {
        return proceedWithRename;
    }

}
