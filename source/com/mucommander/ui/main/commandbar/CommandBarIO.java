/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.ui.main.commandbar;

import com.mucommander.AppLogger;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.ResourceLoader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class contains the common things for reading and writing the command-bar actions and modifier.
 * 
 * @author Arik Hadas
 */
public abstract class CommandBarIO extends DefaultHandler {
	
	/* Variables used for XML parsing */
	/** Root element */
	protected static final String ROOT_ELEMENT = "command_bar";
	/** Attribute containing the last muCommander version that was used to create the file */
	protected static final String VERSION_ATTRIBUTE  = "version";
	protected static final String MODIFIER_ATTRIBUTE = "modifier";
    /** Element describing one of the button in the list */
	protected static final String BUTTON_ELEMENT = "button";
    /** Attribute containing the action associated with the button */
	protected static final String ACTION_ATTRIBUTE = "action";
	/** Attribute containing the alternative action associated with the button */
	protected static final String ALT_ACTION_ATTRIBUTE = "alt_action";
 
	/** Default command bar descriptor filename */
	protected final static String DEFAULT_COMMAND_BAR_FILE_NAME = "command_bar.xml";

    /** Path to the command bar descriptor resource file within the application JAR file */
	protected final static String COMMAND_BAR_RESOURCE_PATH = "/" + DEFAULT_COMMAND_BAR_FILE_NAME;

    /** Command bar descriptor file used when calling {@link #loadCommandBar()} */
	protected static AbstractFile commandBarFile;
	
	/** CommandBarWriter instance */
	private static CommandBarWriter commandBarWriter;
	
	/**
     * Parses the XML file describing the command bar's buttons and associated actions.
     * If the file doesn't exist yet, it is copied from the default resource file within the JAR.
     *
     * This method must be called before instantiating CommandBar for the first time.
     */
    public static void loadCommandBar() throws Exception {
    	// Load command-bar resource file:
    	CommandBarReader resourceFileReader = new CommandBarReader(ResourceLoader.getResourceAsFile(COMMAND_BAR_RESOURCE_PATH));
    	CommandBarAttributes.setDefaultAttributes(resourceFileReader.getActionsRead(), resourceFileReader.getAlternateActionsRead(), resourceFileReader.getModifierRead());
    	
    	// Load user's file if exist
    	AbstractFile commandBarFile = getDescriptionFile();
    	if(commandBarFile != null && commandBarFile.exists()) {
    		CommandBarReader reader = new CommandBarReader(commandBarFile);
    		CommandBarAttributes.setAttributes(reader.getActionsRead(), reader.getAlternateActionsRead(), reader.getModifierRead());
    	}
    	else
    		AppLogger.fine(DEFAULT_COMMAND_BAR_FILE_NAME + " was not found");
    	
    	// initialize the writer after setting the command-bar initial attributes:
    	commandBarWriter = CommandBarWriter.create();
    }
    
    /**
     * Writes the current command bar to the user's command bar file.
     * @throws IOException 
     * @throws IOException
     */
    public static void saveCommandBar() throws IOException {
    	if (commandBarWriter != null)
    		commandBarWriter.write();
    	else
    		AppLogger.warning("Could not save command bar. writer is null");
    }
	
	/**
     * Sets the path to the command bar description file to be loaded when calling {@link #loadCommandBar()}.
     * By default, this file is {@link #DEFAULT_COMMAND_BAR_FILE_NAME} within the preferences folder.
     * @param  path                  path to the command bar descriptor file
     * @throws FileNotFoundException if the specified file is not accessible.
     */
    public static void setDescriptionFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setDescriptionFile(new File(path));
        else
            setDescriptionFile(file);
    }

    /**
     * Sets the path to the command bar description file to be loaded when calling {@link #loadCommandBar()}.
     * By default, this file is {@link #DEFAULT_COMMAND_BAR_FILE_NAME} within the preferences folder.
     * @param  file                  path to the command bar descriptor file
     * @throws FileNotFoundException if the specified file is not accessible.
     */
    public static void setDescriptionFile(File file) throws FileNotFoundException {setDescriptionFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the command bar description file to be loaded when calling {@link #loadCommandBar()}.
     * By default, this file is {@link #DEFAULT_COMMAND_BAR_FILE_NAME} within the preferences folder.
     * @param  file                  path to the command bar descriptor file
     * @throws FileNotFoundException if the specified file is not accessible.
     */
    public static void setDescriptionFile(AbstractFile file) throws FileNotFoundException {
        // Makes sure file can be used as a commandbar description file.
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        commandBarFile = file;
    }

    public static AbstractFile getDescriptionFile() throws IOException {
        if(commandBarFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_COMMAND_BAR_FILE_NAME);
        return commandBarFile;
    }
}

