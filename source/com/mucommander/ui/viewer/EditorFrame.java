/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;


/**
 * A specialized <code>JFrame</code> that displays a {@link FileEditor} for a given file and provides some common
 * editing functionalities. The {@link FileEditor} instance is provided by {@link EditorRegistrar}.
 *
 * @author Maxence Bernard
 */
public class EditorFrame extends JFrame implements ActionListener, Runnable, WindowListener {

    private JMenuBar menuBar;
    private MnemonicHelper menuMnemonicHelper;

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
        this.menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        // File menu
        this.menuBar = new JMenuBar();
        JMenu menu = addMenu(Translator.get("file_editor.file_menu"));
        saveItem = MenuToolkit.addMenuItem(menu, Translator.get("file_editor.save"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK), this);
        saveAsItem = MenuToolkit.addMenuItem(menu, Translator.get("file_editor.save_as"), menuItemMnemonicHelper, null, this);
        menu.add(new JSeparator());
        closeItem = MenuToolkit.addMenuItem(menu, Translator.get("file_editor.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
		
        // Add menu to frame
        setJMenuBar(menuBar);
		
        // Do nothing on close, window close events are caught and window disposed manually 
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Catch window close events to ask the user if he wants to save his modifications
        addWindowListener(this);

        setResizable(true);

        new Thread(this, "com.mucommander.ui.viewer.EditorFrame's Thread").start();
    }

    public JMenu addMenu(String menuTitle) {
        JMenu menu = MenuToolkit.addMenu(menuTitle, menuMnemonicHelper, null);
        this.menuBar.add(menu);
        return menu;
    }
	
    private void setEditor(FileEditor editor) {
        this.editor = editor;
	
        //		setBackground(BG_COLOR);
        JScrollPane scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
                public Insets getInsets() {
                    return new Insets(0, 0, 0, 0);
                }
            };
		
        setContentPane(scrollPane);

        // Catch Apple+W keystrokes under Mac OS X to try and close the window
        if(com.mucommander.PlatformManager.getOsFamily()==com.mucommander.PlatformManager.MAC_OS_X) {
            scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK), CUSTOM_DISPOSE_EVENT);
            scrollPane.getActionMap().put(CUSTOM_DISPOSE_EVENT, new AbstractAction() {
                    public void actionPerformed(ActionEvent e){
                        dispose();
                    }
                });
        }
    }
	
    public void setSaveNeeded(boolean saveNeeded) {
        if(this.saveNeeded!=saveNeeded) {
            this.saveNeeded = saveNeeded;
            // Marks/unmarks the window as dirty under Mac OS X (symbolized by a dot in the window closing icon)
            if(PlatformManager.getOsFamily()==PlatformManager.MAC_OS_X)
                this.getRootPane().putClientProperty("windowModified", saveNeeded?Boolean.TRUE:Boolean.FALSE);
        }
		
    }

    private void saveAs() {
        JFileChooser fileChooser = new JFileChooser();
		
        // Sets selected file in JFileChooser to current file
        if(file.getURL().getProtocol().equals(FileProtocols.FILE))
            fileChooser.setSelectedFile(new java.io.File(file.getAbsolutePath()));
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int ret = fileChooser.showSaveDialog(this);
		
        if (ret==JFileChooser.APPROVE_OPTION) {
            AbstractFile selectedFile = null;
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
			
            if (trySaveAs(selectedFile)) {
                this.file = selectedFile;
                editor.setCurrentFile(file);
                setTitle(editor.getTitle());
            }
        }
    }

	
    public boolean trySaveAs(AbstractFile destFile) {
        try {
            editor.saveAs(destFile);
            return true;
        }
        catch(IOException e) {
            JOptionPane.showMessageDialog(this, Translator.get("file_editor.cannot_write"), Translator.get("write_error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
	

    //////////////////////
    // Runnable methods //
    //////////////////////

    public void run() {
        try {
            FileEditor editor = EditorRegistrar.createFileEditor(file);

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

            editor.setFrame(this);
            editor.setCurrentFile(file);
            editor.edit(file);
            setEditor(editor);

            // Sets panel to preferred size, without exceeding a maximum size and with a minumum size
            pack();
            DialogToolkit.centerOnWindow(this, mainFrame);
            setVisible(true);
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught: "+e);

            JOptionPane.showMessageDialog(mainFrame, Translator.get("file_editor.edit_error"), Translator.get("file_editor.edit_error_title"), JOptionPane.ERROR_MESSAGE);
        }
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // File menu
        if (source==saveItem) {
            trySaveAs(file);
        }		
        else if (source==saveAsItem) {
            saveAs();
        }		
        else if (source==closeItem) {
            dispose();
        }			
    }


    ///////////////////////////////////
    // WindowListener implementation //
    ///////////////////////////////////

    public void windowClosing(WindowEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
        
        dispose();
    }

    public void windowActivated(WindowEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
        
        editor.requestFocus();
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public void pack() {
        super.pack();

        setTitle(editor.getTitle());

        DialogToolkit.fitToScreen(this);
        DialogToolkit.fitToMinDimension(this, MIN_DIMENSION);
    }


    public void dispose() {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");

        // Has any change to the file been made ?
        if (saveNeeded) {
            QuestionDialog dialog = new QuestionDialog(this, null, Translator.get("file_editor.save_warning"), this,
                                                       new String[] {Translator.get("save"), Translator.get("dont_save"), Translator.get("cancel")},
                                                       new int[]  {YES_ACTION, NO_ACTION, CANCEL_ACTION},
                                                       0);
            int ret = dialog.getActionValue();

            if ((ret==YES_ACTION && trySaveAs(file)) || ret==NO_ACTION) {   // Do not dispose editor if save failed
                super.dispose();
            }
        }
        else {
            super.dispose();
        }
    }
}
