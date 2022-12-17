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

import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.job.impl.SearchJob;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

import org.fife.ui.rsyntaxtextarea.FileTypeUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Text editor implementation used by {@link TextViewer} and {@link TextEditor}.
 *
 * @author Maxence Bernard, Mariusz Jakubowski, Nicolas Rinaudo, Arik Hadas
 */
class TextEditorImpl implements ThemeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextEditorImpl.class);

    private JFrame frame;

    private RSyntaxTextArea textArea;

    /**
     * Indicates whether there is a line separator in the original file
     */
    private boolean lineSeparatorExists;

    ////////////////////
    // Initialization //
    ////////////////////

    public TextEditorImpl(boolean isEditable) {
        // Initialize text area
        initTextArea(isEditable);

        // Listen to theme changes to update the text area if it is visible
        ThemeManager.addCurrentThemeListener(this);
    }

    private void initTextArea(boolean isEditable) {
        textArea = new RSyntaxTextArea() {
            @Override
            public Insets getInsets() {
                return new Insets(0, 3, 4, 3);
            }
        };

        // TODO add this pref when https://github.com/bobbylight/RSyntaxTextArea/issues/469 is resolved
        //textArea.setClearWhitespaceLinesEnabled(true);

        if (DesktopManager.canBrowse()) {
            textArea.addHyperlinkListener((event) -> {
                try {
                    DesktopManager.browse(event.getURL());
                } catch (IOException e) {
                    LOGGER.error("Error opening link in a browser", e);
                }
            });
            textArea.setHyperlinksEnabled(true);
        } else {
            textArea.setHyperlinksEnabled(false);
        }
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);

        textArea.setEditable(isEditable);
        // Use theme colors and font
        textArea.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setCaretColor(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        textArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR));
        textArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR));
        // TODO by default I guess, at least on macOS in SolarizedDark theme, the editor font is not-monospaced!
        // tabs may not be shown properly in length (dots are much smaller than letters).
        textArea.setFont(ThemeManager.getCurrentFont(Theme.EDITOR_FONT));

        textArea.setWrapStyleWord(true);

        textArea.addMouseWheelListener(new MouseWheelListener() {

            /**
             * Mouse events bubble up until finding a component with a relative listener.
             * That's why in case we get an event that needs to initiate its default behavior,
             * we just bubble it up to the parent component of the JTextArea.
             */
            public void mouseWheelMoved(MouseWheelEvent e) {
                boolean isCtrlPressed = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
                if (isCtrlPressed) {
                    Font currentFont = textArea.getFont();
                    int currentFontSize = currentFont.getSize();
                    boolean rotationUp = e.getWheelRotation() < 0;
                    if (rotationUp || currentFontSize > 1) {
                        Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFontSize + (rotationUp ? 1 : -1));
                        textArea.setFont(newFont);
                    }
                } else {
                    textArea.getParent().dispatchEvent(e);
                }
            }
        });
    }

    /////////////////
    // Search code //
    /////////////////

    void find() {
        FindDialog findDialog = new FindDialog(frame);

        if (findDialog.wasValidated()) {
            String searchString = findDialog.getSearchString().toLowerCase();

            if (!StringUtils.isNullOrEmpty(searchString)) {
                SearchJob.lastSearchString = searchString;
                doSearch(0, true);
            }
        }
    }

    void findNext() {
        if (StringUtils.isNullOrEmpty(SearchJob.lastSearchString)) {
            find();
        } else {
            doSearch(textArea.getSelectionEnd(), true);
        }
    }

    void findPrevious() {
        doSearch(textArea.getSelectionStart() - 1, false);
    }

    void convertTabsToSpaces() { textArea.convertTabsToSpaces(); }

    void convertSpacesToTabs() { textArea.convertSpacesToTabs(); }

    private String getTextLC() {
        return textArea.getText().toLowerCase();
    }

    private void doSearch(int startPos, boolean forward) {
        String searchString = SearchJob.lastSearchString;
        if (StringUtils.isNullOrEmpty(searchString)) {
            return;
        }

        textArea.requestFocus();

        int pos;
        if (forward) {
            pos = getTextLC().indexOf(searchString, startPos);
        } else {
            pos = getTextLC().lastIndexOf(searchString, startPos);
        }
        if (pos >= 0) {
            textArea.select(pos, pos + searchString.length());
        } else {
            // Beep when no match has been found.
            // The beep method is called from a separate thread because this method seems to lock until the beep has
            // been played entirely. If the 'Find next' shortcut is left pressed, a series of beeps will be played when
            // the end of the file is reached, and we don't want those beeps to played one after the other as to:
            // 1/ not lock the event thread
            // 2/ have those beeps to end rather sooner than later
            new Thread(() -> Toolkit.getDefaultToolkit().beep()).start();
        }
    }

    ////////////////////////////
    // Package-access methods //
    ////////////////////////////

    void wrap(boolean isWrap) {
        textArea.setLineWrap(isWrap);
        textArea.repaint();
    }

    void animateBracketMatching(boolean aBool) {
        textArea.setAnimateBracketMatching(aBool);
    }

    void antiAliasing(boolean aBool) {
        textArea.setAntiAliasingEnabled(aBool);
    }

    void autoIndent(boolean aBool) {
        textArea.setAutoIndentEnabled(aBool);
    }

    void bracketMatching(boolean aBool) {
        textArea.setBracketMatchingEnabled(aBool);
    }

    void closeCurlyBraces(boolean aBool) {
        textArea.setCloseCurlyBraces(aBool);
    }

    void closeMarkupTags(boolean aBool) {
        textArea.setCloseMarkupTags(aBool);
    }

    void codeFolding(boolean aBool) {
        textArea.setCodeFoldingEnabled(aBool);
    }

    void dragEnabled(boolean aBool) {
        textArea.setDragEnabled(aBool);
    }

    void eolMarkersVisible(boolean aBool) {
        textArea.setEOLMarkersVisible(aBool);
    }

    void fadeCurrentLineHighlight(boolean aBool) {
        textArea.setFadeCurrentLineHighlight(aBool);
    }

    void highlightCurrentLine(boolean aBool) {
        textArea.setHighlightCurrentLine(aBool);
    }

    void markOccurrences(boolean aBool) {
        textArea.setMarkOccurrences(aBool);
    }

    void paintTabLines(boolean aBool) {
        textArea.setPaintTabLines(aBool);
    }

    void roundedSelectionEdges(boolean aBool) {
        textArea.setRoundedSelectionEdges(aBool);
    }

    void showMatchedBracketPopup(boolean aBool) {
        textArea.setShowMatchedBracketPopup(aBool);
    }

    void tabsEmulated(boolean aBool) {
        textArea.setTabsEmulated(aBool);
    }

    void whitespaceVisible(boolean aBool) {
        textArea.setWhitespaceVisible(aBool);
    }

    int getTabSize() {
        return textArea.getTabSize();
    }

    void setTabSize(int size) {
        textArea.setTabSize(size);
    }

    void copy() {
        textArea.copy();
    }

    void cut() {
        textArea.cut();
    }

    void paste() {
        textArea.paste();
    }

    void selectAll() {
        textArea.selectAll();
    }

    List<String> getSyntaxStyles() {
        List<String> syntaxList = new ArrayList<>();
        // TODO remove reflection when/if https://github.com/bobbylight/RSyntaxTextArea/issues/479 implemented
        Field[] fields = SyntaxConstants.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (name.startsWith("SYNTAX_STYLE_")) {
                String[] parts = name.substring("SYNTAX_STYLE_".length()).split("_");
                StringBuilder prettyName = new StringBuilder(parts[0]);
                if (parts.length > 1) {
                    for (int i = 1; i < parts.length; i++) {
                        prettyName.append(" ");
                        prettyName.append(parts[i].toLowerCase());
                    }
                }
                syntaxList.add(prettyName.toString());
            }
        }
        return syntaxList;
    }

    void setSyntaxStyle(String syntaxStyleHuman) {
        // TODO use enum when/if https://github.com/bobbylight/RSyntaxTextArea/issues/479 implemented
        String normalizedNAme = "SYNTAX_STYLE_" + syntaxStyleHuman.replace(" ", "_").toUpperCase();
        Field[] fields = SyntaxConstants.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (normalizedNAme.equals(name)) {
                try {
                    textArea.setSyntaxEditingStyle((String)field.get(null));
                    break;
                } catch (IllegalAccessException e) {
                    LOGGER.error("Ooops while trying to set syntax style", e);
                }
            }
        }
    }

    JTextArea getTextArea() {
        return textArea;
    }

    void addDocumentListener(DocumentListener documentListener) {
        textArea.getDocument().addDocumentListener(documentListener);
    }

    void read(Reader reader) throws IOException {
        // Feed the file's contents to text area
        textArea.read(reader, null);

        // If there are more than one lines, there is a line separator
        lineSeparatorExists = textArea.getLineCount() > 1;

        // Move cursor to the top
        textArea.setCaretPosition(0);
    }

    void write(Writer writer) throws IOException {
        Document document = textArea.getDocument();

        // According to the documentation in DefaultEditorKit, the line separator is set to be as the system property
        // if no other line separator exists in the file, but in practice it is not, so this is a workaround for it
        if (!lineSeparatorExists) {
            document.putProperty(DefaultEditorKit.EndOfLineStringProperty, System.getProperty("line.separator"));
        }
        try {
            textArea.getUI().getEditorKit(textArea).write(new BufferedWriter(writer), document, 0, document.getLength());
        } catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void setSyntaxHighlighting(AbstractFile file) {
        String mimeType = FileTypeUtil.get().guessContentType(
                new File(file.getCanonicalPath()), true);
        textArea.setSyntaxEditingStyle(mimeType);
    }

    public void setFocusAndCursorOnFirstLine() {
        textArea.requestFocusInWindow();
        textArea.setCaretPosition(0);
    }

    //////////////////////////////////
    // ThemeListener implementation //
    //////////////////////////////////

    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        switch (event.getColorId()) {
            case Theme.EDITOR_FOREGROUND_COLOR:
                textArea.setForeground(event.getColor());
                break;

            case Theme.EDITOR_BACKGROUND_COLOR:
                textArea.setBackground(event.getColor());
                break;

            case Theme.EDITOR_SELECTED_FOREGROUND_COLOR:
                textArea.setSelectedTextColor(event.getColor());
                break;

            case Theme.EDITOR_SELECTED_BACKGROUND_COLOR:
                textArea.setSelectionColor(event.getColor());
                break;
        }
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if (event.getFontId() == Theme.EDITOR_FONT) {
            textArea.setFont(event.getFont());
        }
    }
}
