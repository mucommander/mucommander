/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

 
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.dialog.DialogToolkit;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.menu.MenuToolkit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


/**
 * 
 *
 * @author Maxence Bernard
 */
public class ViewerFrame extends JFrame implements ActionListener, Runnable {
	
    private MainFrame mainFrame;
    private AbstractFile file;
    private FileViewer viewer;

    private JMenuBar menuBar;
    private MnemonicHelper menuMnemonicHelper;
    private JMenuItem closeItem;

    private final static Dimension MIN_DIMENSION = new Dimension(200, 150);
	
    private final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";

	
    /**
     * Creates a new ViewerFrame to start viewing the given file.
     *
     * <p>This constructor has package access only, ViewerFrame need to be created can
     * {@link ViewerRegistrar#createViewerFrame(MainFrame,AbstractFile,Image)}.
     */
    ViewerFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        super();

        setIconImage(icon);
        this.mainFrame = mainFrame;
        this.file = file;
		
        // Create default menu
        this.menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        // File menu
        this.menuBar = new JMenuBar();
        JMenu menu = addMenu(Translator.get("file_viewer.file_menu"));
        closeItem = MenuToolkit.addMenuItem(menu, Translator.get("file_viewer.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
        menu.add(closeItem);

        // Add menu to frame
        setJMenuBar(menuBar);
		
        // Important: dispose window on close (default is hide)
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setResizable(true);

        new Thread(ViewerFrame.this, "com.mucommander.ui.viewer.ViewerFrame's Thread").start();
    }

    protected JMenu addMenu(String menuTitle) {
        JMenu menu = MenuToolkit.addMenu(menuTitle, menuMnemonicHelper, null);
        this.menuBar.add(menu);
        return menu;
    }
	
	
    public void run() {
        try {
            FileViewer viewer = ViewerRegistrar.createFileViewer(file);

            // Test if file is too large to be viewed and warns user
            long max = viewer.getMaxRecommendedSize();
            if (max!=-1 && file.getSize()>max) {
                QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("warning"), Translator.get("file_viewer.large_file_warning"), mainFrame, 
                                                           new String[] {Translator.get("file_viewer.open_anyway"), Translator.get("cancel")},
                                                           new int[]  {0, 1},
                                                           0);
	
                int ret = dialog.getActionValue();
				
                if (ret==1 || ret==-1)
                    return;
            }

            viewer.setFrame(this);
            viewer.setCurrentFile(file);
            viewer.view(file);
            setViewer(viewer);

            // Sets panel to preferred size, without exceeding a maximum size and with a minumum size
            pack();
            setVisible(true);
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught: "+e);

            JOptionPane.showMessageDialog(mainFrame, Translator.get("file_viewer.view_error"), Translator.get("file_viewer.view_error_title"), JOptionPane.ERROR_MESSAGE);
        }
    }


    private void setViewer(FileViewer viewer) {
        this.viewer = viewer;
	
        JScrollPane scrollPane = new JScrollPane(viewer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
                public Insets getInsets() {
                    return new Insets(0, 0, 0, 0);
                }
            };
		
        setContentPane(scrollPane);

        // Catch Apple+W keystrokes under Mac OS X to close the window
        if(com.mucommander.PlatformManager.OS_FAMILY==com.mucommander.PlatformManager.MAC_OS_X) {
            scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK), CUSTOM_DISPOSE_EVENT);
            scrollPane.getActionMap().put(CUSTOM_DISPOSE_EVENT, new AbstractAction() {
                    public void actionPerformed(ActionEvent e){
                        dispose();
                    }
                });
        }

        // Request focus on text area when visible
        FocusRequester.requestFocus(viewer);
    }
	

    public void pack() {
        super.pack();

        setTitle(viewer.getTitle());

        DialogToolkit.fitToScreen(this);
        DialogToolkit.fitToMinDimension(this, MIN_DIMENSION);
    }
	
	
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==closeItem)
            dispose();
    }

	
}
