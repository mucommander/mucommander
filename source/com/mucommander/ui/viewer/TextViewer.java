
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;

import java.awt.*;
import javax.swing.*;
import java.io.DataInputStream;
import java.io.*;

import com.mucommander.conf.ConfigurationManager;

public class TextViewer extends FileViewer {

	private String encoding;
	
	public TextViewer() {
		this.encoding = ConfigurationManager.getVariable("prefs.text_viewer.last_encoding", "utf-8");
	}
	
	public void startViewing(AbstractFile fileToView, boolean isSeparateWindow) throws IOException {
		setLayout(new BorderLayout());

/*		
		byte b[] = new byte[(int)fileToView.getSize()];
		DataInputStream din = new DataInputStream(fileToView.getInputStream());
		din.readFully(b);
		din.close();
*/

//		JTextArea textArea = new JTextArea(new String(b));
		JTextArea textArea = new JTextArea();
		textArea.read(new InputStreamReader(fileToView.getInputStream(), encoding), null);
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
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


	public void actionPerformed() {
		
	}
}