
package com.mucommander.ui.viewer;

import com.mucommander.text.Translator;

import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ViewerFrame extends JFrame implements ActionListener, WindowListener {
	private JMenuItem closeItem;
	
	public ViewerFrame(String title) {
		super(title);

		// Create default menu
		MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
		MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

		// File menu
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = MenuToolkit.addMenu(Translator.get("text_viewer.file"), menuMnemonicHelper, null);
		closeItem = MenuToolkit.addMenuItem(menu, Translator.get("text_viewer.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
		menu.add(closeItem);

		// Add menu to frame
		menuBar.add(menu);
		setJMenuBar(menuBar);

		// Catches window close event
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
	}


	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==closeItem)
			close();
	}

	private void close() {
		setVisible(false);
	}

    /**************************
     * WindowListener methods *
     **************************/	

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
}