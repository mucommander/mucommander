

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
import java.util.Vector;

/**
 * Command bar panel 
 */
//public class CommandBarPanel extends JPanel implements ActionListener, MouseListener {
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

	private final static String MOVE_TEXT = Translator.get("command_bar.move")+" [F6]";
	private final static String RENAME_TEXT = Translator.get("command_bar.rename")+" [F6]";
	
	private final static String BUTTONS_TEXT[] = new String[] {
		Translator.get("command_bar.view")+" [F3]",
		Translator.get("command_bar.edit")+" [F4]",
		Translator.get("command_bar.copy")+" [F5]",
		MOVE_TEXT,
		Translator.get("command_bar.mkdir")+" [F7]",
		Translator.get("command_bar.delete")+" [F8]",
		Translator.get("command_bar.refresh")+" [F9]",
		Translator.get("command_bar.close")+" [F10]"
	};

	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	

    
	public CommandBarPanel(MainFrame mainFrame) {
		super(new GridLayout(0,8));
        this.mainFrame = mainFrame;

		this.buttons = new JButton[NB_BUTTONS];
		for(int i=0; i<NB_BUTTONS; i++)
			buttons[i] = addButton(BUTTONS_TEXT[i]);	
	}

	
	public JButton getButton(int buttonIndex) {
		return buttons[buttonIndex];
	}
	
	
	private JButton addButton(String text) {
		JButton button = new JButton(text);
		button.setMargin(new Insets(1,1,1,1));
		// For Mac OS X whose minimum width for buttons is enormous
		button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getWidth()));
		button.addActionListener(this);
        button.setToolTipText(text);
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
			buttons[MOVE_INDEX].setText(on&(mainFrame.getLastActiveTable().getSelectedFiles().size()<=1)?RENAME_TEXT:MOVE_TEXT);
			buttons[MOVE_INDEX].repaint();
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
		
		try {
			FileViewer viewer = ViewerRegistrar.getViewer(file, mainFrame);

			// Test if file is too large to be viewed and warns user
			long max = viewer.getMaxRecommendedSize();
			if (max!=-1 && file.getSize()>max) {
				QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("warning"), Translator.get("command_bar.large_file_warning"), mainFrame, 
					new String[] {Translator.get("command_bar.open_anyway"), Translator.get("cancel")},
					new int[]  {0, 1},
					0);

                int ret = dialog.getActionValue();
				
				if (ret==1 || ret==-1)
					return;
			}
			
			// All OK, start viewing
			viewer.startViewing(file, true);
		
			viewer.getFrame().show();
		}
		catch(Exception e) {
			showErrorDialog(Translator.get("file_viewer.view_error"), Translator.get("file_viewer.view_error_title"));
if(com.mucommander.Debug.ON) e.printStackTrace();
		}
	}
	
	
	/**
	 * Fires up the appropriate file editor for the selected file.
	 */
	public void doEdit() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
		if(file==null || (file.isDirectory() && !file.isSymlink()))
			return;

		try {
			FileEditor editor = EditorRegistrar.getEditor(file);
			// Default file editor
			if (editor==null) {
				// we should ask the user if he wants to try the default editor
				editor = new TextEditor();
			}

			// Tests if file is too large to be edited and warns user
			long max = editor.getMaxRecommendedSize();
			if (max!=-1 && file.getSize()>max) {
				QuestionDialog dialog = new QuestionDialog(mainFrame, Translator.get("warning"), Translator.get("command_bar.large_file_warning"), mainFrame, 
					new String[] {Translator.get("command_bar.open_anyway"), Translator.get("cancel")},
					new int[]  {0, 1},
					0);
				int ret = dialog.getActionValue();
				
				if (ret==1 || ret==-1)
					return;
			}

			// All OK, start editing
			editor.startEditing(file);
			
			// Sets dialog to preferred size, without exceeding a maximum size and with a minumum size
			editor.pack();
			Dimension d = editor.getSize();
			Dimension screend = Toolkit.getDefaultToolkit().getScreenSize();
			
			// Frame width is 800 max and 480 min
			int width = Math.max(480, Math.min(d.width, Math.min(screend.width-44, 800-44)));
			
			// Frame height is 3/4 of width
			editor.setSize(
				width, 
				(int)(width*3/((float)4))
			);

			
			editor.setVisible(true);
		}
		catch(IOException e) {
			showErrorDialog(Translator.get("file_editor.edit_error"), Translator.get("file_editor.edit_error_title"));
		}
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
			mainFrame.getLastActiveTable().refresh();
		}
        else if(source == buttons[CLOSE_INDEX]) {
			WindowManager.getInstance().disposeMainFrame(mainFrame);
		}
		else {
			// The following actions need to work on files, so return
			// if no files are selected
			Vector files = mainFrame.getLastActiveTable().getSelectedFiles();
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