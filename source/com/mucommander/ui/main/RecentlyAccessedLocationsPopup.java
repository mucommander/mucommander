package com.mucommander.ui.main;

import java.util.LinkedList;

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
 * This pop up shows the recently accessed locations from the FileTable.
 * 
 * @author Arik Hadas
 */
public class RecentlyAccessedLocationsPopup extends FileTablePopupWithIcons implements LocationListener{
	private static int MAX_ELEMENTS = 10;
	private LinkedList linkedList;

	public RecentlyAccessedLocationsPopup(FolderPanel panel) {
		super(Translator.get("recently_accessed_locations.title"), Translator.get("recently_accessed_locations.empty_message"), panel);
		
		linkedList = new LinkedList();
		panel.getLocationManager().addLocationListener(this);
	}
	
	protected ImageIcon getImageIcon(String value) {
		AbstractFile file = FileFactory.getFile(value);
		if (file != null)
			return IconManager.getImageIcon(FileIcons.getFileIcon(file));
		return null;
	}

	protected void acceptListItem(String item) {
		folderPanel.tryChangeCurrentFolder(item);
	}

	public void locationCancelled(LocationEvent locationEvent) {
		
	}

	public void locationChanged(LocationEvent locationEvent) {
		String currentFolder = folderPanel.getCurrentFolder().getAbsolutePath();
		if (!linkedList.remove(currentFolder) && linkedList.size() > MAX_ELEMENTS)
			linkedList.removeLast();
		linkedList.addFirst(currentFolder);
		
		setData(linkedList);
	}

	public void locationChanging(LocationEvent locationEvent) {
		
	}

	public void locationFailed(LocationEvent locationEvent) {
		
	}
}
