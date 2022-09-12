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

package com.mucommander.ui.action.impl;

import com.jediterm.terminal.ui.JediTermWidget;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.main.MainFrame;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * This action shows built-in terminal (it mimics the behavior of Midnight Commander Ctrl-O command
 * that originates back from Norton Commander).
 */
public class ToggleTerminalAction extends ActiveTabAction {

    private JediTermWidget terminal;

    private String cwd; // keep it as String or as MonitoredFile maybe?
    private boolean visible; // is terminal visible?

    public ToggleTerminalAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setEnabled(DesktopManager.canOpenInFileManager());
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(LocalFile.SCHEMA));
    }

    private void setVisible(boolean visible) {
        this.visible = visible;
        setLabel(Translator.get(visible?ActionType.ToggleTerminal+".hide":ActionType.ToggleTerminal+".label"));
    }

    private boolean isVisible() {
        return visible;
    }

    @Override
    public void performAction() {
        if (!isVisible()) {
            try {
                LOGGER.info("Going to show Terminal...");
                // TODO either hide them, or disable all the options (or maybe add 'return' option?)
                //mainFrame.getToolBarPanel().setVisible(false);
                //mainFrame.getCommandBar().setVisible(false);
                mainFrame.getSplitPane().setVisible(false);
                String newCwd = mainFrame.getActivePanel().getCurrentFolder().getAbsolutePath();
                // If !connected means that terminal process has ended (via `exit` command for ex.).
                if (terminal == null || !terminal.getTtyConnector().isConnected()) {
                    terminal = getTerminal(newCwd);
                } else {
                    if (cwd == null || !cwd.equals(newCwd)) {
                        // TODO check somehow if term is busy..... or find another way to set CWD
                        // trailing space added deliberately to skip history
                        terminal.getTtyConnector().write(
                                " cd \"" + newCwd + "\""
                                        + System.getProperty("line.separator"));
                    }
                }
                cwd = newCwd;
                mainFrame.getMainPanel().add(terminal, BorderLayout.CENTER);
                terminal.revalidate();
                SwingUtilities.invokeLater(() -> {
                    terminal.requestFocusInWindow();
                });

                terminal.getTerminalPanel().addCustomKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent keyEvent) {
                        KeyStroke pressedKeyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
                        KeyStroke accelerator = ActionKeymap.getAccelerator(ActionType.ToggleTerminal.toString());
                        KeyStroke alternateAccelerator = ActionKeymap.getAlternateAccelerator(ActionType.ToggleTerminal.toString());
                        if (pressedKeyStroke.equals(accelerator) || pressedKeyStroke.equals(alternateAccelerator)) {
                            revertToTableView();
                            setVisible(false);
                        }
                    }
                });
                setVisible(true);
            } catch (Exception e) {
                LOGGER.error("Caught exception while trying to show Terminal", e);
                revertToTableView();
            }
        } else {
            // Normally this case is being handled by keyadapter above
            revertToTableView();
            setVisible(false);
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ToggleTerminal";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        @Override
        public String getLabel() {
            return Translator.get(ActionProperties.getActionLabelKey(ToggleTerminalAction.Descriptor.ACTION_ID),
                    DesktopManager.canOpenInFileManager() ? DesktopManager.getFileManagerName()
                            : Translator.get("file_manager"));
        }
    }

    private JediTermWidget getTerminal(String initialPath) {
        return TerminalWindow.createTerminal(initialPath);
    }

    private void revertToTableView() {
        LOGGER.info("Going to hide Terminal...");
        if (terminal != null) {
            mainFrame.getMainPanel().remove(terminal);
        }
        // TODO see #performAction above
        //mainFrame.getToolBarPanel().setVisible(true);
        //mainFrame.getCommandBar().setVisible(true);
        mainFrame.getSplitPane().setVisible(true);
        SwingUtilities.invokeLater(() -> {
            mainFrame.getActiveTable().requestFocusInWindow();
        });
    }
}
