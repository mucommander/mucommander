 
package com.mucommander.ui.viewer;

import com.mucommander.text.Translator;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.*;

import com.mucommander.file.AbstractFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ViewerFrame extends JFrame implements ActionListener, Runnable {
	private JMenuItem closeItem;
	
	private MainFrame mainFrame;
	private AbstractFile file;
	private FileViewer viewer;
	
	
	public ViewerFrame(MainFrame mainFrame, AbstractFile file) {
		super();
	
		this.mainFrame = mainFrame;
		this.file = file;
		
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
//		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
//		addWindowListener(this);

		setResizable(true);
	}

	
	public void run() {
		try {
			FileViewer viewer = ViewerRegistrar.getViewer(file);

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

			viewer.setFrame(this);
			viewer.setCurrentFile(file);
			setViewer(viewer);

			// Sets panel to preferred size, without exceeding a maximum size and with a minumum size
			pack();
			super.show();
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(mainFrame, Translator.get("file_viewer.view_error"), Translator.get("file_viewer.view_error_title"), JOptionPane.ERROR_MESSAGE);
if(com.mucommander.Debug.ON) e.printStackTrace();
		}
	}

	
//	public MainFrame getMainFrame() {
//		return mainFrame;
//	}
	
	private void setViewer(FileViewer viewer) {
		this.viewer = viewer;
	
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
		viewport.setBackground(FileViewer.BG_COLOR);
//		viewport.setBorder(null);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
//System.out.println("contentPane insets = "+getContentPane().getInsets());
//System.out.println("viewer insets = "+viewer.getInsets());
		// Request focus on text area when visible
		FocusRequester.requestFocus(viewer);
	}
	

	public void pack() {
		super.pack();

		setTitle(viewer.getTitle());

		DialogToolkit.fitToScreen(this);
		DialogToolkit.fitToMinDimension(this, new Dimension(160, 120));
	}
	
	
	public void show() {
		new Thread(this).start();
	}

	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==closeItem)
//			close();
			dispose();
	}

	
}