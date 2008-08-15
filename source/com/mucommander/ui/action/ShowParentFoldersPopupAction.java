package com.mucommander.ui.action;

import java.util.Hashtable;

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This action shows ParentFoldersPopup on the current active FileTable.
 * 
 * @author Arik Hadas
 */
public class ShowParentFoldersPopupAction extends ShowFileTablePopupAction {
	
	public ShowParentFoldersPopupAction(MainFrame mainFrame, Hashtable properties) {
		super(mainFrame, properties);
	}
	
	public void performAction() {
		openPopup(FolderPanel.PARENT_FOLDERS_POPUP_INDEX);
	}
}
