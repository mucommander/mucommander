package com.mucommander.ui;

import com.mucommander.conf.*;
import com.mucommander.shell.*;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.*;

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
public class RunDialog extends FocusDialog implements ActionListener, ProcessListener, KeyListener {
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

        // Set custom text, selected text, background and selection background colors
        Color textColor = ConfigurationManager.getVariableColor(ConfigurationVariables.SHELL_TEXT_COLOR,
                                                                ConfigurationVariables.DEFAULT_SHELL_TEXT_COLOR);
        outputTextArea.setForeground(textColor);
        // Selected text color and text color are the same
        outputTextArea.setSelectedTextColor(textColor);
        outputTextArea.setBackground(ConfigurationManager.getVariableColor(ConfigurationVariables.SHELL_BACKGROUND_COLOR,
                                                                           ConfigurationVariables.DEFAULT_SHELL_BACKGROUND_COLOR));
        outputTextArea.setSelectionColor(ConfigurationManager.getVariableColor(ConfigurationVariables.SHELL_SELECTION_COLOR,
                                                                               ConfigurationVariables.DEFAULT_SHELL_SELECTION_COLOR));
        outputTextArea.setCaretColor(textColor);

        // Use a monospaced font in the command field and process output text area, as most terminals do.
        // The logical "Monospaced" font name is always available in Java.
        // The font size is the one of the default JTextArea, style is plain.
        Font monospacedFont = new Font("Monospaced", Font.PLAIN, outputTextArea.getFont().getSize());
        outputTextArea.setFont(monospacedFont);

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
}
