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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.FileTypeUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.search.SearchProperty;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

/**
 * Text editor implementation used by {@link TextViewer} and {@link TextEditor}.
 *
 * @author Maxence Bernard, Mariusz Jakubowski, Nicolas Rinaudo, Arik Hadas
 */
class TextEditorImpl implements ThemeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextEditorImpl.class);

    private JFrame frame;

    private RSyntaxTextArea textArea;

    private RTextScrollPane scrollPane;

    /**
     * Indicates whether there is a line separator in the original file
     */
    private boolean lineSeparatorExists;

    /**
     * Whether the opened file is from File Search with Content (to initiate findNext() to move the caret).
     */
    private final boolean fromSearchWithContent;

    /**
     * A listener that is being called when syntax style has been changed.
     */
    private Consumer<String> syntaxChangeListener;

    public TextEditorImpl(boolean editable, boolean fromSearchWithContent) {
        this.fromSearchWithContent = fromSearchWithContent;

        // Initialize text area
        initTextArea(editable);

        // Listen to theme changes to update the text area if it is visible
        ThemeManager.addCurrentThemeListener(this);
    }

    private void initTextArea(boolean editable) {
        textArea = new RSyntaxTextArea() {
            @Override
            public Insets getInsets() {
                return new Insets(0, 3, 4, 3);
            }
        };
        scrollPane = new RTextScrollPane(textArea, TextViewerPreferences.LINE_NUMBERS.getValue());

        if (DesktopManager.canBrowse()) {
            int linkScanningMask = OsFamily.MAC_OS.isCurrent() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
            textArea.setLinkScanningMask(linkScanningMask);
            textArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.getModifiersEx() & linkScanningMask) == linkScanningMask)
                        fakeMouseMove(e);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    fakeMouseMove(e);
                }

                private void fakeMouseMove(KeyEvent e) {
                    Point mousePosition = textArea.getMousePosition();
                    if (mousePosition != null) {
                        MouseEvent mouseEvent = new MouseEvent(textArea, e.getID(), e.getWhen(), e.getModifiersEx(), mousePosition.x, mousePosition.y, 0, false);
                        Stream.of(textArea.getMouseMotionListeners()).forEach(listener -> listener.mouseMoved(mouseEvent));
                    }
                }
            });
            textArea.addHyperlinkListener((event) -> {
                URL url = event.getURL();
                if (url != null && event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            DesktopManager.browse(url);
                        } catch (IOException e) {
                            LOGGER.error("Error opening link in a browser", e);
                        }
                    });
                }
            });
            textArea.setHyperlinksEnabled(true);
        } else {
            textArea.setHyperlinksEnabled(false);
        }
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);

        textArea.setEditable(editable);
        // Use theme colors and font
        textArea.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setCaretColor(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        Color selFgColor = ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR);
        // Override Alpha to make selection visible across almost all L&Fs.
        int alpha = 170;
        textArea.setSelectedTextColor(new Color(selFgColor.getRed(), selFgColor.getGreen(), selFgColor.getBlue(), alpha));
        Color selBgColor = ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR);
        textArea.setSelectionColor(new Color(selBgColor.getRed(), selBgColor.getGreen(), selBgColor.getBlue(), alpha));
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
                    scrollPane.dispatchEvent(e);
                }
            }
        });
        // Setting TEXT_CURSOR for text area regardless if it is editable or readonly - for both it makes sense.
        // TODO still not ideal - initially most often it opens with DEFAULT_CURSOR, moving to line numbers panel and back to editor it shows TEXT_CURSOR properly
        textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    void find() {
        boolean editable = textArea.isEditable();
        FindDialog findDialog = new FindDialog(frame, editable);

        if (findDialog.wasValidated()) {
            String searchString = findDialog.getSearchString();

            if (!StringUtils.isNullOrEmpty(searchString)) {
                SearchProperty.SEARCH_TEXT.setValue(searchString);
                SearchProperty.TEXT_CASESENSITIVE.setValue(findDialog.getCaseSensitivity());
                SearchProperty.TEXT_MATCH_REGEX.setValue(findDialog.getRegexMatch());
                SearchProperty.TEXT_WHOLE_WORDS.setValue(findDialog.isWholeWords());
                SearchProperty.TEXT_SEARCH_FORWARD.setValue(findDialog.isForwardDirection());

                doSearchAndReplace(findDialog.isForwardDirection(), editable ? findDialog.getReplaceString() : null);
            }
        }
    }

    void findNext() {
        if (StringUtils.isNullOrEmpty(SearchProperty.SEARCH_TEXT.getValue())) {
            find();
        } else {
            doSearch(true);
        }
    }

    void findPrevious() {
        doSearch(false);
    }

    void convertTabsToSpaces() { textArea.convertTabsToSpaces(); }

    void convertSpacesToTabs() { textArea.convertSpacesToTabs(); }

    private void doSearch(boolean forward) {
        doSearchAndReplace(forward, null);
    }

    /**
     * Does Search or Replace All if replaceWith is not null.
     *
     * @param forward search forward if 'true' or backward otherwise, ignored when replaceWith is not null
     * @param replaceWith replace string, if null then only search is being done
     */
    private void doSearchAndReplace(boolean forward, String replaceWith) {
        String searchString = SearchProperty.SEARCH_TEXT.getValue();
        if (StringUtils.isNullOrEmpty(searchString)) {
            return;
        }

        SearchContext context = new SearchContext();
        context.setMatchCase(SearchProperty.TEXT_CASESENSITIVE.getBoolValue());
        context.setRegularExpression(SearchProperty.TEXT_MATCH_REGEX.getBoolValue());
        context.setSearchFor(searchString);
        context.setSearchSelectionOnly(false); // TODO currently not supported by library (https://github.com/bobbylight/RSyntaxTextArea/blob/9097e51fc5e289d761dca139a3a08459bf12614a/RSyntaxTextArea/src/main/java/org/fife/ui/rtextarea/SearchContext.java#L382)
        context.setWholeWord(SearchProperty.TEXT_WHOLE_WORDS.getBoolValue());

        var found = false;
        try {
            if (replaceWith != null) {
                context.setReplaceWith(replaceWith);
                found = SearchEngine.replaceAll(textArea, context).wasFound();
            } else {
                context.setMarkAll(true);
                context.setSearchWrap(true);
                context.setSearchForward(forward);
                found = SearchEngine.find(textArea, context).wasFound();
            }
        } catch(Exception e) { // precisely PatternSyntaxException | IndexOutOfBoundsException
            LOGGER.error("Error while running search/replace", e);
        }

        if (!found) {
            // Beep when no match has been found.
            // The beep method is called from a separate thread because this method seems to lock until the beep has
            // been played entirely. If the 'Find next' shortcut is left pressed, a series of beeps will be played when
            // the end of the file is reached, and we don't want those beeps to played one after the other as to:
            // 1/ not lock the event thread
            // 2/ have those beeps to end rather sooner than later
            new Thread(Toolkit.getDefaultToolkit()::beep).start();
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

    void clearWhitespaceLines(boolean aBool) {
        textArea.setClearWhitespaceLinesEnabled(aBool);
    }

    void closeCurlyBraces(boolean aBool) {
        textArea.setCloseCurlyBraces(aBool);
    }

    void closeMarkupTags(boolean aBool) {
        textArea.setCloseMarkupTags(aBool);
    }

    void codeFolding(boolean aBool) {
        scrollPane.setFoldIndicatorEnabled(aBool);
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

    public void undo() {
        textArea.undoLastAction();
    }

    public boolean canUndo() {
        return textArea.canUndo();
    }

    public void redo() {
        textArea.redoLastAction();
    }

    public boolean canRedo() {
        return textArea.canRedo();
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

    public void showLineNumbers(boolean show) {
        scrollPane.setLineNumbersEnabled(show);
    }

    public boolean getLineNumbersEnabled() {
        return scrollPane.getLineNumbersEnabled();
    }

    /**
     * Returns the map of supported syntax styles, where key is mime-type and value is
     * human-readable name of the style.
     * @return the map, won't be null.
     */
    Map<String, String> getSyntaxStyles() {
        Map<String, String> syntaxMap = new LinkedHashMap<>();
        // TODO remove reflection when/if https://github.com/bobbylight/RSyntaxTextArea/issues/479 implemented
        Field[] fields = SyntaxConstants.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (name.startsWith("SYNTAX_STYLE_")) {
                String[] parts = name.substring("SYNTAX_STYLE_".length()).split("_");
                String prettyName = Stream.of(parts).map(String::toLowerCase).collect(Collectors.joining(" "));
                try {
                    syntaxMap.put((String) field.get(null), prettyName);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Ooops while trying to get syntax style mime-type", e);
                }
            }
        }
        return syntaxMap;
    }

    void setSyntaxStyle(String syntaxStyleMimeType) {
        textArea.setSyntaxEditingStyle(syntaxStyleMimeType);
    }

    public void setSyntaxStyle(AbstractFile file) {
        String mimeType = FileTypeUtil.get().guessContentType(
                new File(file.getCanonicalPath()), true);

        textArea.setSyntaxEditingStyle(mimeType);
        if (syntaxChangeListener != null) {
            syntaxChangeListener.accept(mimeType);
        }
    }

    public void setSyntaxStyleChangeListener(Consumer<String> syntaxChangeListener) {
        this.syntaxChangeListener = syntaxChangeListener;
    }

    JScrollPane getScrollPane() {
        return scrollPane;
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

    public void setInitialFocusAndCursor() {
        textArea.requestFocusInWindow();

        // Check if opened file is from Search with content if so, then try to show the found string,
        // otherwise set caret on the first line.
        if (fromSearchWithContent) {
            findNext();
        } else {
            textArea.setCaretPosition(0);
        }
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
