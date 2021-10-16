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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.capability.EditModeCapable;
import org.exbin.bined.swing.basic.CodeArea;

import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.viewer.binary.ui.BinaryStatusPanel;
import com.mucommander.viewer.binary.ui.GoToBinaryPanel;

/**
 * Base class for both viewer and editor for binary files.
 *
 * @author Miroslav Hajda
 */
@ParametersAreNonnullByDefault
class BinaryBase {

    protected JMenu editMenu;
    protected JMenu viewMenu;
    protected JMenuItem copyMenuItem;
    protected int copyMenuItemPosition;
    protected JMenuItem selectAllMenuItem;
    protected JMenuItem goToMenuItem;
    protected JFrame windowFrame;

    private JMenu codeTypeMenu;
    private JMenu codeCharacterCaseMenu;
    private ButtonGroup codeTypeButtonGroup;
    private ButtonGroup codeCharacterCaseButtonGroup;
    private JRadioButtonMenuItem binaryCodeTypeRadioButtonMenuItem;
    private JRadioButtonMenuItem octalCodeTypeRadioButtonMenuItem;
    private JRadioButtonMenuItem decimalCodeTypeRadioButtonMenuItem;
    private JRadioButtonMenuItem hexadecimalCodeTypeRadioButtonMenuItem;
    private JRadioButtonMenuItem lowerCaseRadioButtonMenuItem;
    private JRadioButtonMenuItem upperCaseRadioButtonMenuItem;

    protected BinaryComponent binaryComponent = new BinaryComponent();

    public BinaryBase() {
        initMenuBars();
    }

    private void initMenuBars() {
        editMenu = new JMenu(Translator.get("binary_viewer.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
        int metaMask = getMetaMask();

        copyMenuItemPosition = editMenu.getItemCount();
        copyMenuItem = MenuToolkit.addMenuItem(editMenu,
                Translator.get("binary_viewer.copy"),
                menuItemMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, metaMask),
                e -> binaryComponent.getCodeArea().copy());

        selectAllMenuItem = MenuToolkit.addMenuItem(editMenu,
                Translator.get("binary_viewer.select_all"),
                menuItemMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_A, metaMask),
                e -> binaryComponent.getCodeArea().selectAll());

        editMenu.addSeparator();

        goToMenuItem = MenuToolkit.addMenuItem(editMenu,
                Translator.get("binary_viewer.go_to"),
                menuItemMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_G, metaMask),
                e -> goToPosition());

        viewMenu = new JMenu(Translator.get("binary_viewer.view"));

        codeTypeMenu = new JMenu(Translator.get("binary_viewer.code_type"));

        codeTypeButtonGroup = new ButtonGroup();
        binaryCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(binaryCodeTypeRadioButtonMenuItem);
        binaryCodeTypeRadioButtonMenuItem.setText(Translator.get("binary_viewer.code_type.binary"));
        binaryCodeTypeRadioButtonMenuItem
                .addActionListener(e -> binaryComponent.getCodeArea().setCodeType(CodeType.BINARY));
        codeTypeMenu.add(binaryCodeTypeRadioButtonMenuItem);

        octalCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(octalCodeTypeRadioButtonMenuItem);
        octalCodeTypeRadioButtonMenuItem.setText(Translator.get("binary_viewer.code_type.octal"));
        octalCodeTypeRadioButtonMenuItem
                .addActionListener(e -> binaryComponent.getCodeArea().setCodeType(CodeType.OCTAL));
        codeTypeMenu.add(octalCodeTypeRadioButtonMenuItem);

        decimalCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(decimalCodeTypeRadioButtonMenuItem);
        decimalCodeTypeRadioButtonMenuItem.setText(Translator.get("binary_viewer.code_type.decimal"));
        decimalCodeTypeRadioButtonMenuItem
                .addActionListener(e -> binaryComponent.getCodeArea().setCodeType(CodeType.DECIMAL));
        codeTypeMenu.add(decimalCodeTypeRadioButtonMenuItem);

        hexadecimalCodeTypeRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeTypeButtonGroup.add(hexadecimalCodeTypeRadioButtonMenuItem);
        hexadecimalCodeTypeRadioButtonMenuItem.setSelected(true);
        hexadecimalCodeTypeRadioButtonMenuItem.setText(Translator.get("binary_viewer.code_type.hexadecimal"));
        hexadecimalCodeTypeRadioButtonMenuItem
                .addActionListener(e -> binaryComponent.getCodeArea().setCodeType(CodeType.HEXADECIMAL));
        codeTypeMenu.add(hexadecimalCodeTypeRadioButtonMenuItem);
        viewMenu.add(codeTypeMenu);

        codeCharacterCaseMenu = new JMenu(Translator.get("binary_viewer.char_case"));
        codeCharacterCaseButtonGroup = new ButtonGroup();

        upperCaseRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeCharacterCaseButtonGroup.add(upperCaseRadioButtonMenuItem);
        upperCaseRadioButtonMenuItem.setSelected(true);
        upperCaseRadioButtonMenuItem.setText(Translator.get("binary_viewer.char_case.upper"));
        upperCaseRadioButtonMenuItem
                .addActionListener(e -> binaryComponent.getCodeArea().setCodeCharactersCase(CodeCharactersCase.UPPER));
        codeCharacterCaseMenu.add(upperCaseRadioButtonMenuItem);

