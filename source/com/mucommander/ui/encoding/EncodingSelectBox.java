/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.encoding;

import com.mucommander.runtime.OsFamilies;
import com.mucommander.ui.combobox.SaneComboBox;
import com.mucommander.ui.dialog.DialogOwner;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * This compound component lets the user choose a character encoding among a list of {@link EncodingPreferences#getPreferredEncodings()
 * preferred encodings} using a combo box, and customize the list of preferred encodings when the 'customize' button
 * is pressed.

 * @author Maxence Bernard
 */
public class EncodingSelectBox extends JPanel {

    /** Allows the encoding to be selected */
    protected SaneComboBox comboBox;

    /** Button that invokes the dialog that allows to customize the list of preferred encodings */
    protected JButton customizeButton;

    /** Contains all registered encoding listeners, stored as weak references */
    protected final WeakHashMap<EncodingListener, ?> listeners = new WeakHashMap<EncodingListener, Object>();

    /** The encoding that is currently selected, may be null */
    protected String currentEncoding;


    /**
     * Creates a new <code>EncodingSelectBox</code> with no specific encoding initially selected.
     *
     * @param dialogOwner the dialog/frame that owns this component
     */
    public EncodingSelectBox(DialogOwner dialogOwner) {
        this(dialogOwner, null);
    }

    /**
     * Creates a new <code>EncodingSelectBox</code> with the specified encoding initially selected.
     * The encoding must be one of the preferred encodings, or <code>null</code>. In the latter case, the first encoding
     * will be selected. 
     *
     * @param dialogOwner the dialog/frame that owns this component
     * @param selectedEncoding the encoding that will be initially selected, <code>null</code> for the first preferred
     * encoding
     */
    public EncodingSelectBox(final DialogOwner dialogOwner, String selectedEncoding) {
        super(new BorderLayout());

        comboBox = new SaneComboBox();
        populateComboBox(selectedEncoding);

        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String oldEncoding = currentEncoding;
                currentEncoding = (String)comboBox.getSelectedItem();

                if(currentEncoding==null || !currentEncoding.equals(oldEncoding)) {
                    // Notify listeners of the new encoding
                    fireEncodingListener(oldEncoding, EncodingSelectBox.this.currentEncoding);
                }
            }
        });

        add(comboBox, BorderLayout.CENTER);

        // Customize button
        customizeButton = new JButton("...");
        // Mac OS X: small component size
        if(OsFamilies.MAC_OS_X.isCurrent())
            customizeButton.putClientProperty("JComponent.sizeVariant", "small");

        customizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedEncoding = getSelectedEncoding();

                Window owner = dialogOwner.getOwner();
                if(owner instanceof Frame)
                    new PreferredEncodingsDialog((Frame)owner).showDialog();
                else
                    new PreferredEncodingsDialog((Dialog)owner).showDialog();

                comboBox.removeAllItems();
                populateComboBox(selectedEncoding);
            }
        });

        add(customizeButton, BorderLayout.EAST);
    }

    /**
     * Adds a checkbox menu item for each of the preferred encodings, and a special item that invokes a dialog
     * that allows the list of preferred encodings to be customized.
     *
     * @param selectEncoding the encoding that will be selected, <code>null</code> for the first one
     */
    protected void populateComboBox(String selectEncoding) {
        Vector<String> encodings = EncodingPreferences.getPreferredEncodings();

        // Ignore the specified encoding if it is not in the list of preferred encodings
        if(selectEncoding!=null && !encodings.contains(selectEncoding))
            selectEncoding = null;

        // Add preferred encodings to the combo box
        int nbEncodings = encodings.size();
        for(int i=0; i<nbEncodings; i++)
            comboBox.addItem(encodings.elementAt(i));

        if(selectEncoding!=null) {
            comboBox.setSelectedItem(selectEncoding);
            currentEncoding = selectEncoding;
        }
        else if(nbEncodings>0) {
            comboBox.setSelectedItem(encodings.elementAt(0));
            currentEncoding = selectEncoding;
        }
    }

    /**
     * Returns the encoding that is currently selected, <code>null</code> if none is selected.
     *
     * @return the encoding that is currently selected, <code>null</code> if none is selected.
     */
    public String getSelectedEncoding() {
        int index = comboBox.getSelectedIndex();

        if(index==-1)
            return null;

        return (String)comboBox.getItemAt(index);
    }
    

    //////////////////////
    // Listener methods //
    //////////////////////

    public void addEncodingListener(EncodingListener listener) {
        synchronized(listeners) {
            listeners.put(listener, null);
        }
    }

    public void removeEncodingListener(EncodingListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    protected void fireEncodingListener(String oldEncoding, String newEncoding) {
        synchronized(listeners) {
            for(EncodingListener listener : listeners.keySet())
                listener.encodingChanged(this, oldEncoding, newEncoding);
        }

    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void setEnabled(boolean enabled) {
        comboBox.setEnabled(enabled);
        customizeButton.setEnabled(enabled);

        super.setEnabled(enabled);
    }
}
