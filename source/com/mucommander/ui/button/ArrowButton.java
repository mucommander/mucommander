/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.button;

import com.mucommander.ui.icon.IconManager;

import javax.swing.*;

/**
 * ArrowButton is a button displaying an arrow icon pointing to a specified direction (up/down/left/right).
 * The direction of the arrow can be changed at any time using {@link #setArrowDirection(int)}.
 *
 * @author Maxence Bernard
 */
public class ArrowButton extends JButton {

    public final static int UP_DIRECTION = 0;
    public final static int DOWN_DIRECTION = 1;
    public final static int LEFT_DIRECTION = 2;
    public final static int RIGHT_DIRECTION = 3;

    private final static String ICONS[] = {
        "arrow_up.png",
        "arrow_down.png",
        "arrow_left.png",
        "arrow_right.png"
    };


    /**
     * Creates a new ArrowButton with no initial arrow icon.
     */
    public ArrowButton() {
    }

    /**
     * Creates a new ArrowButton showing an arrow icon pointing to the specified direction.
     */
    public ArrowButton(int direction) {
        setArrowDirection(direction);
    }

    /**
     * Creates a new ArrowButton using the specified Action and showing an arrow icon pointing to the specified direction.
     */
    public ArrowButton(Action action, int direction) {
        super(action);

        setArrowDirection(direction);
    }


    /**
     * Changes the direction of the arrow icon to the specified one.
     *
     * @param direction can have one of the following values: {@link #UP_DIRECTION}, {@link #DOWN_DIRECTION},
     * {@link #LEFT_DIRECTION} or {@link #RIGHT_DIRECTION}
     */
    public void setArrowDirection(int direction) {
        setIcon(IconManager.getIcon(IconManager.COMMON_ICON_SET, ICONS[direction]));
    }
}