        lowerCaseRadioButtonMenuItem = new JRadioButtonMenuItem();
        codeCharacterCaseButtonGroup.add(lowerCaseRadioButtonMenuItem);
        lowerCaseRadioButtonMenuItem.setText(Translator.get("binary_viewer.char_case.lower"));
        lowerCaseRadioButtonMenuItem
                .addActionListener(e -> binaryComponent.getCodeArea().setCodeCharactersCase(CodeCharactersCase.LOWER));
        codeCharacterCaseMenu.add(lowerCaseRadioButtonMenuItem);
        viewMenu.add(codeCharacterCaseMenu);
    }

    /**
     * Returns platform specific down mask filter.
     *
     * @return down mask for meta keys
     */
    @SuppressWarnings("deprecation")
    public static int getMetaMask() {
        try {
            switch (java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
            case java.awt.Event.META_MASK:
                return KeyEvent.META_DOWN_MASK;
            case java.awt.Event.SHIFT_MASK:
                return KeyEvent.SHIFT_DOWN_MASK;
            case java.awt.Event.ALT_MASK:
                return KeyEvent.ALT_DOWN_MASK;
            case java.awt.Event.CTRL_MASK:
            default:
                return KeyEvent.CTRL_DOWN_MASK;
            }
        } catch (java.awt.HeadlessException ex) {
            return KeyEvent.CTRL_DOWN_MASK;
        }
    }

    public void setWindowFrame(JFrame windowFrame) {
        this.windowFrame = windowFrame;
    }

    /**
     * Notifies component when original source file was changed (saved / loaded / refreshed).
     */
    public void notifyOrigFileChanged() {
        binaryComponent.notifyOrigFileChanged();
    }

    public void goToPosition() {
        CodeArea codeArea = binaryComponent.getCodeArea();
        FocusDialog dialog = new FocusDialog(windowFrame,
                Translator.get("binary_viewer.go_to.dialog_title"),
                windowFrame);
        Container contentPane = dialog.getContentPane();
        GoToBinaryPanel goToPanel = new GoToBinaryPanel();
        goToPanel.setCursorPosition(codeArea.getDataPosition());
        goToPanel.setMaxPosition(codeArea.getDataSize());
        contentPane.add(goToPanel, BorderLayout.CENTER);

        final JButton okButton = new JButton(Translator.get("binary_viewer.go_to.ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, dialog.getRootPane(), e -> {
            Object source = e.getSource();
            if (source == okButton) {
                goToPanel.acceptInput();

                long targetPosition = goToPanel.getTargetPosition();
                codeArea.setCaretPosition(targetPosition);
                codeArea.revealCursor();
            }
            dialog.dispose();

        }), BorderLayout.SOUTH);

        SwingUtilities.invokeLater(goToPanel::initFocus);

        dialog.showDialog();
    }

    public void changeEncoding(String encoding) {
        binaryComponent.changeEncoding(encoding);
    }

    @ParametersAreNonnullByDefault
    public class BinaryComponent extends JPanel implements ThemeListener {

        private final CodeArea codeArea = new CodeArea();
        private final BinaryStatusPanel statusPanel = new BinaryStatusPanel();
        private BinaryStatusApi binaryStatus = null;
        private long origFileSize;
        private Color backgroundColor;

        BinaryComponent() {
            init();
        }

        private void init() {
            setLayout(new BorderLayout());
            backgroundColor = ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR);
            super.setBackground(backgroundColor);
            ThemeManager.addCurrentThemeListener(this);

            registerBinaryStatus(statusPanel);
            add(codeArea, BorderLayout.CENTER);
            add(statusPanel, BorderLayout.SOUTH);
        }

        @Nonnull
        public CodeArea getCodeArea() {
            return codeArea;
        }

        @Nonnull
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

        public void registerBinaryStatus(BinaryStatusApi binaryStatusApi) {
            this.binaryStatus = binaryStatusApi;
            codeArea.addCaretMovedListener(
                    (CodeAreaCaretPosition caretPosition) -> binaryStatus.setCursorPosition(caretPosition));
            codeArea.addSelectionChangedListener(() -> binaryStatus.setSelectionRange(codeArea.getSelection()));
            codeArea.addDataChangedListener(
                    () -> binaryStatus.setCurrentDocumentSize(codeArea.getDataSize(), origFileSize));
            codeArea.addEditModeChangedListener(
                    (EditMode mode, EditOperation operation) -> binaryStatus.setEditMode(mode, operation));
            notifyOrigFileChanged();
            binaryStatus.setEditMode(codeArea.getEditMode(), codeArea.getActiveOperation());

            binaryStatus.setControlHandler(new BinaryStatusApi.StatusControlHandler() {
                @Override
                public void changeEditOperation(EditOperation editOperation) {
                    codeArea.setEditOperation(editOperation);
                }

                @Override
                public void changeCursorPosition() {
                    goToPosition();
                }

                @Override
                public void cycleEncodings() {
                }

                @Override
                public void encodingsPopupEncodingsMenu(MouseEvent mouseEvent) {
                }

                @Override
                public void changeMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
                }
            });

            updateCurrentMemoryMode();
        }

        public void notifyOrigFileChanged() {
            origFileSize = codeArea.getDataSize();
            binaryStatus.setCurrentDocumentSize(codeArea.getDataSize(), origFileSize);
        }

        public void changeEncoding(String encoding) {
            codeArea.setCharset(Charset.forName(encoding));
            if (binaryStatus != null) {
                statusPanel.setEncoding(encoding);
            }
        }

        public void updateCurrentMemoryMode() {
            BinaryStatusApi.MemoryMode memoryMode = BinaryStatusApi.MemoryMode.RAM_MEMORY;
            if (codeArea.getContentData() instanceof FileBinaryData) {
                memoryMode = BinaryStatusApi.MemoryMode.DIRECT_ACCESS;
            } else if (((EditModeCapable) codeArea).getEditMode() == EditMode.READ_ONLY) {
                memoryMode = BinaryStatusApi.MemoryMode.READ_ONLY;
            }

            if (binaryStatus != null) {
                binaryStatus.setMemoryMode(memoryMode);
            }
        }
    }
}
