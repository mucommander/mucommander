
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.QuestionDialog;

import com.mucommander.file.AbstractFile;

import com.mucommander.text.Translator;

import java.awt.*;
import javax.swing.*;

import java.text.SimpleDateFormat;
import java.text.NumberFormat;

import java.util.Date;


public class FileExistsDialog extends QuestionDialog {

	public final static int CANCEL_ACTION = 0;
	public final static int SKIP_ACTION = 1;
	public final static int OVERWRITE_ACTION = 2;
	public final static int APPEND_ACTION = 3;
	public final static int SKIP_ALL_ACTION = 4;
	public final static int OVERWRITE_ALL_ACTION = 5;
	public final static int APPEND_ALL_ACTION = 6;
	public final static int OVERWRITE_ALL_OLDER_ACTION = 7;

	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String SKIP_TEXT = Translator.get("skip");
	private final static String OVERWRITE_TEXT = Translator.get("overwrite");
	private final static String APPEND_TEXT = Translator.get("append");
	private final static String SKIP_ALL_TEXT = Translator.get("skip_all");
	private final static String OVERWRITE_ALL_TEXT = Translator.get("overwrite_all");
	private final static String APPEND_ALL_TEXT = Translator.get("append_all");
	private final static String OVERWRITE_ALL_OLDER_TEXT = Translator.get("overwrite_all_older");

	
	public FileExistsDialog(Dialog parent, Component locationRelative, AbstractFile sourceFile, AbstractFile destFile) {
	    super(parent, "File already exists in destination", locationRelative);
 
	   	JPanel panel = new JPanel(new GridLayout(0,1));
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
    	NumberFormat numberFormat = NumberFormat.getInstance();
    	panel.add(new JLabel("Source: "+sourceFile.getAbsolutePath()));
    	panel.add(new JLabel("  "+numberFormat.format(sourceFile.getSize())
    			+" bytes, "+dateFormat.format(new Date(sourceFile.getDate()))));
    	panel.add(new JLabel(""));
    	panel.add(new JLabel("Destination: "+destFile.getAbsolutePath()));
    	panel.add(new JLabel("  "+numberFormat.format(destFile.getSize())
    			+" bytes, "+dateFormat.format(new Date(destFile.getDate()))));
    	
    	init(parent, panel,
    		new String[] {SKIP_TEXT, OVERWRITE_TEXT, APPEND_TEXT, SKIP_ALL_TEXT, OVERWRITE_ALL_TEXT, APPEND_ALL_TEXT, CANCEL_TEXT, OVERWRITE_ALL_OLDER_TEXT},
    		new int[]  {SKIP_ACTION, OVERWRITE_ACTION, APPEND_ACTION, SKIP_ALL_ACTION, OVERWRITE_ALL_ACTION, APPEND_ALL_ACTION, CANCEL_ACTION, OVERWRITE_ALL_OLDER_ACTION},
    		3);
	}

}
