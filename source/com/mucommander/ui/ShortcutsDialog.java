package com.mucommander.ui;

import com.mucommander.job.FileJob;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ShortcutsDialog extends FocusDialog implements ActionListener {
	private final static String keys[][][] =  { 
	 	{	{"Navigation"},
			{"ALT+F1", "Change left location"},
		 	{"ALT+F2", "Change right location"},
		 	{"TAB", "Switch between left and right folder"},
		 	{"ENTER", "Enter folder / enter archive / execute"},
			{"Shift+ENTER", "Execute with native file association"},
		 	{"BACKSPACE", "Go to parent folder"},
		 	{"HOME", "Go to first file"},
		 	{"END", "Go to last file"},
	 	},
	 	{	{"View"},
			{"ALT+LEFT", "Go back"},
		 	{"ALT+RIGHT", "Go forward"},
			{"CTRL+F3", "Sort by name"},
			{"CTRL+F4", "Sort by extension"},
			{"CTRL+F5", "Sort by date"},
			{"CTRL+F6", "Sort by size"},
			{"CTRL+U", "Swap folders"},
			{"CTRL+E", "Set same folder"}
		},
		{	{"Selection"},
			{"INSERT/SPACE", "Mark file"},
			{"+", "Add to selection"},
			{"-", "Remove from selection"},
			{"*", "Invert selection"},
			{"CTRL+A", "Mark all"},
			{"CTRL+D", "Unmark all"},
            {"CTRL+M", "Compare directories"}
		},
		{	{"Actions"},
			{"F3", "View"},
			{"F4", "Edit"},
			{"F5", "Copy"},
			{"SHIFT+F5", "Copy to current folder"},
			{"F6", "Move/Rename"},
			{"SHIFT+F6", "Rename"},
			{"F7", "Make directory"},
			{"F8", "Delete"},
			{"F9", "Refresh"},
			{"F10 or ALT+F4", "Quit"}, 
			{"CTRL+K", "Connect to Server"},
			{"CTRL+R", "Run command"},
			{"CTRL+I", "Zip files"},
			{"CTRL+P", "Unzip file"},
			{"ALT+ENTER", "Properties"},
		},
		{	{"Windows"},
			{"CTRL+N", "Create new window"},
			{"CTRL+1, CTRL+2...", "Recall a window"},
			{"CTRL+LEFT", "Switch to previous window"},
		 	{"CTRL+RIGHT", "Switch to next window"},
		},
	};


	public ShortcutsDialog(MainFrame mainFrame) {
		super(mainFrame, "Keyboard shortcuts", mainFrame);
		
		JButton okButton = new JButton("OK");
		setInitialFocusComponent(okButton);
		okButton.addKeyListener(new EscapeKeyAdapter(this));
		setMaximumSize(new Dimension(1000, 360));
		Container contentPane = getContentPane();

		StringBuffer sb = new StringBuffer();
		sb.append("<html><body text=\"#000084\" bgcolor=\"#FFFFFF\">");
		sb.append("<table>");

		for(int i=0; i<keys.length; i++) {
			sb.append("<tr><td><b>"+keys[i][0][0]+"</b></td></tr>");
			
			for(int j=1; j<keys[i].length; j++) {
				sb.append("<tr>");
				sb.append("<td>"+keys[i][j][0]+"</td>");
				sb.append("<td>"+keys[i][j][1]+"</td>");
				sb.append("</tr>");
			}
			sb.append("<tr><td>&nbsp;</td></tr>");
		}

		sb.append("</table>");
		sb.append("</body></html>");
		
		contentPane.add(new JScrollPane(new JLabel(sb.toString()), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

		// Selects OK when enter is pressed
		getRootPane().setDefaultButton(okButton);

		contentPane.add(DialogToolkit.createOKPanel(okButton, this), BorderLayout.SOUTH);
	}


	public void actionPerformed(ActionEvent e) {
		dispose();
	}
}