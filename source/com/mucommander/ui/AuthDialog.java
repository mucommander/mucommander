
package com.mucommander.ui;


import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


public class AuthDialog extends FocusDialog {

	public AuthDialog(MainFrame mainFrame) {
		super(mainFrame, Translator.get("server_connect_dialog.server_connect"), mainFrame);
	}
	
	
	
}
