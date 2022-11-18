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

import com.mucommander.commons.conf.Configuration;
import static com.mucommander.snapshot.MuSnapshot.FILE_PRESENTER_SECTION;

import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.snapshot.MuSnapshotable;

import java.util.function.BiConsumer;

/**
 * Snapshot preferences for text editor & viewer.
 *
 * @author Miroslav Hajda, Piotr Skowronek
 */
public final class TextViewerSnapshot implements MuSnapshotable {

    /**
     * Section describing information specific to text file presenter.
     */
    private static final String TEXT_FILE_PRESENTER_SECTION = FILE_PRESENTER_SECTION + "." + "text";

    enum Preferences {
        // Whether to wrap long lines.
        LINE_WRAP("line_wrap", "text_viewer.line_wrap", false,
                (textEditorImpl, aBoolean) -> textEditorImpl.wrap(aBoolean)),
        // Whether to show line numbers.
        LINE_NUMBERS("line_numbers", "text_viewer.line_numbers", true),
        // Last known file presenter full screen mode.
        FULL_SCREEN(null, "text_viewer.full_screen", false),
        // Whether to animate bracket matching
        ANIMATE_BRACKET_MATCHING("animate_bracket_matching", "text_viewer.animate_bracket_matching", true,
                (textEditorImpl, aBoolean) -> textEditorImpl.animateBracketMatching(aBoolean)),
        // Whether to use anti-aliasing
        ANTI_ALIASING("anti_aliasing", "text_viewer.anti_aliasing", true,
                (textEditorImpl, aBoolean) -> textEditorImpl.antiAliasing(aBoolean)),
        // Whether to use auto-indent
        AUTO_INDENT("auto_indent", "text_viewer.auto_indent", false,
                (textEditorImpl, aBoolean) -> textEditorImpl.autoIndent(aBoolean)),
        // Whether to show EOL markers
        EOL_MARKERS("eol_markers", "text_viewer.eol_markers", false,
                (textEditorImpl, aBoolean) -> textEditorImpl.eolMarkersVisible(aBoolean)),
        // Whether to use code folding
        CODE_FOLDING("code_folding", "text_viewer.code_folding", true,
                (textEditorImpl, aBoolean) -> textEditorImpl.codeFolding(aBoolean)),
        // Whether to mark occurrences
        MARK_OCCURRENCES("mark_occurrences", "text_viewer.mark_occurrences", true,
                (textEditorImpl, aBoolean) -> textEditorImpl.markOccurrences(aBoolean)),
        // Whether to paint tab lines
        PAINT_TAB_LINES("paint_tab_lines", "text_viewer.paint_tab_lines", true,
                (textEditorImpl, aBoolean) -> textEditorImpl.paintTabLines(aBoolean)),

        ; // end of prefs (syntax sugar aka developer vanity marker)

        String prefKey;
        String i18nKey;
        boolean currentValue;

        BiConsumer<TextEditorImpl, Boolean> textEditorSetter;

        Preferences(String prefKey, String i18nKey, boolean defaultValue) {
            this(prefKey, i18nKey, defaultValue, null);
        }
        Preferences(String prefKey, String i18nKey, boolean defaultValue,
                    BiConsumer<TextEditorImpl, Boolean> textEditorSetter) {
            this.prefKey = prefKey;
            this.i18nKey = i18nKey;
            this.textEditorSetter = textEditorSetter;
            this.currentValue = prefKey != null ?
                    MuSnapshot.getSnapshot().getVariable(prefKey, defaultValue) : false;
        }

        public String getPrefKey() {
            return TEXT_FILE_PRESENTER_SECTION + "." + prefKey;
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

        boolean isTextEditorPref() {
            return textEditorSetter != null;
        }
    }

    @Override
    public void read(Configuration configuration) {
    }

    @Override
    public void write(Configuration configuration) {
        for (Preferences pref : Preferences.values()) {
            if (pref.getPrefKey() != null) {
                configuration.setVariable(pref.getPrefKey(), pref.getValue());
            }
        }
    }
}
