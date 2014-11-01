/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.notifier;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.runtime.JavaVersion;
import com.mucommander.ui.action.AWTActionProxy;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.BringAllToFrontAction;
import com.mucommander.ui.action.impl.NewWindowAction;
import com.mucommander.ui.action.impl.QuitAction;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.WindowManager;

/**
 * SystemTrayNotifier implements a notifier that uses the System Tray to display notifications. When enabled, this
 * notifier displays an icon in the systrem tray that recalls the current {@link com.mucommander.ui.main.MainFrame}
 * when double-clicked, or shows a popup menu with additional actions ('Bring all to front', 'Quit') when right-clicked.
 *
 * <p>This notifier is available only with Java 1.6 and up.</p>
 *
 * @author Maxence Bernard
 */
public class SystemTrayNotifier extends AbstractNotifier implements ActionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(SystemTrayNotifier.class);
	
    /** TrayIcon being displayed in the system tray, null when this notifier is not enabled */
    private TrayIcon trayIcon;

    /** Is this notifier enabled ? */
    private boolean isEnabled;

    /** Name of the tray icon image */
    private final static String TRAY_ICON_NAME = "icon16_8.png";

    /** Width of the muCommander tray icon */
    private final static int TRAY_ICON_WIDTH = 16;

    /** Height of the muCommander tray icon */
    private final static int TRAY_ICON_HEIGHT = 16;

    /** System tray message types for the different notification types */
    private final static Map<NotificationType, TrayIcon.MessageType> MESSAGE_TYPES;

    static {
        MESSAGE_TYPES = new Hashtable<NotificationType, TrayIcon.MessageType>();

        MESSAGE_TYPES.put(NotificationType.JOB_COMPLETED, TrayIcon.MessageType.INFO);
        MESSAGE_TYPES.put(NotificationType.JOB_ERROR, TrayIcon.MessageType.ERROR);
    }

    SystemTrayNotifier() {
    }

    /**
     * Creates and adds a menu item that triggers the MuAction denoted by the given Class. The menu item's label
     * is set to the value returned by {@link MuAction#getLabel()}.
     */
    private void addMenuItem(Menu menu, String muActionId) {
        MuAction action = ActionManager.getActionInstance(muActionId, WindowManager.getCurrentMainFrame());
        MenuItem menuItem = new MenuItem(action.getLabel());
        menuItem.addActionListener(new AWTActionProxy(action));
        menu.add(menuItem);
    }


    /////////////////////////////////////
    // AbstractNotifier implementation //
    /////////////////////////////////////

    @Override
    public boolean setEnabled(boolean enabled) {
        if(enabled) {
            // No need to bother if the current Java runtime version is not 1.6 or up, or if SystemTray is not available
            if(JavaVersion.JAVA_1_6.isCurrentLower() || !SystemTray.isSupported())
                return false;

            // If System Tray has already been initialized
            if(trayIcon!=null) {
                return (isEnabled = true);
            }

            SystemTray systemTray = SystemTray.getSystemTray();

            Image iconImage = IconManager.getIcon(IconManager.MUCOMMANDER_ICON_SET, TRAY_ICON_NAME).getImage();
            Dimension trayIconSize = systemTray.getTrayIconSize();
            // If the sytem tray icon size is larger than the icon size, center the icon as the default is to display
            // the icon in the top left corner which is plain ugly
            if(trayIconSize.width>TRAY_ICON_WIDTH || trayIconSize.height>TRAY_ICON_HEIGHT) {
                // The buffered image uses ARGB for transparency
                BufferedImage bi = new BufferedImage(trayIconSize.width, trayIconSize.height, BufferedImage.TYPE_INT_ARGB);
                bi.getGraphics().drawImage(iconImage, (trayIconSize.width-TRAY_ICON_WIDTH)/2, (trayIconSize.height-TRAY_ICON_HEIGHT)/2, null);
                iconImage = bi;
            }

            // Create the tray icon and disable image auto-size which shouldn't be used anyway but just in case
            trayIcon = new TrayIcon(iconImage);
            trayIcon.setImageAutoSize(false);

            // Create the popup (AWT!) menu. Note there is no way with java.awt.Menu to know when the menu is selected
            // and thus it makes it hard to have contextual menu items such as the list of open windows.
            PopupMenu menu = new PopupMenu();
            addMenuItem(menu, NewWindowAction.Descriptor.ACTION_ID);
            addMenuItem(menu, BringAllToFrontAction.Descriptor.ACTION_ID);
            menu.addSeparator();
            addMenuItem(menu, QuitAction.Descriptor.ACTION_ID);

            trayIcon.setPopupMenu(menu);

            // Add the tray icon to the system tray. If an exception is caught, clean things up and leave this notifier
            // disabled.
            try {
                systemTray.add(trayIcon);
                // Tray icon was added OK, listen to action events
                trayIcon.addActionListener(this);

                return (isEnabled = true);
            }
            catch(java.awt.AWTException e) {
                trayIcon = null;

                return (isEnabled = false);
            }
        }
        else {
            if(trayIcon!=null) {
                // Remove tray icon from the system tray
                SystemTray.getSystemTray().remove(trayIcon);
                trayIcon.removeActionListener(this);

                trayIcon = null;
            }

            return (isEnabled = false);
        }
    }

    @Override
    public boolean isEnabled() {
        return trayIcon!=null && isEnabled;
    }

    @Override
    public boolean displayNotification(NotificationType notificationType, String title, String description) {
        LOGGER.debug("notificationType="+notificationType+" title="+title+" description="+description);

        if(!isEnabled()) {
            LOGGER.debug("Ignoring notification, this notifier is not enabled");

            return false;
        }

        trayIcon.displayMessage(title, description, MESSAGE_TYPES.get(notificationType));
        return true;
    }

    @Override
    public String getPrettyName() {
        return "System Tray";
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent actionEvent) {
        LOGGER.trace("caught SystemTray ActionEvent");

        WindowManager.getCurrentMainFrame().toFront();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected void finalize() throws Throwable {
        // This ensures that the system tray icon is removed when the application terminates.
        // Even though this is a bit of a shot in the dark, this may fix a problem reported under Linux where the
        // tray icon stayed after the application had quit:
        /// http://www.mucommander.com/forums/viewtopic.php?t=604
        if(isEnabled())
            setEnabled(false);

        super.finalize();
    }
}
