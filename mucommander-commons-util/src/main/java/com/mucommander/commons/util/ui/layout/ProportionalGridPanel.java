/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.commons.util.ui.layout;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

/**
 * @author Maxence Bernard
 */
public class ProportionalGridPanel extends JPanel {

    private int nbColumns;
    private GridBagConstraints gbc;

    public ProportionalGridPanel(int nbColumns) {
        this(nbColumns, getDefaultGridBagConstraints());
    }

    public ProportionalGridPanel(int nbColumns, GridBagConstraints gbc) {
        super(new GridBagLayout());
        this.nbColumns = nbColumns;
        this.gbc = gbc;
    }

    public static GridBagConstraints getDefaultGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
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
        return gbc;
    }

    @Override
    public Component add(Component component) {
        add(component, gbc);

        gbc.gridx = ++gbc.gridx % nbColumns;
        if (gbc.gridx == 0)
            gbc.gridy++;

        return component;
    }
}
