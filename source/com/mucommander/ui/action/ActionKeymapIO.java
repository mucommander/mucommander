/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.action;

import com.mucommander.AppLogger;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class contains the common things to the actions reading and writing.
 * 
 * @author Maxence Bernard, Arik Hadas
 */
public abstract class ActionKeymapIO extends DefaultHandler  {
	
	/* Variables used for XML parsing */
	protected final static String ROOT_ELEMENT = "keymap";
    protected final static String ACTION_ELEMENT = "action";
    protected final static String CLASS_ATTRIBUTE = "class";
    protected final static String ID_ATTRIBUTE = "id";
    protected final static String PRIMARY_KEYSTROKE_ATTRIBUTE = "keystroke";
    protected final static String ALTERNATE_KEYSTROKE_ATTRIBUTE = "alt_keystroke";
    /** Attribute containing the last muCommander version that was used to create the file */
	protected static final String VERSION_ATTRIBUTE  = "version";
    
    /** Actions file used when calling {@link #loadActionKeymap()} */
    private static AbstractFile actionsFile;
	
    /** Default actions filename */
    private final static String DEFAULT_ACTIONS_FILE_NAME = "action_keymap.xml";
    /** Path to the actions resource file within the application JAR file */
    public final static String ACTION_KEYMAP_RESOURCE_PATH = "/" + DEFAULT_ACTIONS_FILE_NAME;
    
    /** Whether the actions have been modified since the last time they were saved */
    protected static boolean wereActionsModified;
    
    private static ActionKeymapWriter writer = new ActionKeymapWriter();
    
	/**
     * Sets the path to the user actions file to be loaded when calling {@link #loadActionKeymap()}.
     * By default, this file is {@link #DEFAULT_ACTIONS_FILE_NAME} within the preferences folder.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setActionsFile(FileFactory.getFile(file))</code>.
     * </p>
     * @param  path                  path to the actions file
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     */
    public static void setActionsFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setActionsFile(new File(path));
        else
            setActionsFile(file);
    }
    
    /**
     * Sets the path to the user actions file to be loaded when calling {@link #loadActionKeymap()}.
     * By default, this file is {@link #DEFAULT_ACTIONS_FILE_NAME} within the preferences folder.
     * <p>
     * This is a convenience method and is strictly equivalent to calling <code>setActionsFile(FileFactory.getFile(file.getAbsolutePath()))</code>.
     * </p>
     * @param  file                  path to the actions file
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     */
    private static void setActionsFile(File file) throws FileNotFoundException {setActionsFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the user actions file to be loaded when calling {@link #loadActionKeymap()}.
     * By default, this file is {@link #DEFAULT_ACTIONS_FILE_NAME} within the preferences folder.
     * @param  file                  path to the actions file
     * @throws FileNotFoundException if <code>file</code> is not accessible.
     */
    private static void setActionsFile(AbstractFile file) throws FileNotFoundException {
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());

        actionsFile = file;
    }
    
    /**
     * Returns the actions file.
     * @return             the actions file.
     * @throws IOException if an error occurred while locating the default actions file.
     */
    protected static AbstractFile getActionsFile() throws IOException {
        if(actionsFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_ACTIONS_FILE_NAME);
        return actionsFile;
    }
    
    /**
     * Mark that actions were modified and therefore should be saved.
     */
    public static void setModified() { wereActionsModified = true; }
    
    /**
     * Writes the current action keymaps to the user's actions file.
     * @throws IOException 
     * @throws IOException
     */
    public static void saveActionKeymap() throws IOException {
    	if (wereActionsModified)
    		writer.write();
    	else
    		AppLogger.fine("Action keymap not modified, not saving");
    }
    
    protected static void createEmptyFile() throws IOException {
    	writer.create();
    }
    
    /**
     * Loads the action files: loads the one contained in the JAR file first, and then the user's one.
     * This means any new action in the JAR action keymap (when a new version is released) will have the default
     * keyboard mapping, but the keyboard mappings customized by the user in the user's action keymap will override
     * the ones from the JAR action keymap.
     *
     * <p>This method must be called before requesting and registering any action.
     */
    public static void loadActionKeymap() throws Exception {
    	// Load user's file if exist
    	AbstractFile actionKeymapFile = getActionsFile();
    	if (actionKeymapFile != null && actionKeymapFile.exists()) {
    		ActionKeymapReader reader = new ActionKeymapReader(actionKeymapFile);
    		ActionKeymap.registerActions(reader.getPrimaryActionsKeymap(), reader.getAlternateActionsKeymap());
    	}
    	else {
    		createEmptyFile();
    		AppLogger.fine(DEFAULT_ACTIONS_FILE_NAME + " was not found, created empty file");
    	}
    }
}

