
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
		
		getContentPane().setLayout(new BorderLayout());
		
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
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
	}

	
	void setViewer(FileViewer viewer) {
		getContentPane().add(viewer, BorderLayout.CENTER);
	}
	
	
	public void show() {
		// Sets panel to preferred size, without exceeding a maximum size and with a minumum size
		super.pack();
/*
		Dimension d = getSize();
		Dimension screend = Toolkit.getDefaultToolkit().getScreenSize();
			
		// width is 800 max and 480 min
		int width = Math.max(480, Math.min(d.width, Math.min(screend.width-44, 800-44)));
			
		// height is 3/4 of width
		setSize(
			width, 
			(int)(width*3/((float)4))
		);
*/
		super.setResizable(true);
		super.show();
	}

	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==closeItem)
			close();
	}

	private void close() {
//		setVisible(false);
		dispose();
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