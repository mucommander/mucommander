/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

import java.awt.Image;

/**
 * ViewerRegistrar maintains a list of registered file viewers and provides methods to dynamically register file viewers
 * and create appropriate FileViewer (Panel) and ViewerFrame (Window) instances for a given AbstractFile.
 *
 * @author Maxence Bernard
 */
public class ViewerRegistrar {
	
    /** List of registered file viewers */ 
    private final static Vector viewersClassNames = new Vector();

    /** Default viewer's class name */
    private final static String DEFAULT_VIEWER_CLASSNAME = "com.mucommander.ui.viewer.TextViewer";
	    
    /** Image viewer's class name */
    private final static String IMAGE_VIEWER_CLASSNAME = "com.mucommander.ui.viewer.ImageViewer";

        
    static {
        // Register internal file viewers
        registerFileViewer(IMAGE_VIEWER_CLASSNAME);
    }
    
    
    /**
     * Registers a FileViewer. The given class name must correspond to a class that extends {@link FileViewer FileViewer}. 
     */
    public static void registerFileViewer(String className) {
        viewersClassNames.add(className);
    }
        
	
    /**
     * Creates and returns a ViewerFrame to start viewing the given file. The ViewerFrame will be monitored
     * so that if it is the last window on screen when it is closed by the user, it will trigger the shutdown sequence.
     *
     * @param mainFrame the parent MainFrame instance
     * @param file the file that will be displayed by the returned ViewerFrame
     * @param icon window's icon.
     * @return the created ViewerFrame
     */
    public static ViewerFrame createViewerFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        ViewerFrame frame = new ViewerFrame(mainFrame, file, icon);

        // WindowManager will listen to window closed events to trigger shutdown sequence
        // if it is the last window visible
        frame.addWindowListener(WindowManager.getInstance());
        
        return frame;
    }
    
    
    /**
     * Creates and returns an appropriate FileViewer for the given file type.
     *
     * @param file the file that will be displayed by the returned FileViewer
     * @return the created FileViewer
     * @throws Exception if a viewer couldn't be instanciated or the given file couldn't be read.
     */
    public static FileViewer createFileViewer(AbstractFile file) throws Exception {
        Class viewerClass = null;
        Class candidateClass;
        Constructor constructor;
        FileViewer fileViewer;
        Method method;
        int nbRegisteredViewers = viewersClassNames.size();
        // Find an editor that can edit the specified file
        for(int i=0; i<nbRegisteredViewers; i++) {
            try {
                // Invoke the 'canViewFile' method
                candidateClass = Class.forName((String)viewersClassNames.elementAt(i));
                method = candidateClass.getMethod("canViewFile", new Class[]{Class.forName("com.mucommander.file.AbstractFile")});

                if(((Boolean)method.invoke(null, new Object[]{file})).booleanValue()) {
                    viewerClass = candidateClass;
                    break;
                }
            }
            catch(Exception e) {    // Catch ClassNotFoundException, NoSuchMethodException and more...
                // Report the error and continue
                if(com.mucommander.Debug.ON)
                    com.mucommander.Debug.trace("Exception thrown while trying to access "+viewersClassNames.elementAt(i)+": "+e);
                continue;
            }
        }

        // If no viewer is able to view the file, use the default viewer 
        if(viewerClass==null)
            viewerClass = Class.forName(DEFAULT_VIEWER_CLASSNAME);

        // Create an instance of the FileViewer's class
        constructor = viewerClass.getConstructor(new Class[]{});
        fileViewer = (FileViewer)constructor.newInstance(new Object[]{});
        return fileViewer;
    }
}
