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

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Utility class to ignore all keyboard events that are not digits, KeyEvent.VK_BACK_SPACE,
 * KeyEvent.VK_DELETE, or KeyEvent.VK_ENTER.
 *
 * @author Mark Collette
 * @since 2.0
 */
public class PageNumberTextFieldKeyListener extends KeyAdapter {
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (!(Character.isDigit(c) ||
                c == KeyEvent.VK_BACK_SPACE ||
                c == KeyEvent.VK_DELETE ||
                c == KeyEvent.VK_ESCAPE ||
                c == KeyEvent.VK_ENTER)) {
            Toolkit.getDefaultToolkit().beep();
            e.consume();
        }
    }
}
