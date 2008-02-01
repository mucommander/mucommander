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

import com.mucommander.file.AbstractFile;
import com.mucommander.runtime.OsFamilies;
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


/**
 * A specialized <code>JFrame</code> that displays a {@link FileViewer} for a given file.
 * The {@link FileViewer} instance is provided by {@link ViewerRegistrar}.
 *
 * @author Maxence Bernard
 */
public class ViewerFrame extends JFrame implements ActionListener {
	
    private MainFrame mainFrame;
    private AbstractFile file;
    private FileViewer viewer;

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
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        // File menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = MenuToolkit.addMenu(Translator.get("file_viewer.file_menu"), menuMnemonicHelper, null);
        closeItem = MenuToolkit.addMenuItem(menu, Translator.get("file_viewer.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
        menu.add(closeItem);
        menuBar.add(menu);

        setJMenuBar(menuBar);

        // Call #dispose() on close (default is hide)
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setResizable(true);

        initContentPane();
    }

    private void initContentPane() {
        try {
            this.viewer = ViewerRegistrar.createFileViewer(file);

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

            AsyncPanel asyncPanel = new AsyncPanel() {
                public JComponent getTargetComponent() {
                    try {
                        viewer.view(file);
                    }
                    catch(Exception e) {
                        showGenericViewErrorDialog();
                        dispose();
                        return viewer;
                    }

                    setTitle(viewer.getTitle());

                    JScrollPane scrollPane = new JScrollPane(viewer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
                            public Insets getInsets() {
                                return new Insets(0, 0, 0, 0);
                            }
                        };

                    // Catch Apple+W keystrokes under Mac OS X to close the window
                    if(OsFamilies.MAC_OS_X.isCurrent()) {
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
                    FocusRequester.requestFocus(viewer);
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

            showGenericViewErrorDialog();
        }
    }

    public void showGenericViewErrorDialog() {
        JOptionPane.showMessageDialog(mainFrame, Translator.get("file_viewer.view_error"), Translator.get("file_viewer.view_error_title"), JOptionPane.ERROR_MESSAGE);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==closeItem)
            dispose();
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
}
