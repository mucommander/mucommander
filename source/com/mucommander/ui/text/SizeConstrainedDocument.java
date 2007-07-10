/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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


package com.mucommander.ui.text;


/**
 * Document that can be used with <code>java.swing.JTextField</code> and <code>javax.swing.JTextArea</code> to limit
 * the number of characters that can be entered by the user.
 *
 * @author Maxence Bernard
 */
public class SizeConstrainedDocument extends javax.swing.text.PlainDocument {

    /** Maximum number of characters allowed */
    private int maxLen = -1;

    /**
     * Creates a new instance of SizeConstrainedDocument, using the specified length
     * to limit the number of characters allowed.
     *
     * @param maxLen maximum number of characters allowed
     */
    public SizeConstrainedDocument(int maxLen) {
        this.maxLen = maxLen;
    }


    //////////////////////////////////
    // PlainDocument implementation //
    //////////////////////////////////

    public void insertString(int offset, String str, javax.swing.text.AttributeSet attributeSet) throws javax.swing.text.BadLocationException {
        if (str != null && maxLen > 0 && this.getLength() + str.length() > maxLen)
            return;

        super.insertString(offset, str, attributeSet);
    }
}
