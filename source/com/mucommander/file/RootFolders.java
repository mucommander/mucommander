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


package com.mucommander.file;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * This class' sole purpose is to resolve root folders, those returned by
 * java.io.File, and platform-specific ones.
 *
 * @author Maxence Bernard
 */
public class RootFolders {

    /**
     * Resolves and returns an array of root (top level) folders. Those folders
     * are purposively not cached so that newly mounted folders will be returned.
     */
    public static AbstractFile[] getRootFolders() {
        Vector rootFoldersV = new Vector();

        // Add Mac OS X's /Volumes subfolders and not file roots ('/') since Volumes already contains a named link 
        // (like 'Hard drive' or whatever silly name the user gave his primary hard disk) to /
        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X) {
            addMacOSXVolumes(rootFoldersV);
            if(Debug.ON) Debug.trace("/Volumes's subfolders added: "+rootFoldersV);
        }
        else {
            // Add java.io.File's root folders
            addFileRoots(rootFoldersV);
            if(Debug.ON) Debug.trace("java.io.File's root folders: "+rootFoldersV);
	
            // Add /etc/fstab folders
            // If we're running Windows, we can just skip that
            if(!PlatformManager.isWindowsFamily()) {
                addFstabEntries(rootFoldersV);
                if(Debug.ON) Debug.trace("/etc/fstab mount points added: "+rootFoldersV);
            }
        }

        // Add home folder
        AbstractFile homeFolder = getUserHomeFolder();
        if(homeFolder!=null)
            rootFoldersV.add(homeFolder);
			
        AbstractFile rootFolders[] = new AbstractFile[rootFoldersV.size()];
        rootFoldersV.toArray(rootFolders);
	
        return rootFolders;
    }


    /**
     * Returns the user home folder. Most if not all OSes have one, but in the unlikely event the OS doesn't have one
     * or the folder can't be resolved, <code>null</code> will be returned.
     */
    public static AbstractFile getUserHomeFolder() {
        return FileFactory.getFile(System.getProperty("user.home"));
    }

	
    /**
     * Retrieves java.io.File's reported root folders and adds them to 
     * the given Vector.
     */
    private static void addFileRoots(Vector v) {
        // Warning : No file operation should be performed on the resolved folders,
        // otherwise this will cause under Win32 a dialog to appear for removable drives such as A:\
        // if no disk is present.
        File fileRoots[] = File.listRoots();	

        int nbFolders = fileRoots.length;
        for(int i=0; i<nbFolders; i++)
            try { v.add(FileFactory.getFile(fileRoots[i].getAbsolutePath(), true)); }
            catch(IOException e) {}
    }
	

    /**
     * Parses /etc/fstab file and adds resolved folders to the given vector.
     */
    private static void addFstabEntries(Vector v) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/etc/fstab")));
            StringTokenizer st;
            String line;
            AbstractFile file;
            String folderPath;
            while((line=br.readLine())!=null) {
                // Skip comments
                if(!line.startsWith("#")) {
                    st = new StringTokenizer(line);
                    // path is second token
                    st.nextToken();
                    folderPath = st.nextToken();
                    if(!(folderPath.equals("/proc") || folderPath.equals("none"))) {
                        file = FileFactory.getFile(folderPath);
                        if(file!=null && !v.contains(file))
                            v.add(file);
                    }
                }
            }
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Error reading /etc/fstab entries: "+ e);
        }
		
    }
	
	
    /**
     * Adds /Volumes subfolders to the given Vector.
     */
    private static void addMacOSXVolumes(Vector v) {
        // /Volumes not resolved for some reason, giving up
        AbstractFile volumesFolder = FileFactory.getFile("/Volumes");
        if(volumesFolder==null)
            return;
		
        // Adds subfolders
        try {
            AbstractFile volumesFiles[] = volumesFolder.ls();
            int nbFiles = volumesFiles.length;
            AbstractFile folder;
            for(int i=0; i<nbFiles; i++)
                if((folder=volumesFiles[i]).isDirectory()) {
                    // Primary hard drive (the one corresponding to '/') is listed under Volumes and should be the first root folder
                    if(folder.getCanonicalPath().equals("/"))
                        v.insertElementAt(folder, 0);
                    else
                        v.add(folder);
                }
        }
        catch(IOException e) {
            if(Debug.ON) com.mucommander.Debug.trace("Can't get /Volumes subfolders: "+ e);
        }
    }
}
