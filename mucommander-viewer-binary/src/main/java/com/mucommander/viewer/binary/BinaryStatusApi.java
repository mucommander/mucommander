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

import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.SelectionRange;

/**
 * Binary editor status interface.
 */
@ParametersAreNonnullByDefault
public interface BinaryStatusApi {

    /**
     * Reports cursor position.
     *
     * @param cursorPosition
     *            cursor position
     */
    void setCursorPosition(CodeAreaCaretPosition cursorPosition);

    /**
     * Sets current selection.
     *
     * @param selectionRange
     *            current selection
     */
    void setSelectionRange(SelectionRange selectionRange);

    /**
     * Reports currently active edit mode.
     *
     * @param mode
     *            edit mode
     * @param operation
     *            edit operation
     */
    void setEditMode(EditMode mode, EditOperation operation);

    /**
     * Sets control handler for status operations.
     *
     * @param statusControlHandler
     *            status control handler
     */
    void setControlHandler(StatusControlHandler statusControlHandler);

    /**
     * Sets current document size.
     *
     * @param documentSize
     *            document size
     * @param initialDocumentSize
     *            document size when file was opened
     */
    void setCurrentDocumentSize(long documentSize, long initialDocumentSize);

    /**
     * Sets current memory mode.
     *
     * @param memoryMode
     *            memory mode
     */
    void setMemoryMode(MemoryMode memoryMode);

    @ParametersAreNonnullByDefault
    interface StatusControlHandler {

        /**
         * Requests change of edit operation from given operation.
         *
         * @param operation
         *            edit operation
         */
        void changeEditOperation(EditOperation operation);

        /**
         * Requests change of cursor position using go-to dialog.
         */
        void changeCursorPosition();

        /**
         * Switches to next encoding in defined list.
         */
        void cycleEncodings();

        /**
         * Handles encodings popup menu.
         *
         * @param mouseEvent
         *            mouse event
         */
        void encodingsPopupEncodingsMenu(MouseEvent mouseEvent);

        /**
         * Requests change of memory mode.
         *
         * @param memoryMode
         *            memory mode
         */
        void changeMemoryMode(MemoryMode memoryMode);
    }

    @ParametersAreNonnullByDefault
    public enum MemoryMode {

        READ_ONLY("R", "read_only"),
        RAM_MEMORY("M", "ram"),
        DIRECT_ACCESS("D", "direct"),
        DELTA_MODE("\u0394", "delta");

        private final String displayChar;
        private final String value;

        private MemoryMode(String displayChar, String preferencesValue) {
            this.displayChar = displayChar;
            this.value = preferencesValue;
        }

        @Nonnull
        public String getDisplayChar() {
            return displayChar;
        }

        @Nonnull
        public String getPreferencesValue() {
            return value;
        }

        @Nonnull
        public static Optional<MemoryMode> findByPreferencesValue(String matchValue) {
            return Arrays.stream(values())
                    .filter(memoryMode -> memoryMode.getPreferencesValue().equals(matchValue))
                    .findFirst();
        }
    }
}
