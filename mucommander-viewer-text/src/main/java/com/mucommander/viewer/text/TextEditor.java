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

package com.mucommander.viewer.text;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.io.bom.BOMWriter;
import com.mucommander.commons.util.ui.dialog.DialogOwner;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.viewer.CloseCancelledException;


import java.awt.event.ActionListener;

import javax.swing.JScrollPane;

import static com.mucommander.viewer.text.TextViewerPreferences.TEXT_FILE_PRESENTER_SECTION;


/**
 * A simple text editor.
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Arik Hadas
 */
class TextEditor extends BasicFileEditor implements DocumentListener, EncodingListener, ActionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextEditor.class);

    private final JScrollPane ui = new JScrollPane();
    private TextLineNumbersPanel lineNumbersPanel;

    /**
     * Menu bar
     */
    // Menus //
    private JMenu editMenu;
    private JMenu viewMenu;
    // Items //
    private JMenuItem copyItem;
    private JMenuItem cutItem;
    private JMenuItem pasteItem;
    private JMenuItem selectAllItem;
    private JMenuItem findItem;
    private JMenuItem findNextItem;
    private JMenuItem findPreviousItem;
    private JMenuItem toggleLineNumbersItem;

    private final TextEditorImpl textEditorImpl;
    private final TextViewer textViewerDelegate;

    public TextEditor() {
        textViewerDelegate = new TextViewer(textEditorImpl = new TextEditorImpl(true)) {
            @Override
            protected void attachView() {
                ui.getViewport().setView(textEditorImpl.getTextArea());
            }

            @Override
            protected void showLineNumbers(boolean show) {
                ui.setRowHeaderView(show ? lineNumbersPanel : null);
                setLineNumbers(show);
            }

            @Override
            protected void initLineNumbersPanel() {
                lineNumbersPanel = new TextLineNumbersPanel(textEditorImpl.getTextArea());
            }

            @Override
            protected void initMenuBarItems() { // TODO code dup with TextViewer, fix it
                // Edit menu
                ActionListener listener = TextEditor.this;
                editMenu = new JMenu(Translator.get("text_editor.edit"));
                MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

                copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.copy"), menuItemMnemonicHelper, null, listener);

                cutItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.cut"), menuItemMnemonicHelper, null, listener);
                pasteItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.paste"), menuItemMnemonicHelper, null, listener);

                selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.select_all"), menuItemMnemonicHelper, null, listener);
                editMenu.addSeparator();

                findItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find"), menuItemMnemonicHelper, DesktopManager.getActionShortcuts().getDefaultKeystroke(ActionType.Find), listener);
                findNextItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), listener);
                findPreviousItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), listener);

                editMenu.addSeparator();

                MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.spaces_to_tabs"), menuItemMnemonicHelper, null, e -> textEditorImpl.convertSpacesToTabs());
                MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.tabs_to_spaces"), menuItemMnemonicHelper, null, e -> textEditorImpl.convertTabsToSpaces());

                viewMenu = new JMenu(Translator.get("text_editor.view"));

                JMenuItem item;
                for (TextViewerPreferences pref : TextViewerPreferences.values()) {
                    if (pref.isTextEditorPref()) {
                        item = MenuToolkit.addCheckBoxMenuItem(viewMenu,
                                Translator.get(pref.getI18nKey()), menuItemMnemonicHelper,
                                null,  e -> pref.setValue(textEditorImpl, ((JMenuItem)e.getSource()).isSelected()));
                        item.setSelected(pref.getValue()); // the last known (or current) value
                    }
                }
                viewMenu.addSeparator();
                toggleLineNumbersItem = MenuToolkit.addCheckBoxMenuItem(viewMenu,
                        Translator.get(TextViewerPreferences.LINE_NUMBERS.getI18nKey()),
                        menuItemMnemonicHelper, null, listener);
                toggleLineNumbersItem.setSelected(ui.getRowHeader().getView() != null);

                viewMenu.addSeparator();
                int tabSize = textEditorImpl.getTabSize();
                JMenu tabSizeMenu = new JMenu(Translator.get("text_editor.tab_size"));

                ButtonGroup group = new ButtonGroup();
                for (int i : new int[]{2, 4, 8}) {
                    JRadioButtonMenuItem radio = new JRadioButtonMenuItem(Integer.toString(i), tabSize == i);
                    radio.addActionListener(
                                e -> {
                                    textEditorImpl.setTabSize(i);
                                    MuSnapshot.getSnapshot().setVariable(
                                        TEXT_FILE_PRESENTER_SECTION + ".tab_size", i);
                                }
                    );
                    group.add(radio);
                    tabSizeMenu.add(radio);
                }
                viewMenu.add(tabSizeMenu);
            }
        };
    }

    void loadDocument(InputStream in, String encoding, DocumentListener documentListener) throws IOException {
        textViewerDelegate.loadDocument(getCurrentFile(), in, encoding, documentListener);
    }

    private void write(OutputStream out) throws IOException {
        textEditorImpl.write(new BOMWriter(out, textViewerDelegate.getEncoding()));
    }

    ///////////////////////////////
    // FileEditor implementation //
    ///////////////////////////////

    @Override
    protected void saveAs(AbstractFile destFile) throws IOException {
        OutputStream out = null;

        try {
            out = destFile.getOutputStream();
            write(out);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }

        // We get here only if the destination file was updated successfully
        // so we can set that no further save is needed at this stage 
        setSaveNeeded(false);

        // Change the parent folder's date to now, so that changes are picked up by folder auto-refresh (see ticket #258)
        if (destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
            try {
                destFile.getParent().changeDate(System.currentTimeMillis());
            } catch (IOException e) {
                LOGGER.debug("failed to change the date of " + destFile, e);
                // Fail silently
            }
        }
    }

    @Override
    public void open(AbstractFile file) throws IOException {
        setCurrentFile(file);
        textViewerDelegate.startEditing(file, this);
        lineNumbersPanel.setPreferredWidth();
    }

    @Override
    public void close() throws CloseCancelledException {
        if (!askSave()) {
            throw new CloseCancelledException();
        }
    }

    @Override
    public void extendMenu(JMenuBar menuBar) {
        super.extendMenu(menuBar);

        // Encoding menu
        EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(presenter.getWindowFrame()), textViewerDelegate.getEncoding());
        encodingMenu.addEncodingListener(this);

        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(encodingMenu);
    }

    @Override
    public JComponent getUI() {
        return ui;
    }

    /////////////////////////////////////
    // DocumentListener implementation //
    /////////////////////////////////////

    @Override
    public void changedUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }

    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == copyItem)
            textEditorImpl.copy();
        else if (source == cutItem)
            textEditorImpl.cut();
        else if (source == pasteItem)
            textEditorImpl.paste();
        else if (source == selectAllItem)
            textEditorImpl.selectAll();
        else if (source == findItem)
            textEditorImpl.find();
        else if (source == findNextItem)
            textEditorImpl.findNext();
        else if (source == findPreviousItem)
            textEditorImpl.findPrevious();
        else if (source == toggleLineNumbersItem)
            textViewerDelegate.showLineNumbers(toggleLineNumbersItem.isSelected());
    }

    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    @Override
    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
        if (!askSave())
            return;         // Abort if the file could not be saved

        try {
            // Reload the file using the new encoding
            // Note: loadDocument closes the InputStream
            loadDocument(getCurrentFile().getInputStream(), newEncoding, null);
        } catch (IOException ex) {
            InformationDialog.showErrorDialog(presenter.getWindowFrame(), Translator.get("read_error"), Translator.get("file_editor.cannot_read_file", getCurrentFile().getName()));
        }
    }
}
