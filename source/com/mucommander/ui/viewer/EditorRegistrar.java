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
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;

/**
 * EditorRegistrar maintains a list of registered file editors and provides methods to dynamically register file editors
 * and create appropriate FileEditor (Panel) and EditorFrame (Window) instances for a given AbstractFile.
 *
 * @author Maxence Bernard
 */
public class EditorRegistrar {
	
    /** List of registered file editors */ 
    private final static Vector editorFactories = new Vector();

    /** Default editor. */
    private final static EditorFactory DEFAULT_EDITOR = new com.mucommander.ui.viewer.text.TextFactory();

    /**
     * Registers a FileEditor.
     * @param factory file editor factory to register.
     */
    public static void registerFileEditor(EditorFactory factory) {editorFactories.add(factory);}

    /**
     * Creates and returns an EditorFrame to start viewing the given file. The EditorFrame will be monitored
     * so that if it is the last window on screen when it is closed by the user, it will trigger the shutdown sequence.
     *
     * @param mainFrame the parent MainFrame instance
     * @param file the file that will be displayed by the returned EditorFrame 
     * @param icon editor frame's icon.
     * @return the created EditorFrame
     */
    public static EditorFrame createEditorFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        EditorFrame frame = new EditorFrame(mainFrame, file, icon);

        // Use new Window decorations introduced in Mac OS X 10.5 (Leopard)
        if(PlatformManager.MAC_OS_X_10_5.isCurrentOrHigher()) {
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
     * Creates and returns an appropriate FileEditor for the given file type.
     *
     * @param file the file that will be displayed by the returned FileEditor
     * @return the created FileEditor
     * @throws Exception if an editor couldn't be instanciated or the given file couldn't be read.
     */
    public static FileEditor createFileEditor(AbstractFile file) throws Exception {
        Iterator      iterator;
        EditorFactory factory;

        iterator = editorFactories.iterator();
        while(iterator.hasNext()) {
            factory = (EditorFactory)iterator.next();
            if(factory.canEditFile(file))
                return factory.createFileEditor();
        }
        return DEFAULT_EDITOR.createFileEditor();
    }
}
