/*
 * @(#)DefaultOverlayable.java 7/31/2007
 *
 * Copyright 2002 - 2007 JIDE Software Inc. All rights reserved.
 */
package com.mucommander.ui.main.table;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <code>DefaultOverlayable</code> is the default implementation of <code>Overlayable</code> using JPanel as the base
 * component.
 *
 * This file that is based on the one provided by JIDE Software as part of JIDE Common Layer
 * has been modified to include all its dependencies so it can be used as a standalone resource.
 * This change was done by Arik Hadas, 12/12/2016.
 */
public class DefaultOverlayable extends JPanel implements SwingConstants, ComponentListener {
    public static final String CLIENT_PROPERTY_OVERLAYABLE = "Overlayable.overlayable";

    private JComponent _actualComponent;
    private Insets _overlayLocationInsets = new Insets(0, 0, 0, 0);
    private List<JComponent> _overlayComponents;
    private Map<JComponent, Integer> _overlayLocations;
//    private Map<JComponent, Component> _overlayRelativeComponent;

    public DefaultOverlayable() {
        initComponents();
    }

    public DefaultOverlayable(JComponent component) {
        initComponents();
        setActualComponent(component);
    }

    public DefaultOverlayable(JComponent actualComponent, JComponent overlayComponent, int overlayLocation) {
        initComponents();
        setActualComponent(actualComponent);
        addOverlayComponent(overlayComponent, overlayLocation);
    }

    public DefaultOverlayable(JComponent actualComponent, JComponent overlayComponent) {
        initComponents();
        setActualComponent(actualComponent);
        addOverlayComponent(overlayComponent, SwingConstants.CENTER);
    }

    private void initComponents() {
        setLayout(null);
        _overlayComponents = new Vector();
        _overlayLocations = new Hashtable();
//        _overlayRelativeComponent = new Hashtable();
    }

