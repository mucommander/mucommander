package com.mucommander.ui.theme;

import com.mucommander.Debug;

import java.awt.Font;
import java.awt.Color;

/**
 * Class used to store raw theme data.
 * @author Nicolas Rinaudo
 */
class ThemeData {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Colors known to the theme. */
    private Color[] colors;
    /** Fonts known to the theme. */
    private Font[]  fonts;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new instance of theme data.
     */
    public ThemeData() {
        colors = new Color[Theme.COLOR_COUNT];
        fonts  = new Font[Theme.FONT_COUNT];
    }



    // - Data access ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the specified font.
     * @param  id                       identifier of the font to set.
     * @param  font                     new font for the specified id.
     * @throws IllegalArgumentException if <code>id</code> is not a legal font id.
     */
    public void setFont(int id, Font font) {
        // Makes sure the font id is legal (only in Debug mode).
        if(Debug.ON && (id < 0 || id >= Theme.FONT_COUNT)) {
            Debug.trace("Illegal font id: " + id);
            throw new IllegalArgumentException();
        }
        fonts[id] = font;
    }

    /**
     * Sets the specified color.
     * @param  id                       identifier of the color to set.
     * @param  color                    new color for the specified id.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color id.
     */
    public void setColor(int id, Color color) {
        // Makes sure the color id is legal (only in Debug mode).
        if(Debug.ON && (id < 0 || id >= Theme.COLOR_COUNT)) {
            Debug.trace("Illegal color id: " + id);
            throw new IllegalArgumentException();
        }
        colors[id] = color;
    }

    /**
     * Returns the data's requested font.
     * <p>
     * If the theme this data is describing doesn't use a custom value for the
     * specified font, this method will return null.
     * </p>
     * @param  id                       identifier of the font to retrieve.
     * @return                          the requested font if it exists, <code>null</code> otherwise.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color id.
     */
    public Font getFont(int id) {
        // Makes sure the font id is legal (only in Debug mode).
        if(Debug.ON && (id < 0 || id >= Theme.FONT_COUNT)) {
            Debug.trace("Illegal font id: " + id);
            throw new IllegalArgumentException();
        }
        return fonts[id];
    }

    /**
     * Returns the data's requested color.
     * <p>
     * If the theme this data is describing doesn't use a custom value for the
     * specified color, this method will return null.
     * </p>
     * @param  id                       identifier of the color to retrieve.
     * @return                          the requested color if it exists, <code>null</code> otherwise.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color id.
     */
    public Color getColor(int id) {
        // Makes sure the color id is legal (only in Debug mode).
        if(Debug.ON && (id < 0 || id >= Theme.COLOR_COUNT)) {
            Debug.trace("Illegal color id: " + id);
            throw new IllegalArgumentException();
        }
        return colors[id];
    }
}
