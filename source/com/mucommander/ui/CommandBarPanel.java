

package com.mucommander.ui;

import com.mucommander.ui.table.*;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.viewer.*;
import com.mucommander.ui.editor.*;
import com.mucommander.text.Translator;
import com.mucommander.job.*;
import com.mucommander.file.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


/**
 * Command bar panel which contains buttons for a number of commands (view, edit, copy, move...) and
 * appears at the bottom of a main window.
 *
 * @author Maxence Bernard
 */
public class CommandBarPanel extends JPanel implements ActionListener {

    private MainFrame mainFrame;
    
	public final static int VIEW_INDEX = 0;
	public final static int EDIT_INDEX = 1;
	public final static int COPY_INDEX = 2;
	public final static int MOVE_INDEX = 3;
	public final static int MKDIR_INDEX = 4;
	public final static int DELETE_INDEX = 5;
	public final static int REFRESH_INDEX = 6;
	public final static int CLOSE_INDEX = 7;
	
	private final static int NB_BUTTONS = 8;

	private boolean shiftDown;
	
	private JButton buttons[];

	private final static String MOVE_TEXT = "command_bar.move";
	private final static String RENAME_TEXT = "command_bar.rename";

	private final static String COPY_TEXT = "command_bar.copy";
	private final static String LOCAL_COPY_TEXT = "command_bar.local_copy";
	
	
	private final static String BUTTONS_TEXT[][] =  {
		{"command_bar.view", "[F3]"},
		{"command_bar.edit", "[F4]"},
		{COPY_TEXT, "[F5]"},
		{MOVE_TEXT, "[F6]"},
		{"command_bar.mkdir", "[F7]"},
		{"command_bar.delete", "[F8]"},
		{"command_bar.refresh", "[F9]"},
		{"command_bar.close", "[F10]"}
	};

	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	

    
	public CommandBarPanel(MainFrame mainFrame) {
		super(new GridLayout(0,8));
        this.mainFrame = mainFrame;

		this.buttons = new JButton[NB_BUTTONS];
		for(int i=0; i<NB_BUTTONS; i++)
			buttons[i] = addButton(Translator.get(BUTTONS_TEXT[i][0])+" "+BUTTONS_TEXT[i][1],
				Translator.get(BUTTONS_TEXT[i][0]+"_tooltip")
			);	
	}

	
	public JButton getButton(int buttonIndex) {
		return buttons[buttonIndex];
	}
	
	
	private JButton addButton(String text, String tooltipText) {
		JButton button = new JButton(text);
        button.setToolTipText(tooltipText);
		button.setMargin(new Insets(1,1,1,1));
		// For Mac OS X whose minimum width for buttons is enormous
		button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getWidth()));
		button.addActionListener(this);
		add(button);
		return button;
	}

	
	/**
	 * Sets shift mode on or off : some buttons such as 'F6 Move' may want to indicate
	 * the action is different when shift is pressed.
	 */
	public void setShiftMode(boolean on) {
		if(shiftDown!=on) {
			this.shiftDown = on;
			boolean singleFileMode = on&&((FileTableModel)(mainFrame.getLastActiveTable().getModel())).getNbMarkedFiles()<=1;

			// Change Move/Rename button's text and tooltip
			String textKey = singleFileMode?RENAME_TEXT:MOVE_TEXT;
			buttons[MOVE_INDEX].setText(Translator.get(textKey)+" "+BUTTONS_TEXT[MOVE_INDEX][1]);
			buttons[MOVE_INDEX].setToolTipText(Translator.get(textKey+"_tooltip"));
			buttons[MOVE_INDEX].repaint();

			// Change Copy/Local copy button's text and tooltip
			textKey = singleFileMode?LOCAL_COPY_TEXT:COPY_TEXT;
			buttons[COPY_INDEX].setText(Translator.get(textKey)+" "+BUTTONS_TEXT[COPY_INDEX][1]);
			buttons[COPY_INDEX].setToolTipText(Translator.get(textKey+"_tooltip"));
			buttons[COPY_INDEX].repaint();
		}
	}

	
	private void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);

//		// FileTable lost focus
//		mainFrame.getLastActiveTable().requestFocus();
	}


	/**
	 * Fires up the appropriate file viewer for the selected file.
	 */
	public void doView() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
		if(file==null || (file.isDirectory() && !file.isSymlink()))
			return;
		
		ViewerFrame frame = new ViewerFrame(mainFrame, file);
		frame.show();
	}
	
	
	/**
	 * Fires up the appropriate file editor for the selected file.
	 */
	public void doEdit() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
		if(file==null || (file.isDirectory() && !file.isSymlink()))
			return;

		EditorFrame frame = new EditorFrame(mainFrame, file);
		frame.show();
	}
	

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
		if(source == buttons[VIEW_INDEX]) {
        	doView();
		}
        else if(source == buttons[EDIT_INDEX]) {
        	doEdit();
		}
        else if(source == buttons[MKDIR_INDEX]) {
            new MkdirDialog(mainFrame);
        }
        else if(source == buttons[REFRESH_INDEX]) {
			mainFrame.getLastActiveTable().getFolderPanel().refresh();
		}
        else if(source == buttons[CLOSE_INDEX]) {
			WindowManager.getInstance().disposeMainFrame(mainFrame);
		}
		else {
			// The following actions need to work on files, so return
			// if no files are selected
			FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
			if(files.size()==0) {
				// Request focus since focus currently belongs to a command bar button
				// and no dialog will request focus
				mainFrame.requestFocus();
				return;
			}
			
			if(source == buttons[COPY_INDEX]) {
				new CopyDialog(mainFrame, files, shiftDown);
			}
			else if(source == buttons[MOVE_INDEX]) {
				new MoveDialog(mainFrame, files, shiftDown);
			}
			else if(source == buttons[DELETE_INDEX]) {
				new DeleteDialog(mainFrame, files);
			}
		}
    }
}