    /**
     * Override to consider the overlayLocationInsets. If the overlayLocationInsets's edges are positive number, we will
     * increase the preferred size so that the overlayout component can be shown. If they are negative, we will still
     * keep the super.getPreferredSize.
     *
     * @return the preferred size of the DefaultOverlayable.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension size = getActualComponent() == null ? new Dimension(0, 0) : getActualComponent().getPreferredSize();
        Insets insets = getOverlayLocationInsets();
        if (insets != null) {
            size.width += Math.max(0, insets.left) + Math.max(0, insets.right);
            size.height += Math.max(0, insets.top) + Math.max(0, insets.bottom);
        }
        return size;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        Insets insets = getOverlayLocationInsets();
        x = Math.max(0, insets.left);
        y = Math.max(0, insets.top);
        width -= Math.max(0, insets.left) + Math.max(0, insets.right);
        height -= Math.max(0, insets.top) + Math.max(0, insets.bottom);
        getActualComponent().setBounds(x, y, width, height);

        updateLocation();
    }

    private void updateLocation() {
        JComponent[] components = getOverlayComponents();
        for (JComponent c : components) {
            if (c == null) {
                return;
            }

            if (c.isVisible()) {
                Rectangle r = getOverlayComponentBounds(c);
                c.setBounds(r);
            }
        }
    }

    private Rectangle getOverlayComponentBounds(JComponent component) {
//        Component relativeComponent = getOverlayRelativeComponent(component);
//        if (relativeComponent == null) {
//            relativeComponent = getActualComponent();
//        }
        Component relativeComponent = getActualComponent();

        Rectangle bounds = relativeComponent.getBounds();
        if (relativeComponent != getActualComponent()) {
            bounds = SwingUtilities.convertRectangle(relativeComponent.getParent(), bounds, getActualComponent());
        }
        Rectangle overlayBounds = new Rectangle(bounds);
        Insets insets = getOverlayLocationInsets();
        overlayBounds.x -= insets.left;
        overlayBounds.y -= insets.top;
        overlayBounds.width += insets.left + insets.right;
        overlayBounds.height += insets.top + insets.bottom;

        int cx = 0;
        int cy = 0;

        Dimension size = component.getPreferredSize();
        int cw = size.width;
        int ch = size.height;

        switch (getOverlayLocation(component)) {
            case CENTER:
                cx = bounds.x + (bounds.width - cw) / 2;
                cy = bounds.y + (bounds.height - ch) / 2;
                break;
            case NORTH:
                cx = bounds.x + (bounds.width - cw) / 2;
                cy = overlayBounds.y;
                break;
            case SOUTH:
                cx = bounds.x + (bounds.width - cw) / 2;
                cy = overlayBounds.y + overlayBounds.height - ch;
                break;
            case WEST:
                cx = overlayBounds.x;
                cy = bounds.y + (bounds.height - ch) / 2;
                break;
            case EAST:
                cx = overlayBounds.x + overlayBounds.width - cw;
                cy = bounds.y + (bounds.height - ch) / 2;
                break;
            case NORTH_WEST:
                cx = overlayBounds.x;
                cy = overlayBounds.y;
                break;
            case NORTH_EAST:
                cx = overlayBounds.x + overlayBounds.width - cw;
                cy = overlayBounds.y;
                break;
            case SOUTH_WEST:
                cx = overlayBounds.x;
                cy = overlayBounds.y + overlayBounds.height - ch;
                break;
            case SOUTH_EAST:
                cx = overlayBounds.x + overlayBounds.width - cw;
                cy = overlayBounds.y + overlayBounds.height - ch;
                break;
        }

        return new Rectangle(cx, cy, cw, ch);
    }

    public int getOverlayLocation(JComponent component) {
        Integer location = _overlayLocations.get(component);
        if (location != null) {
            return location;
        }
        else {
            return -1;
        }
    }

//    public Component getOverlayRelativeComponent(JComponent component) {
//        Component c = _overlayRelativeComponent.get(component);
//        if (c != null) {
//            return c;
//        }
//        else {
//            return null;
//        }
//    }

    public void setOverlayLocation(JComponent component, int location) {
        setOverlayLocation(component, null, location);
    }

    private void setOverlayLocation(JComponent component, Component relativeComponent, int location) {
        boolean updated = false;
        int old = getOverlayLocation(component);
        if (old != location) {
            _overlayLocations.put(component, location);
            updated = true;
        }
//        Component oldComponent = getOverlayRelativeComponent(component);
//        if (oldComponent != relativeComponent) {
//            _overlayRelativeComponent.put(component, relativeComponent);
//            if(!JideSwingUtilities.isListenerRegistered(component, ComponentListener.class, this)) {
//                component.addComponentListener(this);
//            }
//            updated = true;
//        }
        if (updated) {
            updateLocation();
        }
    }

    public void addOverlayComponent(JComponent component) {
        addOverlayComponent(component, SwingConstants.CENTER, -1);
    }

    public void addOverlayComponent(JComponent component, int location) {
        addOverlayComponent(component, location, -1);
    }

    public void addOverlayComponent(JComponent component, int location, int index) {
        addOverlayComponent(component, null, location, index);
    }

    private void addOverlayComponent(JComponent component, Component relativeComponent, int location, int index) {
        if (_overlayComponents.contains(component)) {
            _overlayComponents.remove(component);
        }
        if (index == -1) {
            _overlayComponents.add(component);
            add(component, getComponentCount() - 1); // add it before the the actual component
        }
        else {
            _overlayComponents.add(index, component);
            add(component, index);
        }
        setOverlayLocation(component, relativeComponent, location);
    }

    public void removeOverlayComponent(JComponent component) {
        if (_overlayComponents.contains(component)) {
            _overlayComponents.remove(component);
            _overlayLocations.remove(component);
//            _overlayRelativeComponent.remove(component);
            remove(component);
        }
    }

    public JComponent[] getOverlayComponents() {
        return _overlayComponents.toArray(new JComponent[_overlayComponents.size()]);
    }

    public JComponent getActualComponent() {
        return _actualComponent;
    }

    public void setActualComponent(JComponent actualComponent) {
        if (_actualComponent != null) {
            remove(_actualComponent);
            _actualComponent.putClientProperty(CLIENT_PROPERTY_OVERLAYABLE, null);
        }
        _actualComponent = actualComponent;
        _actualComponent.putClientProperty(CLIENT_PROPERTY_OVERLAYABLE, this);
        add(_actualComponent);
        Container container = getParent();
        if (container != null) {
            invalidate();
            container.validate();
        }
    }


    public Insets getOverlayLocationInsets() {
        return _overlayLocationInsets;
    }

    public void setOverlayLocationInsets(Insets overlayLocationInsets) {
        _overlayLocationInsets = overlayLocationInsets;
        Container container = getParent();
        if (container != null) {
            invalidate();
            container.validate();
        }
    }


    public void setOverlayVisible(boolean visible) {
        JComponent[] components = getOverlayComponents();
        for (JComponent component : components) {
            component.setVisible(visible);
        }
    }

    public void componentResized(ComponentEvent e) {
        updateLocation();
    }

    public void componentMoved(ComponentEvent e) {
        updateLocation();
    }

    public void componentShown(ComponentEvent e) {
        updateLocation();
    }

    public void componentHidden(ComponentEvent e) {
        updateLocation();
    }
}