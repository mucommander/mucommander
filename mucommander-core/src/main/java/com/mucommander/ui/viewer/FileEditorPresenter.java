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
package com.mucommander.ui.viewer;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.viewer.CloseCancelledException;
import com.mucommander.viewer.EditorPresenter;
import com.mucommander.viewer.FileEditor;
import com.mucommander.viewer.FileEditorService;

/**
 * File editor presenter to handle multiple file editors.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class FileEditorPresenter extends FilePresenter implements EditorPresenter {

    private final JMenuBar menuBar;
    private final JMenu editorMenu;
    private final ButtonGroup editorsButtonGroup;
    private final List<FileEditorService> services = new ArrayList<>();
    private FileEditor fileEditor;
    private int editorsCount = 0;
    private int activeEditor = 0;

    private JMenuItem fullScreenMenuItem;
    private JMenuItem closeMenuItem;

    public FileEditorPresenter() {
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        editorMenu = MenuToolkit.addMenu(Translator.get("file_editor.editor_menu"), menuMnemonicHelper, null);
        menuBar = new JMenuBar();
        editorsButtonGroup = new ButtonGroup();
    }

    /**
     * Returns the menu bar that controls the editor's frame. The menu bar should be retrieved using this method and not
     * by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the editor's frame.
     */
    @Override
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    @Override
    public void extendTitle(String title) {
        getFrame().setTitle(super.getTitle() + title);
    }

    private void resetTitle() {
        getFrame().setTitle(super.getTitle());
    }

    @Override
    public JFrame getWindowFrame() {
        return getFrame();
    }

    @Override
    public void longOperation(Runnable operation) {
        getFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        operation.run();
        getFrame().setCursor(Cursor.getDefaultCursor());
    }

    public void addEditorService(FileEditorService service) throws UserCancelledException {
        services.add(service);
        JRadioButtonMenuItem editorMenuItem = new JRadioButtonMenuItem(service.getName());
        final int serviceIndex = editorsCount;
        editorMenuItem.addActionListener((e) -> {
            try {
                switchFileEditor(serviceIndex);
            } catch (IOException ex) {
                Logger.getLogger(FileEditorPresenter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CloseCancelledException ex) {
                editorMenu.getItem(activeEditor).setSelected(true);
            }
        });
        editorsButtonGroup.add(editorMenuItem);
        editorMenu.add(editorMenuItem);
        if (editorsCount == 0) {
            editorMenuItem.setSelected(true);
        }
        editorsCount++;
    }

    @Override
    protected void show(AbstractFile file) throws IOException {
        setCurrentFile(file);
        if (fileEditor == null) {
            getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            getFrame().addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (fileEditor != null) {
                        try {
                            fileEditor.close();
                        } catch (CloseCancelledException ex) {
                            // cancelled
                            return;
                        }

                        close();
                    }
                }
            });

            MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
            editorMenu.addSeparator();

            fullScreenMenuItem = MenuToolkit.addCheckBoxMenuItem(editorMenu,
                    Translator.get("file_editor.fullscreen"),
                    menuItemMnemonicHelper,
                    KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK),
                    (e) -> {
                        boolean fullScreen = getFrame().isFullScreen();
                        switchFullScreenMode(!fullScreen);
                    });

            String windowWidthValue = EditorPreferences.WINDOW_WIDTH.getValue();
            if (!windowWidthValue.isEmpty()) {
                int windowX = Integer.parseInt(EditorPreferences.WINDOW_POSITION_X.getValue());
                int windowY = Integer.parseInt(EditorPreferences.WINDOW_POSITION_Y.getValue());
                int windowWidth = Integer.parseInt(windowWidthValue);
                int windowHeight = Integer.parseInt(EditorPreferences.WINDOW_HEIGHT.getValue());
                getFrame().setBounds(windowX, windowY, windowWidth, windowHeight);
            } else {
                getFrame().setDefaultBounds();
            }

            String showFullscreenValue = EditorPreferences.SHOW_FULLSCREEN.getValue();
            if (Boolean.TRUE.toString().equals(showFullscreenValue)) {
                switchFullScreenMode(true);
            }
            editorMenu.add(fullScreenMenuItem);

            closeMenuItem = MenuToolkit.addMenuItem(editorMenu,
                    Translator.get("file_editor.close"),
                    menuItemMnemonicHelper,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    e -> getFrame().dispatchEvent(new WindowEvent(getFrame(), WindowEvent.WINDOW_CLOSING)));
            editorMenu.add(closeMenuItem);

            try {
                switchFileEditor(0);
            } catch (CloseCancelledException ex) {
                Logger.getLogger(FileEditorPresenter.class.getName()).log(Level.SEVERE, "Unexpected cancellation", ex);
            }
        }
    }

    private void close() {
        FileFrame frame = getFrame();
        frame.revalidate();
        Rectangle bounds = frame.getBounds();
        EditorPreferences.WINDOW_POSITION_X.setValue(Integer.toString(bounds.x));
        EditorPreferences.WINDOW_POSITION_Y.setValue(Integer.toString(bounds.y));
        EditorPreferences.WINDOW_WIDTH.setValue(Integer.toString(bounds.width));
        EditorPreferences.WINDOW_HEIGHT.setValue(Integer.toString(bounds.height));

        frame.dispose();
    }

    private void switchFileEditor(int index) throws IOException, CloseCancelledException {
        if (fileEditor != null) {
            fileEditor.close();
            clearComponentToPresent();
            resetTitle();
        }

        menuBar.removeAll();
        menuBar.add(editorMenu);

        FileEditorService service = services.get(index);
        fileEditor = service.createFileEditor();
        fileEditor.setPresenter(this);

        JComponent editorComponent = fileEditor.getUI();
        fileEditor.open(getCurrentFile());
        fileEditor.extendMenu(menuBar);
        menuBar.revalidate();
        menuBar.repaint();
        setComponentToPresent(editorComponent);
        activeEditor = index;
    }

    private void switchFullScreenMode(boolean showFullScreen) {
        getFrame().setFullScreen(showFullScreen);
        fullScreenMenuItem.setSelected(showFullScreen);
        EditorPreferences.SHOW_FULLSCREEN.setValue(Boolean.toString(showFullScreen));
    }

    @Override
    public void requestFocus() {
        fileEditor.requestFocus();
    }
}
