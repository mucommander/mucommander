package com.mucommander.ui.autocomplete.completers;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.autocomplete.AutocompleterTextComponent;
import com.mucommander.ui.autocomplete.Completer;
import com.mucommander.ui.autocomplete.PrefixFilter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 * FileCompleter is a Completer based on file paths. 
 * 
 * @author Arik Hadas, based on the code of Santhosh Kumar: http://www.jroller.com/santhosh/entry/file_path_autocompletion
 */
 
public class FileCompleter implements Completer { 
    private String[] cachedDirectoryFiles;
    private String cachedDirectoryName;
    private String[] roots;
    
	public FileCompleter(){  
        roots = LocationCompleter.getSortedRootFolderNames();
    }

    private boolean isRoot(String text) {
		int nbRoots = roots.length;
		for (int i = 0; i < nbRoots; i++)
			if (roots[i].equalsIgnoreCase(text))
				return true;
		return false;
	}
 
    public synchronized boolean updateListData(final JList list, AutocompleterTextComponent comp){
    	final String value = comp.getText();
    	
    	Vector filteredFiles = new Vector();    			
        
        int index = Math.max(value.lastIndexOf('\\'), value.lastIndexOf('/')); 
        if(index == -1) {
        	// add roots:
        	roots = LocationCompleter.getSortedRootFolderNames();
        	
        	PrefixFilter rootsFilter = PrefixFilter.createPrefixFilter(value);
    		
        	filteredFiles.addAll(rootsFilter.filter(roots));
    		
    		if (filteredFiles.size() == 1)
				filteredFiles.remove(value);
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

        list.setListData(filteredFiles);            

        return true; 
    } 
 
    public void updateTextComponent(final String selected, AutocompleterTextComponent comp){ 
        if(selected==null) 
            return;
                                 
        if (isRoot(selected)){
        	if (comp.isEnabled())
        		comp.setText(selected);
        }
        else { // It is a file/directory
        	String value = comp.getText();
	        int index = Math.max(value.lastIndexOf('\\'), value.lastIndexOf('/')); 
	        if(index==-1) 
	            return; 
	        
	        int prefixlen = comp.getDocument().getLength()-index-1; 
	        try{
	        	if (comp.isEnabled())
	        		comp.getDocument().insertString(comp.getCaretPosition(), selected.substring(prefixlen), null);        	
	        } catch(BadLocationException e){ } 
        }
    }
}