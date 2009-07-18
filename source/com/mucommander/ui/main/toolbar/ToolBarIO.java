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

package com.mucommander.ui.main.toolbar;

import com.mucommander.AppLogger;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 * @author Arik Hadas
 */
public abstract class ToolBarIO extends DefaultHandler {

	/* Variables used for XML parsing */
	/** Root element */
	protected static final String ROOT_ELEMENT = "toolbar";
	/** Attribute containing the last muCommander version that was used to create the file */
	protected static final String VERSION_ATTRIBUTE  = "version";
    /** Element describing one of the button in the list */
	protected static final String BUTTON_ELEMENT = "button";
    /** Attribute containing the action associated with the button */
	protected static final String ACTION_ATTRIBUTE  = "action";
    /** Element describing one of the separator in the list */
	protected static final String SEPARATOR_ELEMENT = "separator";

	/** Default toolbar descriptor filename */
    protected final static String DEFAULT_TOOLBAR_FILE_NAME = "toolbar.xml";

    /** Toolbar descriptor file used when calling {@link #loadDescriptionFile()} */
    private static AbstractFile descriptionFile;
    
    /**
     * Parses the XML file describing the toolbar's buttons and associated actions.
     * If the file doesn't exist, default toolbar elements will be used.
     */
    public static void loadDescriptionFile() throws Exception {
    	AbstractFile descriptionFile = getDescriptionFile();
        if(descriptionFile.exists())
        	new ToolBarReader(descriptionFile);
        else
        	AppLogger.fine("User toolbar.xml was not found, using default toolbar");
    }
    
    /**
     * Sets the path to the toolbar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_TOOLBAR_FILE_NAME} within the preferences folder.
     * @param file path to the toolbar descriptor file
     */
    public static void setDescriptionFile(AbstractFile file) throws FileNotFoundException {
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file);
        descriptionFile = file;
    }

    public static AbstractFile getDescriptionFile() throws IOException {
        if(descriptionFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_TOOLBAR_FILE_NAME);
        return descriptionFile;
    }
    
    /**
     * Sets the path to the toolbar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_TOOLBAR_FILE_NAME} within the preferences folder.
     * @param path path to the toolbar descriptor file
     */
    public static void setDescriptionFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setDescriptionFile(new File(path));
        else
            setDescriptionFile(file);
    }

    /**
     * Sets the path to the toolbar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_TOOLBAR_FILE_NAME} within the preferences folder.
     * @param file path to the toolbar descriptor file
     */
    public static void setDescriptionFile(File file) throws FileNotFoundException {setDescriptionFile(FileFactory.getFile(file.getAbsolutePath()));}
}
