
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.*;

import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;

import com.mucommander.conf.ConfigurationManager;

import com.mucommander.text.Translator;


public class TextViewer extends FileViewer implements ActionListener {

//	private String encoding;

	private JMenuItem copyItem;
	private JMenuItem selectAllItem;
	
	private JTextArea textArea;
	
	
	public TextViewer(ViewerFrame frame) {
		super(frame);
//		this.encoding = ConfigurationManager.getVariable("prefs.text_viewer.last_encoding", "utf-8");

		setLayout(new BorderLayout());

		// Create default menu
		MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
		MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

		// Edit menu
		JMenuBar menuBar = frame.getJMenuBar();
		JMenu editMenu = MenuToolkit.addMenu(Translator.get("text_viewer.edit"), menuMnemonicHelper, null);
		copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.copy"), menuItemMnemonicHelper, null, this);
		editMenu.add(copyItem);
		selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.select_all"), menuItemMnemonicHelper, null, this);
		editMenu.add(selectAllItem);
		menuBar.add(editMenu);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
	}

	
	public void startViewing(AbstractFile fileToView, boolean isSeparateWindow) throws IOException {
/*		
		byte b[] = new byte[(int)fileToView.getSize()];
		DataInputStream din = new DataInputStream(fileToView.getInputStream());
		din.readFully(b);
		din.close();
*/

//		JTextArea textArea = new JTextArea(new String(b));
		textArea.read(new InputStreamReader(fileToView.getInputStream()), null);
		textArea.setCaretPosition(0);
	}
	
	public static boolean canViewFile(AbstractFile file) {
		String name = file.getName();
		String nameLowerCase = name.toLowerCase();
		return nameLowerCase.endsWith(".txt")
			||nameLowerCase.endsWith(".conf")
			||nameLowerCase.endsWith(".xml")
			||nameLowerCase.endsWith(".ini")
			||nameLowerCase.endsWith(".nfo")
			||nameLowerCase.endsWith(".diz")
			||nameLowerCase.endsWith(".sh")
			||nameLowerCase.equals("readme")
			||nameLowerCase.equals("read.me")
			||nameLowerCase.equals("license");
	}

	public long getMaxRecommendedSize() {
		return 131072;
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == copyItem)
			textArea.copy();
		else if(source == selectAllItem)
			textArea.selectAll();
	}
}