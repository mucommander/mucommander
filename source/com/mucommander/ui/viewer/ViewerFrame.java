
package com.mucommander.ui.viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ViewerFrame extends JFrame implements ActionListener, WindowListener {
	private JMenuItem closeItem;
	
	public ViewerFrame(String title) {
		super(title);
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		
		closeItem = new JMenuItem("Close");
		closeItem.setMnemonic(KeyEvent.VK_C);
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		closeItem.addActionListener(this);
		menu.add(closeItem);

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