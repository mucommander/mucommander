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

package com.mucommander.ui.chooser;

import com.mucommander.text.SizeFormat;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * <code>SizeChooser</code> is a compound component made of a <code>JComboBox</code> and a <code>JSpinner</code> that
 * allows the user to enter a size in multiple of a selectable unit: byte, kilobyte, megabyte, ...
 * Each time the value changes, a <code>ChangeEvent</code> is fired to registered listeners.
 *
 * <p>This component can also serve to enter a speed in byte/kilobyte/megabyte/... per second. This only affects the
 * units displayed, this component works in the exact same way otherwise.</p>
 *
 * @author Maxence Bernard
 */
public class SizeChooser extends JPanel {

    /** Allows to enter a value in multiple of the current unit */
    private JSpinner valueSpinner;

    /** Allows to select the size/speed unit */
    private JComboBox unitComboBox;

    /** Contains all registered listeners, stored as weak references */
    private WeakHashMap listeners = new WeakHashMap();


    /**
     * Creates a new SizeChooser.
     *
     * @param speedUnits if true, speed units will be displayed (B/s, KB/s, MB/s, ...) instead of size unit (B, KB, MB, ...).
     */
    public SizeChooser(boolean speedUnits) {
        super(new FlowLayout(FlowLayout.LEADING));

        valueSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 100));
        valueSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fireChangeEvent();
            }
        });
        add(valueSpinner);

        unitComboBox = new JComboBox();
        for(int i= SizeFormat.BYTE_UNIT; i<SizeFormat.GIGA_BYTE_UNIT; i++)
            unitComboBox.addItem(SizeFormat.getUnitString(i, speedUnits));
        unitComboBox.setSelectedIndex(SizeFormat.KILO_BYTE_UNIT);
        unitComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                fireChangeEvent();
            }
        });
        add(unitComboBox);
    }

    /**
     * Returns the current value expressed in bytes.
     *
     * @return the current value expressed in bytes
     */
    public long getValue() {
        return SizeFormat.getUnitBytes(unitComboBox.getSelectedIndex())*((Integer)valueSpinner.getValue()).intValue();
    }


    /**
     * Adds the specified ChangedListener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #remove}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.
     *
     * @param listener the ChangeListener to add to the list of registered listeners.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
        listeners.put(listener, null);
    }

    /**
     * Removes the specified ChangeListener from the list of registered listeners.
     *
     * @param listener the ChangeListener to remove from the list of registered listeners.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered ChangeListener that the current value has changed. This method is called as the result
     * of a change in the spinner or the combo box.
     */
    public synchronized void fireChangeEvent() {
        // Iterate on all listeners
        Iterator iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ChangeListener)iterator.next()).stateChanged(new ChangeEvent(this));
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public void setEnabled(boolean enabled) {
        valueSpinner.setEnabled(enabled);
        unitComboBox.setEnabled(enabled);
    }
}
