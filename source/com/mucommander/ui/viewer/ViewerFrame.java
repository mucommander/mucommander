 
package com.mucommander.ui.viewer;

import com.mucommander.text.Translator;

import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;

import com.mucommander.file.AbstractFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ViewerFrame extends JFrame implements ActionListener, WindowListener {
	private JMenuItem closeItem;
	
	public ViewerFrame(AbstractFile file) {
		super(file.getAbsolutePath());
		
//		getContentPane().setLayout(new BorderLayout());
		getContentPane().setLayout(new GridLayout(1,0));
		
		// Create default menu
		MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
		MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

		// File menu
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = MenuToolkit.addMenu(Translator.get("file_viewer.file_menu"), menuMnemonicHelper, null);
		closeItem = MenuToolkit.addMenuItem(menu, Translator.get("file_viewer.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
		menu.add(closeItem);

		// Add menu to frame
		menuBar.add(menu);
		setJMenuBar(menuBar);

		// Catches window close event
//		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//		addWindowListener(this);

		setResizable(true);
	}

	
	void setViewer(FileViewer viewer) {
		getContentPane().add(viewer, BorderLayout.CENTER);
//		getContentPane().add(viewer);
	}
	

/**	
	public void show() {
		// Sets panel to preferred size, without exceeding a maximum size and with a minumum size
		super.pack();
		super.show();
	}
*/
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==closeItem)
//			close();
			dispose();
	}

	/*
	private void close() {
//		setVisible(false);
		dispose();
	}
*/
	
    /**************************
     * WindowListener methods *
     **************************/	
/*
    public void windowClosing(WindowEvent e) {
		close();
	}

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }	
*/
}