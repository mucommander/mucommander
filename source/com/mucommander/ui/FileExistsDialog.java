
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.comp.dialog.YBoxPanel;

import com.mucommander.file.AbstractFile;

import com.mucommander.text.Translator;
import com.mucommander.text.SizeFormatter;

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
	public final static int OVERWRITE_IF_OLDER_ACTION = 7;
	public final static int OVERWRITE_ALL_OLDER_ACTION = 8;

	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String SKIP_TEXT = Translator.get("skip");
	private final static String OVERWRITE_TEXT = Translator.get("overwrite");
	private final static String OVERWRITE_IF_OLDER_TEXT = Translator.get("overwrite_if_older");
	private final static String APPEND_TEXT = Translator.get("append");
//	private final static String SKIP_ALL_TEXT = Translator.get("skip_all");
//	private final static String OVERWRITE_ALL_TEXT = Translator.get("overwrite_all");
//	private final static String APPEND_ALL_TEXT = Translator.get("append_all");
//	private final static String OVERWRITE_ALL_OLDER_TEXT = Translator.get("overwrite_all_older");

	
	private JCheckBox applyToAllCheckBox;

	
	public FileExistsDialog(Dialog parent, Component locationRelative, AbstractFile sourceFile, AbstractFile destFile) {
	    super(parent, Translator.get("file_exists_dialog.title"), locationRelative);
 
//	   	JPanel panel = new JPanel(new GridLayout(0,1));
		YBoxPanel panel = new YBoxPanel();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
//    	NumberFormat numberFormat = NumberFormat.getInstance();
    	panel.add(new JLabel("Source: "+sourceFile.getAbsolutePath()));
//    	panel.add(new JLabel("  "+numberFormat.format(sourceFile.getSize())
//    			+" bytes, "+dateFormat.format(new Date(sourceFile.getDate()))));
    	panel.add(new JLabel("  "+SizeFormatter.format(sourceFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
    			+", "+dateFormat.format(new Date(sourceFile.getDate()))));
    	panel.addSpace(10);
    	panel.add(new JLabel("Destination: "+destFile.getAbsolutePath()));
//    	panel.add(new JLabel("  "+numberFormat.format(destFile.getSize())
//    			+" bytes, "+dateFormat.format(new Date(destFile.getDate()))));
    	panel.add(new JLabel("  "+SizeFormatter.format(destFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
    			+", "+dateFormat.format(new Date(destFile.getDate()))));
    	
    	init(parent, panel,
//    		new String[] {SKIP_TEXT, OVERWRITE_TEXT, APPEND_TEXT, SKIP_ALL_TEXT, OVERWRITE_ALL_TEXT, APPEND_ALL_TEXT, CANCEL_TEXT, OVERWRITE_ALL_OLDER_TEXT},
//    		new int[]  {SKIP_ACTION, OVERWRITE_ACTION, APPEND_ACTION, SKIP_ALL_ACTION, OVERWRITE_ALL_ACTION, APPEND_ALL_ACTION, CANCEL_ACTION, OVERWRITE_ALL_OLDER_ACTION},
    		new String[] {SKIP_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT, APPEND_TEXT, CANCEL_TEXT},
    		new int[]  {SKIP_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION, APPEND_ACTION, CANCEL_ACTION},
    		3);
			
		mainPanel.addSpace(10);
		applyToAllCheckBox = new JCheckBox(Translator.get("apply_to_all"));
		mainPanel.add(applyToAllCheckBox);
	}

	
	public FileExistsDialog(Dialog parent, Component locationRelative, String sourceURL, AbstractFile destFile) {
	    super(parent, "File already exists in destination", locationRelative);
 
	   	JPanel panel = new JPanel(new GridLayout(0,1));
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
//    	NumberFormat numberFormat = NumberFormat.getInstance();
    	panel.add(new JLabel("Source: "+sourceURL));
    	panel.add(new JLabel(""));
    	panel.add(new JLabel("Destination: "+destFile.getAbsolutePath()));
//    	panel.add(new JLabel("  "+numberFormat.format(destFile.getSize())
//    			+" bytes, "+dateFormat.format(new Date(destFile.getDate()))));
    	panel.add(new JLabel("  "+SizeFormatter.format(destFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
    			+", "+dateFormat.format(new Date(destFile.getDate()))));

    	
    	init(parent, panel,
    		new String[] {OVERWRITE_TEXT, APPEND_TEXT, CANCEL_TEXT},
    		new int[]  {OVERWRITE_ACTION, APPEND_ACTION, CANCEL_ACTION},
    		3);
	}
	

	public int getActionValue() {
		int ret = super.getActionValue();
		if(applyToAllCheckBox.isSelected()) {
			switch(ret) {
				case SKIP_ACTION:
					return SKIP_ALL_ACTION;
				case OVERWRITE_ACTION:
					return OVERWRITE_ALL_ACTION;
				case OVERWRITE_IF_OLDER_ACTION:
					return OVERWRITE_ALL_OLDER_ACTION;
				case APPEND_ACTION:
					return APPEND_ALL_ACTION;
			};
		}
		
		return ret;
	}

}
