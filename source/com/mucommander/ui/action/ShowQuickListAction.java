package com.mucommander.ui.action;

import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

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
