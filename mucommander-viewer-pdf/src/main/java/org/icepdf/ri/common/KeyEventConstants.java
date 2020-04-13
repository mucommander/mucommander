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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * The KeyEvent Class contains the key event and input event used for menu
 * and button manipulatin via the keyboard.  This class may need to be changed
 * depending on region and languages.
 */
public class KeyEventConstants {

    // Get the correct menu shortcut key for the current platform
    public static final int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public static final int KEY_CODE_OPEN_FILE = KeyEvent.VK_O;
    public static final int MODIFIER_OPEN_FILE = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_OPEN_URL = KeyEvent.VK_U;
    public static final int MODIFIER_OPEN_URL = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_CLOSE = KeyEvent.VK_W;
    public static final int MODIFIER_CLOSE = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_SAVE_AS = KeyEvent.VK_S;
    public static final int MODIFIER_SAVE_AS = MENU_SHORTCUT_KEY_MASK | InputEvent.SHIFT_MASK;
    public static final int KEY_CODE_PRINT_SETUP = KeyEvent.VK_P;
    public static final int MODIFIER_PRINT_SETUP = MENU_SHORTCUT_KEY_MASK | InputEvent.SHIFT_MASK;
    public static final int KEY_CODE_PRINT = KeyEvent.VK_P;
    public static final int MODIFIER_PRINT = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_EXIT = KeyEvent.VK_Q;
    public static final int MODIFIER_EXIT = MENU_SHORTCUT_KEY_MASK;

    public static final int KEY_CODE_FIT_ACTUAL = KeyEvent.VK_1;
    public static final int MODIFIER_FIT_ACTUAL = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_FIT_PAGE = KeyEvent.VK_2;
    public static final int MODIFIER_FIT_PAGE = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_FIT_WIDTH = KeyEvent.VK_3;
    public static final int MODIFIER_FIT_WIDTH = MENU_SHORTCUT_KEY_MASK;

    public static final int KEY_CODE_ZOOM_IN = KeyEvent.VK_I;
    public static final int MODIFIER_ZOOM_IN = MENU_SHORTCUT_KEY_MASK | InputEvent.SHIFT_MASK;
    public static final int KEY_CODE_ZOOM_OUT = KeyEvent.VK_O;
    public static final int MODIFIER_ZOOM_OUT = MENU_SHORTCUT_KEY_MASK | InputEvent.SHIFT_MASK;

    public static final int KEY_CODE_ROTATE_LEFT = KeyEvent.VK_L;
    public static final int MODIFIER_ROTATE_LEFT = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_ROTATE_RIGHT = KeyEvent.VK_R;
    public static final int MODIFIER_ROTATE_RIGHT = MENU_SHORTCUT_KEY_MASK;

    public static final int KEY_CODE_FIRST_PAGE = KeyEvent.VK_UP;
    public static final int MODIFIER_FIRST_PAGE = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_PREVIOUS_PAGE = KeyEvent.VK_LEFT;
    public static final int MODIFIER_PREVIOUS_PAGE = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_NEXT_PAGE = KeyEvent.VK_RIGHT;
    public static final int MODIFIER_NEXT_PAGE = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_LAST_PAGE = KeyEvent.VK_DOWN;
    public static final int MODIFIER_LAST_PAGE = MENU_SHORTCUT_KEY_MASK;

    public static final int KEY_CODE_SEARCH = KeyEvent.VK_S;
    public static final int MODIFIER_SEARCH = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_GOTO = KeyEvent.VK_N;
    public static final int MODIFIER_GOTO = MENU_SHORTCUT_KEY_MASK;

    public static final int KEY_CODE_UNDO = KeyEvent.VK_Z;
    public static final int MODIFIER_UNDO = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_REDO = KeyEvent.VK_Z;
    public static final int MODIFIER_REDO = MENU_SHORTCUT_KEY_MASK | InputEvent.SHIFT_MASK;
    public static final int KEY_CODE_COPY = KeyEvent.VK_C;
    public static final int MODIFIER_COPY = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_DELETE = KeyEvent.VK_D;
    public static final int MODIFIER_DELETE = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_SELECT_ALL = KeyEvent.VK_A;
    public static final int MODIFIER_SELECT_ALL = MENU_SHORTCUT_KEY_MASK;
    public static final int KEY_CODE_DESELECT_ALL = KeyEvent.VK_A;
    public static final int MODIFIER_DESELECT_ALL = MENU_SHORTCUT_KEY_MASK | InputEvent.SHIFT_MASK;
}
