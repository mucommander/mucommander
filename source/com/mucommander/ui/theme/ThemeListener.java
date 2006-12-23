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
     * Note that the color parameter might be set to <code>null</code>.
     * This means that the corresponding color is the system default.
     * </p>
     * @param colorId identifier of the color that has changed.
     * @param color   new value for the color.
     */
    public void colorChanged(int colorId, Color color);

    /**
     * Notifies the listener that a font has been changed.
     * <p>
     * Note that the font parameter might be set to <code>null</code>.
     * This means that the corresponding font is the system default.
     * </p>
     * @param fontId identifier of the font that has changed.
     * @param font   new value for the font.
     */
    public void fontChanged(int fontId, Font font);
}
