
package com.mucommander.file;

import java.io.*;

/**
 * This class contains methods which perform common file operations such as counting them or calculating
 * their total size.
 *
 * @author Maxence Bernard
 */
public class FileOperations {

    /**
     * Recursively computes the total size for the given files and folders.
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
    * Recursively computes the total number of files.
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
