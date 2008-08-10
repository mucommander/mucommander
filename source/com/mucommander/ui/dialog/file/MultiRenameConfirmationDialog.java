package com.mucommander.ui.dialog.file;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

public class MultiRenameConfirmationDialog extends FocusDialog implements ActionListener {

    private InformationPane informationPane;

    private JButton btnRename;
    
    private boolean proceedWithRename = false;
 
    public MultiRenameConfirmationDialog(MainFrame mainFrame, FileSet files, int changed, int unchanged) {
        super(mainFrame, Translator.get("multi_rename_dialog.title"), mainFrame);

        YBoxPanel mainPanel = new YBoxPanel();
        String msg = Translator.get("multi_rename_dialog.proceed_renaming", Integer.toString(changed), Integer.toString(unchanged));
        informationPane = new InformationPane(msg,
                Translator.get("delete_dialog.permanently_delete.confirmation_details"),
                Font.BOLD, InformationPane.getPredefinedIcon(InformationPane.WARNING_ICON));
        mainPanel.add(informationPane);
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
