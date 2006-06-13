
package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.file.FileToolkit;
import com.mucommander.job.MkdirJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.DialogToolkit;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.dialog.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Dialog invoked when the user wants to create a new folder (F7).
 *
 * @author Maxence Bernard
 */
public class MkdirDialog extends FocusDialog implements ActionListener {

    private MainFrame mainFrame;
	
    private JTextField mkdirPathField;
	
    private JButton okButton;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	


    public MkdirDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get("mkdir_dialog.title"), mainFrame);
        this.mainFrame = mainFrame;
		
        Container contentPane = getContentPane();

        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get("mkdir_dialog.description")));
        mkdirPathField = new JTextField();
        mkdirPathField.addActionListener(this);
        mainPanel.add(mkdirPathField);
		
        mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton(Translator.get("mkdir_dialog.create"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(mkdirPathField);		

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
        showDialog();
    }



    /**
     * Creates a new directory. This method is trigged by the 'OK' button or return key.
     */
    public void doMkdir() {
        String dirPath = mkdirPathField.getText();

        // Resolves destination folder
        Object ret[] = FileToolkit.resolvePath(dirPath, mainFrame.getLastActiveTable().getCurrentFolder());
        // The path entered doesn't correspond to any existing folder
        if (ret==null) {
            showErrorDialog(Translator.get("mkdir_dialog.invalid_path", dirPath), Translator.get("mkdir_dialog.error_title"));
            return;
        }

        if(ret[1]==null) {
            showErrorDialog(Translator.get("mkdir_dialog.dir_already_exists", dirPath), Translator.get("mkdir_dialog.error_title"));
            return;
        }

        AbstractFile folder = (AbstractFile)ret[0];
        String newName = (String)ret[1];

        FileSet fileSet = new FileSet(folder);
        // Job's FileSet needs to contain at least one file
        fileSet.add(folder);
        new MkdirJob(mainFrame, fileSet, newName).start();
    }

	
    private void showErrorDialog(String msg, String title) {
        JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        dispose();
		
        // OK Button
        if(source == okButton || source == mkdirPathField) {
            doMkdir();
        }
    }
	
	
}
