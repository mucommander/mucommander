
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
		Class candidateClass;
		for(int i=0; i<viewersClassNames.length; i++) {
			candidateClass = Class.forName(viewersClassNames[i]);
			Method method = candidateClass.getMethod("canViewFile", new Class[]{Class.forName("com.mucommander.file.AbstractFile")});
System.out.println("getViewer: "+viewersClassNames[i]+" "+(((Boolean)method.invoke(null, new Object[]{file})).booleanValue()));
			if(((Boolean)method.invoke(null, new Object[]{file})).booleanValue()) {
				viewerClass = candidateClass;
				break;
			}
		}

		if(viewerClass==null)
			viewerClass = Class.forName(defaultViewerClassName);

		Constructor constructor = viewerClass.getConstructor(new Class[]{frame.getClass()});
		FileViewer fileViewer = (FileViewer)constructor.newInstance(new Object[]{frame});
		frame.setViewer(fileViewer);
		return fileViewer;
	}
}