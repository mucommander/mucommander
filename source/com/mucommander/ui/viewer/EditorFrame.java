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


package com.mucommander.ui.viewer;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.layout.AsyncPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;


/**
 * A specialized <code>JFrame</code> that displays a {@link FileEditor} for a given file and provides some common
 * editing functionalities. The {@link FileEditor} instance is provided by {@link EditorRegistrar}.
 *
 * @author Maxence Bernard
 */
public class EditorFrame extends JFrame implements ActionListener {

    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JMenuItem closeItem;
	
    private MainFrame mainFrame;
    private AbstractFile file;
    private FileEditor editor;
	
    /** Serves to indicate if saving is needed before closing the window, value should only be modified using the setSaveNeeded() method */
    private boolean saveNeeded;
		
    private final static Dimension MIN_DIMENSION = new Dimension(480, 360);

    private final static int YES_ACTION = 0;
    private final static int NO_ACTION = 1;
    private final static int CANCEL_ACTION = 2;

    private final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";

	
    /**
     * Creates a new EditorFrame to start viewing the given file.
     *
     * <p>This constructor has package access only, EditorFrame can to be created by
     * {@link EditorRegistrar#createEditorFrame(MainFrame,AbstractFile,Image)}.
     */
    EditorFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        super();

        setIconImage(icon);
        this.mainFrame = mainFrame;
        this.file = file;
		
        // Create default menu
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        // File menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = MenuToolkit.addMenu(Translator.get("file_editor.file_menu"), menuMnemonicHelper, null);
        saveItem = MenuToolkit.addMenuItem(menu, Translator.get("file_editor.save"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK), this);
        saveAsItem = MenuToolkit.addMenuItem(menu, Translator.get("file_editor.save_as"), menuItemMnemonicHelper, null, this);
        menu.add(new JSeparator());
        closeItem = MenuToolkit.addMenuItem(menu, Translator.get("file_editor.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
		menuBar.add(menu);

        setJMenuBar(menuBar);
        
        // Call #dispose() on close (default is hide)
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setResizable(true);

        initContentPane();
    }

    private void initContentPane() {
        try {
            this.editor = EditorRegistrar.createFileEditor(file);

            // Test if file is too large to be viewed and warns user
            long max = editor.getMaxRecommendedSize();
            if (max!=-1 && file.getSize()>max) {
                QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("warning"), Translator.get("file_editor.large_file_warning"), mainFrame,
                                                           new String[] {Translator.get("file_editor.open_anyway"), Translator.get("cancel")},
                                                           new int[]  {0, 1},
                                                           0);

                int ret = dialog.getActionValue();

                if (ret==1 || ret==-1)
                    return;
            }

            editor.setFrame(EditorFrame.this);
            editor.setCurrentFile(file);

            AsyncPanel asyncPanel = new AsyncPanel() {
                public JComponent getTargetComponent() {
                    try {
                        editor.edit(file);
                    }
                    catch(Exception e) {
                        showGenericEditErrorDialog();
                        dispose();
                        return editor;
                    }

                    setTitle(editor.getTitle());

                    JScrollPane scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
                            public Insets getInsets() {
                                return new Insets(0, 0, 0, 0);
                            }
                        };

                    // Catch Apple+W keystrokes under Mac OS X to try and close the window
                    if(com.mucommander.PlatformManager.getOsFamily()==com.mucommander.PlatformManager.MAC_OS_X) {
                        scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK), CUSTOM_DISPOSE_EVENT);
                        scrollPane.getActionMap().put(CUSTOM_DISPOSE_EVENT, new AbstractAction() {
                                public void actionPerformed(ActionEvent e){
                                    dispose();
                                }
                            });
                    }

                    return scrollPane;
                }

