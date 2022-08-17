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
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.main.MainFrame;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * This action shows built-in terminal (it mimics the behavior of Midnight Commander Ctrl-O command
 * that originates back from Norton Commander).
 */
public class ShowTerminalAction extends ActiveTabAction {

    private JediTermWidget terminal;
    private boolean isTermShown;

    public ShowTerminalAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setEnabled(DesktopManager.canOpenInFileManager());
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(LocalFile.SCHEMA));
    }

    @Override
    public void performAction() {
        if (!isTermShown) {
            try {
                LOGGER.info("Going to show Terminal...");
                mainFrame.getSplitPane().setVisible(false);
                if (terminal == null) {
                    terminal = getTerminal();
                } else {
                    // TODO check somehow if term is busy..... or find another way to set CWD
                    // trailing space added deliberately to skip history
                    terminal.getTtyConnector().write(
                            " cd \"" + mainFrame.getActivePanel().getCurrentFolder() + "\""
                                    + System.getProperty("line.separator"));
                }
                mainFrame.getMainPanel().add(terminal, BorderLayout.CENTER);
                terminal.revalidate();
                SwingUtilities.invokeLater(() -> {
                    terminal.requestFocusInWindow();
                });

                terminal.getTerminalPanel().addCustomKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent keyEvent) {
                        // TODO get the real configured key seq
                        if (keyEvent.getKeyCode() == KeyEvent.VK_O &&
                                (keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                            revertToTableView();
                            isTermShown = false;
                        }
                    }
                });
                isTermShown = true;
            } catch (Exception e) {
                LOGGER.error("Caught exception while trying to show Terminal", e);
                revertToTableView();
            }
        } else {
            // Normally this case is being handled by keyadapter above
            revertToTableView();
            isTermShown = false;
        }

    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ShowTerminal";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        }

        @Override
        public String getLabel() {
            return Translator.get(ActionProperties.getActionLabelKey(ShowTerminalAction.Descriptor.ACTION_ID),
                    DesktopManager.canOpenInFileManager() ? DesktopManager.getFileManagerName()
                            : Translator.get("file_manager"));
        }
    }

    private JediTermWidget getTerminal() {
        return TerminalWindow.createTerminal(mainFrame.getActivePanel().getCurrentFolder().getAbsolutePath());
    }

    private void revertToTableView() {
        LOGGER.info("Going to hide Terminal...");
        if (terminal != null) {
            mainFrame.getMainPanel().remove(terminal);
        }
        mainFrame.getSplitPane().setVisible(true);
        SwingUtilities.invokeLater(() -> {
            mainFrame.getActivePanel().requestFocusInWindow();
            mainFrame.getActiveTable().requestFocusInWindow();
        });
    }
}
