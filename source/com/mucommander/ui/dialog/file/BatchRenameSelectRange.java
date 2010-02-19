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

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog that allows select a range of characters from file name. 
 * Invoked from batch-rename dialog.
 *  
 * @author Mariusz Jakubowski
 *
 */
public class BatchRenameSelectRange extends FocusDialog implements ActionListener {

    private JTextField edtRange;
    private JButton btnCancel;
    private JButton btnOK;
    private String range = null;
    

    public BatchRenameSelectRange(Dialog owner, String filename) {
        super(owner, Translator.get("batch_rename_dialog.range"), owner);
        edtRange = new JTextField();
        ReadOnlyDocument doc = new ReadOnlyDocument();
        edtRange.setDocument(doc);
        edtRange.setText(filename);
        edtRange.setColumns(filename.length() + 5);
        edtRange.setSelectionStart(0);
        edtRange.setSelectionEnd(filename.length());
        doc.setReadOnly(true);
        Container content = getContentPane();
        content.setLayout(new BorderLayout());            
        content.add(edtRange, BorderLayout.CENTER);

        btnOK = new JButton(Translator.get("ok"));
        btnCancel = new JButton(Translator.get("cancel"));
        content.add(DialogToolkit.createOKCancelPanel(btnOK, btnCancel, getRootPane(), this), BorderLayout.SOUTH);
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnCancel) {
            dispose();
        } else if (source == btnOK) {
            range = "[N" + Integer.toString(edtRange.getSelectionStart()+1);
            if (edtRange.getSelectionEnd() > 0 && edtRange.getSelectionEnd() > edtRange.getSelectionStart()+1) {
                range += "-" + Integer.toString(edtRange.getSelectionEnd());
            }
            range += "]";
            dispose();
        }
    }

    /**
     * Returns a token with selected range.
     * @return
     */
    public String getRange() {
        return range;
    }
    
    /**
     * A document model that can be disabled for editing.
     * @author Mariusz Jakubowski
     *
     */
    private static class ReadOnlyDocument extends PlainDocument {
        private boolean readOnly = false;
        
        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }
        
        @Override
        public void insertString(int offs, String str, AttributeSet a)
                throws BadLocationException {
            if (!readOnly) {
                super.insertString(offs, str, a);
            }
        }

        @Override
        public void remove(int offs, int len)
                throws BadLocationException {
            if (!readOnly) {
                super.remove(offs, len);
            }
        }
    
    }
    
    
}
