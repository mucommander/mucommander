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

package com.mucommander.ui.dialog.shell;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.XBoxPanel;
import com.mucommander.desktop.ActionType;
import com.mucommander.process.AbstractProcess;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.terminal.TerminalWidget;

/**
 * Dialog used to execute a user-defined command.
 * <p>
 * Creates and displays a new dialog allowing the user to input a command which will be executed once the action is confirmed.
 * The command output of the user command is displayed in a text area
 * </p>
 * <p>
 * Note that even though this component is affected by themes, it's impossible to edit the current theme while it's being displayed.
 * For this reason, the RunDialog doesn't listen to theme modifications.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class RunDialog extends FocusDialog implements ActionListener, KeyListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunDialog.class);

    // - UI components -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Cancel button. */
    private JButton       closeButton;

    // - Process management --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Stream used to send characters to the process' stdin process. */
    private PrintStream     processInput;
    /** Process currently running, <code>null</code> if none. */
    private AbstractProcess currentProcess;

    // - Misc. class variables -----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Minimum dimensions for the dialog. */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(600, 400);

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Creates a panel containing the dialog's buttons.
     * @return a panel containing the dialog's buttons.
     */
    private XBoxPanel createButtonsArea() {
        // Buttons panel
         XBoxPanel buttonsPanel = new XBoxPanel();

        // Separator.
        buttonsPanel.add(Box.createHorizontalGlue());

        // 'Run / stop' and 'Cancel' buttons.
        buttonsPanel.add(DialogToolkit.createOKPanel(
                closeButton  = new JButton(Translator.get("close")),
                getRootPane(),
                this));

        return buttonsPanel;
    }

    /**
     * Creates and displays a new RunDialog.
     * @param mainFrame the main frame this dialog is attached to.
     */
    public RunDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(ActionType.RunCommand), mainFrame);

        // Initializes the dialog's UI.
        Container contentPane = getContentPane();
        JComponent terminal = TerminalWidget.createTerminal(mainFrame.getActivePanel().getCurrentFolder().getAbsolutePath(), this::dispose, this);
        contentPane.add(terminal, BorderLayout.CENTER);
        contentPane.add(createButtonsArea(), BorderLayout.SOUTH);

        // Sets default items.
        setInitialFocusComponent(terminal);

        // Makes sure that any running process will be killed when the dialog is closed.
        addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(currentProcess!=null) {
                        processInput.close();
                        currentProcess.destroy();
                    }
                }
            });

        // Sets the dialog's minimum size.
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
    }

    // - KeyListener code ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Notifies the RunDialog that a key has been pressed.
     * <p>
     * This method will ignore all events while a process is not running. If a process is running:
     * <ul>
     *  <li><code>VK_ESCAPE</code> events are skipped and left to the <i>Cancel</i> button to handle.</li>
     *  <li>Printable characters are passed to the process and consumed.</li>
     *  <li>All other events are consumed.</li>
     * </ul>
     * </p>
     * <p>
     * At the time of writing, <code>tab</code> characters do not seem to be caught.
     * </p>
     * @param event describes the key event.
     */
    public void keyPressed(KeyEvent event) {
        // Ignores VK_ESCAPE events, as their behavior is a bit strange: they register
        // as a printable character, and reacting to their being typed apparently consumes
        // the event - preventing the dialog from being closed.
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            event.consume();
            event.setKeyCode(-1);
            dispose();
        }
    }

    /**
     * Not used.
     */
    public void keyTyped(KeyEvent event) {}

    /**
     * Not used.
     */
    public void keyReleased(KeyEvent event) {}

    // - ActionListener code -------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Notifies the RunDialog that an action has been performed.
     * @param e describes the action that occurred.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Cancel button disposes the dialog and kills the process
        if (source == closeButton) {
            dispose();
        }
    }

}
