
package com.mucommander.ui;

import com.mucommander.Launcher;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainMenuBar extends JMenuBar implements ActionListener, LocationListener, MenuListener {
	private MainFrame mainFrame;	
	
	// File menu
	private JMenu fileMenu;
	private JMenuItem newWindowItem;
	private JMenuItem serverConnectItem;
	private JMenuItem zipItem;
	private JMenuItem unzipItem;
	private JMenuItem propertiesItem;
	private JMenuItem preferencesItem;
	private JMenuItem quitItem;

	// Mark menu
	private JMenu markMenu;
	private JMenuItem markGroupItem;
	private JMenuItem unmarkGroupItem;
	private JMenuItem markAllItem;
	private JMenuItem unmarkAllItem;
	private JMenuItem invertSelectionItem;
	private JMenuItem compareDirectoriesItem;

	// View menu
	private JMenu viewMenu;
	private JCheckBoxMenuItem sortByNameItem;
	private JCheckBoxMenuItem sortBySizeItem;
	private JCheckBoxMenuItem sortByDateItem;
	private JCheckBoxMenuItem sortByExtensionItem;
	private JMenuItem reverseOrderItem;
	private JMenuItem goBackItem;
	private JMenuItem goForwardItem;
	private JMenuItem swapFoldersItem;
	private JMenuItem setSameFolderItem;
	private JMenuItem refreshItem;

	// Window menu
	private JMenu windowMenu;
//	private JMenuItem previousWindowItem;
//	private JMenuItem nextWindowItem;

	// Help menu
	private JMenu helpMenu;
	private JMenuItem keysItem;
	private JMenuItem aboutItem;
	
	
	public MainMenuBar(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		// File menu
		fileMenu = addMenu("File", KeyEvent.VK_F, true);
		newWindowItem = addMenuItem(fileMenu, "New window", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		fileMenu.add(new JSeparator());
		serverConnectItem = addMenuItem(fileMenu, "Connect to Server...", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		zipItem = addMenuItem(fileMenu, "Zip...", KeyEvent.VK_Z, KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		unzipItem = addMenuItem(fileMenu, "Unzip...", KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		propertiesItem = addMenuItem(fileMenu, "Properties", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.ALT_MASK));

		fileMenu.add(new JSeparator());
		preferencesItem = addMenuItem(fileMenu, "Preferences...", KeyEvent.VK_R, null);
		
		fileMenu.add(new JSeparator());
		quitItem = addMenuItem(fileMenu, "Quit", KeyEvent.VK_Q, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
	
		// Mark menu
		markMenu = addMenu("Mark", KeyEvent.VK_M, false);
		markGroupItem = addMenuItem(markMenu, "Mark...", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0));
		unmarkGroupItem = addMenuItem(markMenu, "Unmark...", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0));
		markAllItem = addMenuItem(markMenu, "Mark all", KeyEvent.VK_M, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		unmarkAllItem = addMenuItem(markMenu, "Unmark all", KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		invertSelectionItem = addMenuItem(markMenu, "Invert selection", KeyEvent.VK_I, KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0));

		markMenu.add(new JSeparator());
		compareDirectoriesItem = addMenuItem(markMenu, "Compare directories", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

		// View menu
		viewMenu = addMenu("View", KeyEvent.VK_V, false);
		goBackItem = addMenuItem(viewMenu, "Go back", KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK));
		goForwardItem = addMenuItem(viewMenu, "Go forward", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK));
		viewMenu.add(new JSeparator());
		sortByNameItem = addCheckBoxMenuItem(viewMenu, "Sort by Name", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.CTRL_MASK));
		sortByDateItem = addCheckBoxMenuItem(viewMenu, "Sort by Date", KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK));
		sortBySizeItem = addCheckBoxMenuItem(viewMenu, "Sort by Size", KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.CTRL_MASK));
		sortByExtensionItem = addCheckBoxMenuItem(viewMenu, "Sort by Extension", KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_MASK));
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(sortByNameItem);
		buttonGroup.add(sortByDateItem);
		buttonGroup.add(sortBySizeItem);
		buttonGroup.add(sortByExtensionItem);
		sortByNameItem.setState(true);

		reverseOrderItem = addMenuItem(viewMenu, "Reverse order", KeyEvent.VK_R, null);
		viewMenu.add(new JSeparator());
		swapFoldersItem = addMenuItem(viewMenu, "Swap panels", KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
		setSameFolderItem = addMenuItem(viewMenu, "Set same folder", KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
//		refreshItem = addMenuItem(viewMenu, "Refresh", KeyEvent.VK_R, null);

		// Window menu
		windowMenu = addMenu("Window", KeyEvent.VK_W, false);
		
		// Help menu
		helpMenu = addMenu("Help", KeyEvent.VK_H, false);
		keysItem = addMenuItem(helpMenu, "Shortcuts", KeyEvent.VK_K, null);
		helpMenu.add(new JSeparator());
		aboutItem = addMenuItem(helpMenu, "About...", KeyEvent.VK_A, null);		
	}
	
	/**
	 * Returns a new JMenu.
	 */
	private JMenu addMenu(String title, int mnemonic, boolean addMenuListener) {
		JMenu menu = new JMenu(title);
		menu.setMnemonic(mnemonic);
		if(addMenuListener)
			menu.addMenuListener(this);
		add(menu);
		return menu;
	}
	
	/**
	 * Creates a new JMenuItem and adds it to the given JMenu.
	 * @param accelerator can be <code>null</code>
	 */
	private JMenuItem addMenuItem(JMenu menu, String title, int mnemonic, KeyStroke accelerator) {
		return addMenuItem(menu, title, mnemonic, accelerator, false);
	}


	/**
	 * Creates a new JCheckBoxMenuItem initially unselected and adds it to the given JMenu.
	 * @param accelerator can be <code>null</code>
	 */
	private JCheckBoxMenuItem addCheckBoxMenuItem(JMenu menu, String title, int mnemonic, KeyStroke accelerator) {
		return (JCheckBoxMenuItem)addMenuItem(menu, title, mnemonic, accelerator, true);
	}


	/**
	 * Creates a new JMenuItem or JCheckBoxMenuItem and adds it to the given JMenu.
	 * @param accelerator can be <code>null</code>
	 */
	private JMenuItem addMenuItem(JMenu menu, String title, int mnemonic, KeyStroke accelerator, boolean checkBoxMenuItem) {
		JMenuItem menuItem = checkBoxMenuItem?new JCheckBoxMenuItem(title, false):new JMenuItem(title);
		if(mnemonic!=0)
			menuItem.setMnemonic(mnemonic);
		if(accelerator!=null)
			menuItem.setAccelerator(accelerator);
		menuItem.addActionListener(this);
		menu.add(menuItem);
	
		return menuItem;
	}
	
	/**
	 * Called to notify that sort order has changed and that menu items need to reflect
	 * that change.
	 *
	 * @param criteria sort-by criteria.
	 */	
	public void sortOrderChanged(int criteria) {
		switch (criteria) {
			case FileTableModel.NAME:
				sortByNameItem.setState(true);
				break;
			case FileTableModel.DATE:
				sortByDateItem.setState(true);
				break;
			case FileTableModel.SIZE:
				sortBySizeItem.setState(true);
				break;
			case FileTableModel.EXTENSION:
				sortByExtensionItem.setState(true);
				break;
		}
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}
	
	public JMenu getWindowMenu() {
		return windowMenu;	
	}

	public void locationChanged(FolderPanel folderPanel) {
		goBackItem.setEnabled(folderPanel.hasBackFolder());
		goForwardItem.setEnabled(folderPanel.hasForwardFolder());
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		// File menu
		if (source == newWindowItem) {
			Launcher.getLauncher().createNewMainFrame();
		}
		else if (source == propertiesItem) {
			mainFrame.showPropertiesDialog();
		}
		else if (source == serverConnectItem) {
			mainFrame.showServerConnectDialog();
		}
		else if (source == zipItem) {
			new ZipDialog(mainFrame).show();
		}
		else if (source == unzipItem) {
			mainFrame.getCommandBar().showCopyDialog(true);
		}
		else if (source == preferencesItem) {
			mainFrame.showPreferencesDialog();
		}
		else if (source == quitItem) {
			Launcher.getLauncher().disposeMainFrame(mainFrame);
		}
		// Mark menu
		else if (source == markGroupItem) {
			mainFrame.getLastActiveTable().markGroup();	
		}
		else if (source == unmarkGroupItem) {
			mainFrame.getLastActiveTable().unmarkGroup();	
		}
		else if (source == markAllItem) {
			mainFrame.getLastActiveTable().markAll();	
		}
		else if (source == unmarkAllItem) {
			mainFrame.getLastActiveTable().unmarkAll();	
		}
		else if (source == invertSelectionItem) {
			mainFrame.getLastActiveTable().invertSelection();	
		}
		else if (source == compareDirectoriesItem) {
			mainFrame.compareDirectories();	
		}
		// View menu
		else if (source == goBackItem) {
			mainFrame.getLastActiveTable().getBrowser().goBack();	
		}
		else if (source == goForwardItem) {
			mainFrame.getLastActiveTable().getBrowser().goForward();	
		}
		else if (source == sortByNameItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.NAME);	
		}
		else if (source == sortBySizeItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.SIZE);	
		}
		else if (source == sortByDateItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.DATE);	
		}
		else if (source == sortByExtensionItem) {
			mainFrame.getLastActiveTable().sortBy(FileTable.EXTENSION);	
		}
		else if (source == reverseOrderItem) {
			mainFrame.getLastActiveTable().reverseSortOrder();	
		}
		else if (source == swapFoldersItem) {
			mainFrame.swapFolders();	
		}
		else if (source == setSameFolderItem) {
			mainFrame.setSameFolder();	
		}
		// Help menu
		else if (source == keysItem) {
			new ShortcutsDialog(mainFrame).showDialog();
		}
		else if (source == aboutItem) {
			new AboutDialog(mainFrame).showDialog();
		}
	}


	/************************
	 * MenuListener methods *
	 ************************/

	public void menuSelected(MenuEvent e) {
	 	Object source = e.getSource();
		
		if (source==fileMenu) {
			boolean filesSelected = mainFrame.getLastActiveTable().getSelectedFiles().size()!=0;
			
			// disables menu items if no file is selected
			propertiesItem.setEnabled(filesSelected);
			zipItem.setEnabled(filesSelected);
			unzipItem.setEnabled(filesSelected);
		}
	}
	
	public void menuDeselected(MenuEvent e) {
	}
	 
	public void menuCanceled(MenuEvent e) {
	}
}