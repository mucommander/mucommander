
package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;

import com.mucommander.PlatformManager;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Contextual popup menu invoked by FileTable when right-clicking on a file or a group of files.
 * <p>The following items are displayed :<br>
 * Open
 * Open in Finder		-> If Mac OS X
 * ----
 * View					-> If single file
 * Edit					-> If single file
 * ----
 * Copy
 * Rename				-> If single file or folder
 * Move
 * Zip
 * Unzip				-> If Zip archive
 * Email
 * Delete
 * ----
 * Mark all
 * Unmark all
 * ----
 * Refresh
 * Properties
 */
public class FolderPopupMenu extends JPopupMenu implements ActionListener {

	private MainFrame mainFrame;
	private FileSet selectedFiles;

	private JMenuItem openItem;
	private JMenuItem finderItem;
	private JMenuItem mkdirItem;
	private JMenuItem viewItem;
	private JMenuItem editItem;
	private JMenuItem copyItem;
	private JMenuItem renameItem;
	private JMenuItem moveItem;
	private JMenuItem zipItem;
	private JMenuItem unzipItem;
	private JMenuItem emailItem;
	private JMenuItem deleteItem;
	private JMenuItem markAllItem;
	private JMenuItem unmarkAllItem;
	private JMenuItem refreshItem;
	private JMenuItem propertiesItem;


	public FolderPopupMenu(MainFrame mainFrame, FileSet selectedFiles) {
		this.mainFrame = mainFrame;
		this.selectedFiles = selectedFiles;
	
		if(selectedFiles.size()==0) {
			if(com.mucommander.PlatformManager.getOSFamily()==com.mucommander.PlatformManager.MAC_OS_X)
				finderItem = addMenuItem("Open in Finder");
		}
		else {
			boolean singleFile = selectedFiles.size()==1;
			boolean singleDirectory = singleFile && selectedFiles.fileAt(0).isDirectory();

			if(singleFile && !singleDirectory) {
				openItem = addMenuItem("Open");
				if(com.mucommander.PlatformManager.getOSFamily()==com.mucommander.PlatformManager.MAC_OS_X)
					finderItem = addMenuItem("Open in Finder");
				add(new JSeparator());
			
				viewItem = addMenuItem("View");
				editItem = addMenuItem("Edit");
				add(new JSeparator());
			}
			
			copyItem = addMenuItem("Copy");
			moveItem = addMenuItem("Move");
			if(singleFile)
				renameItem = addMenuItem("Rename");
//			zipItem = addMenuItem("Zip");
//			if(singleFile)
//			unzipItem = addMenuItem("Unzip");
//			emailItem = addMenuItem("Email");
			deleteItem = addMenuItem("Delete");
		}

		add(new JSeparator());
		markAllItem = addMenuItem("Mark all");
		unmarkAllItem = addMenuItem("Unmark all");
		add(new JSeparator());
		mkdirItem = addMenuItem("Make dir");
		refreshItem = addMenuItem("Refresh");
		propertiesItem = addMenuItem("Properties");
	}


	/**
	 * Creates, adds a new item to the popup menu and returns it.
	 */
	private JMenuItem addMenuItem(String text) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(this);
		add(menuItem);
		return menuItem;
	}


	////////////////////////////
	// ActionListener methods //
	////////////////////////////
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if(source==openItem) {
			AbstractFile fileToOpen = selectedFiles.fileAt(0);
			if(fileToOpen.isBrowsable()) {
				mainFrame.getFolderPanel1().trySetCurrentFolder(fileToOpen, true);
			}
			else {
				// Tries to execute file
				PlatformManager.open(fileToOpen.getAbsolutePath(), selectedFiles.getBaseFolder());
			}
		}
		else if(source==finderItem) {
			// Opens selected file/folder (is selection is not empty) or current folder (if selection is empty)
			// in Finder
			PlatformManager.openInFinder(
				selectedFiles.size()==0?selectedFiles.getBaseFolder().getAbsolutePath():selectedFiles.fileAt(0).getAbsolutePath(),
				selectedFiles.getBaseFolder());
		}
		else if(source==mkdirItem) {
            new MkdirDialog(mainFrame);
		}
		else if(source==viewItem) {
			mainFrame.getCommandBar().doView();
		}
		else if(source==editItem) {
			mainFrame.getCommandBar().doEdit();
		}
		else if(source==copyItem) {
			new CopyDialog(mainFrame, selectedFiles, false);
		}
		else if(source==renameItem) {
			new MoveDialog(mainFrame, selectedFiles, true);
		}
		else if(source==moveItem) {
			new MoveDialog(mainFrame, selectedFiles, false);
		}
		else if(source==zipItem) {
			new ZipDialog(mainFrame, selectedFiles, false);
		}
		else if(source==unzipItem) {
			new UnzipDialog(mainFrame, selectedFiles, false);
		}
		else if(source==emailItem) {
			new EmailFilesDialog(mainFrame, selectedFiles);
		}
		else if(source==deleteItem) {
			new DeleteDialog(mainFrame, selectedFiles);
		}
		else if(source==markAllItem) {
			mainFrame.getLastActiveTable().markAll();	
		}
		else if(source==unmarkAllItem) {
			mainFrame.getLastActiveTable().unmarkAll();	
		}
		else if(source==refreshItem) {
			mainFrame.getLastActiveTable().getFolderPanel().tryRefresh();
		}
		else if(source==propertiesItem) {
			FileSet fileSet;
			if(selectedFiles.size()==0) {
				fileSet = (FileSet)selectedFiles.clone();
				fileSet.add(selectedFiles.getBaseFolder());
			}
			else
				fileSet = selectedFiles;
			new PropertiesDialog(mainFrame, fileSet).showDialog();
		}
	}
}