

package com.mucommander.ui;

import com.mucommander.Launcher;
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
public class CommandBarPanel extends JPanel implements ActionListener, MouseListener {

    private MainFrame mainFrame;
    
	private JButton viewButton;
	private JButton editButton;
	private JButton copyButton;
	private JButton moveButton;
	private JButton mkdirButton;
	private JButton deleteButton;
	private JButton refreshButton;
	private JButton closeButton;
	
	private final static String VIEW_TEXT = Translator.get("command_bar.view");
	private final static String EDIT_TEXT = Translator.get("command_bar.edit");
	private final static String COPY_TEXT = Translator.get("command_bar.copy");
	private final static String MOVE_TEXT = Translator.get("command_bar.move");
	private final static String RENAME_TEXT = Translator.get("command_bar.rename");
	private final static String MKDIR_TEXT = Translator.get("command_bar.mkdir");
	private final static String DELETE_TEXT = Translator.get("command_bar.delete");
	private final static String REFRESH_TEXT = Translator.get("command_bar.refresh");
	private final static String CLOSE_TEXT = Translator.get("command_bar.close");
	

    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	

    
	public CommandBarPanel(MainFrame mainFrame) {
		super(new GridLayout(0,8));

        this.mainFrame = mainFrame;

		viewButton = addButton(VIEW_TEXT);
		editButton = addButton(EDIT_TEXT);
		copyButton = addButton(COPY_TEXT);
		moveButton = addButton(MOVE_TEXT);
		mkdirButton = addButton(MKDIR_TEXT);
		deleteButton = addButton(DELETE_TEXT);
		refreshButton = addButton(REFRESH_TEXT);
		closeButton = addButton(CLOSE_TEXT);

		// Shift+F6 renames a file but since shift-clicks cannot be caught using
		// an ActionListener (because of a known bug), we use a MouseListener
		moveButton.addMouseListener(this);
		// Same for copy button
		copyButton.addMouseListener(this);
	}

	
	private JButton addButton(String caption) {
		JButton button = new JButton(caption);
		button.setMargin(new Insets(1,1,1,1));
		// For Mac OS X whose minimum width for buttons is enormous
		button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getWidth()));
		button.addActionListener(this);
//        button.setToolTipText(caption);
		add(button);
		return button;
	}

	
	/**
	 * Sets shift mode on or off : some buttons such as 'F6 Move' may want to indicate
	 * the action is different when shift is pressed.
	 */
	public void setShiftMode(boolean on) {
		moveButton.setText(on?RENAME_TEXT:MOVE_TEXT);
		moveButton.repaint();
	}

	
	private void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);

		// FileTable lost focus
		mainFrame.getLastActiveTable().requestFocus();
	}


	/**
	 * Fires up the appropriate file viewer for the selected file.
	 */
	public void doView() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
//		if(file.isFolder() && !(file instanceof ArchiveFile))
		if(file.isDirectory())
			return;
		
		try {
			FileViewer viewer = ViewerRegistrar.getViewer(file);
			// Default file viewer
			if (viewer==null) {
				// we should ask the user if he wants to try the default viewer
				viewer = new TextViewer();
			}

			// Tests if file is too large to be viewed and warns user
			long max = viewer.getMaxRecommendedSize();
			if (max!=-1 && file.getSize()>max) {
				QuestionDialog dialog = new QuestionDialog(mainFrame, "Warning", "This file may be too large to be viewed", mainFrame, 
					new String[] {"Open anyway", "Cancel"},
					new int[]  {0, 1},
					new int[]  {KeyEvent.VK_O, KeyEvent.VK_C},
					0);

                int ret = dialog.getActionValue();
				
				if (ret==1 || ret==-1)
					return;
			}
			
			// All OK, start viewing
			viewer.startViewing(file, true);
			
			JFrame frame = new ViewerFrame(file.getAbsolutePath());
			
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(viewer, BorderLayout.CENTER);
			
			// Sets panel to preferred size, without exceeding a maximum size and with a minumum size
			frame.pack();
			Dimension d = frame.getSize();
			Dimension screend = Toolkit.getDefaultToolkit().getScreenSize();
			
			// width is 800 max and 480 min
			int width = Math.max(480, Math.min(d.width, Math.min(screend.width-44, 800-44)));
			
			// height is 3/4 of width
			frame.setSize(
				width, 
				(int)(width*3/((float)4))
			);

			frame.setResizable(true);
			frame.setVisible(true);
		}
		catch(IOException e) {
			showErrorDialog("Unable to view file.", "View error");
		}
	}
	
	
	/**
	 * Fires up the appropriate file editor for the selected file.
	 */
	public void doEdit() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
//		if(file.isFolder() && !(file instanceof ArchiveFile))
		if(file.isDirectory() && !file.isSymlink())
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
				QuestionDialog dialog = new QuestionDialog(mainFrame, "Warning", "This file may be too large to be edited", mainFrame,
                        new String[] {"Open anyway", "Cancel"},
                        new int[]  {0, 1},
                        new int[]  {KeyEvent.VK_O, KeyEvent.VK_C},
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
			showErrorDialog("Unable to edit file.", "Edit error");
		}
	}
	
	
	public void doRefresh()  {
		try  {
			mainFrame.getLastActiveTable().refresh();
		}
		catch(IOException e)  {
		}
	}
	

	public void doExit()  {
		Launcher.getLauncher().disposeMainFrame(mainFrame);
	}
	
	
	///////////////////////////////////////////////////////
	// MouseListener methods to catch shift-clicked buttons
	///////////////////////////////////////////////////////
	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		
		if(source == moveButton) {
			new MoveDialog(mainFrame, e.isShiftDown());
		}
		else if(source == copyButton) {
			new CopyDialog(mainFrame, false, e.isShiftDown());	
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
	


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if(source == viewButton) {
        	doView();
		}
        else if(source == editButton) {
        	doEdit();
		}
        else if(source == mkdirButton) {
            new MkdirDialog(mainFrame);
        }
        else if(source == deleteButton) {
            new DeleteDialog(mainFrame);
        }
        else if(source == refreshButton) {
			doRefresh();
		}
        else if(source == closeButton) {
        	doExit();
		}

        // FileTable lost focus since a button was clicked
        mainFrame.getLastActiveTable().requestFocus();
    }
}