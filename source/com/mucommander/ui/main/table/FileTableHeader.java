/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.ui.main.table;

import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.impl.ToggleAutoSizeAction;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * @author Maxence Bernard
 */
public class FileTableHeader extends JTableHeader implements MouseListener {

    private FileTable table;

    public FileTableHeader(FileTable table) {
        super(table.getColumnModel());

        this.table = table;
        addMouseListener(this);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public boolean getReorderingAllowed() {
        return true;
    }

    
    //////////////////////////////////
    // MouseListener implementation //
    //////////////////////////////////

    public void mouseClicked(MouseEvent e) {
        int colNum = table.convertColumnIndexToModel(getColumnModel().getColumnIndexAtX(e.getX()));

        table.requestFocus();

        // One of the table headers was left-clicked, sort the table by the clicked column's criterion
        if(DesktopManager.isLeftMouseButton(e)) {
            // If the table was already sorted by this criteria, reverse order
            if (table.getSortInfo().getCriterion()==colNum)
                table.reverseSortOrder();
            else
                table.sortBy(colNum);
        }
        // One of the table headers was right-clicked, popup a menu that offers to hide the column
        else if(DesktopManager.isRightMouseButton(e)) {
            JPopupMenu popupMenu = new JPopupMenu();
            MainFrame mainFrame = table.getFolderPanel().getMainFrame();

            String toggleColumnActionIds[] = Columns.getToggleColumnActions();
            JCheckBoxMenuItem checkboxMenuItem;
            int nbToggleColumnActions = toggleColumnActionIds.length;
            for(int i=0; i<nbToggleColumnActions; i++) {
                if(i==Columns.NAME)
                    continue;

                checkboxMenuItem = new JCheckBoxMenuItem(ActionManager.getActionInstance(toggleColumnActionIds[i], mainFrame));

                checkboxMenuItem.setSelected(table.isColumnEnabled(i));
                checkboxMenuItem.setEnabled(table.isColumnDisplayable(i));

                popupMenu.add(checkboxMenuItem);
            }

            popupMenu.add(new JSeparator());

            checkboxMenuItem = new JCheckBoxMenuItem(ActionManager.getActionInstance(ToggleAutoSizeAction.Descriptor.ACTION_ID, mainFrame));
            checkboxMenuItem.setSelected(mainFrame.isAutoSizeColumnsEnabled());
            popupMenu.add(checkboxMenuItem);

            popupMenu.show(this, e.getX(), e.getY());
            popupMenu.setVisible(true);
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
