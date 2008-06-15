/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.MultiRenameJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.util.StringUtils;

/**
 * Dialog used to set parameters for renaming multiple files.
 * 
 * @author Mariusz Jakubowski
 */
public class MultiRenameDialog extends FocusDialog implements ActionListener,
        DocumentListener {
    private static final int CASE_UNCHANGED = 0;
    private static final int CASE_LOWER = 1;
    private static final int CASE_UPPER = 2;
    private static final int CASE_FIRST_UPPER = 3;
    private static final int CASE_WORD_UPPER = 4;

    private MainFrame mainFrame;
    private JTextField edtFileNameMask;
    private JTextField edtExtensionMask;
    private JTable tblNames;
    private JButton btnRename;
    private JButton btnClose;
    private JTextField edtSearchFor;
    private JTextField edtReplaceWith;
    private JTextField edtCounterStart;
    private JTextField edtCounterStep;
    private JComboBox cbCounterDigits;
    private JComboBox cbCase;
    private JButton btnRemove;
    private RenameTableModel tableModel;

    /** files to rename */
    private FileSet files;
    
    /** a list of generated names */
    private List newNames = new ArrayList();
    
    /** a flag indicating that there are duplicates in file names */
    private boolean duplicates;
    
    /** a list of parsed tokens */
    private List tokens = new ArrayList();
    private AbstractAction actRemove;

    
    public MultiRenameDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, Translator.get("multi_rename_dialog.title"), null);
        this.mainFrame = mainFrame;
        this.files = files;
        initialize();
        generateNewNames();
    }

    /**
     * Initializes the dialog.
     */
    private void initialize() {
        setLayout(new BorderLayout());
        add(getPnlTop(), BorderLayout.NORTH);
        add(new JScrollPane(getTblNames()), BorderLayout.CENTER);
        add(getPnlButtons(), BorderLayout.SOUTH);
    }

    /**
     * Creates bottom panel with buttons.
     */
    private JPanel getPnlButtons() {
        JPanel pnlButtons = new JPanel(new BorderLayout());
        btnRemove = new JButton(getActRemove());
        pnlButtons.add(btnRemove, BorderLayout.WEST);
        XBoxPanel pnlButtonsRight = new XBoxPanel();
        btnRename = new JButton(Translator.get("multi_rename_dialog.start"));
        btnRename.addActionListener(this);
        pnlButtonsRight.add(btnRename);
        btnClose = new JButton(Translator.get("close"));
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
            tblNames.getActionMap().put("del", getActRemove());
            tblNames.getInputMap().put((KeyStroke) getActRemove().getValue(Action.ACCELERATOR_KEY), "del");    
            // tblNames.getColumnModel().getColumn(1).setCellEditor(new
            // DefaultCellEditor(new JTextField()));
            // tblNames.set
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
            actRemove.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
            actRemove.putValue(Action.NAME, Translator.get("multi_rename_dialog.remove"));
        }
        return actRemove;
    }

    /**
     * Creates a panel with edit controls.
     */
    private JPanel getPnlTop() {
        // file & extension mask
        edtFileNameMask = new JTextField("[N]");
        edtFileNameMask.setColumns(20);
        edtFileNameMask.getDocument().addDocumentListener(this);
        String tooltip = "<html><ul><li>[N] - whole name<li>[N2,3] - 3 characters starting from the 2nd character of a name" +
        "<li>[N2-5] - characters 2 to 5<li>[N2-] - all characters starting from the 2nd character" +
        "<li>[N-3,2] - two characters starting at 3rd character from the end of a name" +
        "<li>[N2--2] - characters from the 2nd to the 2nd-last character" +
        "<li>[C] - inserts counter" +
        "<li>[C10,2,3] - inserts counter starting at 10, step by 2, use 3 digits to display" +
        "<li>[YMD] - inserts file last modified year, month and day";       // TODO add to dictionary
        edtFileNameMask.setToolTipText(tooltip);

        edtExtensionMask = new JTextField("[E]");
        edtExtensionMask.setColumns(20);
        edtExtensionMask.getDocument().addDocumentListener(this);

        // search & replace
        edtSearchFor = new JTextField();
        edtSearchFor.setColumns(20);
        edtSearchFor.getDocument().addDocumentListener(this);

        edtReplaceWith = new JTextField();
        edtReplaceWith.setColumns(20);
        edtReplaceWith.getDocument().addDocumentListener(this);

        // upper/lower case
        Vector ulcase = new Vector();
        ulcase.add(Translator.get("multi_rename_dialog.no_change"));
        ulcase.add(Translator.get("multi_rename_dialog.lower_case"));
        ulcase.add(Translator.get("multi_rename_dialog.upper_case"));
        ulcase.add(Translator.get("multi_rename_dialog.first_upper"));
        ulcase.add(Translator.get("multi_rename_dialog.word"));
        cbCase = new JComboBox(ulcase);
        cbCase.addActionListener(this);

        // counter
        edtCounterStart = new JTextField("1");
        edtCounterStart.getDocument().addDocumentListener(this);
        edtCounterStart.setColumns(2);

        edtCounterStep = new JTextField("1");
        edtCounterStep.getDocument().addDocumentListener(this);
        edtCounterStep.setColumns(2);

        Vector digits = new Vector();
        String zeros = "0000";
        for (int i = 1; i <= 5; i++) {
            digits.add(zeros.substring(0, i - 1) + Integer.toString(i));
        }
        cbCounterDigits = new JComboBox(digits);
        cbCounterDigits.addActionListener(this);

        // add controls
        XBoxPanel pnlTop = new XBoxPanel();

        XAlignedComponentPanel pnl1 = new XAlignedComponentPanel(5);
        pnl1.setBorder(BorderFactory.createTitledBorder(Translator
                .get("multi_rename_dialog.mask")));
        pnl1.addRow(Translator.get("multi_rename_dialog.file_name_mask"),
                edtFileNameMask, 5);
        pnl1.addRow(Translator.get("multi_rename_dialog.extension_mask"),
                edtExtensionMask, 5);
        pnl1.addRow(new JLabel(" "), 5);
        pnlTop.add(pnl1);

        XAlignedComponentPanel pnl2 = new XAlignedComponentPanel(5);
        pnl2.setBorder(BorderFactory.createTitledBorder(Translator
                .get("multi_rename_dialog.search_replace")));
        pnl2.addRow(Translator.get("multi_rename_dialog.search_for"),
                edtSearchFor, 5);
        pnl2.addRow(Translator.get("multi_rename_dialog.replace_with"),
                edtReplaceWith, 5);
        pnl2.addRow(Translator.get("multi_rename_dialog.upper_lower_case"),
                cbCase, 5);
        pnlTop.add(pnl2);

        XAlignedComponentPanel pnl3 = new XAlignedComponentPanel(5);
        pnl3.setBorder(BorderFactory.createTitledBorder(Translator
                .get("multi_rename_dialog.counter")));
        pnl3.addRow(Translator.get("multi_rename_dialog.start_at"),
                edtCounterStart, 5);
        pnl3.addRow(Translator.get("multi_rename_dialog.step_by"),
                edtCounterStep, 5);
        pnl3.addRow(Translator.get("multi_rename_dialog.digits"),
                cbCounterDigits, 5);
        pnlTop.add(pnl3);

        return pnlTop;
    }

    /**
     * Removes selected files from a list of files to rename.
     */
    private void removeSelectedFiles() {
        int[] sel = tblNames.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            files.remove(sel[i]);
            newNames.remove(sel[i]);
            tableModel.fireTableRowsDeleted(sel[i], sel[i]);
        }
        if (files.size() == 0)
            dispose();
    }

    /**
     * Generates new names for all files.
     */
    private void generateNewNames() {
        compilePattern(edtFileNameMask.getText() + "."
                + edtExtensionMask.getText());
        newNames.clear();
        duplicates = false;
        for (int i = 0; i < files.size(); i++) {
            AbstractFile file = (AbstractFile) files.get(i);
            String newName = generateNewName(file);
            if (newNames.contains(newName)) {
                duplicates = true;
                // TODO check if parent dir is the same
                // (it has to be implemented when search or branch view will be
                // available)
            }
            newNames.add(newName);
        }
        btnRename.setEnabled(!duplicates);      // TODO add warning about duplicates
        tableModel.fireTableChanged(new TableModelEvent(tableModel, 0, newNames
                .size(), 1, TableModelEvent.UPDATE));
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
     * @param pattern
     *            a pattern for changing a file name and it's extension
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
                    AbstractToken t = null;
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
                    if (t != null) {
                        t.parse();
                        tokens.add(t);
                    }
                }
                i = tokenEnd;
            } else {
                tokens.add(new CopyChar(Character.toString(c)));
            }
        }
    }

    /**
     * Generate a new name for a file.
     * 
     * @param file
     *            a file to change name to
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
        for (Iterator i = tokens.iterator(); i.hasNext();) {
            AbstractToken token = (AbstractToken) i.next();
            filename.append(token.apply(file));
        }
        return filename.toString();
    }

    /**
     * Renames files.
     */
    private void doRename() {
        dispose();
        // remove non changed files
        Iterator fi = files.iterator();
        Iterator ni = newNames.iterator();
        while (fi.hasNext()) {        
            AbstractFile file = (AbstractFile) fi.next();
            String nn = (String) ni.next();
            if (file.getName().equals(nn)) {
                fi.remove();
                ni.remove();
            }
        }
        // start rename job
        if (files.size() > 0) {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame,
                    Translator.get("progress_dialog.processing_files"));
            MultiRenameJob job = new MultiRenameJob(progressDialog, mainFrame,
                    files, newNames);
            progressDialog.start(job);
        }
    }

    /**
     * Table model with old and new file names.
     * @author Mariusz Jakubowski
     * 
     */
    private class RenameTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return files.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            AbstractFile f = (AbstractFile) files.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return f.getName();
            case 1:
                return newNames.get(rowIndex);
            }
            return null;
        }

        public String getColumnName(int column) {
            switch (column) {
            case 0:
                return Translator.get("multi_rename_dialog.old_name");
            case 1:
                return Translator.get("multi_rename_dialog.new_name");
            }
            return "";
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // return columnIndex == 1; TODO make new name editable (don't
            // forget about duplicates check)
            return false;
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
            doRename();
        } else if (source == cbCase) {
            generateNewNames();
        } else if (source == cbCounterDigits) {
            generateNewNames();
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
     * Base class for handling tokens.
     * 
     * @author Mariusz Jakubowski
     * 
     */
    private abstract static class AbstractToken {
        /** a string with a token */
        protected String token;

        /** a type of the token (first char of the token string) */
        protected char tokenType;

        /** a current position in the token */
        protected int pos = 1;

        /** a length of the token */
        protected int len;

        public AbstractToken(String token) {
            this.token = token;
            this.tokenType = token.charAt(0);
            this.len = token.length();
        }

        /**
         * Parses a token information.
         */
        protected abstract void parse();

        /**
         * Applies this token to a file.
         * 
         * @param file
         *            a file
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

        protected void parse() {
        }

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
     * <li>[N-3,2] - two characters starting at 3rd character from the end of a
     * name
     * <li>[N2--2] - characters from the 2nd to the 2nd-last character
     * <li>[N-5-10] - characters from 5th from end to 10th from beginning of
     * name
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
                        System.err.println(e.getMessage());
                        System.err.println(currentStartIndex);
                        System.err.println(currentEndIndex);
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

        public String apply(AbstractFile file) {
            AbstractFile parent = file.getParentSilently();
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

        protected void parse() {
        }

    }

}
