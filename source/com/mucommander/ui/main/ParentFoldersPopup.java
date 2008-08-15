package com.mucommander.ui.main;

import java.util.Vector;

import javax.swing.ImageIcon;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.popup.FileTablePopupWithIcons;
import com.mucommander.text.Translator;

/**
 * This pop up shows the parent folders of the current location in the FileTable.
 * 
 * @author Arik Hadas
 */
public class ParentFoldersPopup extends FileTablePopupWithIcons implements LocationListener {
		
	public ParentFoldersPopup(FolderPanel panel) {
		super(Translator.get("parent_folders_popup.title"), Translator.get("parent_folders_popup.empty_message"), panel);
		
		panel.getLocationManager().addLocationListener(this);		
	}
	
	protected void acceptListItem(String item) {
		folderPanel.tryChangeCurrentFolder(item);
	}
	
	public void locationChanged(LocationEvent locationEvent) {
       populateParentFolders(folderPanel.getCurrentFolder());
	}
	
	private void populateParentFolders(AbstractFile folder) {
		Vector parents = new Vector();
				
		while((folder=folder.getParentSilently())!=null)
            parents.add(folder.getAbsolutePath());
		
        setData(parents);
    }
	
	public void locationCancelled(LocationEvent locationEvent) {}

	public void locationChanging(LocationEvent locationEvent) {}

	public void locationFailed(LocationEvent locationEvent) {}

	protected ImageIcon getImageIcon(String value) {
		AbstractFile file = FileFactory.getFile(value);
		if (file != null)
			return IconManager.getImageIcon(FileIcons.getFileIcon(file));
		return null;
	}
}
