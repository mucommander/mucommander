/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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


package com.mucommander.ui.progress;

import javax.swing.*;
import java.awt.*;


/**
 * A text fields which can display progress information, a la Mac OS X Safari's location bar, filling a portion of the
 * text field's background with a specified color.
 *
 * @author Maxence Bernard
 */
public class ProgressTextField extends JTextField {

    /** Progress value, between 0 and 100 */
    private int progressValue;

    /** Background color used to symbolize progress */
    private Color progressColor;

    /**
     * Creates a new ProgressTextField, using the given initial progress value
     * and progress color which will be used as background color to show progress.
     *
     * @param initialProgressValue initial progress value, between 0 and 100
     * @param progressColor background color used to symbolize progress
     */
    public ProgressTextField(int initialProgressValue, Color progressColor) {
        this.progressValue = initialProgressValue;
        this.progressColor = progressColor;
    }
	
	
    /**
     * Sets current progress value and repaints this component.
     *
     * @param value current progress value, between 0 and 100.
     */
    public void setProgressValue(int value) {
        this.progressValue = value;
        repaint();
    }

    /**
     * Returns current progress value, as displayed on the component.
     *
     * @return current progress value
     */
    public int getProgressValue() {
        return progressValue;
    }

    /**
     * Sets the color used to represent progress.
     * @param color new progress color.
     */
    public void setProgressColor(Color color) {
        if(color != null && progressColor != null && !color.equals(progressColor)) {
            progressColor = color;
            repaint();
        }
    }
	
    /**
     * Override JTextField's paint method to show progress information.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if(progressValue>0) {
            g.setColor(progressColor);
            g.fillRect(0, 0, (int)(getWidth()*progressValue/(float)100), getHeight());
        }
    }
}
