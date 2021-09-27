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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.auxiliary.paged_data.ByteArrayEditableData;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.ViewerPresenter;

/**
 * General viewer for binary files.
 *
 * @author Miroslav Hajda
 */
class BinaryViewer implements FileViewer {

    private ViewerPresenter presenter;

    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu codeTypeMenu;
    private JMenu codeCharacterCaseMenu;
    private javax.swing.ButtonGroup codeTypeButtonGroup;
    private javax.swing.ButtonGroup codeCharacterCaseButtonGroup;
    private JMenuItem copyItem;
    private JMenuItem selectAllItem;
    private javax.swing.JRadioButtonMenuItem binaryCodeTypeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem octalCodeTypeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem decimalCodeTypeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem hexadecimalCodeTypeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem lowerCaseRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem upperCaseRadioButtonMenuItem;

    private BinaryViewerImpl binaryViewerImpl;

    public BinaryViewer() {
        binaryViewerImpl = new BinaryViewerImpl();

        initMenuBars();
    }

    private void initMenuBars() {
        editMenu = new JMenu(Translator.get("text_viewer.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.copy"), menuItemMnemonicHelper, null,
                e -> binaryViewerImpl.copy()
        );

        selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.select_all"), menuItemMnemonicHelper, null,
                e -> binaryViewerImpl.selectAll()
        );

        // View menu
        viewMenu = new JMenu(Translator.get("text_viewer.view"));

        codeTypeMenu = new JMenu("Code Type");

        codeTypeButtonGroup = new ButtonGroup();
        binaryCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(binaryCodeTypeRadioButtonMenuItem);
        binaryCodeTypeRadioButtonMenuItem.setText("Binary");
        binaryCodeTypeRadioButtonMenuItem.addActionListener(e -> binaryViewerImpl.setCodeType(CodeType.BINARY));
        codeTypeMenu.add(binaryCodeTypeRadioButtonMenuItem);

        octalCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(octalCodeTypeRadioButtonMenuItem);
        octalCodeTypeRadioButtonMenuItem.setText("Octal");
        octalCodeTypeRadioButtonMenuItem.addActionListener(e -> binaryViewerImpl.setCodeType(CodeType.OCTAL));
        codeTypeMenu.add(octalCodeTypeRadioButtonMenuItem);

        decimalCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(decimalCodeTypeRadioButtonMenuItem);
        decimalCodeTypeRadioButtonMenuItem.setText("Decimal");
        decimalCodeTypeRadioButtonMenuItem.addActionListener(e -> binaryViewerImpl.setCodeType(CodeType.DECIMAL));
        codeTypeMenu.add(decimalCodeTypeRadioButtonMenuItem);

        hexadecimalCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(hexadecimalCodeTypeRadioButtonMenuItem);
        hexadecimalCodeTypeRadioButtonMenuItem.setSelected(true);
        hexadecimalCodeTypeRadioButtonMenuItem.setText("Hexadecimal");
        hexadecimalCodeTypeRadioButtonMenuItem.addActionListener(e -> binaryViewerImpl.setCodeType(CodeType.HEXADECIMAL));
        codeTypeMenu.add(hexadecimalCodeTypeRadioButtonMenuItem);
        viewMenu.add(codeTypeMenu);

        codeCharacterCaseMenu = new JMenu("Code Character Case");
        codeCharacterCaseButtonGroup = new ButtonGroup();

        upperCaseRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeCharacterCaseButtonGroup.add(upperCaseRadioButtonMenuItem);
        upperCaseRadioButtonMenuItem.setSelected(true);
        upperCaseRadioButtonMenuItem.setText("Upper Case");
        upperCaseRadioButtonMenuItem.addActionListener(e -> binaryViewerImpl.setCodeCharactersCase(CodeCharactersCase.UPPER));
        codeCharacterCaseMenu.add(upperCaseRadioButtonMenuItem);

        lowerCaseRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeCharacterCaseButtonGroup.add(lowerCaseRadioButtonMenuItem);
        lowerCaseRadioButtonMenuItem.setText("Lower Case");
        lowerCaseRadioButtonMenuItem.addActionListener(e -> binaryViewerImpl.setCodeCharactersCase(CodeCharactersCase.LOWER));
        codeCharacterCaseMenu.add(lowerCaseRadioButtonMenuItem);
        viewMenu.add(codeCharacterCaseMenu);
    }

    @Override
    public void extendMenu(JMenuBar menuBar) {
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
    }

    private synchronized void loadFile(AbstractFile file) throws IOException {
        // TODO provide method for long operations in presenter instead
        presenter.getWindowFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));

        ByteArrayEditableData data = new ByteArrayEditableData();
        try {
            data.loadFromStream(file.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(BinaryViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        binaryViewerImpl.setContentData(data);
        binaryViewerImpl.setEditMode(EditMode.READ_ONLY);

        presenter.getWindowFrame().setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void open(AbstractFile file) throws IOException {
        loadFile(file);
    }

    @Override
    public void close() {

    }

    /**
     * Returns UI for viewer.
     *
     * @return UI component instance
     */
    @Override
    public JComponent getUI() {
        return binaryViewerImpl;
    }

    @Override
    public void setPresenter(ViewerPresenter presenter) {
        this.presenter = presenter;
    }

    private class BinaryViewerImpl extends CodeArea implements ThemeListener {

        private Color backgroundColor;

        BinaryViewerImpl() {
            backgroundColor = ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR);
            super.setBackground(backgroundColor);
            ThemeManager.addCurrentThemeListener(this);
        }

        @Override
        public synchronized Dimension getPreferredSize() {
            return new Dimension(750, 500);
        }

        /**
         * Receives theme color changes notifications.
         */
        @Override
        public void colorChanged(ColorChangedEvent event) {
            if (event.getColorId() == Theme.EDITOR_BACKGROUND_COLOR) {
                backgroundColor = event.getColor();
                super.setBackground(backgroundColor);
                repaint();
            }
        }

        /**
         * Not used, implemented as a no-op.
         */
        @Override
        public void fontChanged(FontChangedEvent event) {
        }
    }
}
