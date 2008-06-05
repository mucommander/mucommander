/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.autocomplete.completers;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.RootFolders;
import com.mucommander.ui.autocomplete.AutocompleterTextComponent;
import com.mucommander.ui.autocomplete.Completer;
import com.mucommander.ui.autocomplete.PrefixFilter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 * LocationCompleter is a Completer based on locations, meaning file paths and bookmarks. 
 * 
 * @author Arik Hadas, based on the code of Santhosh Kumar: http://www.jroller.com/santhosh/entry/file_path_autocompletion
 */

public class LocationCompleter implements Completer, BookmarkListener {
    private String[] cachedDirectoryFiles;
    private String cachedDirectoryName;
    private String[] bookmarksNames;
    private String[] rootsNames;
    
	public LocationCompleter(){ 
        rootsNames = getSortedRootFolderNames();
        bookmarksNames = getSortedBookmarksNames();
        
        // Register as a bookmark-listener, in order to be up-to-date with the existing bookmarks.
        BookmarkManager.addBookmarkListener(this);
    }

    /**
     * Returns a sorted array of bookmarks names.
     *
     * @return a sorted array of bookmarks names
     */
    public static String[] getSortedBookmarksNames() {
    	Vector bookmarks = BookmarkManager.getBookmarks();
        int nbBookmarks = bookmarks.size();
    	String[] result = new String[nbBookmarks];
    	for (int i=0; i<nbBookmarks; i++)
    		result[i] = ((Bookmark) bookmarks.elementAt(i)).getName();
    	Arrays.sort(result, String.CASE_INSENSITIVE_ORDER);
    	return result;
    }

    /**
     * Resolves and returns a sorted array of root (top level) folder names. Those folders are purposively not cached
     * so that newly mounted folders will be returned.
     *
     * @return a sorted array of root folder names
     */
    public static String[] getSortedRootFolderNames() {
    	AbstractFile fileRoots[] = RootFolders.getRootFolders();
    	int nbFolders = fileRoots.length;
    	String[] result = new String[nbFolders];
    	for (int i=0; i<nbFolders; i++)
    		result[i] = fileRoots[i].getAbsolutePath();
    	Arrays.sort(result, String.CASE_INSENSITIVE_ORDER);
    	return result;
    }

    private boolean isRoot(String text) {
		int nbRoots = rootsNames.length;
		for (int i = 0; i < nbRoots; i++)
			if (rootsNames[i].equalsIgnoreCase(text))
				return true;
		return false;
	}

    public synchronized boolean updateListData(final JList list, AutocompleterTextComponent comp) {
    	final String value = comp.getText();
    	
    	Vector filteredFiles = new Vector();    			
        
        int index = Math.max(value.lastIndexOf('\\'), value.lastIndexOf('/')); 
        if(index == -1) {
        	// add roots:
        	rootsNames = getSortedRootFolderNames();
        	
        	PrefixFilter rootsFilter = PrefixFilter.createPrefixFilter(value);
    		
        	filteredFiles.addAll(rootsFilter.filter(rootsNames));
        }
        else {
        	// add files in current directory:
    		final String prefix = index==value.length()-1 ? null : value.substring(index + 1).toLowerCase();
            String currentDirectoryName = value.substring(0, index+1);
            
            if (cachedDirectoryName == null || !cachedDirectoryName.equals(currentDirectoryName)) {
				AbstractFile currentDirectory = FileFactory.getFile(currentDirectoryName);				
				
				if (currentDirectory == null || !currentDirectory.exists())
					return false;
				
				AbstractFile[] currentDirectoryFiles;
				try {
					currentDirectoryFiles = currentDirectory.ls();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}				
    			    			
				cachedDirectoryName = currentDirectoryName;
				
				int nbCurrentDirectoryFiles = currentDirectoryFiles.length;
				cachedDirectoryFiles = new String[nbCurrentDirectoryFiles];			
				for (int i = 0; i < nbCurrentDirectoryFiles; i++) {
					AbstractFile abstractFileI = currentDirectoryFiles[i];
					cachedDirectoryFiles[i] = abstractFileI.getName() + (abstractFileI.isDirectory() ? abstractFileI.getSeparator() : "");
		        }
				
				Arrays.sort(cachedDirectoryFiles, String.CASE_INSENSITIVE_ORDER);
            }
            
            PrefixFilter filter = PrefixFilter.createPrefixFilter(prefix);
    			
            filteredFiles.addAll(filter.filter(cachedDirectoryFiles));
			
			if (filteredFiles.size() == 1)
				filteredFiles.remove(prefix);
        }
        
        // add bookmarks:
        PrefixFilter bookmarksFilter = PrefixFilter.createPrefixFilter(value);
    	
        filteredFiles.addAll(bookmarksFilter.filter(bookmarksNames));
        
        if (filteredFiles.size() == 1)
			filteredFiles.remove(value);

        list.setListData(filteredFiles);            

        return true;
    }
 
    public void updateTextComponent(final String selected, AutocompleterTextComponent comp){ 
        if(selected==null) 
            return;
        
        if (BookmarkManager.getBookmark(selected) != null) {
        	if (comp.isEnabled())
        		comp.setText(selected);
        }
        else if (isRoot(selected)){
        	if (comp.isEnabled())
        		comp.setText(selected);
        }
        else { // It is a file/directory
        	final String value = comp.getText();
	        int index = Math.max(value.lastIndexOf('\\'), value.lastIndexOf('/')); 
	        if(index==-1) 
	            return; 
	        
	        int prefixlen = comp.getDocument().getLength()-index-1; 
	        try{
	        	if (comp.isEnabled())
	        		comp.getDocument().insertString(comp.getCaretPosition(), selected.substring(prefixlen), null);        	
	        } catch(BadLocationException e) { } 
        }
    }

    // Bookmarks changes
	public void bookmarksChanged() {
		bookmarksNames = getSortedBookmarksNames();
	}
}
