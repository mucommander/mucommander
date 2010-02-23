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

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.PathUtils;
import com.mucommander.job.SplitFileJob;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.SplitFileAction;
import com.mucommander.ui.combobox.ComboBoxListener;
import com.mucommander.ui.combobox.EditableComboBox;
import com.mucommander.ui.combobox.SaneComboBox;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Dialog used to split a file into several parts.
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
	private JTextField edtSize;
	private JSpinner spnParts;

	protected boolean edtChange;


    /**
     * Creates a new split file dialog.
     * @param mainFrame the main frame
     * @param file a file to split
     * @param destFolder default destination folder 
     */
    public SplitFileDialog(MainFrame mainFrame, AbstractFile file, AbstractFile destFolder) {
        super(mainFrame, ActionProperties.getActionLabel(SplitFileAction.Descriptor.ACTION_ID), new FileSet());
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
        XAlignedComponentPanel pnlMain = new XAlignedComponentPanel(10);

        pnlMain.addRow(Translator.get("split_file_dialog.file_to_split") + ":", new JLabel(file.getName()), 0);
        String size = SizeFormat.format(file.getSize(), SizeFormat.DIGITS_FULL | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE);
        pnlMain.addRow(Translator.get("size") + ":", new JLabel(size), 10);
        
		edtTargetDirectory = new FilePathField(destFolder.getAbsolutePath(), 40);
        pnlMain.addRow(Translator.get("split_file_dialog.target_directory") + ":", edtTargetDirectory, 5);

        XBoxPanel pnlSize = new XBoxPanel();
		String[] sizes = new String[] {
			MSG_AUTO,	
			"10 " + Translator.get("unit.mb"),
			"100 " + Translator.get("unit.mb"),
			"250 " + Translator.get("unit.mb"),
			"650 " + Translator.get("unit.mb"),
			"700 " + Translator.get("unit.mb")
		};
		edtSize = new JTextField();
		EditableComboBox cbSize = new EditableComboBox(edtSize, sizes);
		cbSize.setComboSelectionUpdatesTextField(true);
		cbSize.setSelectedIndex(1);
		edtSize.addKeyListener(new KeyAdapter() {
			@Override
            public void keyReleased(KeyEvent e) {
				updatePartsNumber();
			}
		});
		cbSize.addComboBoxListener(new ComboBoxListener() {			
			public void comboBoxSelectionChanged(SaneComboBox source) {
				updatePartsNumber();				
			}
		});
		pnlSize.add(cbSize);
		pnlSize.addSpace(10);
		pnlSize.add(new JLabel(Translator.get("split_file_dialog.parts") + ":"));
		pnlSize.addSpace(5);
		spnParts = new JSpinner(new SpinnerNumberModel(1, 1,
                file.getSize(), 1));
		spnParts.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent e) {
				if (!edtChange) {
					long parts = ((Number)spnParts.getValue()).longValue();
					long newsize = file.getSize() / parts;
					if (file.getSize() % parts != 0) {
						newsize++;
					}
					if (getBytes() != newsize) {
						edtSize.setText(Long.toString(newsize));
					}
				}
			}
		});   
		pnlSize.add(spnParts);
        pnlMain.addRow(Translator.get("split_file_dialog.part_size") + ":", pnlSize, 0);
        
		cbGenerateCRC = new JCheckBox(Translator.get("split_file_dialog.generate_CRC"));
		cbGenerateCRC.setSelected(true);
		pnlMain.addRow("", cbGenerateCRC, 0);

		content.add(pnlMain, BorderLayout.CENTER);
        content.add(getPnlButtons(), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnSplit);
        updatePartsNumber();
    }


	/**
     * Creates bottom panel with buttons.
     */
    private JPanel getPnlButtons() {
        btnSplit = new JButton(Translator.get("split"));
        btnClose = new JButton(Translator.get("cancel"));
        return DialogToolkit.createOKCancelPanel(btnSplit, btnClose, getRootPane(), this);
    }

    
    /**
     * Executes the split job.
     */
	private void startJob() {
		long size = getBytes();
		if (size < 1) { 
			return;		
		}

		String destPath = edtTargetDirectory.getText();
        PathUtils.ResolvedDestination resolvedDest = 
        	PathUtils.resolveDestination(destPath, mainFrame.getActiveTable().getCurrentFolder());
        // The path entered doesn't correspond to any existing folder
        if (resolvedDest==null || (files.size()>1 && 
        		resolvedDest.getDestinationType()!=PathUtils.ResolvedDestination.EXISTING_FOLDER)) {
            showErrorDialog(Translator.get("invalid_path", destPath), Translator.get("split_file_dialog.error_title"));
            return;
        }

        long parts = getParts();
        if (parts > MAX_PARTS) {
            showErrorDialog(Translator.get("split_file_dialog.max_parts", 
            		Integer.toString(MAX_PARTS)), Translator.get("split_file_dialog.error_title"));
        	return;
        }
        ProgressDialog progressDialog = new ProgressDialog(mainFrame,
                Translator.get("progress_dialog.processing_files"));
		SplitFileJob job = new SplitFileJob(progressDialog, mainFrame,
		        file, resolvedDest.getDestinationFolder(), size, (int)parts);
		job.setIntegrityCheckEnabled(cbGenerateCRC.isSelected());
        progressDialog.start(job);
	}
 
    /**
     * Returns number of bytes entered in "Bytes per part" control.
     * @return
     */
    private long getBytes() {
		String strVal = edtSize.getText().trim();
		if (MSG_AUTO.equals(strVal)) {
			return file.getSize();
		}
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
			return (long) size;
		} catch (Exception e) {
			return -1;
		}
    }
    
    /**
     * Returns number of parts this file will be splitted.
     * @return
     */
    private long getParts() {
		long size = getBytes();
		if (size < 1) { 
			return -1;
		}
		return (long) Math.ceil((double)file.getSize() / (double)size);
    }

    /**
     * Updates number of parts displayed in "Parts" control based on
     * "Bytes per part" control.
     */
	private void updatePartsNumber() {
		long parts = getParts();
		if (parts < 1) {
			return;
		}
		edtChange = true;
		spnParts.setValue(parts);
		edtChange = false;
	}

    // /////////////////////////////////
    // ActionListener implementation //
    // /////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnClose) {
            dispose();
        } else if (source == btnSplit) {
            dispose();
            startJob();
        } 
    }
    
}
