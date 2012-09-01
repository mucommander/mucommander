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

package com.mucommander.ui.main.quicklist;

import java.io.IOException;
import java.util.LinkedList;

import javax.swing.Icon;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.job.TempExecJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowRecentExecutedFilesQLAction;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows recently executed files.
 * 
 * @author Arik Hadas
 */

public class RecentExecutedFilesQL extends QuickListWithIcons<AbstractFile> {
	private static LinkedList<AbstractFile> list = new LinkedList<AbstractFile>();
	private static final int MAX_NUM_OF_ELEMENTS = 10;
	private FolderPanel folderPanel;
	
	public RecentExecutedFilesQL(FolderPanel folderPanel) {
		super(folderPanel, ActionProperties.getActionLabel(ShowRecentExecutedFilesQLAction.Descriptor.ACTION_ID), Translator.get("recent_executed_files_quick_list.empty_message"));
		
		this.folderPanel = folderPanel;
	}
	
	@Override
    protected void acceptListItem(AbstractFile item) {
		MainFrame mainFrame = WindowManager.getCurrentMainFrame();

		if(item.getURL().getScheme().equals(FileProtocols.FILE) && (item.hasAncestor(LocalFile.class))) {
            try { DesktopManager.open(item); }
            catch(IOException e) {}
        }

        // Copies non-local file in a temporary local file and opens them using their native association.
        else {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempExecJob job = new TempExecJob(progressDialog, mainFrame, item);
            progressDialog.start(job);
        }
	}
	
	public static void addFile(AbstractFile file) {
		if (!list.remove(file) && list.size() > MAX_NUM_OF_ELEMENTS)
			list.removeLast();
		list.addFirst(file);
	}

	@Override
    protected AbstractFile[] getData() {
		return list.toArray(new AbstractFile[0]);
	}

	@Override
    protected Icon itemToIcon(AbstractFile item) {
		return getIconOfFile(item);
	}
}
