/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.PathUtils;
import com.mucommander.job.SplitFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;

/**
 * Dialog used to split a file into parts.
 * 
 * @author Mariusz Jakubowski
 */
public class SplitFileDialog extends JobDialog implements ActionListener {
    
	private static final int MAX_PARTS = 100;

	private static final String[] unitNames = new String[] {
		Translator.get("unit.bytes_short").toLowerCase(),
		Translator.get("unit.kb").toLowerCase(),
		Translator.get("unit.mb").toLowerCase(),
		Translator.get("unit.gb").toLowerCase()
	};
	
	private static final int[] unitBytes = new int[] {
		1,
		1024,
		1024*1024,
		1024*1024*1024
	};
	
    private final static DecimalFormat DECIMAL_FORMAT = (DecimalFormat)NumberFormat.getInstance();
	
    static {
    	DECIMAL_FORMAT.setGroupingUsed(false);
    }
    
    private String MSG_AUTO = Translator.get("split_file_dialog.auto");

    private AbstractFile file;
	private AbstractFile destFolder;

    private JButton btnSplit;
    private JButton btnClose;

	private FilePathField edtTargetDirectory;
	private JCheckBox cbGenerateCRC;
	private JComboBox cbSize;
	private JSpinner spnParts;
    
 

    /**
     * Creates a new split file dialog.
     * @param mainFrame the main frame
     * @param file a file to split
     * @param destFolder default destination folder 
     */
    public SplitFileDialog(MainFrame mainFrame, AbstractFile file, AbstractFile destFolder) {
        super(mainFrame, Translator.get("split_file_dialog.title"), new FileSet());
        this.file = file;
        this.destFolder = destFolder;
        initialize();
    }

    /**
     * Initializes the dialog.
     */
    protected void initialize() {
        Container content = getContentPane();
        content.setLayout(new BorderLayout(0, 5));
        YBoxPanel pnlInfo = new YBoxPanel();
        pnlInfo.add(new JLabel(Translator.get("split_file_dialog.file_to_split") + ": " + file.getName()));
        pnlInfo.add(new JLabel(Translator.get("size") + ": " + Long.toString(file.getSize())));
        content.add(pnlInfo, BorderLayout.NORTH);
        content.add(getPnlControls(), BorderLayout.CENTER);
        content.add(getPnlButtons(), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnSplit);
        updatePartsNumber();
    }

    private JPanel getPnlControls() {
		YBoxPanel pnlControls = new YBoxPanel();
		XBoxPanel pnlDir = new XBoxPanel();
		pnlDir.add(new JLabel(Translator.get("split_file_dialog.target_directory") + ":"));
		pnlDir.addSpace(5);
		edtTargetDirectory = new FilePathField(destFolder.getAbsolutePath(), 40);
		pnlDir.add(edtTargetDirectory);
		pnlControls.add(pnlDir);
		pnlControls.addSpace(5);
		XBoxPanel pnlSize = new XBoxPanel();
		pnlSize.add(new JLabel(Translator.get("split_file_dialog.part_size") + ":"));
		pnlSize.addSpace(5);
		String[] sizes = new String[] {
			MSG_AUTO,	
			"10 " + Translator.get("unit.mb"),
			"100 " + Translator.get("unit.mb"),
			"250 " + Translator.get("unit.mb"),
			"650 " + Translator.get("unit.mb"),
			"700 " + Translator.get("unit.mb")
		};
		cbSize = new JComboBox(sizes);
		cbSize.setEditable(true);
		cbSize.addActionListener(this);
		pnlSize.add(cbSize);
		pnlSize.addSpace(10);
		pnlSize.add(new JLabel(Translator.get("split_file_dialog.parts") + ":"));
		pnlSize.addSpace(5);
		spnParts = new JSpinner(new SpinnerNumberModel(1, 1, file.getSize(), 1));
		//spnParts.addChangeListener(this);   // TODO make editable
		spnParts.setEnabled(false);
		pnlSize.add(spnParts);
		pnlControls.add(pnlSize);
		pnlControls.addSpace(5);
		cbGenerateCRC = new JCheckBox(Translator.get("split_file_dialog.generate_CRC"));
		cbGenerateCRC.setSelected(true);
		pnlControls.add(cbGenerateCRC);
		return pnlControls;
	}

	/**
     * Creates bottom panel with buttons.
     */
    private JPanel getPnlButtons() {
        btnSplit = new JButton(Translator.get("split_file_dialog.split"));
        btnClose = new JButton(Translator.get("cancel"));
        return DialogToolkit.createOKCancelPanel(btnSplit, btnClose, getRootPane(), this);
    }

    
    /**
     * Executes the split job.
     */
	private void startJob() {
		int size = getBytes();
		if (size < 1) 
			return;		

		String destPath = edtTargetDirectory.getText();
        PathUtils.ResolvedDestination resolvedDest = 
        	PathUtils.resolveDestination(destPath, mainFrame.getActiveTable().getCurrentFolder());
        // The path entered doesn't correspond to any existing folder
        if (resolvedDest==null || (files.size()>1 && 
        		resolvedDest.getDestinationType()!=PathUtils.ResolvedDestination.EXISTING_FOLDER)) {
            showErrorDialog(Translator.get("invalid_path", destPath), Translator.get("split_file_dialog.title"));
            return;
        }

        long parts = getParts();
        if (parts > MAX_PARTS) {
            showErrorDialog(Translator.get("split_file_dialog.max_parts", 
            		Integer.toString(MAX_PARTS)), Translator.get("split_file_dialog.title"));
        	return;
        }
        ProgressDialog progressDialog = new ProgressDialog(mainFrame,
                Translator.get("progress_dialog.processing_files"));
		SplitFileJob job = new SplitFileJob(progressDialog, mainFrame,
		        file, resolvedDest.getDestinationFolder(), size, (int) parts);
		job.setIntegrityCheckEnabled(cbGenerateCRC.isSelected());
        progressDialog.start(job);
	}
 
    /**
     * Returns number of bytes entered in "Bytes per part" control.
     * @return
     */
    private int getBytes() {
		String strVal = ((String) cbSize.getSelectedItem()).trim();
		if (MSG_AUTO.equals(strVal))
			return (int) file.getSize();
		String[] strArr = strVal.split(" ");
		if (strArr.length < 1 || strArr.length > 2) {
			return -1;
		}
		int unit = 1;
		if (strArr.length == 2) {
			unit = -1;
			strArr[1] = strArr[1].toLowerCase();
			for (int i = 0; i < unitNames.length; i++) {
				if (unitNames[i].equals(strArr[1])) {
					unit = unitBytes[i];
					break;
				}
			}
			if (unit == -1) {
				return -1;
			}
		}
		try {
			double size = DECIMAL_FORMAT.parse(strArr[0]).doubleValue();
			size *= unit;
			return (int) size;
		} catch (Exception e) {
			return -1;
		}
    }
    
    /**
     * Returns number of parts this file will be splitted.
     * @return
     */
    private int getParts() {
		int size = getBytes();
		if (size < 1) 
			return -1;
		return (int) Math.ceil((double)file.getSize() / (double)size);
    }

    /**
     * Updates number of parts displayed in "Parts" control based on
     * "Bytes per part" control.
     */
	private void updatePartsNumber() {
		int parts = getParts();
		if (parts < 1)
			return;
		spnParts.setValue(new Integer(parts));
	}

    // /////////////////////////////////
    // ActionListener implementation //
    // /////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnClose) {
            dispose();
        } else if (source == cbSize) {
        	updatePartsNumber();
        } else if (source == btnSplit) {
            dispose();
            startJob();
        } 
    }
    
}
