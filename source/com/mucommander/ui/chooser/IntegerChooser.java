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

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * Component meant to let users choose an integer value from a specific range.
 * <p>
 * An IntegerChooser is an compound-component created by associating a JSpinner and a JSlider.
 * </p>
 * <p>
 * In order to track the chooser's state, listeners can be registered through {@link #addChangeListener(ChangeListener)}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class IntegerChooser extends JPanel implements ChangeListener {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** List of all registered state change listeners. */
    private WeakHashMap listeners;


    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Integer slider. */
    private JSlider  slider;
    /** Integer spinner. */
    private JSpinner spinner;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new integer chooser.
     * @param min          chooser's minimum value.
     * @param max          chooser's maximum value.
     * @param initialValue chooser's initial value.
     */
    public IntegerChooser(int min, int max, int initialValue) {
        super();

        // Initialises the listeners.
        listeners = new WeakHashMap();

        // Creates the components.
        slider  = new JSlider(JSlider.HORIZONTAL, min, max, initialValue);
        spinner = new JSpinner(new SpinnerNumberModel(initialValue, min, max, 1));

        // Registers listeners.
        slider.addChangeListener(this);
        spinner.addChangeListener(this);

        // Creates the panel.
        this.add(slider);
        this.add(spinner, BorderLayout.EAST);
    }



    // - Slider modifications ---------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * This method sets the major tick spacing.
     * The number that is passed-in represents the distance, measured in values, between each major tick mark. If you have a
     * slider with a range from 0 to 50 and the major tick spacing is set to 10, you will get major ticks next to the following
     * values: 0, 10, 20, 30, 40, 50.
     */
    public void setMajorTickSpacing(int spacing) {slider.setMajorTickSpacing(spacing);}

    /**
     * This method sets the minor tick spacing.
     * The number that is passed-in represents the distance, measured in values, between each minor tick mark. If you have a
     * slider with a range from 0 to 50 and the minor tick spacing is set to 10, you will get minor ticks next to the following
     * values: 0, 10, 20, 30, 40, 50.
     */
    public void setMinorTickSpacing(int spacing) {slider.setMinorTickSpacing(spacing);}

    /**
     * Determines whether tick marks are painted on the slider
     */
    public void setPaintTicks(boolean b) {slider.setPaintTicks(b);}

    /**
     * Determines whether labels are painted on the slider
     */
    public void setPaintLabels(boolean b) {slider.setPaintLabels(b);}

    /**
     * Determines whether labels are painted on the slider.
     */
    public void setValue(int value) {
        slider.setValue(value);
        spinner.setValue(new Integer(value));
    }



    // - Value retrieval --------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns the chooser's current value.
     * @return the chooser's current value.
     */
    public int getValue() {return slider.getValue();}




    // - State changing code ----------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Registers the specified object as a change listener.
     */
    public void addChangeListener(ChangeListener listener) {listeners.put(listener, null);}

    /**
     * Un-registers the specified object as a change listener.
     */
    public void removeChangeListener(ChangeListener listener) {listeners.remove(listener);}

    /**
     * This method is public as an implementation side effect and should never be called directly.
     */
    public void stateChanged(ChangeEvent e) {
        Iterator    iterator;
        ChangeEvent event;

        // Updates the chooser's value.
        if(e.getSource() == spinner)
            slider.setValue(((Integer)spinner.getValue()).intValue());
        else if(e.getSource() == slider)
            spinner.setValue(new Integer(slider.getValue()));

        // Notifies listeners.
        event    = new ChangeEvent(this);
        iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ChangeListener)iterator.next()).stateChanged(event);
    }
}
