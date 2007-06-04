package com.mucommander.ui.theme;

import java.awt.Color;

public class ColorChangedEvent {
    private Theme source;
    private int   colorId;
    private Color  color;

    ColorChangedEvent(Theme source, int colorId, Color color) {
        this.source = source;
        this.colorId = colorId;
        this.color   = color;
    }

    public Theme getSource() {return source;}
    public int getColorId() {return colorId;}
    public Color getColor() {return color;}
}
