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

import java.util.function.Consumer;
import java.util.function.Function;


/**
 * TextEditor preferences gluing visual aspects (like i18n, source of menu generation),
 * configuration to be stored and actual setter for TextArea UI.
 *
 * @author Piotr Skowronek
 */
enum TextViewerPreferences {

    // Whether to wrap long lines.
    LINE_WRAP("line_wrap", "text_viewer.line_wrap", false, t -> t::wrap),
    // Whether to show line numbers.
    LINE_NUMBERS("line_numbers", "text_viewer.line_numbers", true),
    // Whether to animate bracket matching
    ANIMATE_BRACKET_MATCHING("animate_bracket_matching", "text_viewer.animate_bracket_matching", true, t -> t::animateBracketMatching),
    // Whether to use anti-aliasing
    ANTI_ALIASING("anti_aliasing", "text_viewer.anti_aliasing", true, t -> t::antiAliasing),
    // Whether to use auto-indent
    AUTO_INDENT("auto_indent", "text_viewer.auto_indent", false, t -> t::autoIndent, EditorViewerMode.EDITOR),
    // Whether to match brackets
    BRACKET_MATCHING("bracket_matching", "text_viewer.bracket_matching", true, t -> t::bracketMatching),
    // Whether to clear lines with whitespaces
    CLEAR_WHITE_SPACE("clear_whitespace_lines", "text_viewer.clear_whitespace_lines", true, t -> t::clearWhitespaceLines, EditorViewerMode.EDITOR),
    // Whether to close curly braces
    CLOSE_CURLY_BRACES("close_curly_braces", "text_viewer.close_curly_braces", true, t -> t::closeCurlyBraces, EditorViewerMode.EDITOR),
    // Whether to markup tags (only honored for markup languages, such as HTML, XML and PHP)
    CLOSE_MARKUP_TAGS("close_markup_tags", "text_viewer.close_markup_tags", true, t -> t::closeMarkupTags, EditorViewerMode.EDITOR),
    // Whether to use code folding
    CODE_FOLDING("code_folding", "text_viewer.code_folding", true, t -> t::codeFolding),
    // Whether text drag-n-drop is enabled
    DRAG_ENABLED("drag_n_drop", "text_viewer.drag_n_drop", true, t -> t::dragEnabled),
    // Whether to show EOL markers
    EOL_MARKERS("eol_markers", "text_viewer.eol_markers", false, t -> t::eolMarkersVisible),
    // Whether to slightly fade current line highlight
    FADE_CURRENT_LINE_HL("fade_current_line", "text_viewer.fade_current_line", true, t -> t::fadeCurrentLineHighlight),
    // Whether to highlight a current line
    HIGHLIGHT_CURRENT_LINE("highlight_current_line", "text_viewer.highlight_current_line", true, t -> t::highlightCurrentLine),
    // Whether to mark occurrences
    MARK_OCCURRENCES("mark_occurrences", "text_viewer.mark_occurrences", true, t -> t::markOccurrences),
    // Whether to paint tab lines
    PAINT_TAB_LINES("tab_lines", "text_viewer.tab_lines", true, t -> t::paintTabLines),
    // Whether to paint rounded edges of selection
    ROUNDED_SELECTION("rounded_selection", "text_viewer.rounded_selection", true, t -> t::roundedSelectionEdges),
    // Whether to show popup with matched end-bracket (when off-screen)
    SHOW_MATCHED_BRACKET("show_matched_bracket_popup", "text_viewer.show_matched_bracket_popup", true, t -> t::showMatchedBracketPopup),
    // Whether to emulated tabs with spaces
    TABS_EMULATED("tabs_emulated", "text_viewer.tabs_emulated", true, t -> t::tabsEmulated, EditorViewerMode.EDITOR),
    // Whether to show whitespace chars
    WHITESPACE_VISIBLE("whitespace_visible", "text_viewer.whitespace_visible", false, t -> t::whitespaceVisible),

    ; // end of prefs (syntax sugar aka developer vanity marker)

    enum EditorViewerMode {
        BOTH,
        EDITOR
    };

    String prefKey;
    String i18nKey;
    boolean value;
    EditorViewerMode mode;

    Function<TextEditorImpl, Consumer<Boolean>> textEditorSetter;

    TextViewerPreferences(String prefKey, String i18nKey, boolean defaultValue) {
        this(prefKey, i18nKey, defaultValue, null, EditorViewerMode.BOTH);
    }

    TextViewerPreferences(String prefKey, String i18nKey, boolean defaultValue,
            Function<TextEditorImpl, Consumer<Boolean>> textEditorSetter) {
        this(prefKey, i18nKey, defaultValue, textEditorSetter, EditorViewerMode.BOTH);
    }

    TextViewerPreferences(String prefKey, String i18nKey, boolean defaultValue,
            Function<TextEditorImpl, Consumer<Boolean>> textEditorSetter, EditorViewerMode mode) {
        this.prefKey = prefKey;
        this.i18nKey = i18nKey;
        this.textEditorSetter = textEditorSetter;
        this.value = defaultValue;
        this.mode = mode;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void setValue(TextEditorImpl editorImpl, boolean value) {
        if (textEditorSetter != null) {
            textEditorSetter.apply(editorImpl).accept(value);
        }
        setValue(value);
    }

    public boolean isTextEditorPref() {
        return textEditorSetter != null;
    }

    public boolean isEditorOnly() { return mode == EditorViewerMode.EDITOR; };

}
