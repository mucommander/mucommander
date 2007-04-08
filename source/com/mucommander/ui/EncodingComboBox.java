package com.mucommander.ui;

import com.mucommander.ui.comp.combobox.SaneComboBox;

import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * EncodingComboBox is a combo box that contains all available encodings/charsets (UTF-8, ISO-8859-1, ...).
 *
 * @author Maxence Bernard
 */
public class EncodingComboBox extends SaneComboBox {

    public EncodingComboBox() {
        Iterator availableEncodings = Charset.availableCharsets().keySet().iterator();
        while(availableEncodings.hasNext())
            addItem(availableEncodings.next());
    }
}
