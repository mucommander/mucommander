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

package com.mucommander.ui.list;

import com.mucommander.text.Translator;
import com.mucommander.ui.button.ArrowButton;
import com.mucommander.util.AlteredVector;

import javax.swing.*;
import java.awt.*;

/**
 * SortableListPanel is a JPanel which contains a scrollable {@link DynamicList} in the center and two buttons
 * 'Move up' and 'Move down' buttons on the right side of the list which allow to move the items up and down and
 * easily reorder them within the list.
 *
 * @author Maxence Bernard
 */
public class SortableListPanel extends JPanel {

    private DynamicList dynamicList;


    /**
     * Creates a new SortableListPanel with a {@link DynamicList} that uses the provided items {@link com.mucommander.util.AlteredVector}.
     *
     * @param items the items Vector used by DynamicList
     */
    public SortableListPanel(AlteredVector items) {
        super(new BorderLayout());

        this.dynamicList = new DynamicList(items);

        // Allow vertical scrolling in bookmarks list
        add(new JScrollPane(dynamicList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));

        // Create 'Move item up' button
        JButton moveButton = new ArrowButton(dynamicList.getMoveUpAction(), ArrowButton.UP_DIRECTION);
        // Constrain the button's size which by default is huge under Windows/Java 1.5
        moveButton.setPreferredSize(new Dimension(19, 0));

        // Make the button non focusable so that it doesn't steal focus from the list
        moveButton.setFocusable(false);

        moveButton.setToolTipText(Translator.get("sortable_list.move_up"));
        buttonPanel.add(moveButton);

        // Create 'Move item down' button
        moveButton = new ArrowButton(dynamicList.getMoveDownAction(), ArrowButton.DOWN_DIRECTION);
        // Constrain the button's size which by default is huge under Windows/Java 1.5
        moveButton.setPreferredSize(new Dimension(19, 0));

        // Make the button non focusable so that it doesn't steal focus from the list
        moveButton.setFocusable(false);

        moveButton.setToolTipText(Translator.get("sortable_list.move_down"));
        buttonPanel.add(moveButton);

        add(buttonPanel, BorderLayout.EAST);
    }

    
    /**
     * Returns the DynamicList used by this SortableListPanel.
     */
    public DynamicList getDynamicList() {
        return dynamicList;
    }
}
