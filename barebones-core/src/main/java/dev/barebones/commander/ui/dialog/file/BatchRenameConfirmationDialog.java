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

package dev.barebones.commander.ui.dialog.file;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.commons.util.ui.dialog.DialogToolkit;
import dev.barebones.commander.commons.util.ui.dialog.FocusDialog;
import dev.barebones.commander.commons.util.ui.layout.YBoxPanel;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.ActionProperties;
import dev.barebones.commander.ui.layout.InformationPane;
import dev.barebones.commander.ui.main.MainFrame;

public class BatchRenameConfirmationDialog extends FocusDialog implements ActionListener {

    private JButton btnRename;
    
    private boolean proceedWithRename = false;
 
    public BatchRenameConfirmationDialog(MainFrame mainFrame, FileSet files, int changed, int unchanged) {
        super(mainFrame.getJFrame(), ActionProperties.getActionLabel(ActionType.BatchRename), mainFrame.getJFrame());

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
