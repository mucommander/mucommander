package com.mucommander.ui.theme;

import java.awt.Color;
import java.awt.Font;

/**
 * Implementations of this interface can listen to changes in the current theme.
 * @author Nicolas Rinaudo
 */
public interface ThemeListener {
    /**
     * Notifies the listener that a color has been changed.
     * <p>
     * Note that the color parameters might be set to <code>null</code>.
     * This means that the corresponding color is the system default.
     * </p>
     * <p>
     * This method is guaranteed to only be called if <code>oldColor</code>
     * and <code>newColor</code> actually differ.
     * </p>
     * @param colorId  identifier of the color that has changed.
     * @param oldColor old value for the color.
     * @param newColor new value for the color.
     */
    public void colorChanged(int colorId, Color oldColor, Color newColor);

    /**
     * Notifies the listener that a font has been changed.
     * <p>
     * Note that the font parameters might be set to <code>null</code>.
     * This means that the corresponding font is the system default.
     * </p>
     * <p>
     * This method is guaranteed to only be called if <code>oldFont</code>
     * and <code>newFont</code> actually differ.
     * </p>
     * @param fontId  identifier of the font that has changed.
     * @param oldFont old value for the font.
     * @param newFont new value for the font.
     */
    public void fontChanged(int fontId, Font oldFont, Font newFont);
}
