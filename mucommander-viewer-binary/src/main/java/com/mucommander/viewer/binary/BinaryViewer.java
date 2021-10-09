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
package com.mucommander.viewer.binary;

import java.awt.event.KeyEvent;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.exbin.bined.EditMode;
import org.exbin.bined.swing.basic.CodeArea;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.dialog.DialogOwner;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.ViewerPresenter;

/**
 * General viewer for binary files.
 *
 * @author Miroslav Hajda
 */
@ParametersAreNonnullByDefault
class BinaryViewer extends BinaryBase implements FileViewer {

    private ViewerPresenter presenter;
    private JMenuItem copyPopupMenuItem;

    public BinaryViewer() {
        super();

        initMenuBars();
        init();
    }

    private void initMenuBars() {
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
        JPopupMenu popupMenu = new JPopupMenu();
        int metaMask = getMetaMask();

        copyPopupMenuItem = MenuToolkit.createMenuItem(Translator.get("binary_viewer.copy"),
                menuItemMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, metaMask),
                e -> binaryComponent.getCodeArea().copy());
        popupMenu.add(copyPopupMenuItem);
        popupMenu.add(MenuToolkit.createMenuItem(Translator.get("binary_viewer.select_all"),
                menuItemMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, metaMask),
                e -> binaryComponent.getCodeArea().selectAll()));

        binaryComponent.getCodeArea().setComponentPopupMenu(popupMenu);
    }

    private void init() {
        CodeArea codeArea = binaryComponent.getCodeArea();
        codeArea.addSelectionChangedListener(this::updateClipboardActionsStatus);
        updateClipboardActionsStatus();
    }

    private void updateClipboardActionsStatus() {
        CodeArea codeArea = binaryComponent.getCodeArea();
        copyMenuItem.setEnabled(codeArea.hasSelection());
        copyPopupMenuItem.setEnabled(codeArea.hasSelection());
    }

    @Override
    public void extendMenu(JMenuBar menuBar) {
        menuBar.add(editMenu);
        menuBar.add(viewMenu);

        EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(presenter.getWindowFrame()),
                binaryComponent.getCodeArea().getCharset().name());
        encodingMenu.addEncodingListener((source, oldEncoding, newEncoding) -> changeEncoding(newEncoding));
        menuBar.add(encodingMenu);
    }

    private synchronized void loadFile(AbstractFile file) {
        CodeArea codeArea = binaryComponent.getCodeArea();
        codeArea.setContentData(new FileBinaryData(file));
        codeArea.setEditMode(EditMode.READ_ONLY);
        notifyOrigFileChanged();
        binaryComponent.updateCurrentMemoryMode();
    }

    @Override
    public void open(AbstractFile file) {
        loadFile(file);
    }

    @Override
    public void close() {
        Objects.requireNonNull(((FileBinaryData) binaryComponent.getCodeArea().getContentData())).close();
    }

    @Nonnull
    @Override
    public JComponent getUI() {
        return binaryComponent;
    }

    @Override
    public void setPresenter(ViewerPresenter presenter) {
        this.presenter = presenter;
        setWindowFrame(presenter.getWindowFrame());
    }
}
