
package com.mucommander.file;

import java.io.*;

/**
 * This class contains methods which perform common file operations such as counting them or getting
 * the total byte size.
 */
public class FileOperations {

    /**
     * Recursively computes the total size for the given files and folders.
     */
    private static int getFileSize(AbstractFile files[]) {
        AbstractFile file;
        int total = 0;
        for(int i=0; i<files.length; i++) {
            file = files[i];
//            if(file.isFolder() && !(file instanceof ArchiveFile)) {
			if(file.isDirectory() && !file.isSymlink()) {
                try {
                    total += getFileCount(file.ls());
                }
                catch(IOException e) {
                }
            }
            else
                total += file.getSize();
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
//            if(file.isFolder() && !(file instanceof ArchiveFile)) {
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
