
package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;

public interface LocationListener {
	/**
	 * Invoked when FileTable's current folder has changed.
	 */
	public void locationChanged(FolderPanel folderPanel);
}
