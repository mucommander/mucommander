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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    @Metadata(false)
    LINE_WRAP("line_wrap", "text_viewer.line_wrap", t -> t::wrap),
    // Whether to show line numbers.
    @Metadata
    LINE_NUMBERS("line_numbers", "text_viewer.line_numbers"),
    // Whether to animate bracket matching
    @Metadata
    ANIMATE_BRACKET_MATCHING("animate_bracket_matching", "text_viewer.animate_bracket_matching", t -> t::animateBracketMatching),
    // Whether to use anti-aliasing
    @Metadata
    ANTI_ALIASING("anti_aliasing", "text_viewer.anti_aliasing", t -> t::antiAliasing),
    // Whether to use auto-indent
    @Metadata(value = false, mode = EditorViewerMode.EDITOR)
    AUTO_INDENT("auto_indent", "text_viewer.auto_indent", t -> t::autoIndent),
    // Whether to match brackets
    @Metadata
    BRACKET_MATCHING("bracket_matching", "text_viewer.bracket_matching", t -> t::bracketMatching),
    // Whether to clear lines with whitespaces
    @Metadata(mode = EditorViewerMode.EDITOR)
    CLEAR_WHITE_SPACE("clear_whitespace_lines", "text_viewer.clear_whitespace_lines", t -> t::clearWhitespaceLines),
    // Whether to close curly braces
    @Metadata(mode = EditorViewerMode.EDITOR)
    CLOSE_CURLY_BRACES("close_curly_braces", "text_viewer.close_curly_braces", t -> t::closeCurlyBraces),
    // Whether to markup tags (only honored for markup languages, such as HTML, XML and PHP)
    @Metadata(mode = EditorViewerMode.EDITOR)
    CLOSE_MARKUP_TAGS("close_markup_tags", "text_viewer.close_markup_tags", t -> t::closeMarkupTags),
    // Whether to use code folding
    @Metadata
    CODE_FOLDING("code_folding", "text_viewer.code_folding", t -> t::codeFolding),
    // Whether text drag-n-drop is enabled
    @Metadata
    DRAG_ENABLED("drag_n_drop", "text_viewer.drag_n_drop", t -> t::dragEnabled),
    // Whether to show EOL markers
    @Metadata(false)
    EOL_MARKERS("eol_markers", "text_viewer.eol_markers", t -> t::eolMarkersVisible),
    // Whether to slightly fade current line highlight
    @Metadata
    FADE_CURRENT_LINE_HL("fade_current_line", "text_viewer.fade_current_line", t -> t::fadeCurrentLineHighlight),
    // Whether to highlight a current line
    @Metadata
    HIGHLIGHT_CURRENT_LINE("highlight_current_line", "text_viewer.highlight_current_line", t -> t::highlightCurrentLine),
    // Whether to mark occurrences
    @Metadata
    MARK_OCCURRENCES("mark_occurrences", "text_viewer.mark_occurrences", t -> t::markOccurrences),
    // Whether to paint tab lines
    @Metadata
    PAINT_TAB_LINES("tab_lines", "text_viewer.tab_lines", t -> t::paintTabLines),
    // Whether to paint rounded edges of selection
    @Metadata
    ROUNDED_SELECTION("rounded_selection", "text_viewer.rounded_selection", t -> t::roundedSelectionEdges),
    // Whether to show popup with matched end-bracket (when off-screen)
    @Metadata
    SHOW_MATCHED_BRACKET("show_matched_bracket_popup", "text_viewer.show_matched_bracket_popup", t -> t::showMatchedBracketPopup),
    // Whether to emulated tabs with spaces
    @Metadata(mode = EditorViewerMode.EDITOR)
    TABS_EMULATED("tabs_emulated", "text_viewer.tabs_emulated", t -> t::tabsEmulated),
    // Whether to show whitespace chars
    @Metadata(false)
    WHITESPACE_VISIBLE("whitespace_visible", "text_viewer.whitespace_visible", t -> t::whitespaceVisible),

    ; // end of prefs (syntax sugar aka developer vanity marker)

    enum EditorViewerMode {
        BOTH,
        EDITOR
    };

    String prefKey;
    String i18nKey;
    Function<TextEditorImpl, Consumer<Boolean>> textEditorSetter;

    boolean value = true;
    EditorViewerMode mode = EditorViewerMode.BOTH;

    @Retention(RetentionPolicy.RUNTIME)
    private @interface Metadata {
        // Default value
        boolean value() default true;
        EditorViewerMode mode() default EditorViewerMode.BOTH;
    }

    TextViewerPreferences(String prefKey, String i18nKey) {
        this(prefKey, i18nKey, null);
    }

    TextViewerPreferences(String prefKey, String i18nKey, Function<TextEditorImpl, Consumer<Boolean>> textEditorSetter) {
        this.prefKey = prefKey;
        this.i18nKey = i18nKey;
        this.textEditorSetter = textEditorSetter;

        Metadata metadata;
        try {
            metadata = getDeclaringClass().getDeclaredField(this.name()).getDeclaredAnnotation(Metadata.class);
            mode = metadata.mode();
            value = metadata.value();
        } catch (NoSuchFieldException | SecurityException e) {
            // ignore, the properties are initialized with the annotation's default values
        }
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
