 
package com.mucommander.ui.viewer;

import com.mucommander.text.Translator;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.DialogToolkit;

import com.mucommander.file.AbstractFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


//public class ViewerFrame extends JFrame implements ActionListener, WindowListener {
public class ViewerFrame extends JFrame implements ActionListener {
	private JMenuItem closeItem;
	
	private MainFrame mainFrame;
	private AbstractFile file;
	
	public final static Color BG_COLOR = new Color(0xFFFFFF);
	
	
	public ViewerFrame(MainFrame mainFrame, AbstractFile file) {
		super();
	
		this.mainFrame = mainFrame;
		setCurrentFile(file);
		
		getContentPane().setLayout(new BorderLayout());
//		getContentPane().setLayout(new GridLayout(1,0));
		
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

	
	public MainFrame getMainFrame() {
		return mainFrame;
	}
	
	void setViewer(FileViewer viewer) {
//		setBackground(BG_COLOR);
JScrollPane scrollPane = new JScrollPane(viewer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
//JScrollPane scrollPane = new JScrollPane(viewer, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}
	};
//System.out.println("scrollPane insets = "+scrollPane.getInsets());
		
//		scrollPane.setBackground(BG_COLOR);
		JViewport viewport = scrollPane.getViewport();
//System.out.println("viewport insets = "+viewport.getInsets());
		viewport.setBackground(BG_COLOR);
//		viewport.setBorder(null);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
//System.out.println("contentPane insets = "+getContentPane().getInsets());
//System.out.println("viewer insets = "+viewer.getInsets());
		// Request focus on text area when visible
		FocusRequester.requestFocus(viewer);
	}
	
	public void setCurrentFile(AbstractFile file) {
		this.file = file;
		setTitle(file.getAbsolutePath());
	}


	public void pack() {
		super.pack();

		DialogToolkit.fitToScreen(this);
		DialogToolkit.fitToMinDimension(this, new Dimension(160, 120));
	}
	
	public void show() {
		// Sets panel to preferred size, without exceeding a maximum size and with a minumum size
		pack();
		super.show();
	}

	
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