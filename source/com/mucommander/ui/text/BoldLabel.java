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

package com.mucommander.ui.text;

import javax.swing.*;
import java.awt.*;

/**
 * A JLabel with a bold font. The font is derived from the label's default font returned by {@link #getFont()}. 
 *
 * @author Maxence Bernard
 */
public class BoldLabel extends JLabel {

    public BoldLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        setBoldFont();
    }

    public BoldLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        setBoldFont();
    }

    public BoldLabel(String text) {
        super(text);
        setBoldFont();
    }

    public BoldLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        setBoldFont();
    }

    public BoldLabel(Icon image) {
        super(image);
        setBoldFont();
    }

    public BoldLabel() {
        super();
        setBoldFont();
    }

    private void setBoldFont() {
        setFont(getFont().deriveFont(Font.BOLD));
    }
}
