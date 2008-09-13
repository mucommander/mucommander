package com.mucommander.ui.action;

import java.util.Hashtable;

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

public class ShowBookmarksQLAction extends ShowQuickListAction {
	
	public ShowBookmarksQLAction(MainFrame mainFrame, Hashtable properties) {
		super(mainFrame, properties);
	}
	
	public void performAction() {
		openQuickList(FolderPanel.BOOKMARKS_QUICK_LIST_INDEX);
	}
}