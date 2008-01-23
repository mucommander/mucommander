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

import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * @author Maxence Bernard
 */
public class ColorPicker extends JButton implements ActionListener, AWTEventListener {

    private Robot robot;
    private boolean isActive;

    private WeakHashMap listeners = new WeakHashMap();

    /** True if this component is supported (java.awt.Robot can be used) */
    private static boolean isSupported;


    static {
        try {
            new Robot();
            isSupported = true;
        }
        catch(Exception e) {
            // java.awt.Robot constructor throws an AWTException "if the platform configuration does not allow low-level input control."
            // In this case, isSupported will be false
        }
    }

    public ColorPicker() {
        super(IconManager.getIcon(IconManager.COMMON_ICON_SET, "picker.png"));
        addActionListener(this);
    }


    public static boolean isSupported() {
        return isSupported;
    }


    public void setActive(boolean active) {
        if(active==isActive)
            return;

        final Toolkit toolkit = Toolkit.getDefaultToolkit();

        if(active) {
            if(!isVisible())
                return;

            try {
                // Create a java.awt.Robot operating on the screen device that contains the window this component is in.
                // Not sure what happens if the window spawns across 2 screens...
                robot = new Robot(getTopLevelAncestor().getGraphicsConfiguration().getDevice());
            }
            catch(Exception e) {
                // If Robot is not available, ColorPicker will simply be ineffective, clicking it won't do anything
                return;
            }

            // Change the cursor to the 'eye dropper' icon filled with white
            setPickerCursor(Color.WHITE);

            // These are invoked after all pending events are processed
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // Listen to all mouse events on the window that contains this button 
                    toolkit.addAWTEventListener(ColorPicker.this, AWTEvent.MOUSE_MOTION_EVENT_MASK|AWTEvent.MOUSE_EVENT_MASK);

                    // Leave this button selected until a color is picked or this button is pressed again (to cancel) 
                    setSelected(true);
                }
            });
        }
        else {
            // Stop listening to mouse events
            toolkit.removeAWTEventListener(this);

            // Restore default cursor
            setCustomCursor(Cursor.getDefaultCursor());

            // Make the button unselected
            setSelected(false);
        }

        this.isActive = active;
    }


    public boolean isActive() {
        return isActive;
    }

    public void addColorChangeListener(ColorChangeListener listener) {
        listeners.put(listener, null);
    }

    public void removeColorChangeListener(ColorChangeListener listener) {
        listeners.remove(listener);
    }


    private void setPickerCursor(Color fillColor) {
        ImageIcon cursorIcon = (ImageIcon)getIcon();
        int iconWidth = cursorIcon.getIconWidth();
        int iconHeight = cursorIcon.getIconHeight();
        int colorRGB  = fillColor.getRGB();

        // Retrieve the cursor icon fill mask as an alpha-enabled BufferedImage
        BufferedImage iconMaskBi = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = iconMaskBi.getGraphics();
        g.drawImage(IconManager.getIcon(IconManager.COMMON_ICON_SET, "picker_mask.png").getImage(), 0, 0, null);

        // Replace solid (non-transparent) pixels with specified fill color
        for(int y=0; y<iconHeight; y++) {
            for(int x=0; x<iconWidth; x++) {
                int rgba = iconMaskBi.getRGB(x, y);
                if((rgba>>24)!=0)
                    iconMaskBi.setRGB(x, y, colorRGB);
            }
        }

        // Retrieve the cursor icon as an alpha-enabled BufferedImage and paint the fill mask on it
        BufferedImage iconBi = new BufferedImage(cursorIcon.getIconWidth(), cursorIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        g = iconBi.getGraphics();
        g.drawImage(cursorIcon.getImage(), 0, 0, null);
        g.drawImage(iconMaskBi, 0, 0, null);

        setCustomCursor(Toolkit.getDefaultToolkit().createCustomCursor(iconBi, new Point(0,15), getClass().getName()));
    }

    private void setCustomCursor(Cursor cursor) {
        getTopLevelAncestor().setCursor(cursor);
    }

    private void fireColorPicked(Color color) {
        // Iterate on all listeners
        Iterator iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((ColorChangeListener)iterator.next()).colorChanged(new ColorChangeEvent(this, color));
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent actionEvent) {
        setActive(!isActive);
    }


    /////////////////////////////////////
    // AWTEventListener implementation //
    /////////////////////////////////////

    public void eventDispatched(AWTEvent awtEvent) {
        if(awtEvent instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent)awtEvent;

            Point mousePoint = mouseEvent.getPoint();
            Component source = (Component)mouseEvent.getSource();

            // Convert the mouse X/Y into screen coordinates
            SwingUtilities.convertPointToScreen(mousePoint, source);

            int x = (int)mousePoint.getX();
            int y = (int)mousePoint.getY();

            // Retrieve the color of the pixel the mouse is currently over
            Color color = robot.getPixelColor(x, y);

            int button = mouseEvent.getButton();
            if(button!=MouseEvent.NOBUTTON) {
                // If left button was clicked (not released)
                if(button==MouseEvent.BUTTON1 && (mouseEvent.getModifiers()&MouseEvent.MOUSE_CLICKED)!=0) {
                    // If this color picker was clicked, cancel the color picking without firing an event
                    if(source!=this)
                        fireColorPicked(color);

                    // Consume the event so that it doesn't get caught by a clicked component
                    mouseEvent.consume();

                    // We're done
                    setActive(false);
                }

                // If any other button was clicked or if this is not a MOUSE_CLICKED event, do nothing
                return;
            }

            setPickerCursor(color);
        }
    }
}
