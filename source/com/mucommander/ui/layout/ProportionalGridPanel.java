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

package com.mucommander.ui.layout;

import javax.swing.*;
import java.awt.*;

/**
 * @author Maxence Bernard
 */
public class ProportionalGridPanel extends JPanel {

    private int nbColumns;
    private GridBagConstraints gbc;

    public ProportionalGridPanel(int nbColumns) {
        super(new GridBagLayout());
        this.nbColumns = nbColumns;

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 3, 2, 3);

//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.ipadx = 0;
//        gbc.ipady = 0;
//        gbc.weightx = 0;
//        gbc.weighty = 0;
//        gbc.fill = GridBagConstraints.NONE;

        gbc.anchor = GridBagConstraints.WEST;
    }


    public Component add(Component component) {
        add(component, gbc);

        if(gbc.gridx<nbColumns-1)
            gbc.gridx++;
        else {
            gbc.gridy++;
            gbc.gridx = 0;
        }

        return component;
    }
}
