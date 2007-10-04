/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.shell.Shell;
import com.mucommander.shell.ShellHistoryManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;


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
public class RunDialog extends FocusDialog implements ActionListener, ProcessListener, KeyListener {
    // - UI components -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Main frame this dialog depends on. */
    private MainFrame mainFrame;
    /** Editable combo box used for shell input and history. */
    private ShellComboBox inputCombo;
    /** Run/stop button. */
    private JButton       runStopButton;
    /** Cancel button. */
    private JButton       cancelButton;
    /** Clear shell history button. */
    private JButton       clearButton;
    /** Text area used to display the shell output. */
    private JTextArea     outputTextArea;



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
     * Creates the dialog's shell output area.
     * @return a scroll pane containing the dialog's shell output area.
     */
    private JScrollPane createOutputArea() {
        // Creates and initialises the output area.
        outputTextArea = new JTextArea();
        outputTextArea.setLineWrap(true);
        outputTextArea.setCaretPosition(0);
        outputTextArea.setRows(10);
        outputTextArea.setEditable(false);
        outputTextArea.addKeyListener(this);

        // Applies the current theme to the shell output area.
        outputTextArea.setForeground(ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR));
        outputTextArea.setCaretColor(ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR));
        outputTextArea.setBackground(ThemeManager.getCurrentColor(Theme.SHELL_BACKGROUND_COLOR));
        outputTextArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.SHELL_SELECTED_FOREGROUND_COLOR));
        outputTextArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.SHELL_SELECTED_BACKGROUND_COLOR));
        outputTextArea.setFont(ThemeManager.getCurrentFont(Theme.SHELL_FONT));

        // Creates a scroll pane on the shell output area.
        return new JScrollPane(outputTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Creates the shell input part of the dialog.
     * @return the shell input part of the dialog.
     */
    private YBoxPanel createInputArea() {
        YBoxPanel mainPanel;

        mainPanel = new YBoxPanel();

        // Adds a textual description:
        // - if we're working in a local directory, 'run in current folder'.
        // - if we're working on a non-standard FS, 'run in home folder'.
        mainPanel.add(new JLabel(mainFrame.getActiveTable().getCurrentFolder().canRunProcess() ?
                                 Translator.get("run_dialog.run_command_description")+":" : Translator.get("run_dialog.run_in_home_description")+":"));

        // Adds the shell input combo box.
        mainPanel.add(inputCombo = new ShellComboBox(this));
        inputCombo.setEnabled(true);

        // Adds a textual description of the shell output area.
        mainPanel.addSpace(10);
        mainPanel.add(new JLabel(Translator.get("run_dialog.command_output")+":"));

        return mainPanel;
    }

    /**
     * Creates a panel containing the dialog's buttons.
     * @return a panel containing the dialog's buttons.
     */
    private XBoxPanel createButtonsArea() {
        // Buttons panel
        XBoxPanel buttonsPanel;

        buttonsPanel = new XBoxPanel();

        // 'Clear history' button.
        buttonsPanel.add(clearButton = new JButton(Translator.get("run_dialog.clear_history")));
        clearButton.addActionListener(this);

        // Separator.
        buttonsPanel.add(Box.createHorizontalGlue());

        // 'Run / stop' and 'Cancel' buttons.
        buttonsPanel.add(DialogToolkit.createOKCancelPanel(runStopButton = new JButton(Translator.get("run_dialog.run")),
                                                           cancelButton  = new JButton(Translator.get("cancel")), this));

        return buttonsPanel;
    }

    /**
     * Creates and displays a new RunDialog.
     * @param mainFrame the main frame this dialog is attached to.
     */
    public RunDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.RunCommandAction.class.getName()+".label"), mainFrame);
        this.mainFrame = mainFrame;
		
        // Initialises the dialog's UI.
        Container contentPane = getContentPane();
        contentPane.add(createInputArea(), BorderLayout.NORTH);
        contentPane.add(createOutputArea(), BorderLayout.CENTER);
        contentPane.add(createButtonsArea(), BorderLayout.SOUTH);

        // Sets default items.
        setInitialFocusComponent(inputCombo);
        getRootPane().setDefaultButton(runStopButton);

        // Makes sure that any running process will be killed when the dialog is closed.
        addWindowListener(new WindowAdapter() {
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



    // - ProcessListener code ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Notifies the RunDialog that the current process has died.
     * @param retValue process' return code (not used).
     */	
    public void processDied(int retValue) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("process exit, return value= "+retValue);
        currentProcess = null;
        processInput.close();
        processInput = null;
        switchToRunState();
    }	

    /**
     * Ignored.
     */
    public void processOutput(byte[] buffer, int offset, int length) {}

    /**
     * Notifies the RunDialog that the process has output some text.
     * @param output contains the process' output.
     */
    public void processOutput(String output) {addToTextArea(output);}



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
        // Only handle keyPressed events when a process is running.
        if(currentProcess != null) {

            // Ignores VK_ESCAPE events, as their behavior is a bit strange: they register
            // as a printable character, and reacting to their being typed apparently consumes
            // the event - preventing the dialog from being closed.
            if(event.getKeyCode() != KeyEvent.VK_ESCAPE) {
                char character;

                // Only printable key typed are passed to the shell.
                if((character = event.getKeyChar()) != KeyEvent.CHAR_UNDEFINED) {
                    processInput.print(character);
                    addToTextArea(String.valueOf(character));
                }
                event.consume();
            }
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
     * @param e describes the action that occured.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // 'Clear shell history' has been pressed, clear shell history.
        if(source == clearButton) {
            ShellHistoryManager.clear();

            // Sets the new focus depending on whether a process is currently running or not.
            if(currentProcess == null) {
                inputCombo.requestFocus();
                outputTextArea.setText("");
            }
            else {
                outputTextArea.requestFocus();
                outputTextArea.getCaret().setVisible(true);
            }
        }

        // 'Run / stop' button has been pressed.
        else if(source == runStopButton) {

            // If we're not running a process, start a new one.
            if(currentProcess == null)
                runCommand(inputCombo.getCommand());

            // If we're running a process, kill it.
            else {
                processInput.close();
                currentProcess.destroy();
                this.currentProcess = null;
                switchToRunState();
            }
        }

        // Cancel button disposes the dialog and kills the process
        else if(source == cancelButton) {
            if(currentProcess != null)
                currentProcess.destroy();
            dispose();
        }
    }



    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Switches the UI back to 'Run command' state.
     */
    private void switchToRunState() {
        // Change 'Stop' button to 'Run'
        this.runStopButton.setText(Translator.get("run_dialog.run"));

        // Make command field active again
        this.inputCombo.setEnabled(true);
        inputCombo.requestFocus();

        // Disables the caret in the process output area.
        outputTextArea.getCaret().setVisible(false);

        // Repaint this dialog
        repaint();
    }	

    /**
     * Runs the specified command.
     * @param command command to run.
     */
    public void runCommand(String command) {
        try {
            // Change 'Run' button to 'Stop'
            this.runStopButton.setText(Translator.get("run_dialog.stop"));

            // Resets the process output area.
            outputTextArea.setText("");
            outputTextArea.setCaretPosition(0);
            outputTextArea.getCaret().setVisible(true);
            outputTextArea.requestFocus();

            // No new command can be entered while a process is running.
            inputCombo.setEnabled(false);

            // Starts the new process.
            currentProcess = Shell.execute(command, mainFrame.getActiveTable().getCurrentFolder(), this);
            processInput   = new PrintStream(currentProcess.getOutputStream(), true);

            // Repaints the dialog.
            repaint();
        }
        catch(Exception e1) {
            // Probably should notify the user if the command could not be executed
        }
    }

    /**
     * Appends the specified string to the shell output area.
     * @param s string to append to the shell output area.
     */
    private void addToTextArea(String s) {
        outputTextArea.append(s);
        outputTextArea.setCaretPosition(outputTextArea.getText().length());
        outputTextArea.getCaret().setVisible(true);
        outputTextArea.repaint();
    }
}
