package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.*;
import com.mucommander.file.*;
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
	

	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(240,0);	
	private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	


	/**
	 * Creates a new 'mark' or 'unmark' dialog.
	 *
	 * @param addToSelection if <true>, files matching
	 */
	public FileSelectionDialog(MainFrame mainFrame, boolean addToSelection) {

		super(mainFrame, addToSelection?"Mark":"Unmark", mainFrame);
	
		this.mainFrame = mainFrame;
		this.addToSelection = addToSelection;

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
//		northPanel.addKeyListener(this);
		northPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		JLabel label = new JLabel((addToSelection?"Mark":"Unmark")+" files whose filename:");
		label.setAlignmentX(LEFT_ALIGNMENT);
		northPanel.add(label);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		comparisonComboBox = new JComboBox();
		comparisonComboBox.addItem("Contains");
		comparisonComboBox.addItem("Starts with");
		comparisonComboBox.addItem("Ends with");
		comparisonComboBox.addItem("Is");
		comparisonComboBox.setSelectedIndex(comparison);
		comparisonComboBox.addKeyListener(escapeKeyAdapter);
		tempPanel.add(comparisonComboBox);
				
		// selectionField is initialized with last textfield's value (if any)
		selectionField = new JTextField(keywordString);
		selectionField.addActionListener(this);
		selectionField.addKeyListener(escapeKeyAdapter);
		selectionField.setSelectionStart(0);
		selectionField.setSelectionEnd(keywordString.length());
		tempPanel.add(selectionField);
		tempPanel.setAlignmentX(LEFT_ALIGNMENT);
		northPanel.add(tempPanel);

		// Add some vertical space
		northPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		caseSensitiveCheckBox = new JCheckBox("Case sensitive", caseSensitive);
		caseSensitiveCheckBox.addKeyListener(escapeKeyAdapter);
		caseSensitiveCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		northPanel.add(caseSensitiveCheckBox);

		includeFoldersCheckBox = new JCheckBox("Include folders", includeFolders);
		includeFoldersCheckBox.addKeyListener(escapeKeyAdapter);
		includeFoldersCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		northPanel.add(includeFoldersCheckBox);
		
		northPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		northPanel.add(Box.createVerticalGlue());

		contentPane.add(northPanel, BorderLayout.NORTH);

		// Sets default 'enter' button
		okButton = new JButton("OK");
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton = new JButton("Cancel");
		cancelButton.addKeyListener(escapeKeyAdapter);
		contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

		// Selection field receives initial keyboard focus
		setInitialFocusComponent(selectionField);

        setResizable(true);
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
	}


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
			AbstractFile files[] = tableModel.getFileArray();
			int nbFiles = files.length;
			for(int i=activeTable.getCurrentFolder().getParent()==null?0:1; i<nbFiles; i++) {
			    file = files[i];
				if (includeFolders || (!file.isFolder() || (file instanceof ArchiveFile)))  {
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
						activeTable.setFileMarked(file, addToSelection);
				}
			}
			activeTable.repaint();
		}
		
		dispose();
		activeTable.requestFocus();
	}

}