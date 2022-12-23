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

import com.mucommander.snapshot.MuSnapshot;

import java.util.function.BiConsumer;

import static com.mucommander.snapshot.MuSnapshot.FILE_PRESENTER_SECTION;

/**
 * TextEditor preferences gluing visual aspects (like i18n, source of menu generation),
 * configuration to be stored and actual setter for TextArea UI.
 *
 * @author Piotr Skowronek
 */
enum TextViewerPreferences {

    // Whether to wrap long lines.
    LINE_WRAP("line_wrap", "text_viewer.line_wrap", false,
            (textEditorImpl, aBoolean) -> textEditorImpl.wrap(aBoolean)),
    // Whether to show line numbers.
    LINE_NUMBERS("line_numbers", "text_viewer.line_numbers", true),
    // Whether to animate bracket matching
    ANIMATE_BRACKET_MATCHING("animate_bracket_matching", "text_viewer.animate_bracket_matching", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.animateBracketMatching(aBoolean)),
    // Whether to use anti-aliasing
    ANTI_ALIASING("anti_aliasing", "text_viewer.anti_aliasing", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.antiAliasing(aBoolean)),
    // Whether to use auto-indent
    AUTO_INDENT("auto_indent", "text_viewer.auto_indent", false,
            (textEditorImpl, aBoolean) -> textEditorImpl.autoIndent(aBoolean), EditorViewerMode.EDITOR),
    // Whether to match brackets
    BRACKET_MATCHING("bracket_matching", "text_viewer.bracket_matching", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.bracketMatching(aBoolean)),
    // Whether to clear lines with whitespaces
    CLEAR_WHITE_SPACE("clear_whitespace_lines", "text_viewer.clear_whitespace_lines", true,
            (textEditorImpl, aBoolean) ->
                    textEditorImpl.clearWhitespaceLines(aBoolean), EditorViewerMode.EDITOR),
    // Whether to close curly braces
    CLOSE_CURLY_BRACES("close_curly_braces", "text_viewer.close_curly_braces", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.closeCurlyBraces(aBoolean), EditorViewerMode.EDITOR),
    // Whether to markup tags (only honored for markup languages, such as HTML, XML and PHP)
    CLOSE_MARKUP_TAGS("close_markup_tags", "text_viewer.close_markup_tags", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.closeMarkupTags(aBoolean), EditorViewerMode.EDITOR),
    // Whether to use code folding
    CODE_FOLDING("code_folding", "text_viewer.code_folding", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.codeFolding(aBoolean)),
    // Whether text drag-n-drop is enabled
    DRAG_ENABLED("drag_n_drop", "text_viewer.drag_n_drop", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.dragEnabled(aBoolean)),
    // Whether to show EOL markers
    EOL_MARKERS("eol_markers", "text_viewer.eol_markers", false,
            (textEditorImpl, aBoolean) -> textEditorImpl.eolMarkersVisible(aBoolean)),
    // Whether to slightly fade current line highlight
    FADE_CURRENT_LINE_HL("fade_current_line", "text_viewer.fade_current_line", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.fadeCurrentLineHighlight(aBoolean)),
    // Whether to highlight a current line
    HIGHLIGHT_CURRENT_LINE("highlight_current_line", "text_viewer.highlight_current_line", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.highlightCurrentLine(aBoolean)),
    // Whether to mark occurrences
    MARK_OCCURRENCES("mark_occurrences", "text_viewer.mark_occurrences", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.markOccurrences(aBoolean)),
    // Whether to paint tab lines
    PAINT_TAB_LINES("tab_lines", "text_viewer.tab_lines", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.paintTabLines(aBoolean)),
    // Whether to paint rounded edges of selection
    ROUNDED_SELECTION("rounded_selection", "text_viewer.rounded_selection", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.roundedSelectionEdges(aBoolean)),
    // Whether to show popup with matched end-bracket (when off-screen)
    SHOW_MATCHED_BRACKET("show_matched_bracket_popup", "text_viewer.show_matched_bracket_popup", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.showMatchedBracketPopup(aBoolean)),
    // Whether to emulated tabs with spaces
    TABS_EMULATED("tabs_emulated", "text_viewer.tabs_emulated", true,
            (textEditorImpl, aBoolean) -> textEditorImpl.tabsEmulated(aBoolean), EditorViewerMode.EDITOR),
    // Whether to show whitespace chars
    WHITESPACE_VISIBLE("whitespace_visible", "text_viewer.whitespace_visible", false,
            (textEditorImpl, aBoolean) -> textEditorImpl.whitespaceVisible(aBoolean)),

    ; // end of prefs (syntax sugar aka developer vanity marker)

    /**
     * Section describing information specific to text file presenter.
     */
    public static final String TEXT_FILE_PRESENTER_SECTION = FILE_PRESENTER_SECTION + "." + "text";

    enum EditorViewerMode {
        BOTH,
        EDITOR
    };

    String prefKey;
    String i18nKey;
    boolean currentValue;
    EditorViewerMode mode;

    BiConsumer<TextEditorImpl, Boolean> textEditorSetter;

    TextViewerPreferences(String prefKey, String i18nKey, boolean defaultValue) {
        this(prefKey, i18nKey, defaultValue, null, EditorViewerMode.BOTH);
    }

    TextViewerPreferences(String prefKey, String i18nKey, boolean defaultValue,
                          BiConsumer<TextEditorImpl, Boolean> textEditorSetter) {
        this(prefKey, i18nKey, defaultValue, textEditorSetter, EditorViewerMode.BOTH);
    }

    TextViewerPreferences(String prefKey, String i18nKey, boolean defaultValue,
                          BiConsumer<TextEditorImpl, Boolean> textEditorSetter, EditorViewerMode mode) {
        this.prefKey = prefKey != null ? TEXT_FILE_PRESENTER_SECTION + "." + prefKey : null;
        this.i18nKey = i18nKey;
        this.textEditorSetter = textEditorSetter;
        this.currentValue = prefKey != null ?
                MuSnapshot.getSnapshot().getVariable(this.prefKey, defaultValue) : false;
        this.mode = mode;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public boolean getValue() {
        return currentValue;
    }

    public void setValue(boolean value) {
        currentValue = value;
    }

    public void setValue(TextEditorImpl editorImpl, boolean value) {
        if (textEditorSetter != null) {
            textEditorSetter.accept(editorImpl, value);
        }
        setValue(value);
    }

    public boolean isTextEditorPref() {
        return textEditorSetter != null;
    }

    public boolean isEditorOnly() { return mode == EditorViewerMode.EDITOR; };

}
