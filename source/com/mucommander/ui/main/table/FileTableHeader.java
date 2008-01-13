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

package com.mucommander.ui.main.table;

import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.*;

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

    
    //////////////////////////////////
    // MouseListener implementation //
    //////////////////////////////////

    public void mouseClicked(MouseEvent e) {
        int colNum = table.convertColumnIndexToModel(getColumnModel().getColumnIndexAtX(e.getX()));

        table.requestFocus();

        // One of the table headers was left-clicked, sort the table by the clicked column's criterion
        if(PlatformManager.isLeftMouseButton(e)) {
            // If the table was already sorted by this criteria, reverse order
            if (table.getSortByCriteria()==colNum)
                table.reverseSortOrder();
            else
                table.sortBy(colNum);
        }
        // One of the table headers was right-clicked, popup a menu that offers to hide the column
        else if(PlatformManager.isRightMouseButton(e)) {
            Class hideActionClass;
            switch(colNum) {
                case Columns.EXTENSION:
                    hideActionClass = ToggleExtensionColumnAction.class;
                    break;
                case Columns.SIZE:
                    hideActionClass = ToggleSizeColumnAction.class;
                    break;
                case Columns.DATE:
                    hideActionClass = ToggleDateColumnAction.class;
                    break;
                case Columns.PERMISSIONS:
                    hideActionClass = TogglePermissionsColumnAction.class;
                    break;
                case Columns.OWNER:
                    hideActionClass = ToggleOwnerColumnAction.class;
                    break;
                case Columns.GROUP:
                    hideActionClass = ToggleGroupColumnAction.class;
                    break;
                default:        // Name column cannot be hidden
                    return;
            }

            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem item = new JMenuItem(ActionManager.getActionInstance(hideActionClass, table.getFolderPanel().getMainFrame()));
            item.setText(Translator.get("table.hide_column"));
            popupMenu.add(item);
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

    public boolean getReorderingAllowed() {return true;}
}
