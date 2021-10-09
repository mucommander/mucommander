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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.FileViewerService;
import com.mucommander.viewer.ViewerPresenter;
import com.mucommander.viewer.WarnUserException;
import com.mucommander.text.Translator;

/**
 * File viewer presenter to handle multiple file viewers.
 *
 * <p>
 * <b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.
 * </p>
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class FileViewerPresenter extends FilePresenter implements ViewerPresenter {

    private FileViewer fileViewer = null;
    private final JMenuBar menuBar;
    private final JMenu viewerMenu;
    private final ButtonGroup viewersButtonGroup;
    private final List<FileViewerService> services = new ArrayList<>();
    private int viewersCount = 0;
    private JMenuItem closeMenuItem;

    public FileViewerPresenter() {
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        viewerMenu = MenuToolkit.addMenu(Translator.get("file_viewer.viewer_menu"), menuMnemonicHelper, null);
        menuBar = new JMenuBar();
        viewersButtonGroup = new ButtonGroup();
    }

    /**
     * Returns the menu bar that controls the viewer's frame. The menu bar should be retrieved using this method and not
     * by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the viewer's frame.
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
    public void goToFile(Function<Integer, Integer> advance, FileViewerService viewerService) throws IOException {
        FileTable fileTable = getFrame().getMainFrame().getActiveTable();

        AbstractFile newFile;
        int originalRow = fileTable.getSelectedRow();
        boolean canView;
        do {
            int currentRow = fileTable.getSelectedRow();
            int newRow = advance.apply(currentRow);

            if (newRow < 0 || newRow >= fileTable.getRowCount()) {
                fileTable.selectRow(originalRow);
                return;
            }
            fileTable.selectRow(newRow);
            newFile = fileTable.getSelectedFile();
            try {
                canView = viewerService.canViewFile(newFile);
            } catch (WarnUserException ex) {
                canView = false;
            }
        } while (newFile == null || !canView);

        setCurrentFile(newFile);
        fileViewer.open(newFile);
    }

    @Override
    public boolean isFullScreen() {
        return getFrame().isFullScreen();
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        getFrame().setFullScreen(fullScreen);
    }

    @Override
    public void longOperation(Runnable operation) {
        getFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        operation.run();
        getFrame().setCursor(Cursor.getDefaultCursor());
    }

    public void addViewerService(FileViewerService service) throws UserCancelledException {
        services.add(service);
        JRadioButtonMenuItem viewerMenuItem = new JRadioButtonMenuItem(service.getName());
        final int serviceIndex = viewersCount;
        viewerMenuItem.addActionListener((e) -> {
            try {
                switchFileViewer(serviceIndex);
            } catch (IOException ex) {
                Logger.getLogger(FileViewerPresenter.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        viewersButtonGroup.add(viewerMenuItem);
        viewerMenu.add(viewerMenuItem);
        if (viewersCount == 0) {
            viewerMenuItem.setSelected(true);
        }
        viewersCount++;
    }

    @Override
    protected void show(AbstractFile file) throws IOException {
        setCurrentFile(file);
        if (fileViewer == null) {
            MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
            viewerMenu.addSeparator();
            closeMenuItem = MenuToolkit.addMenuItem(viewerMenu,
                    Translator.get("file_viewer.close"),
                    menuItemMnemonicHelper,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    (e) -> {
                        fileViewer.close();
                        getFrame().dispose();
                    });
            viewerMenu.add(closeMenuItem);

            switchFileViewer(0);
        }
    }

    private void switchFileViewer(int index) throws IOException {
        if (fileViewer != null) {
            clearComponentToPresent();
            fileViewer.close();
            resetTitle();
        }

        menuBar.removeAll();
        menuBar.add(viewerMenu);

        FileViewerService service = services.get(index);
        fileViewer = service.createFileViewer();
        fileViewer.setPresenter(this);

        JComponent viewerComponent = fileViewer.getUI();
        fileViewer.open(getCurrentFile());
        fileViewer.extendMenu(menuBar);
        menuBar.revalidate();
        setComponentToPresent(viewerComponent);
    }
}
