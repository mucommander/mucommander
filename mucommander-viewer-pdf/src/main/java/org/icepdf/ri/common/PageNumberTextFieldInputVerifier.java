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
package org.icepdf.ri.common;

import javax.swing.*;
import java.awt.*;

/**
 * Utility method to verify that a components input text is a whole number.
 *
 * @author Mark Collette
 * @since 2.0
 */
public class PageNumberTextFieldInputVerifier extends InputVerifier {
    final static int maxLength = 4;

    public boolean verify(JComponent comp) {
        boolean returnValue = true;
        JTextField textField = (JTextField) comp;
        String content = textField.getText();
        // if the string has a valid length
        if (content.length() != 0 && content.length() < maxLength) {
            try {
                // parse the string just to make sure it is a valid number
                Integer.parseInt(textField.getText());
            } catch (NumberFormatException e) {
                returnValue = false;
            }
        } else {
            if (content.length() > 0) {
                // we don't have to reverify as the keylistener makes
                // sure that the string is a number.
                textField.setText(content.substring(0, maxLength));
            } else {
                textField.setText("");
            }
        }
        return returnValue;
    }

    public boolean shouldYieldFocus(JComponent input) {
        boolean valid = super.shouldYieldFocus(input);
        if (!valid) {
            Toolkit.getDefaultToolkit().beep();
        }
        return valid;
    }
}
