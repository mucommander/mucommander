package com.mucommander.bonjour;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.action.OpenLocationAction;
import com.mucommander.text.Translator;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

/**
 * A JMenu that contains an item for each available Bonjour service (as returned {@link BonjourDirectory#getServices()}
 * , displaying the Bonjour service's name. When a menu item is clicked, the corresponding url is opened in the
 * active table.
 *
 * <p>Note: the items list is refreshed each time the menu is selected. In other words, a new instance of BonjourMenu
 * does not have to be created in order to see new Bonjour services.
 *
 * @author Maxence Bernard
 */
public class BonjourMenu extends JMenu implements MenuListener {

    private MainFrame mainFrame;

    public BonjourMenu(MainFrame mainFrame) {
        super(Translator.get("bonjour.bonjour_services"));
        this.mainFrame = mainFrame;

        // Menu items will be added when menu gets selected
        addMenuListener(this);
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
                for(int i=0; i<nbServices; i++) {
                    menuItem = new JMenuItem(new OpenLocationAction(mainFrame, services[i]));
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
