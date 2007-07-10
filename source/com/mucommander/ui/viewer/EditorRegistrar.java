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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * EditorRegistrar maintains a list of registered file editors and provides methods to dynamically register file editors
 * and create appropriate FileEditor (Panel) and EditorFrame (Window) instances for a given AbstractFile.
 *
 * @author Maxence Bernard
 */
public class EditorRegistrar {
	
    /** List of registered file editors */ 
    private final static Vector editorsClassNames = new Vector();

    /** Default editor's class name */
    private final static String DEFAULT_EDITOR_CLASSNAME = "com.mucommander.ui.viewer.text.TextEditor";


    static {
        // Register internal file editors
    }


    /**
     * Registers a FileEditor. The given class name must correspond to a class that extends {@link FileEditor FileEditor}. 
     */
    public static void registerFileEditor(String className) {
        editorsClassNames.add(className);
    }


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
        Class editorClass = null;
        Class candidateClass;
        Constructor constructor;
        FileEditor fileEditor;
        Method method;
        int nbRegisteredEditors = editorsClassNames.size();
        // Find an editor that can edit the specified file
        for(int i=0; i<nbRegisteredEditors; i++) {
            try {
                // Invoke the 'canEditFile' method
                candidateClass = Class.forName((String)editorsClassNames.elementAt(i));
                method = candidateClass.getMethod("canEditFile", new Class[]{Class.forName("com.mucommander.file.AbstractFile")});

                if(((Boolean)method.invoke(null, new Object[]{file})).booleanValue()) {
                    editorClass = candidateClass;
                    break;
                }
            }
            catch(Exception e) {    // Catch ClassNotFoundException, NoSuchMethodException and more...
                // Report the error and continue
                if(com.mucommander.Debug.ON)
                    com.mucommander.Debug.trace("Exception thrown while trying to access "+editorsClassNames.elementAt(i)+": "+e);
                continue;
            }
        }

        // If no editor is able to edit the file, use the default editor 
        if(editorClass==null)
            editorClass = Class.forName(DEFAULT_EDITOR_CLASSNAME);

        // Create an instance of the FileEditor's class
        constructor = editorClass.getConstructor(new Class[]{});
        fileEditor = (FileEditor)constructor.newInstance(new Object[]{});
        return fileEditor;
    }
}
