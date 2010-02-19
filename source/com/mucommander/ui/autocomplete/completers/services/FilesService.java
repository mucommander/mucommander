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

package com.mucommander.ui.autocomplete.completers.services;

import com.mucommander.AppLogger;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 * This <code>CompletionService</code> handles file paths completion.
 * 
 * @author Arik Hadas
 */

public abstract class FilesService implements CompletionService {
	private String cachedDirectoryName;
	private String[] cachedDirectoryFileNames;
	private long cachedDirectoryDate;
	
	public FilesService() {
		cachedDirectoryFileNames = new String[0];
		cachedDirectoryDate = -1;
	}

	/**
	 * This abstract function gets a directory and should return it's children
	 * files that match a certain criteria.
	 * 
	 * @param directory - a directory.
	 * @return subgroup of the given directory's children files.
	 * @throws IOException
	 */
	protected abstract AbstractFile[] getFiles(AbstractFile directory) throws IOException;
	
	public Vector<String> getPossibleCompletions(String path) {
		Vector<String> result = new Vector<String>();
		int index = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));	
		if (index != -1) {
	        String currentDirectoryName = path.substring(0, index+1);
	        
	        AbstractFile currentDirectory = FileFactory.getFile(currentDirectoryName);
	        if (currentDirectory != null && currentDirectory.exists()) {	        
		        long currentDirectoryDate = currentDirectory.getDate();
		        if (cachedDirectoryName == null || !cachedDirectoryName.equals(currentDirectoryName) || currentDirectoryDate != cachedDirectoryDate) {
		        	AbstractFile[] currentDirectoryFiles;
					try {
						currentDirectoryFiles = getFiles(currentDirectory);
					} catch (IOException e) {
                        AppLogger.fine("Caught exception", e);
						return new Vector<String>();
					}
		
		        	int nbCurrentDirectoryFiles = currentDirectoryFiles.length;
		        	cachedDirectoryFileNames = new String[nbCurrentDirectoryFiles];
		        	
		        	for (int i=0; i<nbCurrentDirectoryFiles; i++) {
		        		AbstractFile abstractFileI = currentDirectoryFiles[i];
						cachedDirectoryFileNames[i] = abstractFileI.getName() + (abstractFileI.isDirectory() ? abstractFileI.getSeparator() : "");
		        	}
		        	
		        	Arrays.sort(cachedDirectoryFileNames, String.CASE_INSENSITIVE_ORDER);
		        	
		        	cachedDirectoryName = currentDirectory.getAbsolutePath() + (currentDirectory.isDirectory() ? "" : currentDirectory.getSeparator());
		        	cachedDirectoryDate = currentDirectoryDate;
		        }
				
		        final String prefix = index==path.length()-1 ? null : path.substring(index + 1).toLowerCase();
		        result = PrefixFilter.createPrefixFilter(prefix).filter(cachedDirectoryFileNames);
	        }
		}
		return result;
	}
	
	public String complete(String selectedCompletion) {
		String result = null;
       	int nbCachedFileNames = cachedDirectoryFileNames.length;
        for (int i=0; i < nbCachedFileNames; i++)
        	if (cachedDirectoryFileNames[i].equalsIgnoreCase(selectedCompletion)) {
        		result = cachedDirectoryName + cachedDirectoryFileNames[i];
        		break;
        	}
        return result;
	}
}
