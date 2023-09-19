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

package com.mucommander.bonjour;

import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.OpenLocationAction;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

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
public class BonjourMenu extends JMenu implements MenuListener {

    private MainFrame mainFrame;
    private FolderPanel folderPanel;

    /**
     * Creates a new instance of <code>BonjourMenu</code>.
     */
    public BonjourMenu(MainFrame mainFrame, FolderPanel folderPanel) {
        super(Translator.get("bonjour.bonjour_services"));
        this.mainFrame = mainFrame;
        this.folderPanel = folderPanel;
        setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, "bonjour.png"));

        // Menu items will be added when menu gets selected
        addMenuListener(this);
        // init Bonjour
        BonjourDirectory.setActive(MuConfigurations.getPreferences()
                .getVariable(MuPreference.ENABLE_BONJOUR_DISCOVERY,
                        MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY));
    }


    /**
     * Returns the action to perform for the given {@link BonjourService}. This method is called for every
     * BonjourService available when this menu is selected.
     *
     * @param bs the BonjourService
     * @return the action to perform for the given BonjourService
     */
    private MuAction getMenuItemAction(BonjourService bs) {
        return new OpenLocationAction(mainFrame, new Hashtable<>(), bs.getURL(), bs.getNameWithProtocol()) {
            @Override
            protected FolderPanel getFolderPanel() {
                return folderPanel != null ? folderPanel : super.getFolderPanel();
            }
        };
    }

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
        } else if (BonjourDirectory.isStarting()) {
            add(new JMenuItem(Translator.get("bonjour.bonjour_initializing"))).setEnabled(false);
        } else {
            // Inform that Bonjour support has been disabled
            add(new JMenuItem(Translator.get("bonjour.bonjour_disabled"))).setEnabled(false);
        }
    }

    public void menuDeselected(MenuEvent menuEvent) {
    }

    public void menuCanceled(MenuEvent menuEvent) {
    }
}
