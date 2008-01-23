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
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * EditableComboBox is an editable combo box (really!) that can use a specified JTextField to be used as the editor.
 *
 * <p>EditableComboBox also extends JComboBox to make it much easier to use, instead of having to work around its
 * numerous bugs and weird behavior (understatement). Registering a {@link EditableComboBoxListener} makes it
 * easy to know for sure when an item has been selected from the combo popup menu, or when the text field has been
 * validated ('Enter' key pressed) or cancelled ('Escape' key pressed). It is strongly recommanded to use this interface
 * instead of ActionListener / ItemListener, their already erratic behavior could be further aggravated by the tweakings
 * used in this class.
 *
 * <p>The {@link #setComboSelectionUpdatesTextField(boolean)} method allows to automatically replace the text field's
 * contents when an item is selected from the associated combo box, replacing its value by the selected item's
 * string representation. This feature is disabled by default.
 *
 * @see EditableComboBoxListener
 * @author Maxence Bernard
 */
public class EditableComboBox extends SaneComboBox {
    /** Used to render the content of the combo box. */
    private ComboBoxCellRenderer renderer;
    /** The text field used as the combo box's editor */
    private JTextField textField;

    /** Contains all registered EditableComboBoxListener instances, stored as weak references */
    private WeakHashMap editableCBListeners = new WeakHashMap();

    /** Specifies whether the text field's contents is updated when an item is selected in the associated combo box */
    private boolean comboSelectionUpdatesTextField;


    /**
     * Creates a new editable combo box and a JTextField to be used as the editor.
     * Has the same effect as calling {@link #EditableComboBox(javax.swing.JTextField)} with a null value.
     */
    public EditableComboBox() {
        init(null);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     */
    public EditableComboBox(JTextField textField) {
        init(textField);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor and ComboBoxModel.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     * @param comboBoxModel the ComboBoxModel to use for this combo box
     */
    public EditableComboBox(JTextField textField, ComboBoxModel comboBoxModel) {
        super(comboBoxModel);
        init(textField);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor and items to populate the initial items list.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     * @param items items used to populate the initial items list.
     */
    public EditableComboBox(JTextField textField, Object[] items) {
        super(items);
        init(textField);
    }

    /**
     * Creates a new editable combo box using the given text field as the editor and items to populate the initial items list.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a new JTextField instance
     * will be created and used.
     * @param items items used to populate the initial items list.
     */
    public EditableComboBox(JTextField textField, Vector items) {
        super(items);
        init(textField);
    }


    /**
     * Returns the text field used as the combo box's editor.
     */
    public JTextField getTextField() {return textField;}


    /**
     * If true is specified, when an item is selected in this combo box, the text field's contents
     * will be automatically replaced by the selected item's string representation.
     */
    public void setComboSelectionUpdatesTextField(boolean comboSelectionUpdatesTextField) {
        this.comboSelectionUpdatesTextField = comboSelectionUpdatesTextField;
    }


    /**
     * If true is returned, when an item is selected in this combo box, the text field's contents
     * will be automatically replaced by the selected item's string representation.
     * This feature is disabled by default (false is returned).
     */
    public boolean getComboSelectionUpdatesTextField() {
        return comboSelectionUpdatesTextField;
    }

    /**
     * Initializes the combo box to make it editable and use the given text field.
     *
     * @param textField the text field to be used as the combo box's editor. If null, a JTextField instance will be created and used.
     */
    private void init(JTextField textField) {
        setRenderer(renderer = new ComboBoxCellRenderer());
        // Create a new JTextField if no text field was specified
        if(textField==null) {
            this.textField = new JTextField();
        }
        // Use the specified text field
        else
            this.textField = textField;

        // Use a custom editor that uses the text field
        setEditor(new BasicComboBoxEditor() {
                public Component getEditorComponent() {
                    return EditableComboBox.this.textField;
                }
            });

        // Make this combo box editable
        setEditable(true);

        // Note: the default JComboBox behavior is to also fire an ActionEvent when enter is pressed on the text field
        // and the popup menu is not visible, making the ActionEvent indistinguishable from a combo item selection.
        // This awful behavior is overridden by keyPressed() so that ActionEvent is only fired for item selections.

        // Listen to the text field's key events. These are fired regardless of the combo box popup menu being visible or not.
        // The following KeyListener is added as an anonymous inner class so that any class overridding EditableComboBox
        // can safely implement KeyListener without risking to override those methods by accident.
        this.textField.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();

                // Combo popup menu is visible
                if(isPopupVisible()) {
                    if(keyCode==KeyEvent.VK_ENTER) {
                        // Under Java 1.5 or under, we need to explicitely hide the popup.
                        if(com.mucommander.PlatformManager.JAVA_1_5.isCurrentOrLower())
                            hidePopup();
                        // Note that since the event is not consumed, JComboBox will catch it and fire
                    }
                    else if(keyCode==KeyEvent.VK_ESCAPE) {
                        // Explicitely hide popup menu, JComboBox does not seem do it automatically (at least under Mac OS X + Java 1.5 and Java 1.4)
                        hidePopup();
                        // Consume the event so that it is not propagated, since dialogs catch this event to close the window
                        keyEvent.consume();
                    }
                }
                // Combo popup menu is not visible, these events really belong to the text field
                else {
                    if(keyCode==KeyEvent.VK_ENTER) {
                        // Notify listeners that the text field has been validated
                        fireComboFieldValidated();
                        // /!\ Consume the event so to prevent JComboBox from firing an ActionEvent (default JComboBox behavior)
                        keyEvent.consume();
                    }
                    else if(keyCode==KeyEvent.VK_ESCAPE) {
                        // Notify listeners that the text field has been cancelled
                        fireComboFieldCancelled();
                    }
                }
            }
        });
    }


    //////////////////////////////////////////////
    // EditableComboBoxListener support methods //
    //////////////////////////////////////////////

    /**
     * Adds the specified EditableComboBoxListener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeEditableComboBoxListener(EditableComboBoxListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.
     *
     * @param listener the EditableComboBoxListener to add to the list of registered listeners.
     */
    public void addEditableComboBoxListener(EditableComboBoxListener listener) {
        addComboBoxListener(listener);
        editableCBListeners.put(listener, null);
    }

    /**
     * Removes the specified EditableComboBoxListener from the list of registered listeners.
     *
     * @param listener the EditableComboBoxListener to remove from the list of registered listeners.
     */
    public void removeEditableComboBoxListener(EditableComboBoxListener listener) {
        removeComboBoxListener(listener);
        editableCBListeners.remove(listener);
    }


    /**
     * Overrides {@link SaneComboBox#fireComboBoxSelectionChanged()} to set the text field's contents to the item that
     * has been selected, if {@link #setComboSelectionUpdatesTextField(boolean)} has been enabled.  
     */
    protected void fireComboBoxSelectionChanged() {
        if(comboSelectionUpdatesTextField) {
            // Replace the text field's contents by the selected item's string representation,
            // only if this feature has been enabled
            if(getSelectedIndex() != -1)
                textField.setText(getSelectedItem().toString());
        }

        super.fireComboBoxSelectionChanged();
    }


    /**
     * Notifies all registered EditableComboBoxListener instances that the text field has been validated, that is
     * the 'Enter' key has been pressed in the text field, without the popup menu being visible.
     *
     * <p>Note: Unlike JComboBox's weird ActionEvent handling, this event is *not* fired when 'Enter' is pressed
     * in the combo popup menu.
     */
    protected void fireComboFieldValidated() {
        // Iterate on all listeners
        Iterator iterator = editableCBListeners.keySet().iterator();
        while(iterator.hasNext())
            ((EditableComboBoxListener)iterator.next()).textFieldValidated(this);
    }


    /**
     * Notifies all registered EditableComboBoxListener instances that the text field has been cancelled, that is
     * the 'Escape' key has been pressed in the text field, without the popup menu being visible.
     *
     * <p>Note: This event is *not* fired when 'Escape' is pressed in the combo popup menu.
     */
    protected void fireComboFieldCancelled() {
        // Iterate on all listeners
        Iterator iterator = editableCBListeners.keySet().iterator();
        while(iterator.hasNext())
            ((EditableComboBoxListener)iterator.next()).textFieldCancelled(this);
    }


    // - Aspect managenement -------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void setForeground(Color color) {
        if(renderer == null)
	    super.setForeground(color);
        else {
            renderer.setForeground(color);
            textField.setForeground(color);
        }
    }

    public void setBackground(Color color) {
        if(renderer == null)
	    super.setBackground(color);
        else {
            renderer.setBackground(color);
            textField.setBackground(color);
        }
    }

    public void setSelectionForeground(Color color) {
        if(renderer != null) {
            renderer.setSelectionForeground(color);
            textField.setSelectedTextColor(color);
        }
    }

    public void setSelectionBackground(Color color) {
        if(renderer != null) {
            renderer.setSelectionBackground(color);
	    textField.setSelectionColor(color);
        }
    }

    public void setFont(Font font) {
        super.setFont(font);
        if(renderer != null) {
            renderer.setFont(font);
            textField.setFont(font);
        }
    }
}
