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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentListener;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.io.EncodingDetector;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.bom.BOMInputStream;
import com.mucommander.commons.util.ui.dialog.DialogOwner;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.ViewerPresenter;
import java.awt.event.ActionListener;
import javax.swing.JScrollPane;

/**
 * A simple text viewer. Most of the implementation is located in {@link TextEditorImpl}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class TextViewer implements FileViewer, EncodingListener, ActionListener {

    private JScrollPane ui = new JScrollPane();
    private ViewerPresenter presenter;
    private TextEditorImpl textEditorImpl;
    private AbstractFile currentFile;
    private TextLineNumbersPanel lineNumbersPanel;

    /** Menu items */
    // Menus //
    private JMenu editMenu;
    private JMenu viewMenu;
    // Items //
    private JMenuItem copyItem;
    private JMenuItem selectAllItem;
    private JMenuItem findItem;
    private JMenuItem findNextItem;
    private JMenuItem findPreviousItem;
    private JMenuItem toggleLineNumbersItem;

    private String encoding;

    TextViewer() {
        this(new TextEditorImpl(false));
    }

    TextViewer(TextEditorImpl textEditorImpl) {
        this.textEditorImpl = textEditorImpl;
        init();
    }
    
    private void init() {
        attachView();

        initLineNumbersPanel();
        showLineNumbers(TextViewerPreferences.LINE_NUMBERS.getValue());

        for (TextViewerPreferences pref : TextViewerPreferences.values()) {
            if (pref.isTextEditorPref()) {
                pref.setValue(textEditorImpl, pref.getValue());
            }
        }
        initMenuBarItems();
    }
    
    protected void attachView() {
        ui.getViewport().setView(textEditorImpl.getTextArea());
    }

    }

    static void setLineNumbers(boolean lineNumbers) {
        TextViewerPreferences.LINE_NUMBERS.setValue(lineNumbers);
    }

    void startEditing(AbstractFile file, DocumentListener documentListener) throws IOException {
        // Auto-detect encoding

        // Get a RandomAccessInputStream on the file if possible, if not get a simple InputStream
        InputStream in = null;

        try {
            if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
                try {
                    in = file.getRandomAccessInputStream();
                } catch (IOException e) {
                    // In that case we simply get an InputStream
                }
            }

            if (in == null) {
                in = file.getInputStream();
            }

            String encoding = EncodingDetector.detectEncoding(in);

            if (in instanceof RandomAccessInputStream) {
                // Seek to the beginning of the file and reuse the stream
                ((RandomAccessInputStream) in).seek(0);
            } else {
                // TODO: it would be more efficient to use some sort of PushBackInputStream, though we can't use PushBackInputStream because we don't want to keep pushing back for the whole InputStream lifetime

                // Close the InputStream and open a new one
                // Note: we could use mark/reset if the InputStream supports it, but it is almost never implemented by
                // InputStream subclasses and a broken by design anyway.
                in.close();
                in = file.getInputStream();
            }

            // Load the file into the text area
            loadDocument(file, in, encoding, documentListener);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Nothing to do here.
                }
            }
        }
    }

    void loadDocument(AbstractFile file, InputStream in, final String encoding, DocumentListener documentListener) throws IOException {
        // If the encoding is UTF-something, wrap the stream in a BOMInputStream to filter out the byte-order mark
        // (see ticket #245)
        if (encoding != null && encoding.toLowerCase().startsWith("utf")) {
            in = new BOMInputStream(in);
        }

        // If the given encoding is invalid (null or not supported), default to "UTF-8" 
        this.encoding = encoding == null || !Charset.isSupported(encoding) ? "UTF-8" : encoding;

        textEditorImpl.read(new BufferedReader(new InputStreamReader(in, this.encoding)));
        textEditorImpl.setSyntaxHighlighting(file);
        // TODO it doesn't work - should it be invoked somewhere else?
        textEditorImpl.setFocusAndCursorOnFirstLine();

        // Listen to document changes
        if (documentListener!=null) {
            textEditorImpl.addDocumentListener(documentListener);
        }
    }

    @Override
    public JComponent getUI() {
        return ui;
    }

    @Override
    public void setPresenter(ViewerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void extendMenu(JMenuBar menuBar) {
        // Encoding menu
        EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(presenter.getWindowFrame()), encoding);
        encodingMenu.addEncodingListener(this);

        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(encodingMenu);
    }

    String getEncoding() {
        return encoding;
    }

    protected void showLineNumbers(boolean show) {
        ui.setRowHeaderView(show ? lineNumbersPanel : null);
        setLineNumbers(show);
    }

    protected void initLineNumbersPanel() {
        lineNumbersPanel = new TextLineNumbersPanel(textEditorImpl.getTextArea());
    }

    protected void initMenuBarItems() {
        // Edit menu
        editMenu = new JMenu(Translator.get("text_viewer.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.copy"), menuItemMnemonicHelper, null, this);

        selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.select_all"), menuItemMnemonicHelper, null, this);
        editMenu.addSeparator();

        findItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.find"), menuItemMnemonicHelper, DesktopManager.getActionShortcuts().getDefaultKeystroke(ActionType.Find), this);
        findNextItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), this);
        findPreviousItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), this);

        // View menu
        viewMenu = new JMenu(Translator.get("text_viewer.view"));

        JMenuItem item;

        for (TextViewerPreferences pref : TextViewerPreferences.values()) {
            if (pref.isTextEditorPref()) {
                item = MenuToolkit.addCheckBoxMenuItem(viewMenu,
                        Translator.get(pref.getI18nKey()), menuItemMnemonicHelper,
                        null,  e -> pref.setValue(textEditorImpl, ((JMenuItem)e.getSource()).isSelected()));
                item.setSelected(pref.getValue()); // the last known (or the most current) value
            }
        }

        toggleLineNumbersItem = MenuToolkit.addCheckBoxMenuItem(viewMenu,
                Translator.get(TextViewerPreferences.LINE_NUMBERS.getI18nKey()),
                menuItemMnemonicHelper, null, this);
        toggleLineNumbersItem.setSelected(ui.getRowHeader().getView() != null);
    }

    ///////////////////////////////
    // FileViewer implementation //
    ///////////////////////////////

    @Override
    public void open(AbstractFile file) throws IOException {
        currentFile = file;
        startEditing(file, null);
        lineNumbersPanel.setPreferredWidth();
    }
    
    @Override
    public void close() {
    }

    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        // TODO declare inline action handlers instead of those ifs
        if(source == copyItem)
            textEditorImpl.copy();
        else if(source == selectAllItem)
            textEditorImpl.selectAll();
        else if(source == findItem)
            textEditorImpl.find();
        else if(source == findNextItem)
            textEditorImpl.findNext();
        else if(source == findPreviousItem)
            textEditorImpl.findPrevious();
        else if(source == toggleLineNumbersItem)
            showLineNumbers(toggleLineNumbersItem.isSelected());
    }

    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    @Override
    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
        try {
            // Reload the file using the new encoding
            // Note: loadDocument closes the InputStream
            loadDocument(currentFile, currentFile.getInputStream(), newEncoding, null);
        } catch (IOException ex) {
            InformationDialog.showErrorDialog(presenter.getWindowFrame(), Translator.get("read_error"), Translator.get("file_editor.cannot_read_file", currentFile.getName()));
        }
    }
}
