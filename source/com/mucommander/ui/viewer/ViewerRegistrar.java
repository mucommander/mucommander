
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;

import java.io.IOException;

public class ViewerRegistrar {
	public static FileViewer getViewer(AbstractFile file) throws IOException {
		if(TextViewer.canViewFile(file))
			return new TextViewer();
//        if(ImageViewer.canViewFile(file))
//            return new ImageViewer();

		return null;		
	}
}