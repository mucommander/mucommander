/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * Text field (field type Tx) is a box or space for text fill-in data typically
 * entered from a keyboard. The text may be restricted to a single line or may
 * be permitted to span multiple lines, depending on the setting of the Multi line
 * flag in the field dictionary’s Ff entry. Table 228 shows the flags pertaining
 * to this type of field. A text field shall have a field type of Tx. A conforming
 * PDF file, and a conforming processor shall obey the usage guidelines as
 * defined by the big flags below.
 * <p/>
 * The field’s text shall be held in a text string (or, beginning with PDF 1.5,
 * a stream) in the V (value) entry of the field dictionary. The contents of this
 * text string or stream shall be used to construct an appearance stream for
 * displaying the field, as described under 12.7.3.3, “Variable Text.” The text
 * shall be presented in a single style (font, size, colour, and so forth), as
 * specified by the DA (default appearance) string.
 * <p/>
 * If the FileSelect flag (PDF 1.4) is set, the field shall function as a file-select
 * control. In this case, the field’s text represents the pathname of a file whose
 * contents shall be submitted as the field’s value:
 * <ul>
 * <li>For fields submitted in HTML Form format, the submission shall use
 * the MIME content type multipart/form-data, as described in Internet RFC 2045,
 * Multipurpose Internet Mail Extensions (MIME), Part One: Format of Internet
 * Message Bodies (see the Bibliography).</li>
 * <li>For Forms Data Format (FDF) submission, the value of the V entry in
 * the FDF field dictionary (see FDF Fields in 12.7.7.3, “FDF Catalog”) shall
 * be a file specification (7.11, “File Specifications”) identifying the
 * selected file.</li>
 * <li>XML format is not supported for file-select controls; therefore, no
 * value shall be submitted in this case.</li>
 * </ul>
 * Besides the usual entries common to all fields (see Table 220) and to fields
 * containing variable text (see Table 222), the field dictionary for a text field may
 * contain the additional entry shown in Table 229.
 *
 * @since 5.1
 */
public class TextFieldDictionary extends VariableTextFieldDictionary {

    /**
     * The maximum length of the fields text, in characters.
     */
    public static final Name MAX_LENGTH_KEY = new Name("MaxLen");

    /**
     * (Radio buttons only) If set, exactly one radio button shall be selected at
     * all times; selecting the currently selected button has no effect. If clear,
     * clicking the selected button deselects it, leaving no button selected.
     */
    public static final int MULTILINE_BIT_FLAG = 0x1000;

    /**
     * should not be echoed visibly to the screen. Characters typed from the
     * keyboard shall instead be echoed in some unreadable form, such as asterisks
     * or bullet characters.
     * NOTE: To protect password confidentiality, readers should never store the
     * value of the text field in the PDF file if this flag is set.
     */
    public static final int PASSWORD_BIT_FLAG = 0x2000;

    /**
     * (PDF 1.4) If set, the text entered in the field represents the pathname of
     * a file whose contents shall be submitted as the value of the field.
     */
    public static final int FILE_SELECT_BIT_FLAG = 0x100000;

    /**
     * (PDF 1.4) If set, text entered in the field shall not be spell-checked.
     */
    public static final int TEXT_DO_NOT_SPELL_CHECK_BIT_FLAG = 0x100000;

    /**
     * (PDF 1.4) If set, the field shall not scroll (horizontally for single-line
     * fields, vertically for multiple-line fields) to accommodate more text than
     * fits within its annotation rectangle. Once the field is full, no further
     * text shall be accepted for interactive form filling; for non-interactive
     * form filling, the filler should take care not to add more character than
     * will visibly fit in the defined area.
     */
    public static final int DO_NOT_SCROLL_BIT_FLAG = 0x800000;

    /**
     * (PDF 1.5) May be set only if the MaxLen entry is present in the text field
     * dictionary (see Table 229) and if the Multiline, Password, and FileSelect
     * flags are clear. If set, the field shall be automatically divided into as
     * many equally spaced positions, or combs, as the value of MaxLen, and the
     * text is laid out into those combs.
     */
    public static final int COMB_BIT_FLAG = 0x1000000;

    public enum TextFieldType {
        TEXT_INPUT, TEXT_AREA, TEXT_PASSWORD, FILE_SELECT
    }

    /**
     * (PDF 1.5) If set, the value of this field shall be a rich text string
     * (see 12.7.3.4, “Rich Text Strings”). If the field has a value, the RV entry
     * of the field dictionary (Table 222) shall specify the rich text string.
     */
    public static final int RICH_TEXT_BIT_FLAG = 0x2000000;
    protected TextFieldType textFieldType;
    protected int maxLength = 0;

    public TextFieldDictionary(Library library, HashMap entries) {
        super(library, entries);
        // parse out max length.
        Object value = library.getObject(entries, MAX_LENGTH_KEY);
        if (value != null && value instanceof Number) {
            maxLength = ((Number) value).intValue();
        }
        // determine the text type
        int flags = getFlags();
        if ((flags & MULTILINE_BIT_FLAG) ==
                MULTILINE_BIT_FLAG) {
            textFieldType = TextFieldType.TEXT_AREA;
        } else if ((flags & PASSWORD_BIT_FLAG) ==
                PASSWORD_BIT_FLAG) {
            textFieldType = TextFieldType.TEXT_PASSWORD;
        } else if ((flags & FILE_SELECT_BIT_FLAG) ==
                FILE_SELECT_BIT_FLAG) {
            textFieldType = TextFieldType.FILE_SELECT;
        } else {
            textFieldType = TextFieldType.TEXT_INPUT;
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    public TextFieldType getTextFieldType() {
        return textFieldType;
    }

    /**
     * Field may container multiple lines of text.
     *
     * @return true if multiline text,  otherwise false.
     */
    public boolean isMultiLine() {
        return (getFlags() & MULTILINE_BIT_FLAG) == MULTILINE_BIT_FLAG;
    }

    /**
     * Filed is a file select component.
     *
     * @return true if file select,  otherwise false.
     */
    public boolean isFileSelect() {
        return (getFlags() & FILE_SELECT_BIT_FLAG) == FILE_SELECT_BIT_FLAG;
    }

    /**
     * If set, the field shall not scroll (horizontally for single-line fields, vertically for multiple-line fields)
     * to accommodate more text than fits within its annotation rectangle. Once the field is full, no further text
     * shall be accepted for interactive form filling; for non-interactive form filling, the filler should take care
     * not to add more character than will visibly fit in the defined area.
     *
     * @return true if do not scroll is enabled,  otherwise false.
     */
    public boolean isDoNotScroll() {
        return (getFlags() & DO_NOT_SCROLL_BIT_FLAG) == DO_NOT_SCROLL_BIT_FLAG;
    }

    /**
     * May be set only if the MaxLen entry is present in the text field dictionary (see Table 229) and if the Multiline,
     * Password, and FileSelect flags are clear. If set, the field shall be automatically divided into as many equally
     * spaced positions, or combs, as the value of MaxLen, and the text is laid out into those combs.
     *
     * @return true if comb is enabled,  otherwise false.
     */
    public boolean isComb() {
        return (getFlags() & COMB_BIT_FLAG) == COMB_BIT_FLAG;
    }

    /**
     * If set, the value of this field shall be a rich text string (see 12.7.3.4, “Rich Text Strings”). If the field has
     * a value, the RV entry of the field dictionary (Table 222) shall specify the rich text string.
     *
     * @return true if file select,  otherwise false.
     */
    public boolean isRichText() {
        return (getFlags() & RICH_TEXT_BIT_FLAG) == RICH_TEXT_BIT_FLAG;
    }
}
