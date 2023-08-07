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
import com.mucommander.ui.action.ActionId;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.terminal.TerminalWindow;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * This action shows built-in terminal (it mimics the behavior of Midnight Commander Ctrl-O command
 * that originates back from Norton Commander).
 */
public class ToggleTerminalAction extends ActiveTabAction {

    private JediTermWidget terminal;

    /**
     * When divider is close to max by this value we conclude it is maximised.
     */
    private static final float TREAT_AS_MAXIMIZED = 0.3f;

    private String cwd; // keep it as String or as MonitoredFile maybe?
    private boolean terminalMaximized; // is terminal maximized?

    private int lastMinDividerLocation; // last vertical split pane divider location when minimized
    private int lastMaxDividerLocation; // last vertical split pane divider location when maximised

    public ToggleTerminalAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setEnabled(DesktopManager.canOpenInFileManager());
        prepareVerticalSplitPaneForTerminal();
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(LocalFile.SCHEMA));
    }

    private void setTerminalMaximized(boolean terminalMaximized) {
        this.terminalMaximized = terminalMaximized;
        setLabel(Translator.get(terminalMaximized ? ActionType.ToggleTerminal + ".hide" : ActionType.ToggleTerminal + ".show"));
    }

    private boolean isTerminalMaximized() {
        return terminalMaximized;
    }

    @Override
    public void performAction() {
        toggleTerminal();
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
            return Translator.get(ActionProperties.getActionLabelKey(
                    ActionId.asTerminalAction(ToggleTerminalAction.Descriptor.ACTION_ID)),
                    DesktopManager.canOpenInFileManager() ? DesktopManager.getFileManagerName()
                            : Translator.get("file_manager"));
        }
    }

    private KeyAdapter termCloseKeyHandler() {
        return new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                KeyStroke pressedKeyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
                KeyStroke accelerator = ActionKeymap.getAccelerator(
                        ActionId.asTerminalAction(ActionType.ToggleTerminal.getId()));
                KeyStroke alternateAccelerator = ActionKeymap.getAlternateAccelerator(
                        ActionId.asTerminalAction(ActionType.ToggleTerminal.getId()));
                if (pressedKeyStroke.equals(accelerator) || pressedKeyStroke.equals(alternateAccelerator)) {
                    keyEvent.consume();
                    hideTerminal();
                } else if (!terminal.getTtyConnector().isConnected()) {
                    // just close terminal if it is not active/connected (for example when sb typed 'exit')
                    hideTerminal();
                }
            }
        };
    }

    private JediTermWidget getTerminal(String initialPath) {
        return TerminalWindow.createTerminal(initialPath);
    }

    /**
     * Toggles the Terminal, i.e. shows (maximized) or hides it (minimized).
     */
    private void toggleTerminal() {
        if (!isTerminalMaximized() || terminal == null) {
            showTerminal();
        } else {
            hideTerminal();
        }
    }

    private void showTerminal() {
        try {
            LOGGER.info("Going to show Terminal...");
            String newCwd = mainFrame.getActivePanel().getCurrentFolder().getAbsolutePath();
            // If !connected means that terminal process has ended (via `exit` command for ex.).
            if (terminal == null || !terminal.getTtyConnector().isConnected()) {
                mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                terminal = getTerminal(newCwd);
                terminal.getTerminalPanel().addCustomKeyListener(termCloseKeyHandler());
                // TODO do this better? For now 2 lines ~height + 20%
                terminal.setMinimumSize(new Dimension(-1,
                        (int) (terminal.getFontMetrics(terminal.getFont()).getHeight() * 2 * 1.2)));
                mainFrame.getVerticalSplitPane().setBottomComponent(terminal);
            } else {
                if (cwd == null || !cwd.equals(newCwd)) {
                    // TODO check somehow if term is busy..... or find another way to set CWD
                    // TODO cont'd: In Idea they've got TerminalUtil#hasRunningCommands for that...
                    // trailing space added deliberately to skip history (sometimes doesn't work, tho :/)
                    terminal.getTtyConnector().write(
                            " cd \"" + newCwd + "\""
                                    + System.getProperty("line.separator"));
                }
            }
            cwd = newCwd;
            mainFrame.getVerticalSplitPane().setDividerLocation(lastMaxDividerLocation);

            SwingUtilities.invokeLater(terminal::requestFocusInWindow);
            setTerminalMaximized(true);
        } catch (Exception e) {
            LOGGER.error("Caught exception while trying to show Terminal", e);
            hideTerminal();
        }
    }

    private void hideTerminal() {
        LOGGER.info("Going to hide Terminal...");
        var verticalSplitPane = mainFrame.getVerticalSplitPane();
        if (terminal != null && !terminal.getTtyConnector().isConnected()) {
            verticalSplitPane.remove(terminal);
            terminal = null;
        }

        // try to use last location falling back to the minimum of terminal constraints
        // however, user may move divider below terminal constraints (hide it completely) and we
        // should accept that
        verticalSplitPane.setDividerLocation(
                lastMinDividerLocation <= 0 ||    // ignore unknown or max
                lastMinDividerLocation > verticalSplitPane.getHeight() || // or outside vertical height
                lastMinDividerLocation < verticalSplitPane.getHeight() * TREAT_AS_MAXIMIZED // or too close to max
                ? verticalSplitPane.getMaximumDividerLocation() : lastMinDividerLocation);

        SwingUtilities.invokeLater(mainFrame.getActiveTable()::requestFocusInWindow);
        setTerminalMaximized(false);
    }

    private void alterSplitPaneButton(String buttonName, JSplitPane splitPane, Runnable action, String tooltip) {
        // https://stackoverflow.com/a/31709568/1715521
        try {
            Field field = BasicSplitPaneDivider.class.getDeclaredField(buttonName);
            field.setAccessible(true);
            JButton oneTouchButton = (JButton) field.get(((BasicSplitPaneUI) splitPane.getUI()).getDivider());
            oneTouchButton.setToolTipText(tooltip);
            oneTouchButton.setActionCommand(buttonName);
            oneTouchButton.addActionListener((e) -> SwingUtilities.invokeLater(action::run));
        } catch (Exception e) {
            LOGGER.error("Problem running reflection on vertical split pane: {}", e.getMessage(), e);
        }
    }

    private void alterSplitPaneDivider(JSplitPane splitPane, Runnable action, String tooltip) {

        try {
            // https://stackoverflow.com/a/27432464/1715521
            BasicSplitPaneUI splitUI = ((BasicSplitPaneUI) splitPane.getUI());
            splitPane.setToolTipText(tooltip);

            splitUI.getDivider().addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    SwingUtilities.invokeLater(action::run);
                }

                // force tooltip when mouse enters divider
                public void mouseEntered(MouseEvent e) {
                    // https://stackoverflow.com/a/39803911/1715521
                    final ToolTipManager ttm = ToolTipManager.sharedInstance();
                    final int oldDelay = ttm.getInitialDelay();
                    ttm.setInitialDelay(0);
                    ttm.mouseMoved(new MouseEvent(splitPane, 0, 0, 0,
                            splitUI.getDivider().getX() + e.getX(),
                            splitUI.getDivider().getY() + e.getY(), 0, false));
                    SwingUtilities.invokeLater(() -> ttm.setInitialDelay(oldDelay));
                }
            });
        } catch (Exception e) {
            LOGGER.error("Problem running reflection on vertical split pane: {}", e.getMessage(), e);
        }
    }

    private void prepareVerticalSplitPaneForTerminal() {
        var verticalSplitPane = mainFrame.getVerticalSplitPane();
        alterSplitPaneButton("leftButton", verticalSplitPane,
                () -> SwingUtilities.invokeLater(this::showTerminal),
                Translator.get(ActionType.ToggleTerminal + ".show"));
        alterSplitPaneButton("rightButton", verticalSplitPane,
                () -> SwingUtilities.invokeLater(this::hideTerminal),
                Translator.get(ActionType.ToggleTerminal + ".hide"));
        alterSplitPaneDivider(
                verticalSplitPane,
                () -> SwingUtilities.invokeLater(this::toggleTerminal),
                Translator.get(ActionType.ToggleTerminal + ".toggle"));

        mainFrame.getVerticalSplitPane().addPropertyChangeListener(
                JSplitPane.DIVIDER_LOCATION_PROPERTY,
                (e) -> {
                    if (terminal != null) {
                        var location = ((Integer)e.getNewValue()).intValue();
                        if (terminal == null) {
                            setTerminalMaximized(false);
                        } else if (location > verticalSplitPane.getMaximumDividerLocation() * TREAT_AS_MAXIMIZED) {
                            lastMinDividerLocation = location;
                            setTerminalMaximized(false);
                        } else {
                            lastMaxDividerLocation = location;
                            setTerminalMaximized(true);
                        }
                    }
                });
    }
}
