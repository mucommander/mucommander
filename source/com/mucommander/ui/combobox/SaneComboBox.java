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

package com.mucommander.ui.combobox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;


/**
 * SaneComboBox is a JComboBox which does not have the awful default JComboBox behavior of firing ActionEvents
 * when navigating with the arrow keys between choices of the popup menu.
 * This page describes the problem in details: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4199622
 *
 * <p>Also when using {@link ComboBoxListener}, action events that are normally triggered by JComboBox
 * when the add/insert/remove item methods are called are filtered out, only actual selection changes performed
 * by the user are fired.
 *
 * @author Maxence Bernard
 */
public class SaneComboBox extends JComboBox {

    private WeakHashMap cbListeners = new WeakHashMap();
    private boolean ignoreActionEvent;


    public SaneComboBox() {
        super();
        init();
    }

    public SaneComboBox(ComboBoxModel comboBoxModel) {
        super(comboBoxModel);
        init();
    }

    public SaneComboBox(Object[] items) {
        super(items);
        init();
    }

    public SaneComboBox(Vector items) {
        super(items);
        init();
    }


    private void init() {
        // Prevent up/down keys from firing ActionEvents
        // for Java 1.3
        putClientProperty("JComboBox.lightweightKeyboardNavigation","Lightweight");
        // for Java 1.4 and up
        putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        // Listen to combo box action events, these are fired each time an item is selected when the popup menu
        // is visible, either by pressing 'Enter' on an item or by clicking on it.
        addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                // Filter out action events triggered by the add/insert/remove item methods
                if(!ignoreActionEvent)
                    fireComboBoxSelectionChanged();
            }
        });
    }


    //////////////////////////////////////
    // ComboBoxListener support methods //
    //////////////////////////////////////

    /**
     * Adds the specified ComboBoxListener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeComboBoxListener(ComboBoxListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.
     *
     * @param listener the ComboBoxListener to add to the list of registered listeners.
     */
    public void addComboBoxListener(ComboBoxListener listener) {
        cbListeners.put(listener, null);
    }

    /**
     * Removes the specified ComboBoxListener from the list of registered listeners.
     *
     * @param listener the ComboBoxListener to remove from the list of registered listeners.
     */
    public void removeComboBoxListener(ComboBoxListener listener) {
        cbListeners.remove(listener);
    }

    /**
     * Notifies all registered ComboBoxListener instances that an item has been selected from the
     * combo box popup menu. The item may have been selected either with the 'Enter' key, or by clicking on the item.
     *
     * <p>Unlike JComboBox ActionListener behavior, calls to the add/insert/remove item methods do *not* trigger
     * a selection event.
     */
    protected void fireComboBoxSelectionChanged() {
        // Iterate on all listeners
        Iterator iterator = cbListeners.keySet().iterator();
        while(iterator.hasNext())
            ((ComboBoxListener)iterator.next()).comboBoxSelectionChanged(this);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////


    public void addItem(Object object) {
        ignoreActionEvent = true;
        super.addItem(object);
        ignoreActionEvent = false;
    }

    public void insertItemAt(Object object, int i) {
        ignoreActionEvent = true;
        super.insertItemAt(object, i);
        ignoreActionEvent = false;
    }

    public void removeItem(Object object) {
        ignoreActionEvent = true;
        super.removeItem(object);
        ignoreActionEvent = false;
    }

    public void removeItemAt(int i) {
        ignoreActionEvent = true;
        super.removeItemAt(i);
        ignoreActionEvent = false;
    }

    public void removeAllItems() {
        ignoreActionEvent = true;
        super.removeAllItems();
        ignoreActionEvent = false;
    }
}
