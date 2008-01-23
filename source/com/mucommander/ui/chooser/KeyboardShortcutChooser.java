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

package com.mucommander.ui.chooser;

import com.mucommander.Debug;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.combobox.ComboBoxListener;
import com.mucommander.ui.combobox.SaneComboBox;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public class KeyboardShortcutChooser extends JPanel implements ItemListener, ComboBoxListener, FocusListener, KeyListener {

    private JTextField textField;
    private JCheckBox modifierCheckBoxes[];
    private SaneComboBox keyComboBox;

    private KeyStroke currentKeyStroke;

    private boolean updatingTextField;
    private boolean updatingComboBox;
    private boolean updatingCheckBoxes;

    private String noneString = "<"+Translator.get("none")+">";

    private final static int KEY_CHOICES[] = new int[] {
        KeyEvent.VK_ESCAPE, KeyEvent.VK_TAB, KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE, KeyEvent.VK_ENTER,
        KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_MINUS, KeyEvent.VK_EQUALS, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_SEMICOLON, KeyEvent.VK_QUOTE, KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH,
        KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
        KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN,
        KeyEvent.VK_HOME, KeyEvent.VK_END, KeyEvent.VK_INSERT, KeyEvent.VK_PRINTSCREEN,
        KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R, KeyEvent.VK_S, KeyEvent.VK_T, KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W, KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_Z,
        KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9,
        KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4, KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12,
        KeyEvent.VK_ADD, KeyEvent.VK_SUBTRACT, KeyEvent.VK_MULTIPLY, KeyEvent.VK_DIVIDE, KeyEvent.VK_NUM_LOCK,
        KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9
    };

    private final static int MODIFIER_TABLE[] = {
        KeyEvent.SHIFT_MASK,
        KeyEvent.CTRL_MASK,
        KeyEvent.ALT_MASK,
        KeyEvent.META_MASK
    };

    private final static Color FOCUSED_TEXT_FIELD_FOREGROUND = Color.BLACK;
    private final static Color UNFOCUSED_TEXT_FIELD_FOREGROUND = Color.DARK_GRAY;


    public KeyboardShortcutChooser() {
        this(null);
    }

    public KeyboardShortcutChooser(KeyStroke keyStroke) {
        super(new BorderLayout());

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        this.currentKeyStroke = keyStroke;

        textField = new JTextField(30) {
            protected boolean processKeyBinding(KeyStroke keyStroke, KeyEvent keyEvent, int i, boolean b) {
                return true;
            }
        };

        textField.setDocument(new PlainDocument() {
            public void insertString(int i, String string, AttributeSet attributeSet) throws BadLocationException {
                if(updatingTextField)
                    super.insertString(i, string, attributeSet);
            }

            public void remove(int i, int i1) throws BadLocationException {
                if(updatingTextField)
                    super.remove(i, i1);
            }
        });

        flowPanel.add(textField);

        modifierCheckBoxes = new JCheckBox[MODIFIER_TABLE.length];
        for(int i=0; i< MODIFIER_TABLE.length; i++) {
            modifierCheckBoxes[i] = new JCheckBox(KeyEvent.getKeyModifiersText(MODIFIER_TABLE[i]));
            flowPanel.add(modifierCheckBoxes[i]);
            modifierCheckBoxes[i].addItemListener(this);
        }
        

        keyComboBox = new SaneComboBox();
        keyComboBox.addItem(new KeyChoice(0, noneString));
        for(int k=0; k<KEY_CHOICES.length; k++)
            addKeyChoice(KEY_CHOICES[k]);

        flowPanel.add(keyComboBox);

        add(flowPanel, BorderLayout.NORTH);

        updateKeyComboBox();
        updateCheckBoxes();
        updateTextField();

        keyComboBox.addComboBoxListener(this);
        textField.addKeyListener(this);
        textField.addFocusListener(this);
        textField.setForeground(UNFOCUSED_TEXT_FIELD_FOREGROUND);
    }

    private void addKeyChoice(int keyValue) {
        keyComboBox.addItem(new KeyChoice(keyValue, KeyEvent.getKeyText(keyValue)));
    }

    private void updateTextField() {
if(Debug.ON) Debug.trace("currentKeyStroke="+currentKeyStroke);
if(Debug.ON) Debug.trace("keyCode="+ (currentKeyStroke==null?"null":""+currentKeyStroke.getKeyCode()));

        updatingTextField = true;

        if(currentKeyStroke==null || currentKeyStroke.getKeyCode()==0)
            textField.setText(noneString);
        else
            textField.setText(MuAction.getKeyStrokeRepresentation(currentKeyStroke));

        updatingTextField = false;
    }

    private void updateKeyComboBox() {
        updatingComboBox = true;
        int keyCode = currentKeyStroke==null?0:currentKeyStroke.getKeyCode();

if(Debug.ON) Debug.trace("keyCode="+ keyCode);

        int nbChoices = keyComboBox.getItemCount();
        for(int i=1; i<nbChoices; i++) {
if(Debug.ON) Debug.trace("i="+i+" value="+((KeyChoice)keyComboBox.getItemAt(i)).getKeyValue()+" label="+((KeyChoice)keyComboBox.getItemAt(i)).getKeyLabel());
            if(((KeyChoice)keyComboBox.getItemAt(i)).getKeyValue()== keyCode)
            {
                keyComboBox.setSelectedIndex(i);

                updatingComboBox = false;
                return;
            }
        }

        keyComboBox.setSelectedIndex(0);
        updatingComboBox = false;
    }

    private void updateCheckBoxes() {
        int modifiers = currentKeyStroke==null?0:currentKeyStroke.getModifiers();

        updatingCheckBoxes = true;

        for(int i=0; i< MODIFIER_TABLE.length; i++) {
            modifierCheckBoxes[i].setSelected((modifiers&MODIFIER_TABLE[i])!=0);
        }

        updatingCheckBoxes = false;
    }

    private void updateKeyStroke() {
        int modifiers = 0;

        for(int i=0; i< MODIFIER_TABLE.length; i++) {
            if(modifierCheckBoxes[i].isSelected())
                modifiers |= MODIFIER_TABLE[i];
        }

        currentKeyStroke = KeyStroke.getKeyStroke(
            ((KeyChoice)keyComboBox.getSelectedItem()).getKeyValue(),
            modifiers
        );
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent itemEvent) {
        if(updatingCheckBoxes)
            return;

        updateKeyStroke();
        updateTextField();
    }


    /////////////////////////////////////
    // ComboBoxListener implementation //
    /////////////////////////////////////

    public void comboBoxSelectionChanged(SaneComboBox source) {
        if(updatingComboBox)
            return;

        updateKeyStroke();
        updateTextField();
    }


    //////////////////////////////////
    // FocusListener implementation //
    //////////////////////////////////

    public void focusGained(FocusEvent focusEvent) {
        textField.setText("");
        textField.setForeground(FOCUSED_TEXT_FIELD_FOREGROUND);
    }

    public void focusLost(FocusEvent focusEvent) {
        textField.setForeground(UNFOCUSED_TEXT_FIELD_FOREGROUND);
        updateTextField();
    }


    ////////////////////////////////
    // KeyListener implementation //
    ////////////////////////////////

    public void keyPressed(KeyEvent keyEvent) {
if(Debug.ON) Debug.trace("keyModifiers="+keyEvent.getModifiers()+" keyCode="+keyEvent.getKeyCode());

        int keyCode = keyEvent.getKeyCode();
        if(keyCode==KeyEvent.VK_SHIFT || keyCode==KeyEvent.VK_CONTROL || keyCode==KeyEvent.VK_ALT || keyCode==KeyEvent.VK_META)
            return;

        currentKeyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);

        updateKeyComboBox();
        updateCheckBoxes();
        updateTextField();
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    private static class KeyChoice {

        private int keyValue;
        private String keyLabel;

        private KeyChoice(int choiceValue, String choiceLabel) {
            this.keyValue = choiceValue;
            this.keyLabel = choiceLabel;
        }

        private int getKeyValue() {
            return keyValue;
        }

        private String getKeyLabel() {
            return keyLabel;
        }

        public boolean equals(Object o) {
            if(!(o instanceof KeyChoice))
                return false;

            return ((KeyChoice)o).keyValue == keyValue;
        }

        public String toString() {
            return keyLabel;
        }
    }



    public static void main(String args[]) throws IOException {
        Translator.loadDictionaryFile();

        JFrame frame = new JFrame();

        frame.getContentPane().add(new KeyboardShortcutChooser());

        frame.pack();
//        Dimension d = frame.getSize();
//        frame.setSize(new Dimension(d.width+200, d.height));
        frame.setVisible(true);
    }

}
