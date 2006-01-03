package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.*;
import com.mucommander.file.*;
import com.mucommander.text.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This dialog allows the user to add (mark) or remove (unmark)
 * files from current selection, matching a specified keyword.
 */
public class FileSelectionDialog extends FocusDialog implements ActionListener {

	/* Filename comparison */		
	private final static int CONTAINS = 0;
	private final static int STARTS_WITH = 1;
	private final static int ENDS_WIDTH = 2;
	private final static int IS = 3;

	/** Add to or remove from selection ? */	 
	private boolean addToSelection;

	private JComboBox comparisonComboBox;
	private JTextField selectionField;

	private JCheckBox caseSensitiveCheckBox;
	private JCheckBox includeFoldersCheckBox;

	private JButton okButton;
	private JButton cancelButton;
	
	private MainFrame mainFrame;
	
	/** 
	 * Is selection case sensitive? (initially true)
	 * <br>Note: this field is static so the value is kept after the dialog is OKed.
	 */ 
	private static boolean caseSensitive = true;

	/** 
	 * Does the selection include folders? (initially false)
	 * <br>Note: this field is static so the value is kept after the dialog is OKed.
	 */ 
	private static boolean includeFolders = false;
	
	/** 
	 * Filename comparison: contains, starts with, ends with, is ?
	 * <br>Note: this field is static so the value is kept after the dialog is OKed.
	 */ 
	private static int comparison = CONTAINS;

	/** 
	 * Keyword which has last been typed to mark or unmark files.
	 * <br>Note: this field is static so the value is kept after the dialog is OKed.
	 */ 
	private static String keywordString = "*";
	

	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	


	/**
	 * Creates a new 'mark' or 'unmark' dialog.
	 *
	 * @param addToSelection if <true>, files matching
	 */
	public FileSelectionDialog(MainFrame mainFrame, boolean addToSelection) {

		super(mainFrame, Translator.get(addToSelection?"file_selection_dialog.mark":"file_selection_dialog.unmark"), mainFrame);
	
		this.mainFrame = mainFrame;
		this.addToSelection = addToSelection;

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		YBoxPanel northPanel = new YBoxPanel(5);
		JLabel label = new JLabel(Translator.get(addToSelection?"file_selection_dialog.mark_description":"file_selection_dialog.unmark_description"));
		northPanel.add(label);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		comparisonComboBox = new JComboBox();
		comparisonComboBox.addItem(Translator.get("file_selection_dialog.contains"));
		comparisonComboBox.addItem(Translator.get("file_selection_dialog.starts_with"));
		comparisonComboBox.addItem(Translator.get("file_selection_dialog.ends_with"));
		comparisonComboBox.addItem(Translator.get("file_selection_dialog.is"));
		comparisonComboBox.setSelectedIndex(comparison);
		tempPanel.add(comparisonComboBox);
				
		// selectionField is initialized with last textfield's value (if any)
		selectionField = new JTextField(keywordString);
		selectionField.addActionListener(this);
		selectionField.setSelectionStart(0);
		selectionField.setSelectionEnd(keywordString.length());
		tempPanel.add(selectionField);
		northPanel.add(tempPanel);

		// Add some vertical space
		northPanel.addSpace(10);
		
		caseSensitiveCheckBox = new JCheckBox(Translator.get("file_selection_dialog.case_sensitive"), caseSensitive);
		northPanel.add(caseSensitiveCheckBox);

		includeFoldersCheckBox = new JCheckBox(Translator.get("file_selection_dialog.include_folders"), includeFolders);
		northPanel.add(includeFoldersCheckBox);
		
		northPanel.addSpace(10);
		northPanel.add(Box.createVerticalGlue());

		contentPane.add(northPanel, BorderLayout.NORTH);

		okButton = new JButton(Translator.get(addToSelection?"file_selection_dialog.mark":"file_selection_dialog.unmark"));
		// Sets default 'enter' button
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);
		cancelButton = new JButton(Translator.get("cancel"));
		contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

		// Selection field receives initial keyboard focus
		setInitialFocusComponent(selectionField);

		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
	}

	////////////////////////////
	// ActionListener methods //
	////////////////////////////
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		FileTable activeTable = mainFrame.getLastActiveTable();

		// Action coming from the selection dialog
		if ((source==okButton || source==selectionField)) {
			// Saves values for next dialog
			caseSensitive = caseSensitiveCheckBox.isSelected();
			includeFolders = includeFoldersCheckBox.isSelected();
			comparison = comparisonComboBox.getSelectedIndex();

			// Removes '*' characters
			this.keywordString = selectionField.getText();

			StringBuffer sb = new StringBuffer();
			char c;
			String testString;
			for(int i=0; i<keywordString.length(); i++) {
				c = keywordString.charAt(i);
				if(c!='*')
					sb.append(c);
			}
			testString = sb.toString();

			if(!caseSensitive)
				testString = testString.toLowerCase();
			
			// Marks or unmarks matching *files* (not folders)
			AbstractFile file;
			String fileName;
			FileTableModel tableModel = (FileTableModel)activeTable.getModel();
//			int nbFiles = tableModel.getRowCount();
			int nbFiles = tableModel.getFileCount();
//			for(int i=activeTable.getCurrentFolder().getParent()==null?0:1; i<nbFiles; i++) {
			for(int i=0; i<nbFiles; i++) {
			    file = tableModel.getFile(i);
				if (includeFolders || !file.isDirectory())  {
					fileName = file.getName();
					if(!caseSensitive)
						fileName = fileName.toLowerCase();
					
					boolean markFile = false;
					switch (comparison) {
						case CONTAINS:
							markFile = fileName.indexOf(testString) != -1;
							break;
						case STARTS_WITH:
							markFile = fileName.startsWith(testString);
							break;
						case ENDS_WIDTH:
							markFile = fileName.endsWith(testString);
							break;
						case IS:
							markFile = fileName.equals(testString);
							break;
					}
					
					if(markFile)
						tableModel.setFileMarked(file, addToSelection);
				}
			}
			activeTable.repaint();
		}
		
		dispose();
	}

}