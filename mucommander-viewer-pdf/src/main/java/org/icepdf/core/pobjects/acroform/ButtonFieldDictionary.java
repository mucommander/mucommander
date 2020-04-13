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

import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * The ButtonFieldDictionary contains all the dictionary entries specific to
 * the button widget.
 *
 * @since 5.1
 */
public class ButtonFieldDictionary extends VariableTextFieldDictionary {

    /**
     * (Radio buttons only) If set, exactly one radio button shall be selected at
     * all times; selecting the currently selected button has no effect. If clear,
     * clicking the selected button deselects it, leaving no button selected.
     */
    public static final int NO_TOGGLE_TO_OFF_BIT_FLAG = 0x4000;

    /**
     * If set, the field is a set of radio buttons; if clear, the field is a
     * check box. This flag may be set only if the Push button flag is clear.
     */
    public static final int RADIO_BIT_FLAG = 0x8000;

    /**
     * If set, the field is a push button that does not retain a permanent value.
     */
    public static final int PUSH_BUTTON_BIT_FLAG = 0x10000;

    /**
     * (PDF 1.5) If set, a group of radio buttons within a radio button field
     * that use the same value for the on state will turn on and off in unison;
     * that is if one is checked, they are all checked. If clear, the buttons are
     * mutually exclusive (the same behavior as HTML radio buttons).
     */
    public static final int RADIO_IN_UNISON_BIT_FLAG = 0x1000000;

    public enum ButtonFieldType {
        PUSH_BUTTON, RADIO_BUTTON, CHECK_BUTTON
    }

    protected ButtonFieldType buttonFieldType;

    @SuppressWarnings("unchecked")
    public ButtonFieldDictionary(Library library, HashMap entries) {
        super(library, entries);

    }

    /**
     * (Radio buttons only) If set, exactly one radio button shall be selected at
     * all times; selecting the currently selected button has no effect. If clear,
     * clicking the selected button deselects it, leaving no button selected.
     *
     * @return true if only one button state should be selected at all times.
     */
    public boolean isNoToggleToOff() {
        return (getFlags() & NO_TOGGLE_TO_OFF_BIT_FLAG) == NO_TOGGLE_TO_OFF_BIT_FLAG;
    }

    /**
     * If set, the field is a set of radio buttons; if clear, the field is a
     * check box. This flag may be set only if the Push button flag is clear.
     *
     * @return true if button field is of type push button.
     */
    public boolean isRadioButton() {
        return (getFlags() & RADIO_BIT_FLAG) == RADIO_BIT_FLAG;
    }

    /**
     * If set, the field is a push button that does not retain a permanent value.
     *
     * @return true if button field is of type push button.
     */
    public boolean isPushButton() {
        return (getFlags() & PUSH_BUTTON_BIT_FLAG) == PUSH_BUTTON_BIT_FLAG;
    }

    /**
     * If set, a group of radio buttons within a radio button field
     * that use the same value for the on state will turn on and off in unison;
     * that is if one is checked, they are all checked. If clear, the buttons are
     *
     * @return true if radio buttons in unison; otherwise false.
     */
    public boolean isRadioInUnison() {
        return (getFlags() & RADIO_IN_UNISON_BIT_FLAG) == RADIO_IN_UNISON_BIT_FLAG;
    }

    public ButtonFieldType getButtonFieldType() {
        // apply button bit logic
        if ((getFlags() & PUSH_BUTTON_BIT_FLAG) ==
                PUSH_BUTTON_BIT_FLAG) {
            buttonFieldType = ButtonFieldType.PUSH_BUTTON;
        } else {
            // check for checkbox/radio.
            if (isRadioButton()) {
                buttonFieldType = ButtonFieldType.RADIO_BUTTON;
            } else {
                buttonFieldType = ButtonFieldType.CHECK_BUTTON;
            }
        }
        return buttonFieldType;
    }

    public void setButtonFieldType(ButtonFieldType buttonFieldType) {
        this.buttonFieldType = buttonFieldType;
    }


}
