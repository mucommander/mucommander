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
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogAction;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.viewer.CanOpen;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.FileViewerService;
import com.mucommander.viewer.ViewerPresenter;

import org.jetbrains.annotations.NotNull;

import static com.mucommander.ui.dialog.QuestionDialog.DIALOG_DISPOSED_ACTION;

/**
 * File viewer presenter to handle multiple file viewers.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class FileViewerPresenter extends FilePresenter implements ViewerPresenter {

    private final JMenuBar menuBar;
    private final JMenu viewerMenu;
    private final ButtonGroup viewersButtonGroup;
    private final List<FileViewerService> services = new ArrayList<>();
    private FileViewer fileViewer;
    private int viewersCount = 0;
    private JMenuItem fullScreenMenuItem;
    private JMenuItem closeMenuItem;

    private JRadioButtonMenuItem currentViewerSelected;

    private static class WaitCursor implements AutoCloseable {

        private final JComponent comp;

        WaitCursor(JComponent comp) {
            this.comp = comp;
            comp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }

        @Override
        public void close() {
            comp.setCursor(Cursor.getDefaultCursor());
        }
    }

    public FileViewerPresenter() {
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        viewerMenu = MenuToolkit.addMenu(Translator.get("file_viewer.viewer_menu"),
                menuMnemonicHelper, null);
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
    public void extendTitle(@NotNull String title) {
        getFrame().setTitle(super.getTitle() + title);
    }

    private void resetTitle() {
        getFrame().setTitle(super.getTitle());
    }

    @Override
    public @NotNull JFrame getWindowFrame() {
        return getFrame();
    }

    @Override
    public void goToFile(Function<Integer, Integer> advance,
                         @NotNull FileViewerService viewerService) throws IOException {
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
            canView = newFile != null && viewerService.canOpenFile(newFile) != CanOpen.NO;
        } while (newFile == null || !canView);

        setCurrentFile(newFile);
        try (var ignored = new WaitCursor(this)) {
            fileViewer.open(newFile);
        }
    }

    public void addViewerService(FileViewerService service) {
        services.add(service);
        JRadioButtonMenuItem viewerMenuItem = new JRadioButtonMenuItem(service.getName());
        final int serviceIndex = viewersCount;
        viewerMenuItem.addActionListener((e) -> {
            try {
                viewersButtonGroup.getSelection().setSelected(true);
                if (switchFileViewer(serviceIndex, false)) {
                    currentViewerSelected = viewerMenuItem;
                } else {
                    currentViewerSelected.setSelected(true);    // re-set previous selection
                }
            } catch (IOException ex) {
                Logger.getLogger(FileViewerPresenter.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        viewersButtonGroup.add(viewerMenuItem);
        viewerMenu.add(viewerMenuItem);
        if (viewersCount == 0) {
            currentViewerSelected = viewerMenuItem;
            viewerMenuItem.setSelected(true);
        }
        viewersCount++;
    }

    @Override
    public void requestFocus() {
        if (fileViewer != null) {
            fileViewer.requestFocus();
        }
    }

    @Override
    protected void show(AbstractFile file, boolean fromSearchWithContent) throws IOException {
        setCurrentFile(file);
        if (fileViewer == null) {
            getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            getFrame().addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (fileViewer != null) {
                        fileViewer.close();
                    }

                    close();
                }
            });

            MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
            viewerMenu.addSeparator();

            fullScreenMenuItem = MenuToolkit.addCheckBoxMenuItem(viewerMenu,
                    Translator.get("file_viewer.fullscreen"),
                    menuItemMnemonicHelper,
                    KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK),
                    (e) -> {
                        boolean fullScreen = getFrame().isFullScreen();
                        switchFullScreenMode(!fullScreen);
                    });

            String windowWidthValue = ViewerPreferences.WINDOW_WIDTH.getValue();
            if (!windowWidthValue.isEmpty()) {
                int windowX = Integer.parseInt(ViewerPreferences.WINDOW_POSITION_X.getValue());
                int windowY = Integer.parseInt(ViewerPreferences.WINDOW_POSITION_Y.getValue());
                int windowWidth = Integer.parseInt(windowWidthValue);
                int windowHeight = Integer.parseInt(ViewerPreferences.WINDOW_HEIGHT.getValue());
                getFrame().setBounds(windowX, windowY, windowWidth, windowHeight);
            } else {
                getFrame().setDefaultBounds();
            }

            String showFullScreenValue = ViewerPreferences.SHOW_FULLSCREEN.getValue();
            if (Boolean.TRUE.toString().equals(showFullScreenValue)) {
                switchFullScreenMode(true);
            }
            viewerMenu.add(fullScreenMenuItem);

            closeMenuItem = MenuToolkit.addMenuItem(viewerMenu,
                    Translator.get("file_viewer.close"),
                    menuItemMnemonicHelper,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    e -> getFrame().dispatchEvent(new WindowEvent(getFrame(), WindowEvent.WINDOW_CLOSING)));
            viewerMenu.add(closeMenuItem);

            switchFileViewer(0, fromSearchWithContent);
        }
    }

    private void close() {
        FileFrame frame = getFrame();
        Rectangle bounds = frame.getBounds();
        ViewerPreferences.WINDOW_POSITION_X.setValue(Integer.toString(bounds.x));
        ViewerPreferences.WINDOW_POSITION_Y.setValue(Integer.toString(bounds.y));
        ViewerPreferences.WINDOW_WIDTH.setValue(Integer.toString(bounds.width));
        ViewerPreferences.WINDOW_HEIGHT.setValue(Integer.toString(bounds.height));

        frame.dispose();
    }

    private boolean switchFileViewer(int index, boolean fromSearchWithContent) throws IOException {
        var service = services.get(index);
        var fileToView = getCurrentFile();

        switch (service.canOpenFile(fileToView)) {
            case YES_USER_CONSENT:
                if (userToConfirm(service)) {
                    break;
                }
            case NO:
                if (fileViewer == null) {
                    close();
                }
                return false;
        }

        if (fileViewer != null) {
            clearComponentToPresent();
            fileViewer.close();
            resetTitle();
        }

        menuBar.removeAll();
        menuBar.add(viewerMenu);

        fileViewer = service.createFileViewer(fromSearchWithContent);
        fileViewer.setPresenter(this);

        JComponent viewerComponent = fileViewer.getUI();
        try (var ignored = new WaitCursor(this)) {
            fileViewer.open(fileToView);
        }
        fileViewer.extendMenu(menuBar);
        menuBar.revalidate();
        menuBar.repaint();
        setComponentToPresent(viewerComponent);
        return true;
    }

    private void switchFullScreenMode(boolean showFullScreen) {
        getFrame().setFullScreen(showFullScreen);
        fullScreenMenuItem.setSelected(showFullScreen);
        ViewerPreferences.SHOW_FULLSCREEN.setValue(Boolean.toString(showFullScreen));
    }

    /**
     * Displays a dialog box to confirm the action.
     * @param service file viewer service
     * @return true if user confirmed, false otherwise
     */
    private boolean userToConfirm(FileViewerService service) {
        QuestionDialog dialog = new QuestionDialog((Frame) null, Translator.get("warning"),
                Translator.get(service.getConfirmationMsg()), getWindowFrame(),
                Arrays.asList(BaseOpenFileRegistrar.OpenFileRegistrarAction.OPEN_ANYWAY,
                        BaseOpenFileRegistrar.OpenFileRegistrarAction.CANCEL), 0);

        DialogAction ret = dialog.getActionValue();
        return ret != BaseOpenFileRegistrar.OpenFileRegistrarAction.CANCEL &&
                ret != DIALOG_DISPOSED_ACTION;
    }
}