                protected void updateLayout() {
                    super.updateLayout();

                    // Request focus on the viewer when it is visible
                    FocusRequester.requestFocus(editor);
                }
            };

            // Add the AsyncPanel to the content pane
            JPanel contentPane = new JPanel(new BorderLayout());
            contentPane.add(asyncPanel, BorderLayout.CENTER);
            setContentPane(contentPane);

            // Sets panel to preferred size, without exceeding a maximum size and with a minumum size
            pack();
            setVisible(true);
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught: "+e);

            showGenericEditErrorDialog();
        }
    }

    public void showGenericEditErrorDialog() {
        JOptionPane.showMessageDialog(mainFrame, Translator.get("file_editor.edit_error"), Translator.get("file_editor.edit_error_title"), JOptionPane.ERROR_MESSAGE);
    }


    public void setSaveNeeded(boolean saveNeeded) {
        if(this.saveNeeded!=saveNeeded) {
            this.saveNeeded = saveNeeded;
            // Marks/unmarks the window as dirty under Mac OS X (symbolized by a dot in the window closing icon)
            if(PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X)
                this.getRootPane().putClientProperty("windowModified", saveNeeded?Boolean.TRUE:Boolean.FALSE);
        }
		
    }

    public void trySaveAs() {
        JFileChooser fileChooser = new JFileChooser();
		
        // Sets selected file in JFileChooser to current file
        if(file.getURL().getProtocol().equals(FileProtocols.FILE))
            fileChooser.setSelectedFile(new java.io.File(file.getAbsolutePath()));
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int ret = fileChooser.showSaveDialog(this);
		
        if (ret==JFileChooser.APPROVE_OPTION) {
            AbstractFile selectedFile;
            try {
                selectedFile = FileFactory.getFile(fileChooser.getSelectedFile().getAbsolutePath(), true);
            }
            catch(IOException e) {
                JOptionPane.showMessageDialog(this, Translator.get("file_editor.cannot_write"), Translator.get("write_error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
			
            if (selectedFile.exists()) {
                QuestionDialog dialog = new QuestionDialog(this, null, Translator.get("file_already_exists", selectedFile.getName()), this, 
                                                           new String[] {Translator.get("replace"), Translator.get("dont_replace"), Translator.get("cancel")},
                                                           new int[]  {YES_ACTION, NO_ACTION, CANCEL_ACTION},
                                                           0);
                ret = dialog.getActionValue();
				
                if (ret==NO_ACTION || ret==CANCEL_ACTION)
                    return;
            }
			
            if (trySave(selectedFile)) {
                this.file = selectedFile;
                editor.setCurrentFile(file);
                setTitle(editor.getTitle());
            }
        }
    }

    // Returns false if an error occurred while saving the file.
    public boolean trySave(AbstractFile destFile) {
        try {
            editor.saveAs(destFile);
            return true;
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(this, Translator.get("file_editor.cannot_write"), Translator.get("write_error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Returns true if the file does not have any unsaved change or if the user refused to save the changes,
    // false if the user cancelled the dialog or the save failed.
    public boolean askSave() {
        if(!saveNeeded)
            return true;

        QuestionDialog dialog = new QuestionDialog(this, null, Translator.get("file_editor.save_warning"), this,
                                                   new String[] {Translator.get("save"), Translator.get("dont_save"), Translator.get("cancel")},
                                                   new int[]  {YES_ACTION, NO_ACTION, CANCEL_ACTION},
                                                   0);
        int ret = dialog.getActionValue();

        if((ret==YES_ACTION && trySave(file)) || ret==NO_ACTION) {
            setSaveNeeded(false);
            return true;
        }

        return false;       // User cancelled or the file couldn't be properly saved
    }




    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // File menu
        if (source==saveItem) {
            trySave(file);
        }		
        else if (source==saveAsItem) {
            trySaveAs();
        }		
        else if (source==closeItem) {
            dispose();
        }			
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public void pack() {
        super.pack();

        DialogToolkit.fitToScreen(this);
        DialogToolkit.fitToMinDimension(this, MIN_DIMENSION);

        DialogToolkit.centerOnWindow(this, mainFrame);
    }


    public void dispose() {
        if(askSave())   /// Returns true if the file does not have any unsaved change or if the user refused to save the changes
            super.dispose();
    }
}
