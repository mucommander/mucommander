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
     */
    public void colorChanged(ColorChangedEvent event);

    /**
     * Notifies the listener that a font has been changed.
     */
    public void fontChanged(FontChangedEvent event);
}
