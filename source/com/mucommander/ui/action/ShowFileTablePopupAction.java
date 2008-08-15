package com.mucommander.ui.action;

import java.util.Hashtable;

import com.mucommander.ui.main.MainFrame;

/**
 * ShowFileTablePopupAction is an abstract action that shows pop up corresponding to the given
 * 	index on the currently active FileTable.
 *
 * @author Arik Hadas
 */
abstract class ShowFileTablePopupAction extends MuAction {

	public ShowFileTablePopupAction(MainFrame mainFrame, Hashtable properties) {
		super(mainFrame, properties);		
	}
	
	public void openPopup(int index) {		
		mainFrame.getActivePanel().showPopup(index);
	}
}
