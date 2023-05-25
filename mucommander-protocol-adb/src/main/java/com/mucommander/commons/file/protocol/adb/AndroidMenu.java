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

package com.mucommander.commons.file.protocol.adb;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.OpenLocationAction;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * An abstract JMenu that contains an item for each Android ADB devices available
 *
 * <p>Note: the items list is refreshed each time the menu is selected. In other words, a new instance of AdbMenu
 * does not have to be created in order to see new devices.
 *
 * Created on 28/12/15.
 * @author Oleg Trifonov, Arik Hadas
 */
public class AndroidMenu extends JMenu implements MenuListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidMenu.class);

    private MainFrame mainFrame;
    private FolderPanel folderPanel;

    /**
     * Creates a new instance of <code>AndroidMenu</code>.
     */
    public AndroidMenu(MainFrame mainFrame, FolderPanel folderPanel) {
        super(Translator.get("adb.android_devices"));
        this.mainFrame = mainFrame;
        this.folderPanel = folderPanel;
        setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, "android.png"));
        SwingUtilities.invokeLater(() -> menuSelected(null));

        // Menu items will be added when menu gets selected
        addMenuListener(this);
    }

    /**
     * Returns the action to perform for the given item.
     *
     * @param deviceSerial the serial number of the device
     * @return the action to perform for the given Android device
     */
    public MuAction getMenuItemAction(String deviceSerial) {
        return new OpenLocationAction(mainFrame, Collections.emptyMap(), getDeviceURL(deviceSerial)) {
            @Override
            protected FolderPanel getFolderPanel() {
                return folderPanel != null ? folderPanel : mainFrame.getActivePanel();
            }
        };
    }

    private FileURL getDeviceURL(String deviceSerial) {
        try {
            return FileURL.getFileURL("adb://" + deviceSerial);
        } catch (MalformedURLException e) {
            LOGGER.error("failed to get adb device file");
            LOGGER.debug("failed to get adb device file", e);
            return null;
        }
    }

    @Override
    public void menuSelected(MenuEvent e) {
        // Remove previous menu items (if any)
        removeAll();

        List<String> androidDevices = AdbUtils.getDevices();
        if (androidDevices == null) {
            setEnabled(false);
            setToolTipText(Translator.get("adb.android_disabled"));
            return;
        }
        setEnabled(true);
        if (androidDevices.isEmpty()) {
            add(new JMenuItem(Translator.get("adb.no_devices"))).setEnabled(false);
            return;
        }
        MnemonicHelper mnemonicHelper = new MnemonicHelper();
        for (String serial : androidDevices) {
            JMenuItem menuItem = new JMenuItem(getMenuItemAction(serial));
            menuItem.setMnemonic(mnemonicHelper.getMnemonic(menuItem.getText()));
            String name = AdbUtils.getDeviceName(serial);
            menuItem.setText(name == null ? serial : name);
            menuItem.setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, "android.png"));

            add(menuItem);
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {

    }

    @Override
    public void menuCanceled(MenuEvent e) {

    }
}
