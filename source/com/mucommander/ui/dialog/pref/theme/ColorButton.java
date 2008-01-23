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

package com.mucommander.ui.dialog.pref.theme;

import com.mucommander.ui.chooser.*;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author Maxence Bernard, Nicolas Rinaudo
 */
class ColorButton extends JPanel implements ActionListener, ColorChangeListener {

    // - Instance variables -----------------------------------------------------
    // --------------------------------------------------------------------------

    /** ThemeData from which to retrieve the color chooser's values. */
    private ThemeData         themeData;

    /** Identifier of the color that's being edited. */
    private int               colorId;

    /** Dialog on which the color chooser should be centered and modal to. */
    private PreferencesDialog parent;

    /** The preview component that is repainted when the current color changes (can be null) */
    private JComponent previewComponent;

    /** Name of the preview component's property that gets updated with the current color of this button (can be null) */
    private String previewColorPropertyName;

    private Vector updatedPreviewComponents;

    /** Button's border. */
    private Border border = BorderFactory.createEtchedBorder();

    /** The color button */
    private JButton button;

    /** Current color displayed in the button */
    private Color currentColor;


    public ColorButton(PreferencesDialog parent, ThemeData themeData, int colorId) {
        this(parent, themeData, colorId, null, null);
    }

    public ColorButton(PreferencesDialog parent, ThemeData themeData, int colorId, String previewColorPropertyName) {
        this(parent, themeData, colorId, previewColorPropertyName, null);
    }

    public ColorButton(PreferencesDialog parent, ThemeData themeData, int colorId, String previewColorPropertyName, JComponent previewComponent) {
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        setLayout(flowLayout);

        this.themeData                = themeData;
        this.colorId                  = colorId;
        this.parent                   = parent;
        this.previewComponent         = previewComponent;
        this.previewColorPropertyName = previewColorPropertyName;

        if(previewColorPropertyName != null && previewComponent != null)
            addUpdatedPreviewComponent(previewComponent);
 
        button = new JButton() {
                public Dimension getPreferredSize() {return new Dimension(70, 30);}

                public void paint(Graphics g) {
                    int width  = getWidth();
                    int height = getHeight();

                    // Fill the button with the specified color
                    g.setColor((ColorButton.this).currentColor);
                    g.fillRect(0, 0, width, height);

                    // Paint custom border
                    border.paintBorder(this, g, 0, 0, width, height);
                }
            };

        button.addActionListener(this);
        button.setBorderPainted(true);

        add(button);

        // Add a ColorPicker only if this component is supported by the current environment
        if(ColorPicker.isSupported()) {
            ColorPicker colorPicker = new ColorPicker();
            colorPicker.addColorChangeListener(this);
            add(colorPicker);
        }

        setCurrentColor(themeData.getColor(colorId));
    }


    void addUpdatedPreviewComponent(JComponent previewComponent) {
        if(previewColorPropertyName == null)
            return;

        if(updatedPreviewComponents == null)
            updatedPreviewComponents = new Vector();

        updatedPreviewComponents.add(previewComponent);

        previewComponent.putClientProperty(previewColorPropertyName, currentColor);
    }

    private void setCurrentColor(Color color) {
        currentColor = color;
        if(themeData.isColorDifferent(colorId, currentColor))
            themeData.setColor(colorId, currentColor);
        button.repaint();

        if(updatedPreviewComponents != null && previewColorPropertyName != null) {
            int nbPreviewComponents = updatedPreviewComponents.size();
            for(int i = 0; i < nbPreviewComponents; i++)
                ((JComponent)updatedPreviewComponents.elementAt(i)).putClientProperty(previewColorPropertyName, color);
        }
    }


    private ColorChooser createColorChooser() {
        if(previewComponent!=null && previewColorPropertyName!=null && (previewComponent instanceof PreviewLabel)) {
            try {return new ColorChooser(currentColor, (PreviewLabel)((PreviewLabel)previewComponent).clone(), previewColorPropertyName);}
            catch(CloneNotSupportedException e) {}
        }
        return new ColorChooser(currentColor, new PreviewLabel(), PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        ColorChooser chooser;
        ColorChooser.createDialog(parent, chooser = createColorChooser()).showDialog();

        setCurrentColor(chooser.getColor());
    }


    ////////////////////////////////////////
    // ColorChangeListener implementation //
    ////////////////////////////////////////

    public void colorChanged(ColorChangeEvent event) {setCurrentColor(event.getColor());}
}
