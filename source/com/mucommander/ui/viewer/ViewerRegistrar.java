/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.viewer;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;

/**
 * ViewerRegistrar maintains a list of registered file viewers and provides methods to dynamically register file viewers
 * and create appropriate FileViewer (Panel) and ViewerFrame (Window) instances for a given AbstractFile.
 *
 * @author Maxence Bernard
 */
public class ViewerRegistrar {
	
    /** List of registered file viewers */ 
    private final static Vector viewerFactories = new Vector();

    /** Default viewer */
    private final static ViewerFactory DEFAULT_VIEWER = new com.mucommander.ui.viewer.text.TextFactory();
	    
    static {registerFileViewer(new com.mucommander.ui.viewer.image.ImageFactory());}
    
    
    /**
     * Registers a FileViewer.
     * @param factory file viewer factory to register.
     */
    public static void registerFileViewer(ViewerFactory factory) {viewerFactories.add(factory);}
        
	
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

        // Use new Window decorations introduced in Mac OS X 10.5 (Leopard)
        if(OsFamilies.MAC_OS_X.isCurrent() && PlatformManager.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher()) {
            // Displays the document icon in the window title bar, works only for local files
            if(file.getURL().getProtocol().equals(FileProtocols.FILE))
                frame.getRootPane().putClientProperty("Window.documentFile", file.getUnderlyingFileObject()); 
        }

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
        Iterator      iterator;
        ViewerFactory factory;

        iterator = viewerFactories.iterator();
        while(iterator.hasNext()) {
            factory = (ViewerFactory)iterator.next();
            if(factory.canViewFile(file))
                return factory.createFileViewer();
        }
        return DEFAULT_VIEWER.createFileViewer();
    }
}
