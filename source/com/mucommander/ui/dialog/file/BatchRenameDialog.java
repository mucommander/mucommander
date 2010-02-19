/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog.file;

import com.mucommander.AppLogger;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.PathUtils;
import com.mucommander.job.BatchRenameJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.BatchRenameAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.util.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.*;

/**
 * Dialog used to set parameters for renaming multiple files.
 * 
 * @author Mariusz Jakubowski
 */
public class BatchRenameDialog extends FocusDialog implements ActionListener,
        DocumentListener {
    private static final int CASE_UNCHANGED = 0;
    private static final int CASE_LOWER = 1;
    private static final int CASE_UPPER = 2;
    private static final int CASE_FIRST_UPPER = 3;
    private static final int CASE_WORD_UPPER = 4;
    
    private static final int COL_ORIG_NAME = 0;
    private static final int COL_CHANGED_NAME = 1;
    private static final int COL_CHANGE_BLOCK = 2;    

    private MainFrame mainFrame;
    private JTextField edtFileNameMask;
    private JTable tblNames;
    private JButton btnRename;
    private JButton btnClose;
    private JTextField edtSearchFor;
    private JTextField edtReplaceWith;
    private JTextField edtCounterStart;
    private JTextField edtCounterStep;
    private JComboBox cbCounterDigits;
    private JComboBox cbCase;
    private RenameTableModel tableModel;
    private AbstractAction actRemove;
    private JButton btnName;
    private JButton btnNameRange;
    private JButton btnExtension;
    private JButton btnCounter;
    private JLabel lblDuplicates;
    private TableColumn colBlock;

    
    /** files to rename */
    private FileSet files;

    /** a map of old file names used to check for name conflicts */
    private HashMap<String, AbstractFile> oldNames = new HashMap<String, AbstractFile>();

    /** a list of generated names */
    private List<String> newNames = new ArrayList<String>();

    /** a list of flags to block file rename */
    private List<Boolean> blockNames = new ArrayList<Boolean>();
    
    /** a list of parsed tokens */
    private List<AbstractToken> tokens = new ArrayList<AbstractToken>();



    /**
     * Creates a new batch-rename dialog.
     * @param mainFrame the main frame
     * @param files a list of files to rename
     */
    public BatchRenameDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, ActionProperties.getActionLabel(BatchRenameAction.Descriptor.ACTION_ID), null);
        this.mainFrame = mainFrame;
        this.files = files;
        for (AbstractFile f : files) {        	
            this.blockNames.add(Boolean.FALSE);
            this.newNames.add("");
        	oldNames.put(PathUtils.removeTrailingSeparator(f.getAbsolutePath()), f);			
		}
        initialize();
        generateNewNames();
    }

    /**
     * Initializes the dialog.
     */
    private void initialize() {
        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        content.add(getPnlTop(), BorderLayout.NORTH);
        content.add(new JScrollPane(getTblNames()), BorderLayout.CENTER);
        content.add(getPnlButtons(), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnRename);
    }

    /**
     * Creates bottom panel with buttons.
     */
    private JPanel getPnlButtons() {
        JPanel pnlButtons = new JPanel(new BorderLayout());
        pnlButtons.add(new JButton(getActRemove()), BorderLayout.WEST);
        XBoxPanel pnlButtonsRight = new XBoxPanel();
        lblDuplicates = new JLabel(Translator.get("batch_rename_dialog.duplicate_names"));
        lblDuplicates.setForeground(Color.red);
        lblDuplicates.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        pnlButtonsRight.add(lblDuplicates);
        btnRename = new JButton(Translator.get("rename"));
        btnRename.addActionListener(this);
        pnlButtonsRight.add(btnRename);
        btnClose = new JButton(Translator.get("cancel"));
        btnClose.addActionListener(this);
        pnlButtonsRight.add(btnClose);
        pnlButtons.add(pnlButtonsRight, BorderLayout.EAST);
        return pnlButtons;
    }

    /**
     * Creates a table with file names.
     */
    private JTable getTblNames() {
        if (tblNames == null) {
            tableModel = new RenameTableModel();
            tblNames = new JTable(tableModel);
            // add del key for remove action
            tblNames.getActionMap().put("del", getActRemove());            
            tblNames.getInputMap().put((KeyStroke) getActRemove().getValue(Action.ACCELERATOR_KEY), "del");
            // add tab key
            tblNames.getActionMap().put("tab", new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        tblNames.transferFocus();
                    }
            });
            tblNames.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false), "tab");
            // add shift tab key
            tblNames.getActionMap().put("shift tab", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    tblNames.transferFocusBackward();
                }
            });
            tblNames.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 
                    InputEvent.SHIFT_DOWN_MASK, false), "shift tab");
            // 
            tblNames.getColumnModel().getColumn(COL_CHANGED_NAME).setCellEditor(new
                     DefaultCellEditor(new JTextField()));
            colBlock = tblNames.getColumnModel().getColumn(COL_CHANGE_BLOCK); 
            colBlock.setMaxWidth(60);
            tblNames.removeColumn(colBlock);
        }
        return tblNames;
    }
    
    public Action getActRemove() {
        if (actRemove == null) {
            actRemove = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    removeSelectedFiles();
                }
            };
            actRemove.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            actRemove.putValue(Action.NAME, Translator.get("remove"));
        }
        return actRemove;
    }

    /**
     * Creates a panel with edit controls.
     */
    private JPanel getPnlTop() {
        // file & extension mask
        edtFileNameMask = new JTextField("[N].[E]");
        edtFileNameMask.setColumns(20);
        edtFileNameMask.getDocument().addDocumentListener(this);
        edtFileNameMask.setToolTipText(getPatternHelp());

        // search & replace
        edtSearchFor = new JTextField();
        edtSearchFor.setColumns(20);
        edtSearchFor.getDocument().addDocumentListener(this);

        edtReplaceWith = new JTextField();
        edtReplaceWith.setColumns(20);
        edtReplaceWith.getDocument().addDocumentListener(this);

        // upper/lower case
        Vector<String> ulcase = new Vector<String>();
        ulcase.add(Translator.get("batch_rename_dialog.no_change"));
        ulcase.add(Translator.get("batch_rename_dialog.lower_case"));
        ulcase.add(Translator.get("batch_rename_dialog.upper_case"));
        ulcase.add(Translator.get("batch_rename_dialog.first_upper"));
        ulcase.add(Translator.get("batch_rename_dialog.word"));
        cbCase = new JComboBox(ulcase);
        cbCase.addActionListener(this);

        // counter
        edtCounterStart = new JTextField("1");
        edtCounterStart.getDocument().addDocumentListener(this);
        edtCounterStart.setColumns(2);

        edtCounterStep = new JTextField("1");
        edtCounterStep.getDocument().addDocumentListener(this);
        edtCounterStep.setColumns(2);

        Vector<String> digits = new Vector<String>();
        String zeros = "0000";
        for (int i = 1; i <= 5; i++) {
            digits.add(zeros.substring(0, i - 1) + "1");
        }
        cbCounterDigits = new JComboBox(digits);
        cbCounterDigits.addActionListener(this);

        // add controls
        XBoxPanel pnlTop = new XBoxPanel();
        
        YBoxPanel pnl1 = new YBoxPanel();
        pnl1.setBorder(BorderFactory.createTitledBorder(
                Translator.get("batch_rename_dialog.mask")));
        pnl1.add(edtFileNameMask);
        
        JPanel pnl1Btns = new JPanel(new GridLayout(3, 2));

        btnName = new JButton("[N] - " + Translator.get("name"));
        btnName.addActionListener(this);
        btnName.setHorizontalAlignment(SwingConstants.LEFT);
        pnl1Btns.add(btnName);
        
        btnExtension = new JButton("[E] - " + Translator.get("extension"));
        btnExtension.addActionListener(this);
        btnExtension.setHorizontalAlignment(SwingConstants.LEFT);
        pnl1Btns.add(btnExtension);

        btnNameRange = new JButton("[N#-#] - " + Translator.get("batch_rename_dialog.range"));
        btnNameRange.addActionListener(this);
        btnNameRange.setHorizontalAlignment(SwingConstants.LEFT);
        pnl1Btns.add(btnNameRange);
        
        btnCounter = new JButton("[C] - " + Translator.get("batch_rename_dialog.counter"));
        btnCounter.addActionListener(this);
        btnCounter.setHorizontalAlignment(SwingConstants.LEFT);
        pnl1Btns.add(btnCounter);

        pnl1.add(pnl1Btns);
        pnl1.add(new JPanel());
        pnlTop.add(pnl1);
        
        XAlignedComponentPanel pnl2 = new XAlignedComponentPanel(5);
        pnl2.setBorder(BorderFactory.createTitledBorder(Translator
                .get("batch_rename_dialog.search_replace")));
        pnl2.addRow(Translator.get("batch_rename_dialog.search_for"),
                edtSearchFor, 5);
        pnl2.addRow(Translator.get("batch_rename_dialog.replace_with"),
                edtReplaceWith, 5);
        pnl2.addRow(Translator.get("batch_rename_dialog.upper_lower_case"),
                cbCase, 5);
        pnlTop.add(pnl2);

        XAlignedComponentPanel pnl3 = new XAlignedComponentPanel(5);
        pnl3.setBorder(BorderFactory.createTitledBorder(Translator
                .get("batch_rename_dialog.counter") + " [C]"));
        pnl3.addRow(Translator.get("batch_rename_dialog.start_at"),
                edtCounterStart, 5);
        pnl3.addRow(Translator.get("batch_rename_dialog.step_by"),
                edtCounterStep, 5);
        pnl3.addRow(Translator.get("batch_rename_dialog.format"),
                cbCounterDigits, 5);
        pnlTop.add(pnl3);

        return pnlTop;
    }

    /**
     * Creates a label with help.
     * @return a label with help
     */     
    private String getPatternHelp() {
        return "<html>" +
            "[N] - the whole name<br>" +
            "[N2,3] - 3 characters starting from the 2nd character of the name<br>" +
            "[N2-5] - characters 2 to 5<br>" +
            "[N2-] - all characters starting from the 2nd character<br>" +
            "[N-3,2] - two characters starting at 3rd character from the end of the name<br>" +
            "[N2--2] - characters from the 2nd to the 2nd-last character<br>" +
            "[C] - inserts a counter<br>" +
            "[C10,2,3] - inserts a counter starting at 10, steps by 2, uses 3 digits<br>" +
            "[YMD] - inserts a year, month and day when the file was last modified";       // TODO add to dictionary
    }
    

    /**
     * Removes selected files from a list of files to rename.
     */
    private void removeSelectedFiles() {
        int[] sel = tblNames.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            files.remove(sel[i]);
            newNames.remove(sel[i]);
            blockNames.remove(sel[i]);
            tableModel.fireTableRowsDeleted(sel[i], sel[i]);
        }
        if (files.size() == 0) {
            dispose();
        }
    }
    
    /**
     * Checks if there are duplicates in new file names. 
     */
    private void checkForDuplicates() {
        boolean duplicates = false;
        boolean oldNamesConflict = false;
        Set<String> names = new HashSet<String>();
        for (int i=0; i<newNames.size(); i++) {
            String newName = newNames.get(i);
            AbstractFile file = files.get(i);
            AbstractFile parent = file.getParent();
            if (parent != null) {
                newName = parent.getAbsolutePath(true) + newName;
            }
            if (names.contains(newName)) {
                duplicates = true;
                break;
            }
            AbstractFile oldFile = oldNames.get(newName);
            if (oldFile!=null && oldFile!=file) {
            	oldNamesConflict = true;
            	break;
            }
            names.add(newName);
        }            
        if (duplicates) {
        	lblDuplicates.setText(Translator.get("batch_rename_dialog.duplicate_names"));
        }
        if (oldNamesConflict) {
        	lblDuplicates.setText(Translator.get("batch_rename_dialog.names_conflict"));
        }
        lblDuplicates.setVisible(duplicates || oldNamesConflict);
        btnRename.setEnabled(!duplicates && !oldNamesConflict);
    }
    
    /**
     * Generate a new name for a file.
     * 
     * @param file a file to change name to
     * @return the new file name
     */
    private String generateNewName(AbstractFile file) {
        // apply pattern
        String newName = applyPattern(file);

        // search & replace
        if (edtSearchFor.getText().length() > 0) {
            newName = newName.replace(edtSearchFor.getText(), edtReplaceWith
                    .getText());
        }

        // remove trailing dot
        if (newName.endsWith(".")) {
            newName = newName.substring(0, newName.length() - 1);
        }

        // uppercase/lowercase
        newName = changeCase(newName, cbCase.getSelectedIndex());

        return newName;
    }
    
    /**
     * Generates new names for all files.
     */
    private void generateNewNames() {
        compilePattern(edtFileNameMask.getText());
        for (int i = 0; i < files.size(); i++) {
            if (Boolean.FALSE.equals(blockNames.get(i))) {
                AbstractFile file = files.get(i);
                String newName = generateNewName(file);
                newNames.set(i, newName);
            }
        }
        checkForDuplicates();
        tableModel.fireTableChanged(new TableModelEvent(tableModel, 0, 
                newNames.size(), 1, TableModelEvent.UPDATE));
    }
    
    /**
     * Parses a pattern for a filename and it's extension and stores it in a
     * list. A pattern is a combination of file and extension masks that a user
     * enters in fields. These masks can contain special placeholders for
     * previous name, part of it, counter, date, and others. These placeholders
     * (or 'tokens') are always in brackets [ and ]. A part of pattern which is
     * not in brackets is copied to a new name. This metod analyzes a pattern
     * and stores token handlers responsible for substituting these placeholders
     * for actual parts of a new file name.
     * 
     * @see AbstractToken
     * @param pattern a pattern for changing a file name and it's extension
     */
    private void compilePattern(String pattern) {
        tokens.clear();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '[') {
                int tokenEnd = pattern.indexOf(']', i);
                if (tokenEnd == -1) {
                    tokens.add(new CopyChar(pattern.substring(i)));
                    break;
                }
                String strToken = pattern.substring(i + 1, tokenEnd);
                if (strToken.length() > 0) {
                    c = strToken.charAt(0);
                    AbstractToken t;
                    switch (c) {
                    case 'N':
                        t = new NameToken(strToken);
                        break;
                    case 'E':
                        t = new ExtToken(strToken);
                        break;
                    case 'C':
                        int start = StringUtils.parseIntDef(edtCounterStart
                                .getText(), 0);
                        int step = StringUtils.parseIntDef(edtCounterStep
                                .getText(), 0);
                        int digits = cbCounterDigits.getSelectedIndex() + 1;
                        t = new CounterToken(strToken, start, step, digits);
                        break;
                    case 'P':
                        t = new ParentDirToken(strToken);
                        break;
                    case 'Y':
                    case 'M':
                    case 'D':
                    case 'h':
                    case 'm':
                    case 's':
                        t = new DateToken(strToken);
                        break;
                    case '[':
                        t = new CopyChar("[");
                        break;
                    default:
                        t = new CopyChar("[" + strToken + "]");
                        break;
                    }
                    t.parse();
                    tokens.add(t);
                }
                i = tokenEnd;
            } else {
                tokens.add(new CopyChar(Character.toString(c)));
            }
        }
    }

    /**
     * Changes case of a file name.
     * @param oldName a name of file to change case
     * @param newCase a type of change
     * @return the name with changed case
     */
    private String changeCase(String oldName, int newCase) {
        String newName = "";
        switch (newCase) {
        case CASE_UNCHANGED:
            newName = oldName;
            break;
        case CASE_LOWER:
            newName = oldName.toLowerCase();
            break;
        case CASE_UPPER:
            newName = oldName.toUpperCase();
            break;
        case CASE_FIRST_UPPER:
            newName = oldName.substring(0, 1).toUpperCase()
                    + oldName.substring(1).toLowerCase();
            break;
        case CASE_WORD_UPPER:
            boolean afterSpace = true;
            StringBuffer newNameCase = new StringBuffer();
            for (int i = 0; i < oldName.length(); i++) {
                if (oldName.charAt(i) == ' ') {
                    newNameCase.append(' ');
                    afterSpace = true;
                } else {
                    if (afterSpace) {
                        newNameCase.append(Character.toUpperCase(oldName
                                .charAt(i)));
                        afterSpace = false;
                    } else {
                        newNameCase.append(oldName.charAt(i));
                    }
                }
            }
            newName = newNameCase.toString();
            break;
        }
        return newName;
    }

    /**
     * Applies a compiled pattern to a file name and it's extension.
     * @param file a file
     * @return the new file name after applying a pattern
     */
    private String applyPattern(AbstractFile file) {
        StringBuffer filename = new StringBuffer();
        for (AbstractToken token: tokens)
            filename.append(token.apply(file));
        return filename.toString();
    }
    
    /**
     * Counts or removes unchanged files from change set.
     * @param countOnly if true only counts files that are changing.
     * @return number of changed files
     */
    private int removeUnchangedFiles(boolean countOnly) {
        // remove non changed files
        Iterator<AbstractFile> fi = files.iterator();
        Iterator<String> ni = newNames.iterator();
        int changed = 0;
        while (fi.hasNext()) {        
            AbstractFile file = fi.next();
            String nn = ni.next();
            if (file.getName().equals(nn)) {
                if (!countOnly) {
                    fi.remove();
                    ni.remove();
                }
            } else {
                changed++;
            }
        }
        return changed;
    }

    /**
     * Renames files.
     */
    private void doRename() {
        removeUnchangedFiles(false);
        // start rename job
        if (files.size() > 0) {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame,
                    Translator.get("progress_dialog.processing_files"));
            BatchRenameJob job = new BatchRenameJob(progressDialog, mainFrame,
                    files, newNames);
            progressDialog.start(job);
        }
    }
    
    /**
     * Inserts pattern into pattern edit field.
     * @param pattern a text to insert
     */
    private void insertPattern(String pattern) {
        int pos = edtFileNameMask.getSelectionStart();
        try {
        	int selLen = edtFileNameMask.getSelectionEnd() - edtFileNameMask.getSelectionStart();
        	if (selLen > 0) {
        		edtFileNameMask.getDocument().remove(edtFileNameMask.getSelectionStart(), selLen);
        	}
            edtFileNameMask.getDocument().insertString(pos, pattern, null);
        } catch (BadLocationException e) {
            AppLogger.fine("Caught exception", e);
        }
    }
    

    // /////////////////////////////////
    // ActionListener implementation //
    // /////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnClose) {
            dispose();
        } else if (source == btnRename) {
            int unchanged = files.size();
            int changed = removeUnchangedFiles(true);
            if (changed > 0) {
                BatchRenameConfirmationDialog dlg = new BatchRenameConfirmationDialog(mainFrame, files, changed, unchanged);
                if (dlg.isProceedWithRename()) {
                    dispose();
                    doRename();
                }
            }
        } else if (source == cbCase) {
            generateNewNames();
        } else if (source == cbCounterDigits) {
            generateNewNames();
        } else if (source == btnName) {
            insertPattern("[N]");
        } else if (source == btnExtension) {
            insertPattern("[E]");
        } else if (source == btnCounter) {
            insertPattern("[C]");
        } else if (source == btnNameRange) {
            String firstFile = files.get(0).getNameWithoutExtension();
            BatchRenameSelectRange dlg = new BatchRenameSelectRange(this, firstFile);
            dlg.showDialog();
            String range = dlg.getRange();
            if (range != null) {
                insertPattern(range);
            }
        }
    }

    // these methods are invoked when one of edit boxes changes

    public void changedUpdate(DocumentEvent e) {
        generateNewNames();
    }

    public void insertUpdate(DocumentEvent e) {
        generateNewNames();
    }

    public void removeUpdate(DocumentEvent e) {
        generateNewNames();
    }
    

    /**
     * Table model with old and new file names.
     * @author Mariusz Jakubowski
     * 
     */
    private class RenameTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return files.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            AbstractFile f = files.get(rowIndex);
            switch (columnIndex) {
            case COL_ORIG_NAME:
                return f.getName();
            case COL_CHANGED_NAME:
                return newNames.get(rowIndex);
            case COL_CHANGE_BLOCK:
                return blockNames.get(rowIndex);
            }
            return null;
        }
        
        /**
         * Sets the value in the cell at columnIndex and rowIndex to aValue.
         * Called when a user manually entered a new file name or blocked 
         * a name from a rename pattern.
         */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            switch (columnIndex) {
            case COL_CHANGED_NAME:
                if (!newNames.get(rowIndex).equals(value)) {
                    newNames.set(rowIndex, (String)value);
                    if (Boolean.FALSE.equals(blockNames.get(rowIndex))) {
                        blockNames.set(rowIndex, Boolean.TRUE);
                        if (tblNames.getColumnCount() == 2) {
                            tblNames.addColumn(colBlock);
                        }
                        fireTableCellUpdated(rowIndex, COL_CHANGE_BLOCK);
                    }
                    checkForDuplicates();
                }
                break;
            case COL_CHANGE_BLOCK:
                blockNames.set(rowIndex, (Boolean)value);
                if (Boolean.FALSE.equals(value)) {
                    AbstractFile file = files.get(rowIndex);
                    String newName = generateNewName(file);
                    newNames.set(rowIndex, newName);
                    fireTableCellUpdated(rowIndex, COL_CHANGED_NAME);
                }
                checkForDuplicates();
                break;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case COL_ORIG_NAME:
                return Translator.get("batch_rename_dialog.old_name");
            case COL_CHANGED_NAME:
                return Translator.get("batch_rename_dialog.new_name");
            case COL_CHANGE_BLOCK:
                return Translator.get("batch_rename_dialog.block_name");
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == COL_CHANGE_BLOCK) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != COL_ORIG_NAME; 
        }
        
        

    }


    /**
     * Base class for handling tokens.
     * 
     * @author Mariusz Jakubowski
     * 
     */
    private abstract static class AbstractToken {
        /** a string with a token */
        protected String token;

        /** a current position in the token */
        protected int pos = 1;

        /** a length of the token */
        protected int len;

        public AbstractToken(String token) {
            this.token = token;
            this.len = token.length();
        }

        /**
         * Parses a token information.
         */
        protected abstract void parse();

        /**
         * Applies this token to a file.
         * 
         * @param file a file
         * @return a part of filename after applying this token
         */
        public abstract String apply(AbstractFile file);

        /**
         * Gets one char from this token.
         * 
         * @return the next character in the token
         */
        public char getChar() {
            if (pos < len) {
                return token.charAt(pos++);
            }
            return 0;
        }

        /**
         * Trys to get an integer from this token string. Advances the current
         * position in the token string.
         * 
         * @param def
         *            a default value if an integer cannot be parsed
         * @return the integer from this token string or the default value
         */
        public int getInt(int def) {
            int startpos = pos;
            while (pos < len) {
                char c = token.charAt(pos);
                if (c < '0' || c > '9') {
                    if (c != '-' || startpos != pos)
                        break;
                }
                pos++;
            }
            if (startpos == pos)
                return def;
            return StringUtils.parseIntDef(token.substring(startpos, pos), def);
        }

    }

    /**
     * Token handler that copies a character from a source string to a
     * destination. This is used for all characters without brackets.
     * 
     * @author Mariusz Jakubowski
     * 
     */
    static class CopyChar extends AbstractToken {

        public CopyChar(String token) {
            super(token);
        }

        @Override
        protected void parse() {
        }

        @Override
        public String apply(AbstractFile file) {
            return token;
        }
    }

    /**
     * Token handler that parses file name. Examples:
     * <ul>
     * <li>[N] - whole name
     * <li>[N2] - 2nd character of a name
     * <li>[N2,3] - 3 characters starting at the 2nd character of a name
     * <li>[N2-5] - characters 2 to 5
     * <li>[N2-] - all characters starting from the 2nd character
     * <li>[N-2] - 2nd character from the end of name
     * <li>[N-3,2] - two characters starting at 3rd character from the end of a name
     * <li>[N2--2] - characters from the 2nd to the 2nd-last character
     * <li>[N-5-10] - characters from 5th from end to 10th from beginning of a name
     * </ul>
     * 
     * @author Mariusz Jakubowski
     * 
     */
    static class NameToken extends AbstractToken {
        private int startIndex;
        private int endIndex;
        private int charCount;

        public NameToken(String token) {
            super(token);
        }

        @Override
        protected void parse() {
            startIndex = getInt(0);
            char sep = getChar();
            switch (sep) {
            case '-':
                endIndex = getInt(999); // default - to the end
                break;
            case ',':
                charCount = getInt(0);
            }
        }

        @Override
        public String apply(AbstractFile file) {
            // split name & extension
            String name;
            String oldName = file.getName();
            int dot = oldName.lastIndexOf('.');
            if (dot >= 0) {
                name = oldName.substring(0, dot);
            } else {
                name = oldName;
            }

            return extractNamePart(name);
        }

        /**
         * Extracts a part of a name.
         * 
         * @param name
         *            a string from which extract a part
         * @return the part of the name
         */
        protected String extractNamePart(String name) {
            int targetLen = name.length();
            int currentStartIndex = startIndex;
            int currentEndIndex = endIndex;
            if (currentStartIndex < 0) {
                currentStartIndex = targetLen + currentStartIndex + 1;
                if (currentStartIndex < 1)
                    currentStartIndex = 1;
            }
            if (currentEndIndex < 0)
                currentEndIndex = targetLen + currentEndIndex + 1;
            if (currentStartIndex > 0) {
                if (charCount > 0) {
                    currentEndIndex = currentStartIndex + charCount - 1;
                } else if (currentEndIndex == 0) {
                    currentEndIndex = currentStartIndex;
                }
                if (currentStartIndex <= currentEndIndex
                        && currentStartIndex - 1 < targetLen) {
                    try {
                        name = name.substring(currentStartIndex - 1, Math.min(
                                currentEndIndex, targetLen));
                    } catch (Exception e) {
                        AppLogger.info("currentStartIndex="+currentStartIndex+", currentEndIndex="+currentEndIndex, e);
                    }
                } else {
                    name = "";
                }

            }
            return name;
        }

    }

    /**
     * Token handler that parses a file extension. [E] - an extension of a file,
     * this token can also be used with parameters like in [N...]
     * 
     * @author Mariusz Jakubowski
     */
    static class ExtToken extends NameToken {

        public ExtToken(String token) {
            super(token);
        }

        @Override
        public String apply(AbstractFile file) {
            // split name & extension
            String ext;
            String oldName = file.getName();
            int dot = oldName.lastIndexOf('.');
            if (dot >= 0) {
                ext = oldName.substring(dot + 1);
            } else {
                ext = "";
            }

            return extractNamePart(ext);
        }

    }

    /**
     * Token handler that inserts a counter.
     * Examples:
     * <ul>
     * <li>[C] - inserts counter, as defined on the dialog
     * <li>[C10] - inserts counter starting at 10
     * <li>[C10,2] - inserts counter starting at 10, step by 2
     * <li>[C10,-2] - inserts counter starting at 10, step by -2
     * <li>[C10,2,3] - inserts counter starting at 10, step by 2, use 3 digits to display
     * <li>[C10,,3] - inserts counter starting at 10, step by as defined on the dialog, use 3 digits to display
     * </ul> 
     * @author Mariusz Jakubowski
     *
     */
    static class CounterToken extends AbstractToken {
        private int start;
        private int step;
        private int digits;
        private int current;
        private NumberFormat numberFormat;

        public CounterToken(String token, int start, int step, int digits) {
            super(token);
            this.start = start;
            this.step = step;
            this.digits = digits;
        }

        @Override
        protected void parse() {
            start = getInt(start);
            if (getChar() == ',') {
                step = getInt(step);
                if (getChar() == ',') {
                    digits = getInt(digits);
                }
            }
            numberFormat = NumberFormat.getIntegerInstance();
            numberFormat.setMinimumIntegerDigits(digits);
            numberFormat.setGroupingUsed(false);
            current = start;
        }

        @Override
        public String apply(AbstractFile file) {
            String counter = numberFormat.format(current);
            current += step;
            return counter;
        }

    }

    /**
     * Token handler that inserts a directory information.
     * [P] - inserts a name of the parent directory.
     * @author Mariusz Jakubowski
     */
    static class ParentDirToken extends NameToken {

        public ParentDirToken(String token) {
            super(token);
        }

        @Override
        public String apply(AbstractFile file) {
            AbstractFile parent = file.getParent();
            if (parent != null)
                return extractNamePart(parent.getName());
            return "";
        }

    }

    /**
     * Inserts a date or time.
     * <ul>
     * <li>[Y] - inserts year (4 digits)
     * <li>[M] - inserts month (2 digits)
     * <li>[D] - inserts day of a month (2 digits)
     * <li>[h] - inserts hours in 24-hour format (2 digits)
     * <li>[m] - inserts minutes (2 digits)
     * <li>[s] - inserts seconds
     * <li>[YMD] - inserts file last modified year, month and day 
     * </ul>
     * @author Mariusz Jakubowski
     *
     */
    static class DateToken extends AbstractToken {
        private NumberFormat year;
        private NumberFormat digits2;

        public DateToken(String token) {
            super(token);
            year = NumberFormat.getIntegerInstance();
            year.setMinimumIntegerDigits(4);
            year.setGroupingUsed(false);
            digits2 = NumberFormat.getIntegerInstance();
            digits2.setMinimumIntegerDigits(2);
            digits2.setGroupingUsed(false);
        }

        @Override
        public String apply(AbstractFile file) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(file.getDate());
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < len; i++) {
                switch (token.charAt(i)) {
                case 'Y':
                    result.append(year.format(c.get(Calendar.YEAR)));
                    break;
                case 'M':
                    result.append(digits2.format(c.get(Calendar.MONTH)));
                    break;
                case 'D':
                    result.append(digits2.format(c.get(Calendar.DAY_OF_MONTH)));
                    break;
                case 'h':
                    result.append(digits2.format(c.get(Calendar.HOUR_OF_DAY)));
                    break;
                case 'm':
                    result.append(digits2.format(c.get(Calendar.MINUTE)));
                    break;
                case 's':
                    result.append(digits2.format(c.get(Calendar.SECOND)));
                    break;
                }
            }
            return result.toString();
        }

        @Override
        protected void parse() {
        }

    }
    

}
