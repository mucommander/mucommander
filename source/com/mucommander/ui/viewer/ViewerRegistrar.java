
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.MainFrame;

import java.lang.reflect.*;
import java.awt.*;
import java.io.IOException;


public class ViewerRegistrar {
	
	private final static String defaultViewerClassName = "com.mucommander.ui.viewer.TextViewer";
	
	private final static String viewersClassNames[] = new String[] {
		"com.mucommander.ui.viewer.TextViewer",
		"com.mucommander.ui.viewer.ImageViewer"
	};
	
	
	public static FileViewer getViewer(AbstractFile file, MainFrame mainFrame) throws Exception {
		ViewerFrame frame = new ViewerFrame(mainFrame, file);
		
		Class viewerClass = null;
		Class candidateClass;
		Constructor constructor;
		FileViewer fileViewer;
		Method method;
		for(int i=0; i<viewersClassNames.length; i++) {
			candidateClass = Class.forName(viewersClassNames[i]);
			constructor = candidateClass.getConstructor(new Class[]{});
			method = candidateClass.getMethod("canViewFile", new Class[]{Class.forName("com.mucommander.file.AbstractFile")});
			fileViewer = (FileViewer)constructor.newInstance(new Object[]{});

//if(com.mucommander.Debug.ON) System.out.println("getViewer: "+viewersClassNames[i]+" "+(((Boolean)method.invoke(null, new Object[]{file})).booleanValue()));
			if(((Boolean)method.invoke(fileViewer, new Object[]{file})).booleanValue()) {
				viewerClass = candidateClass;
				break;
			}
		}

		if(viewerClass==null)
			viewerClass = Class.forName(defaultViewerClassName);

		constructor = viewerClass.getConstructor(new Class[]{frame.getClass()});
		fileViewer = (FileViewer)constructor.newInstance(new Object[]{frame});
		frame.setViewer(fileViewer);
		return fileViewer;
	}
}