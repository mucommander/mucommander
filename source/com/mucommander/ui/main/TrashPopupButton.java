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

package com.mucommander.ui.main;

import com.mucommander.file.AbstractTrash;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.EmptyTrashAction;
import com.mucommander.ui.action.OpenTrashAction;
import com.mucommander.ui.button.PopupButton;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;

/**
 * TrashPopupButton is a button that allows to interact with the current platform's trash, as returned by
 * {@link com.mucommander.file.FileFactory#getTrash()}.
 * When the button is clicked, a popup menu is displayed, allowing to perform a choice of actions such as opening
 * the trash or emptying it.
 * Note that this button will only be functional if a trash is avaiable on the current platform. 
 *
 * @author Maxence Bernard
 */
public class TrashPopupButton extends PopupButton {

    private MainFrame mainFrame;

    /** Holds a reference to the RolloverButtonAdapter instance so that it doesn't get garbage-collected */
    private RolloverButtonAdapter rolloverButtonAdapter;

    public TrashPopupButton(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setIcon(IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, "trash.png"));

        // Rollover-enable the button and hold a reference to the RolloverButtonAdapter instance so that it doesn't
        // get garbage-collected
        rolloverButtonAdapter = new RolloverButtonAdapter();
        RolloverButtonAdapter.setButtonDecoration(this);
        addMouseListener(rolloverButtonAdapter);
    }

    public JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        AbstractTrash trash = FileFactory.getTrash();
        if(trash!=null) {
            if(trash.canOpen())
                popupMenu.add(ActionManager.getActionInstance(OpenTrashAction.class, mainFrame));

            if(trash.canEmpty()) {
                JMenuItem emptyTrashItem = new JMenuItem(ActionManager.getActionInstance(EmptyTrashAction.class, mainFrame));

                // Retrieve the number of items that the trash contains, -1 if this information is not available.
                int itemCount = trash.getItemCount();
                if(itemCount==0) {
                    // Disable the 'empty trash' action if the trash contains no item
                    emptyTrashItem.setEnabled(false);
                }
                else if(itemCount>0) {
                    // Append the number of items to the menu item's label
                    emptyTrashItem.setText(emptyTrashItem.getText()+" ("+itemCount+")");
                }
                // Note: 'empty trash' is enabled if itemCount==-1

                popupMenu.add(emptyTrashItem);
            }
        }

        return popupMenu;
    }
}
