
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;

import java.awt.*;

import java.io.IOException;

public class ViewerRegistrar {
	
	public static FileViewer getViewer(AbstractFile file) throws IOException {
		ViewerFrame frame = new ViewerFrame(file);
		
		FileViewer viewer;
		if(TextViewer.canViewFile(file))
			viewer = new TextViewer(frame);
//		else if(ImageViewer.canViewFile(file))
//           return new ImageViewer();
		else
			viewer = new TextViewer(frame);

		frame.setViewer(viewer);
		return viewer;
	}
}