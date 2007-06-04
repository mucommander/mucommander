package com.mucommander.ui.theme;

import java.awt.Font;

public class FontChangedEvent {
    private Theme source;
    private int   fontId;
    private Font  font;

    FontChangedEvent(Theme source, int fontId, Font font) {
        this.source = source;
        this.fontId = fontId;
        this.font   = font;
    }

    public Theme getSource() {return source;}
    public int getFontId() {return fontId;}
    public Font getFont() {return font;}
}
