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

import com.mucommander.text.Translator;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * Component used to let users choose a font.
 * @author Nicolas Rinaudo
 */
public class FontChooser extends YBoxPanel implements ActionListener {
    // - Class fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Legal font sizes. */
    private final static int FONT_SIZES[] = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 28};



    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Lists all the available font families. */
    private JComboBox   families;
    /** Lists all the legal font sizes. */
    private JComboBox   sizes;
    /** Whether or not the font should be italic. */
    private JCheckBox   italic;
    /** Whether or not the font should be bold. */
    private JCheckBox   bold;
    /** Used to display a preview of the current font. */
    private JLabel      preview;
    /** Currently selected font. */
    private Font        font;
    /** List of all registered state change listeners. */
    private WeakHashMap listeners = new WeakHashMap();


    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new FontChooser with no preselected font.
     */
    public FontChooser() {
        super();
        initUI(null);
    }

    /**
     * Creates a new FontChooser with the specified selection.
     * @param selection font that should be pre-selected.
     */
    public FontChooser(Font selection) {
        super();
        initUI(selection);
    }

    /**
     * Initialises the font chooser's UI.
     * @param selection default font selection (ignored if <code>null</code>).
     */
    private void initUI(Font selection) {
        String[] familyNames;   // Contains all the available family names.
        int      selectedIndex; // Default selection in combo box.
        JPanel   panel;         // Temporary panel.

        // Initialises the chooser's alignement.
        setAlignmentX(LEFT_ALIGNMENT);

        // Font families.
        families      = new JComboBox();
        familyNames   = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        selectedIndex = 0;
        for(int i = 0; i < familyNames.length; i++) {
            families.addItem(familyNames[i]);
            if(selection != null && selection.getFamily().equalsIgnoreCase(familyNames[i]))
                selectedIndex = i;
        }
        families.setSelectedIndex(selectedIndex);
        families.addActionListener(this);

        // Adds the font families to the component.
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(families);
        add(panel);

        // Font sizes.
        sizes = new JComboBox();
        for(int i = 0; i < FONT_SIZES.length; i++)
            sizes.addItem(Integer.toString(FONT_SIZES[i]));
        if(selection != null)
            sizes.setSelectedItem(Integer.toString(selection.getSize()));
        sizes.addActionListener(this);

        // Font styles.
        bold = new JCheckBox(Translator.get("font_chooser.font_bold"));
        italic = new JCheckBox(Translator.get("font_chooser.font_italic"));
        bold.setSelected(selection.isBold());
        bold.addActionListener(this);
        italic.setSelected(selection.isItalic());
        italic.addActionListener(this);

        // Adds the font size and styles to the component.
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(Translator.get("font_chooser.font_size")+": "));
        panel.add(sizes);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(bold);
        panel.add(italic);
        add(panel);

        // Initialises the current font.
        font = selection == null ? createFont() : selection;

        // Creates the preview panel.
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(Translator.get("preview")+": "));
        preview = new JLabel("aBcDeFgHiJkLmNoPqRsTuVwXyZ");
        updatePreview();
        panel.add(preview);
        add(panel);
    }



    // - Content access ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a font from the current selection.
     * @return the font described by the current selection.
     */
    private Font createFont() {
        return new Font((String)families.getSelectedItem(),
                        (bold.isSelected() ? Font.BOLD : 0) | (italic.isSelected() ? Font.ITALIC : 0),
                        Integer.parseInt((String)sizes.getSelectedItem()));
    }

    /**
     * Returns the font currently selected in the chooser.
     * @return the font currently selected in the chooser.
     */
    public Font getCurrentFont() {return font;}



    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Updates the preview panel.
     */
    private void updatePreview() {
        preview.setFont(font);
        preview.repaint();
    }

    /**
     * Called when the font description has been changed.
     */
    public void actionPerformed(ActionEvent e) {
        Iterator    iterator;
        ChangeEvent event;

        font = createFont();
        updatePreview();

        // Notifies listeners.
        event    = new ChangeEvent(this);
        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ChangeListener)iterator.next()).stateChanged(event);
    }



    // - State changing code ----------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Registers the specified object as a change listener.
     */
    public void addChangeListener(ChangeListener listener) {
        listeners.put(listener, null);
    }

    /**
     * Un-registers the specified object as a change listener.
     */
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

}
