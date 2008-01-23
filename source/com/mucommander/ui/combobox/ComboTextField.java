/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.combobox;

import javax.swing.*;
import javax.swing.text.Document;

/**
 * @author Maxence Bernard
 */
public class ComboTextField extends JTextField {
    private EditableComboBox comboBox;

    public ComboTextField() {
    }

    public ComboTextField(String text) {
        super(text);
    }

    public ComboTextField(int columns) {
        super(columns);
    }

    public ComboTextField(String text, int columns) {
        super(text, columns);
    }

    public ComboTextField(Document document, String text, int columns) {
        super(document, text, columns);
    }


    public EditableComboBox getComboBox() {
        return comboBox;
    }

    public void setComboBox(EditableComboBox comboBox) {
        this.comboBox = comboBox;

//        comboBox.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent actionEvent) {
//                if(ComboTextField.this.comboBox!=null && comboSelectionUpdatesTextField) {
//Debug.trace("selectedIndex="+ComboTextField.this.comboBox.getSelectedIndex()+" selectedItem="+(ComboTextField.this.comboBox.getSelectedItem()));
//                    // Replace the text field's contents by the selected item's string representation,
//                    // only if this feature has been enabled
//                    Object selectedItem = ComboTextField.this.comboBox.getSelectedItem();
//
//                    if(selectedItem!=null)
//                        setText(selectedItem.toString());
//                }
//            }
//        });

//        comboBox.addItemListener(new ItemListener() {
//
//            public void itemStateChanged(ItemEvent itemEvent) {
//Debug.trace("selectedIndex="+ComboTextField.this.comboBox.getSelectedIndex()+" selectedItem="+(ComboTextField.this.comboBox.getSelectedItem()));
//
//                if(ComboTextField.this.comboBox!=null)
//                    ComboTextField.this.comboBox.fireComboBoxSelectionChanged();
//            }
//        });

    }


//    /**
//     * If true is specified, when an item is selected in this combo box, the text field's contents
//     * will be automatically replaced by the selected item's string representation.
//     */
//    public void setComboSelectionUpdatesTextField(boolean comboSelectionUpdatesTextField) {
//        this.comboSelectionUpdatesTextField = comboSelectionUpdatesTextField;
//    }
//
//
//    /**
//     * If true is returned, when an item is selected in this combo box, the text field's contents
//     * will be automatically replaced by the selected item's string representation.
//     * This feature is disabled by default (false is returned).
//     */
//    public boolean getComboSelectionUpdatesTextField() {
//        return comboSelectionUpdatesTextField;
//    }


//    /**
//     *
//     */
//    protected boolean processKeyBinding(KeyStroke keyStroke, KeyEvent keyEvent, int condition, boolean pressed) {
//Debug.trace("keyCode="+keyEvent.getKeyCode()+" pressed="+pressed+" selectedIndex="+comboBox.getSelectedIndex()+" selectedItem="+(comboBox.getSelectedItem())+" isPopupVisible="+comboBox.isPopupVisible());
//
//        if(comboBox==null || pressed)
//            return super.processKeyBinding(keyStroke, keyEvent, condition, pressed);
//
//        int keyCode = keyEvent.getKeyCode();
//        if(comboBox.isPopupVisible()) {
//            if(!(keyCode==KeyEvent.VK_ENTER || keyCode==KeyEvent.VK_ESCAPE || keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_DOWN))
//                return true;    // Consume the key event to prevent it this text field from receiving it
//
//            if(keyCode==KeyEvent.VK_ENTER) {
//                super.processKeyBinding(keyStroke, keyEvent, condition, pressed);
//                comboBox.hidePopup();
//
////                comboBox.fireComboBoxSelectionChanged();
//                return true;    // Consume the key event to prevent it this text field from receiving it
//            }
//            else if(keyCode==KeyEvent.VK_ESCAPE) {
//                super.processKeyBinding(keyStroke, keyEvent, condition, pressed);
//
//                comboBox.hidePopup();
//                return true;    // Consume the key event to prevent it this text field from receiving it
//            }
//
//            return super.processKeyBinding(keyStroke, keyEvent, condition, pressed);
//        }
//        else {
//            boolean ret = super.processKeyBinding(keyStroke, keyEvent, condition, pressed);
//
//            if(keyCode==KeyEvent.VK_ENTER) {
//                comboBox.fireComboFieldValidated();
//            }
//            else if(keyCode==KeyEvent.VK_ESCAPE) {
//                comboBox.fireComboFieldCancelled();
//            }
//
//            return ret;
//        }
//    }
}
