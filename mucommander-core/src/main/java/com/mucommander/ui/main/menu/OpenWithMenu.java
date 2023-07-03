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

package com.mucommander.ui.main.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.CommandAction;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;
import java.util.Collections;


/**
 * 'Open with' menu.
 * <p>
 * It contains entries from:
 * - command list (defined in custom .xml files)
 * - apps that can open a selected file (system specific, currently only macOS supported)
 *
 * Note that this class doesn't yet monitor modifications to the command list.
 * </p>
 * @author Nicolas Rinaudo
 */
public class OpenWithMenu extends JMenu {
    private final MainFrame mainFrame;

    private AbstractFile selectedFile;

    /**
     * Creates a new Open With menu (for contextual i.e. right click when selected file is known).
     *
     * @param frame the Main frame
     * @param selectedFile a selected/clicked file, can be null then no OS-specific apps will be added to the menu
     */
    public OpenWithMenu(MainFrame frame, AbstractFile selectedFile) {
        super(Translator.get("file_menu.open_with") + "...");
        this.mainFrame = frame;
        this.selectedFile = selectedFile;
        populate();
    }

    /**
     * Creates a new Open With menu (for Main Menu when selected file is not known yet).
     *
     * @param frame the Main frame
     */
    public OpenWithMenu(MainFrame frame) {
        this(frame, null);
    }

    /**
     * Refreshes the content of the menu.
     */
    private void populate() {
        populateRegisteredCommands();
        populateNativeApplications();
        setEnabled(getItemCount() > 0);
    }

    private void populateRegisteredCommands() {
        CommandManager.commands().stream()
        .filter(command -> command.getType() == CommandType.NORMAL_COMMAND)
        .map(command -> ActionManager.getActionInstance(command, mainFrame))
        .forEach(this::add);
    }

    /**
     * Refreshes the menu contents with a new selected file information.
     * @param selectedFile the selected file, can be null;
     */
    public void refresh(AbstractFile selectedFile) {
        this.removeAll();
        this.selectedFile = selectedFile;
        populate();
    }

    @Override
    public final JMenuItem add(Action a) {
        JMenuItem item = super.add(a);
        MenuToolkit.configureActionMenuItem(item);
        return item;
    }

    private MuAction createMuAction(Command cmd) {
        return new MuAction(mainFrame, Collections.emptyMap()) {
            @Override
            public void performAction() {
                try {
                    ProcessRunner.executeAsync(cmd.getTokens(selectedFile), selectedFile);
                } catch (IOException e) {
                    LOGGER.error("Error running command: {} for file: {}", cmd, selectedFile, e);
                }
            }

            @Override
            public ActionDescriptor getDescriptor() {
                return new CommandAction.Descriptor(cmd);
            }
        };
    }

    private void populateNativeApplications() {
        if (selectedFile != null && !selectedFile.isDirectory()) {
            if (DesktopManager.isOpenWithAppsAvailable()) {
                var loadingItem = super.add(Translator.get("file_menu.loading") + "...");
                var spinningIcon = new SpinningDial();
                loadingItem.setDisabledIcon(spinningIcon);
                loadingItem.setIcon(spinningIcon);  // need to set both disabled and normal, otherwise it doesn't appear
                loadingItem.setEnabled(false);
                spinningIcon.setAnimated(true);
                // going to run getCommandsForOpenWith in background as it may take some time to complete
                // especially if a given file has a lot of apps that can be opened with...
                new Thread(() -> {

                    var commands = DesktopManager.getAppsForOpenWith(selectedFile);
                    if (!commands.isEmpty() && getItemCount() > 1) {
                        add(new JSeparator());
                    }
                    var separateDefault = commands.size() > 1;
                    for (Command cmd : commands) {
                        MuAction action = createMuAction(cmd);
                        action.setLabel(cmd.getDisplayName());
                        add(action).setIcon(cmd.getIcon());
                        if (separateDefault) {
                            add(new JSeparator());
                            separateDefault = false;
                        }
                    }
                    spinningIcon.setAnimated(false);
                    super.remove(loadingItem);
                    if (getItemCount() == 0) {
                        setEnabled(false);
                    }
                    super.getPopupMenu().pack();
                }).start();
            } else {
                if (DesktopManager.canEnableOpenWithApps()) {
                    if (getItemCount() > 0) {
                        add(new JSeparator());
                    }
                    var howToEnable = super.add(
                            Translator.get("file_menu.open_with_apps_tip"));
                    howToEnable.addActionListener(e ->
                            DesktopManager.howToEnableOpenWithApps(mainFrame));
                }
            }
        }
    }

}
