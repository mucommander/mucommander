
package com.mucommander.file;

import java.io.IOException;

/**
 * This class contains static methods which perform common file operations.
 *
 * @author Maxence Bernard
 */
public class FileToolkit {

    /**
     * Matches a path typed by the user (which can be relative to the current folder or absolute)
     * to an AbstractFile (folder). The folder returned will always exist.
     * If the given path doesn't correspond to any existing folder, a null value will be returned.
     */
    public static Object[] resolvePath(String destPath, AbstractFile currentFolder) {
        // Current path, including trailing separator
        String currentPath = currentFolder.getAbsolutePath(true);
        AbstractFile destFolder;

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("destPath="+destPath+" currentPath="+currentPath);

        // If destination starts with './' or '.', replace '.' by current folder's path
        if(destPath.startsWith(".\\") || destPath.startsWith("./"))
            destPath = currentPath + destPath.substring(2, destPath.length());
        else if(destPath.equals("."))
            destPath = currentPath + destPath.substring(1, destPath.length());

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("destPath ="+destPath);

        String newName = null;

        // Level 0, folder exists, newName is null
		
        // destPath points to an absolute and existing folder
        if ((destFolder=AbstractFile.getAbstractFile(destPath))!=null 
            && destFolder.exists()
            && destFolder.isDirectory()) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found existing folder for "+destPath+" destFolder="+destFolder.getAbsolutePath()+" destURL="+destFolder.getURL()+" URL filename="+destFolder.getURL().getFilename());
        }

        // destPath points to an existing folder relative to current folder
        else if ((destFolder=AbstractFile.getAbstractFile(currentPath+destPath))!=null
		 && destFolder.exists()
		 && destFolder.isDirectory()) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found existing folder "+currentPath+destPath);
        }

        // Level 1, path includes a new destination filename
        else {
            // Removes ending separator character (if any)
            char c = destPath.charAt(destPath.length()-1);
            // Separator characters can be mixed
            if(c=='/' || c=='\\')
                destPath = destPath.substring(0,destPath.length()-1);
			
            // Extracts the new destination filename
            int pos = Math.max(destPath.lastIndexOf('/'), destPath.lastIndexOf('\\'));
            if (pos!=-1) {
                newName = destPath.substring(pos+1, destPath.length());
                destPath = destPath.substring(0,pos+1);
            }
            else  {
                newName = destPath;
                destPath = "";
            }			

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("level1, destPath="+destPath+" newname="+newName);
            // destPath points to an absolute and existing folder
            if (!destPath.equals("") && (destFolder=AbstractFile.getAbstractFile(destPath))!=null && destFolder.exists()) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found existing folder "+destPath+" newname="+newName);
            }
			
            // destPath points to an existing folder relative to current folder
            else if ((destFolder=AbstractFile.getAbstractFile(currentPath+destPath))!=null && destFolder.exists()) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found existing folder "+currentPath+destPath+" newname="+newName);
            }

            else {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("no match, returning null");
                return null;
            }
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("destFolder="+(destFolder==null?null:destFolder.getAbsolutePath())+" newName="+newName);
        return new Object[] {destFolder, newName};
    }


    /**
     * Recursively calculates the total size for the given files and folders.
     */
    private static long getFileSize(AbstractFile files[]) {
        AbstractFile file;
        long total = 0;
        long fileSize;
        for(int i=0; i<files.length; i++) {
            file = files[i];
            if(file.isDirectory() && !file.isSymlink()) {
                try {
                    total += getFileCount(file.ls());
                }
                catch(IOException e) {
                }
            }
            else {
                fileSize = file.getSize();
                if(fileSize>0)
                    total += fileSize;
            }
        }
        return total;
    }

    /**
     * Recursively calculates the total number of files.
     */
    private static int getFileCount(AbstractFile files[]) {
        AbstractFile file;
        int total = 0;
        for(int i=0; i<files.length; i++) {
            file = files[i];
            if(file.isDirectory() && !file.isSymlink()) {
                try {
                    total += getFileCount(file.ls());
                }
                catch(IOException e) {
                }
            }
            else
                total++;
        }
        return total;        
    }
}
