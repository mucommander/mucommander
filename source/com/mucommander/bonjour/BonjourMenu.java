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

package com.mucommander.bonjour;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * An abstract JMenu that contains an item for each Bonjour service available
 * (as returned {@link BonjourDirectory#getServices()} displaying the Bonjour service's name. When an item is clicked,
 * the action returned by {@link #getMenuItemAction(BonjourService)} is returned.
 *
 * <p>Note: the items list is refreshed each time the menu is selected. In other words, a new instance of BonjourMenu
 * does not have to be created in order to see new Bonjour services.</p>
 *
 * @author Maxence Bernard
 */
public abstract class BonjourMenu extends JMenu implements MenuListener {

    /**
     * Creates a new instance of <code>BonjourMenu</code>.
     */
    public BonjourMenu() {
        super(Translator.get("bonjour.bonjour_services"));

        setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, "bonjour.png"));

        // Menu items will be added when menu gets selected
        addMenuListener(this);
    }


    /**
     * Returns the action to perform for the given {@link BonjourService}. This method is called for every
     * BonjourService available when this menu is selected.
     *
     * @param bs the BonjourService
     * @return the action to perform for the given BonjourService
     */
    public abstract MuAction getMenuItemAction(BonjourService bs);


    /////////////////////////////////
    // MenuListener implementation //
    /////////////////////////////////

    public void menuSelected(MenuEvent menuEvent) {
        // Remove previous menu items (if any)
        removeAll();

        if(BonjourDirectory.isActive()) {
            BonjourService services[] = BonjourDirectory.getServices();
            int nbServices = services.length;

            if(nbServices>0) {
                // Add a menu item for each Bonjour service.
                // When clicked, the corresponding URL will opened in the active table.
                JMenuItem menuItem;
                MnemonicHelper mnemonicHelper = new MnemonicHelper();

                for(int i=0; i<nbServices; i++) {
                    menuItem = new JMenuItem(getMenuItemAction(services[i]));
                    menuItem.setMnemonic(mnemonicHelper.getMnemonic(menuItem.getText()));

                    add(menuItem);
                }
            }
            else {
                // Inform that no service have been discovered
                add(new JMenuItem(Translator.get("bonjour.no_service_discovered"))).setEnabled(false);
            }
        }
        else {
            // Inform that Bonjour support has been disabled
            add(new JMenuItem(Translator.get("bonjour.bonjour_disabled"))).setEnabled(false);
        }
    }

    public void menuDeselected(MenuEvent menuEvent) {
    }

    public void menuCanceled(MenuEvent menuEvent) {
    }
}
