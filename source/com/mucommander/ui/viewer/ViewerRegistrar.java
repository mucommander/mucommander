
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;

import java.awt.*;

import java.io.IOException;

import java.lang.reflect.*;


public class ViewerRegistrar {
	
	private final static String defaultViewerClassName = "com.mucommander.ui.viewer.TextViewer";
	
	private final static String viewersClassNames[] = new String[] {
		"com.mucommander.ui.viewer.TextViewer",
		"com.mucommander.ui.viewer.ImageViewer"
	};
	
	
	public static FileViewer getViewer(AbstractFile file) throws Exception {
		ViewerFrame frame = new ViewerFrame(file);
		
		Class viewerClass = null;
		for(int i=0; i<viewersClassNames.length; i++) {
			viewerClass = Class.forName(viewersClassNames[i]);
			Method method = viewerClass.getMethod("canViewFile", new Class[]{Class.forName("com.mucommander.file.AbstractFile")});
			boolean res = ((Boolean)method.invoke(null, new Object[]{file})).booleanValue();
			if(res)
				break;
		}

		if(viewerClass==null)
			viewerClass = Class.forName(defaultViewerClassName);

		Constructor constructor = viewerClass.getConstructor(new Class[]{frame.getClass()});
		FileViewer fileViewer = (FileViewer)constructor.newInstance(new Object[]{frame});
		frame.setViewer(fileViewer);
		return fileViewer;
	}
}