package com.mucommander.ui;

import com.mucommander.conf.*;
import com.mucommander.shell.*;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;


/**
 * Dialog used to execute a user-defined command.
 *
 * Creates and displays a new dialog allowing the user to input a command which will be executed once the action is confirmed.
 * The command output of the user command is displayed in a text area
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class RunDialog extends FocusDialog implements ActionListener, ProcessListener, KeyListener, ThemeListener {
    private MainFrame mainFrame;
	
    private ShellComboBox inputCombo;
    private JButton runStopButton;
    private JButton cancelButton;
    private JButton clearButton;

    private JTextArea outputTextArea;

    private PrintStream    processInput;
    private Process        currentProcess;

    private int caretPos;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(600, 400);	


    /**
     * Creates and displays a new RunDialog.
     * 
     * @param mainFrame the main frame this dialog is attached to.
     */
    public RunDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get("com.mucommander.ui.action.RunCommandAction.label"), mainFrame);
        this.mainFrame = mainFrame;
		
        Container contentPane = getContentPane();
        YBoxPanel mainPanel = new YBoxPanel();

        JLabel label;
        if(mainFrame.getActiveTable().getCurrentFolder() instanceof com.mucommander.file.FSFile)
            label = new JLabel(Translator.get("run_dialog.run_command_description")+":");
        else
            label = new JLabel(Translator.get("run_dialog.run_in_home_description")+":");
        mainPanel.add(label);

        inputCombo = new ShellComboBox(this);

        mainPanel.add(inputCombo);
        mainPanel.addSpace(10);

        contentPane.add(mainPanel, BorderLayout.NORTH);

        mainPanel.add(new JLabel(Translator.get("run_dialog.command_output")+":"));
        outputTextArea = new JTextArea();
        outputTextArea.setLineWrap(true);
        outputTextArea.setRows(10);
        outputTextArea.setEditable(false);
        outputTextArea.addKeyListener(this);

        outputTextArea.setForeground(ThemeManager.getCurrentColor(Theme.SHELL_TEXT));
        outputTextArea.setCaretColor(ThemeManager.getCurrentColor(Theme.SHELL_TEXT));
        outputTextArea.setBackground(ThemeManager.getCurrentColor(Theme.SHELL_BACKGROUND));
        outputTextArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.SHELL_TEXT_SELECTED));
        outputTextArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.SHELL_BACKGROUND_SELECTED));
        outputTextArea.setFont(ThemeManager.getCurrentFont(Theme.SHELL));

        JScrollPane scrollPane = new JScrollPane(outputTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        contentPane.add(scrollPane, BorderLayout.CENTER);
		
        // Buttons panel
        XBoxPanel buttonsPanel;

        buttonsPanel      = new XBoxPanel();

        clearButton   = new JButton(Translator.get("run_dialog.clear_history"));
        runStopButton = new JButton(Translator.get("run_dialog.run"));
        cancelButton  = new JButton(Translator.get("cancel"));

        clearButton.addActionListener(this);
        buttonsPanel.add(clearButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(DialogToolkit.createOKCancelPanel(runStopButton, cancelButton, this));

        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(inputCombo);
			
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        //		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        // Closing this dialog kills the process
        addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    if(currentProcess!=null) {
                        processInput.close();
                        currentProcess.destroy();
                    }
                }
            });

        // Make the 'Run/stop' button the default button
        getRootPane().setDefaultButton(runStopButton);

        ThemeManager.addThemeListener(this);
        
        inputCombo.setEnabled(true);
        showDialog();
    }

	
    public void processDied(int retValue) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("process exit, return value= "+retValue);
        currentProcess = null;
        processInput.close();
        processInput = null;
        switchToRunState();
    }	

	
    public void processOutput(byte buffer[], int offset, int length) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("processOutput output= "+new String(buffer, 0, length));
        addToTextArea(new String(buffer, offset, length));
    }

    private void addToTextArea(String s) {
        outputTextArea.append(s);
        caretPos += s.length();
        outputTextArea.setCaretPosition(caretPos);
        outputTextArea.getCaret().setVisible(true);
        outputTextArea.repaint();
    }

    private void switchToRunState() {
        // Change 'Stop' button to 'Run'
        this.runStopButton.setText(Translator.get("run_dialog.run"));
        //		// Make text area not active anymore
        //		this.outputTextArea.setEnabled(false);
        // Make command field active again
        this.inputCombo.setEnabled(true);
        inputCombo.requestFocus();
        outputTextArea.getCaret().setVisible(false);
        // Repaint this dialog
        repaint();
    }	

    // - KeyListener code ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void keyPressed(KeyEvent event) {
        if(currentProcess != null && event.getKeyCode() == KeyEvent.VK_ENTER)
            event.consume();
    }

    public void keyReleased(KeyEvent event) {}
    public void keyTyped(KeyEvent event) {
        if(currentProcess != null) {
            char character;
            if((character = event.getKeyChar()) != KeyEvent.CHAR_UNDEFINED) {
                processInput.print(character);
                addToTextArea(String.valueOf(character));
            }
        }
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
    public void runCommand(String command) {
        inputCombo.setEnabled(false);
        try {
            currentProcess = Shell.execute(command, mainFrame.getActiveTable().getCurrentFolder(), this);
            processInput   = new PrintStream(currentProcess.getOutputStream(), true);

            // If command could be executed
            // Reset caret position
            caretPos = 0;

            // Change 'Run' button to 'Stop'
            this.runStopButton.setText(Translator.get("run_dialog.stop"));
            // Clear text area
            outputTextArea.setText("");

            outputTextArea.getCaret().setVisible(true);
            outputTextArea.requestFocus();

            // Repaint the dialog
            repaint();
        }
        catch(Exception e1) {
            // Probably should notify the user if the command could not be executed
        }
    }

	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Clears shell history.
        if(source == clearButton) {
            ShellHistoryManager.clear();
            if(currentProcess == null)
                inputCombo.requestFocus();
            else
                outputTextArea.requestFocus();
        }

        // Run button starts a new command
        else if(currentProcess==null && (source == runStopButton))
            runCommand(inputCombo.getCommand());

        // Stop button stops current process
        else if(currentProcess!=null && source==runStopButton) {
            processInput.close();
            currentProcess.destroy();
            this.currentProcess = null;
            switchToRunState();
        }

        // Cancel button disposes the dialog and kills the process
        else if(source == cancelButton) {
            if(currentProcess != null)
                currentProcess.destroy();
            dispose();			
        }
    }



    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     * @param colorId identifier of the color that has changed.
     * @param color   new value for the color.
     */
    public void colorChanged(int colorId, Color color) {
        switch(colorId) {
        case Theme.SHELL_TEXT:
            outputTextArea.setForeground(color);
            break;

        case Theme.SHELL_BACKGROUND:
            outputTextArea.setBackground(color);
            break;

        case Theme.SHELL_TEXT_SELECTED:
            outputTextArea.setSelectedTextColor(color);
            break;

        case Theme.SHELL_BACKGROUND_SELECTED:
            outputTextArea.setSelectionColor(color);
            break;
        }
    }

    /**
     * Receives theme font changes notifications.
     * @param fontId identifier of the font that has changed.
     * @param font   new value for the font.
     */
    public void fontChanged(int fontId, Font font) {
        if(fontId == Theme.SHELL)
            outputTextArea.setFont(font);
    }
}
