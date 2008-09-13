package com.mucommander.ui.action;

import java.util.Hashtable;

import com.mucommander.ui.main.MainFrame;

/**
 * ShowFileTablePopupAction is an abstract action that shows pop up corresponding to the given
 * 	index on the currently active FileTable.
 *
 * @author Arik Hadas
 */
abstract class ShowQuickListAction extends MuAction {

	public ShowQuickListAction(MainFrame mainFrame, Hashtable properties) {
		super(mainFrame, properties);		
	}
	
	public void openQuickList(int index) {		
		mainFrame.getActivePanel().showQuickList(index);
	}
}
