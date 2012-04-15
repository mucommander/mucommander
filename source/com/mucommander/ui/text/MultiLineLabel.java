/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * MultiLineLabel is a line-wrapping label that spawns the text over multiple lines as necessary. Unlike what its name
 * suggests, it is derived from <code>JTextArea</code> and not <code>JLabel</code>, but it looks just like a label.
 * When added to a container, this component takes just the amount of space it needs, without the need to set a fixed
 * number of rows or columns.
 *
 * @author Maxence Bernard
 */
public class MultiLineLabel extends JTextArea {

    /**
     * Equivalent to calling {@link #MultiLineLabel(String, boolean)} with auto-repack enabled. 
     *
     * @param text the initial label's text
     */
    public MultiLineLabel(String text) {
        this(text, true);
    }

    /**
     * Creates a new <code>MultiLineLabel</code>, spawning over multiple lines as necessary. By default, lines are
     * wrapped at word boundaries, i.e. words are not split over multiple lines. This behavior can be changed by
     * calling {@link #setWrapStyleWord(boolean)}.
     * <p>
     * The <code>autoRepack</code> parameter allows to automatically issue an extra call to the <code>pack()</code>
     * method of the Window that contains this component, for the window to be layed out properly. This works around a
     * well-known bug that affects line-wrapping text components which report an incorrect preferred size, causing
     * layout issues. This parameter should be always enabled unless a fixed number of rows or columns is set using
     * {@link #setRows(int)} or {@link #setColumns(int)}.</br>
     * For reference, here are links to the afore-mentionned issue:
     * <ul>
     *  <li>http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4924163</li>
     *  <li>http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4446522</li>
     * </ul>
     * </p>
     *
     * @param text the initial label's text
     * @param autoRepack if <code>true</code>, an extra call to the <code>pack()</code> method of the Window that
     * contains this component will be automatically issued after this component has first been layed out.
     */
    public MultiLineLabel(String text, boolean autoRepack) {
        super(text);
        setEditable(false);
        setLineWrap(true);
        setWrapStyleWord(true);

        // Make this text area look like a label
        setOpaque(false);
        setBackground((Color) UIManager.get("Label.background"));
        setForeground((Color) UIManager.get("Label.foreground"));
        setFont((Font) UIManager.get("Label.font"));

        if(autoRepack) {
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    Container tla = getTopLevelAncestor();
                    if(tla instanceof Window)
                        ((Window)tla).pack();

                    removeComponentListener(this);
                }
            });
        }
    }
}
