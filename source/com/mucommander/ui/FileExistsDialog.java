
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


/**
 * Dialog invoked to ask the user what to do when a file is to be transferred in a folder
 * where a file with the same name already exists.
 *
 * @author Maxence Bernard
 */
public class FileExistsDialog extends QuestionDialog {

	public final static int CANCEL_ACTION = 0;
	public final static int SKIP_ACTION = 1;
	public final static int OVERWRITE_ACTION = 2;
	public final static int APPEND_ACTION = 3;
	public final static int OVERWRITE_IF_OLDER_ACTION = 4;

	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String SKIP_TEXT = Translator.get("skip");
	private final static String OVERWRITE_TEXT = Translator.get("overwrite");
	private final static String OVERWRITE_IF_OLDER_TEXT = Translator.get("overwrite_if_older");
	private final static String APPEND_TEXT = Translator.get("append");

	
	private JCheckBox applyToAllCheckBox;

	
	public FileExistsDialog(Dialog parent, Component locationRelative, AbstractFile sourceFile, AbstractFile destFile, boolean applyToAllOption) {
	    super(parent, Translator.get("file_exists_dialog.title"), locationRelative);
		init(sourceFile, destFile, applyToAllOption);
	}

	public FileExistsDialog(Frame parent, Component locationRelative, AbstractFile sourceFile, AbstractFile destFile, boolean applyToAllOption) {
	    super(parent, Translator.get("file_exists_dialog.title"), locationRelative);
		init(sourceFile, destFile, applyToAllOption);
	}

	
	private void init(AbstractFile sourceFile, AbstractFile destFile, boolean applyToAllOption) {
		YBoxPanel panel = new YBoxPanel();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
    	panel.add(new JLabel("Source: "+sourceFile.getAbsolutePath()));
    	panel.add(new JLabel("  "+SizeFormatter.format(sourceFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
    			+", "+dateFormat.format(new Date(sourceFile.getDate()))));
    	panel.addSpace(10);
		// Use canonical path for destination, to resolve '..', '.' and '~'
    	panel.add(new JLabel("Destination: "+destFile.getCanonicalPath()));
    	panel.add(new JLabel("  "+SizeFormatter.format(destFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
    			+", "+dateFormat.format(new Date(destFile.getDate()))));
    	
    	init(parent, panel,
    		new String[] {SKIP_TEXT, OVERWRITE_TEXT, OVERWRITE_IF_OLDER_TEXT, APPEND_TEXT, CANCEL_TEXT},
    		new int[]  {SKIP_ACTION, OVERWRITE_ACTION, OVERWRITE_IF_OLDER_ACTION, APPEND_ACTION, CANCEL_ACTION},
    		3);
		
		if(applyToAllOption) {
			applyToAllCheckBox = new JCheckBox(Translator.get("apply_to_all"));
			addCheckBox(applyToAllCheckBox);
		}
	}

	
	public FileExistsDialog(Dialog parent, Component locationRelative, String sourceURL, AbstractFile destFile) {
	    super(parent, "File already exists in destination", locationRelative);
 
	   	JPanel panel = new JPanel(new GridLayout(0,1));
    	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
    	panel.add(new JLabel("Source: "+sourceURL));
    	panel.add(new JLabel(""));
    	panel.add(new JLabel("Destination: "+destFile.getAbsolutePath()));
    	panel.add(new JLabel("  "+SizeFormatter.format(destFile.getSize(), SizeFormatter.DIGITS_FULL|SizeFormatter.UNIT_LONG|SizeFormatter.INCLUDE_SPACE)
    			+", "+dateFormat.format(new Date(destFile.getDate()))));

    	
    	init(parent, panel,
    		new String[] {OVERWRITE_TEXT, APPEND_TEXT, CANCEL_TEXT},
    		new int[]  {OVERWRITE_ACTION, APPEND_ACTION, CANCEL_ACTION},
    		3);
	}
	

	/**
	 * Returns <code>true</code> if the 'apply to all' checkbox has been selected.
	 */
	public boolean applyToAllSelected() {
		return applyToAllCheckBox==null?false:applyToAllCheckBox.isSelected();
	}
	
}
