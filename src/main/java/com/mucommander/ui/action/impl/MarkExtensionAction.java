/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.action.impl;

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.AbstractFilenameFilter;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;

/**
 * Action that marks / unmarks all files with a specific extension.
 * <p>
 * Marking behaves as follows:
 * <ul>
 *   <li>
 *     If the current selection is marked, all files whose extension matches that of the current selection will
 *     be unmarked.
 *   </li>
 *   <li>
 *     If the current selection isn't marked, all files whose extension matches that of the current selection will
 *     be marked.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * By default, this action will mark all files whose extension match that of the current selection in a case-insensitive fashion.
 * It can, however, be configured:
 * <ul>
 *   <li>
 *     If the <code>extension</code> property is set, its value prepended by a <code>.</code> is always going to be used regardless of the
 *     current selection.
 *   </li>
 *   <li>
 *     If the <code>case_sensitive</code> property is set to <code>true</code>, extension matching will be done in a case sensitive fashion.
 *   </li>
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 */
public class MarkExtensionAction extends MuAction {
    // - Property names ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Key that controls which extension should be matched. */
    public static final String EXTENSION_PROPERTY_KEY      = "extension";
    /** Key that controls whether extension matching should be done in a case sensitive fashion (defaults to false). */
    public static final String CASE_SENSITIVE_PROPERTY_KEY = "case_sensitive";



    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>MarkExtensionAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public MarkExtensionAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }



    // - Properties retrieval ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the extension that was configured in the action's properties.
     * @return the extension that was configured in the action's properties, <code>null</code> if none.
     */
    private String getExtension() {
        Object o;

        // If the key wasn't set, return null.
        if((o = getValue(EXTENSION_PROPERTY_KEY)) == null)
            return null;

        // If the value is a string, return it.
        if(o instanceof String)
            return (String)o;

        // Otherwise, return null.
        return null;
    }

    /**
     * Returns <code>true</code> if the action must compare string in a case-sensitive fashion.
     * @return <code>true</code> if the action must compare string in a case-sensitive fashion, <code>false</code> otherwise.
     */
    private boolean isCaseSensitive() {
        Object o;

        // If the action hasn't been configured, defaults to false.
        if((o = getValue(CASE_SENSITIVE_PROPERTY_KEY)) == null)
            return false;

        // Returns the configured value if it's a string, false otherwise.
        if(o instanceof String)
            return o.equals("true");
        return false;
    }



    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a {@link com.mucommander.commons.file.filter.FilenameFilter} that should be applied to all current files.
     * <p>
     * If the action has been configured using the <code>file.extension</code> property, the returned filter
     * will match that extension. Otherwise, the currently selected file's extension will be used. If it doesn't
     * have one, the returned filter will match all files such that 
     * <code>file.getExtension() == null</code>.
     * </p>
     * @param  file currently selected file.
     * @return      the filter that should be applied by this action.
     */
    private FilenameFilter getFilter(AbstractFile file) {
        String                  ext;
        ExtensionFilenameFilter filter;

        // If no extension has been configured, analyse the current selection.
        if((ext = getExtension()) == null) {

            // If there is no current selection, abort.
            if(file == null)
                return null;

            // If the current file doesn't have an extension, return a filename filter that
            // match null extensions.
            if((ext = file.getExtension()) == null)
                return new AbstractFilenameFilter() {
                    public boolean accept(String name) {return AbstractFile.getExtension(name) == null;}
                };
        }

        // At this point, ext contains the extension that should be matched.
        filter = new ExtensionFilenameFilter("." + ext);

        // Initialises the filter's case-sensitivy depending on the action's propeties.
        filter.setCaseSensitive(isCaseSensitive());

        return filter;
    }

    /**
     * Marks all files whose extension matches the current selection.
     */
    @Override
    public void performAction() {
        FileTable      fileTable;
        FileTableModel tableModel;
        FilenameFilter filter;
        int            rowCount;
        boolean        mark;

        // Initialization. Aborts if there is no selected file.
        fileTable  = mainFrame.getActiveTable();
        if((filter = getFilter(fileTable.getSelectedFile(false, true))) == null)
            return;
        tableModel = fileTable.getFileTableModel();
        rowCount   = tableModel.getRowCount();
        mark       = !tableModel.isRowMarked(fileTable.getSelectedRow());

        // Goes through all files in the active table, marking all that match 'filter'.
        for(int i = tableModel.getFirstMarkableRow(); i < rowCount; i++)
            if(filter.accept(tableModel.getCachedFileAtRow(i)))
                tableModel.setRowMarked(i, mark);
        fileTable.repaint();

        // Notify registered listeners that currently marked files have changed on the FileTable
        fileTable.fireMarkedFilesChangedEvent();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
			return new MarkExtensionAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "MarkExtension";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.SELECTION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.SHIFT_DOWN_MASK); }
    }
}
