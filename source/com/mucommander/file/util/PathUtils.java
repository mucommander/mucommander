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


package com.mucommander.file.util;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileURL;

import java.net.MalformedURLException;

/**
 * This class contains static helper methods that operate on file paths.
 *
 * @author Maxence Bernard
 */
public class PathUtils {

    /**
     * This class represents a destination entered by the user and resolved by {@link PathUtils#resolveDestination(String, com.mucommander.file.AbstractFile)}
     * into an <code>AbstractFile</code> and a destination type.
     *
     * @see PathUtils#resolveDestination(String, com.mucommander.file.AbstractFile)
     */
    public static class ResolvedDestination {

        /** The destination AbstractFile, may be a regular file or a folder */
        private AbstractFile file;

        /** The destination's folder, the file itself for {@link #EXISTING_FOLDER}, the destination file's parent for
         * other types */
        private AbstractFile folder;

        /** The destination type, see constant values */
        private int type;

        /** Designates a folder, either a directory or archive, that exists on the filesystem. */
        public final static int EXISTING_FOLDER = 0;
        /** Designates a regular file that exists on the filesystem. The file may be a browsable archive but that was
         * refered to as a regular file, i.e. without a trailing separator character in the path. */
        public final static int EXISTING_FILE = 1;
        /** Designates a new file that doesn't exist on the filesystem. The file's parent however does always exist. */
        public final static int NEW_FILE = 2;

        /**
         * Creates a new <code>ResolvedDestination</code> with the specified destination file and type.
         *
         * @param destinationFile the destination file
         * @param destinationType the destination type
         * @param destinationFolder the destination folder
         */
        private ResolvedDestination(AbstractFile destinationFile, int destinationType, AbstractFile destinationFolder) {
            this.file = destinationFile;
            this.type = destinationType;
            this.folder = destinationFolder;
        }

        /**
         * Returns the resolved destination file. The returned file may or may not physically exist on the filesystem.
         * If it exists, the returned file may be a folder (directory or browsable archive) or a regular file.
         *
         * @return the resolved destination file
         * @see #getDestinationType()
         */
        public AbstractFile getDestinationFile() {
            return file;
        }

        /**
         * Returns the resolved destination's folder. Depending on the {@link #getDestinationType() destination type},
         * the destination folder is:
         * <dl>
         *  <dt>for {@link #EXISTING_FOLDER}</dt><dd>the {@link #getDestinationFile() destination file} itself</dd>
         *  <dt>for {@link #EXISTING_FILE} or {@link #NEW_FILE}</dt><dd>the {@link #getDestinationFile() destination file}'s parent</dd>
         * </dl>
         *  The returned <code>AbstractFile</code> is always a folder that exists.
         *
         * @return the resolved destination file
         * @see #getDestinationType()
         */
        public AbstractFile getDestinationFolder() {
            return folder;
        }

        /**
         * Returns the type of destination that was resolved. The returned value will be one of the following constant
         * fields defined in this class:
         * <dl>
         *  <dt>{@link #EXISTING_FOLDER}</dt><dd>if the path denotes a folder, either a directory or a browsable
         * archive.</dd>
         *  <dt>{@link #EXISTING_FILE}</dt><dd>if the path denotes a regular file. The file may be a browsable archive,
         * see below.</dd>
         *  <dt>{@link #NEW_FILE}</dt><dd>if the path denotes a non-existing file whose parent exists.</dd>
         * </dl>
         * Paths to browsable archives are considered as denoting a folder only if they end with a trailing separator
         * character. If they don't, they're considered as denoting a regular file. For example,
         * <code>/existing_folder/existing_archive.zip/</code> refers to the archive as a folder where as
         * <code>/existing_folder/existing_archive.zip</code> refers to the archive as a regular file.
         *
         * @return the type of destination that was resolved
         */
        public int getDestinationType() {
            return type;
        }
    }

    /**
     * Resolves a destination path entered by the user and returns a {@link ResolvedDestination} object that
     * that contains a {@link AbstractFile} instance corresponding to the path and a type that describes the kind of
     * destination that was resolved. <code>null</code> is returned if the path is not a valid destination (see below)
     * or could not be resolved, for example becuase of I/O or authentication error.
     * <p>
     * The given path may be either absolute or relative to the specified base folder. If the base folder argument is
     * <code>null</code> and the path is relative, <code>null</code> will always be returned.
     * The path may contain '.', '..' and '~' tokens which will be left for the corresponding
     * {@link com.mucommander.file.SchemeParser} to canonize.
     * </p>
     * <p>
     * The path may refer to the following listed destination types. In all cases, the destination's parent folder must
     * exist, if it doesn't <code>null</code> will always be returned. For example, <code>/non_existing_folder/file</code>
     * is not a valid destination (provided that '/non_existing_folder' does not exist).
     * <dl>
     *  <dt>{@link ResolvedDestination#EXISTING_FOLDER}</dt><dd>if the path denotes a folder, either a directory or a
     * browsable archive.</dd>
     *  <dt>{@link ResolvedDestination#EXISTING_FILE}</dt><dd>if the path denotes a regular file. The file may be a browsable archive,
     * see below.</dd>
     *  <dt>{@link ResolvedDestination#NEW_FILE}</dt><dd>if the path denotes a non-existing file whose parent exists.</dd>
     * </dl>
     * Paths to browsable archives are considered as denoting a folder only if they end with a trailing separator
     * character. If they don't, they're considered as denoting a regular file. For example,
     * <code>/existing_folder/existing_archive.zip/</code> refers to the archive as a folder where as
     * <code>/existing_folder/existing_archive.zip</code> refers to the archive as a regular file.
     * </p>
     *
     * @param destPath the destination path to resolve
     * @param baseFolder the base folder used for relative paths, <code>null</code> to accept only absolute paths
     * @return the object that that contains a {@link AbstractFile} instance corresponding to the path and a type that
     * describes the kind of destination that was resolved
     */
    public static ResolvedDestination resolveDestination(String destPath, AbstractFile baseFolder) {
        AbstractFile destFile;
        FileURL destURL;

        // Try to resolve the path as a URL
        try {
            destURL = FileURL.getFileURL(destPath);
            // destPath is absolute
        }
        catch(MalformedURLException e) {
            // destPath is relative (or malformed)

            // Abort now if there is no base folder
            if(baseFolder==null)
                return null;

            String separator = baseFolder.getSeparator();

            // Start by cloning the base folder's URL, including credentials and properties
            FileURL baseFolderURL = baseFolder.getURL();
            destURL  = (FileURL)baseFolderURL.clone();
            String basePath = destURL.getPath();
            if(!destPath.equals(""))
                destURL.setPath(basePath + (basePath.endsWith(separator)?"":separator) + destPath);

            // At this point we have the proper URL, except that the path may contain '.', '..' or '~' tokens.
            // => parse the URL from scratch to have the SchemeParser canonize them.
            try {
                destURL = FileURL.getFileURL(destURL.toString(true));
                destURL.importProperties(baseFolderURL);
            }
            catch(MalformedURLException e2) {
                return null;
            }
        }

        // No point in going any further if the URL cannot be resolved into a file
        destFile = FileFactory.getFile(destURL);
        if(destFile ==null) {
            if(Debug.ON) Debug.trace("could not resolve a file for "+destURL);
            return null;
        }

        // Test if the destination file exists
        boolean destFileExists = destFile.exists();
        if(destFileExists) {
            // Note: path to archives must end with a trailing separator character to refer to the archive as a folder,
            //  if they don't, they'll refer to the archive as a file.
            if(destFile.isDirectory() || (destPath.endsWith(destFile.getSeparator()) && destFile.isBrowsable()))
                return new ResolvedDestination(destFile, ResolvedDestination.EXISTING_FOLDER, destFile);
        }

        // Test if the destination's parent exists, if not the path is not a valid destination
        AbstractFile destParent = destFile.getParentSilently();
        if(destParent==null || !destParent.exists())
            return null;

        return new ResolvedDestination(destFile, destFileExists?ResolvedDestination.EXISTING_FILE:ResolvedDestination.NEW_FILE, destParent);
    }


    /**
     * Removes any leading separator character (slash or backslash) from the given path and returns the modified path.
     *
     * @param path the path to modify
     * @return the modified path, free of any leading separator
     */
    public static String removeLeadingSeparator(String path) {
        char firstChar;
        if(path.length()>0 && ((firstChar=path.charAt(0))=='/' || firstChar=='\\'))
            return path.substring(1, path.length());

        return path;
    }

    /**
     * Removes any leading separator character from the given path and returns the modified path.
     *
     * @param path the path to modify
     * @param separator the path separator, usually "/" or "\\"
     * @return the modified path, free of any leading separator
     */
    public static String removeLeadingSeparator(String path, String separator) {
        if(path.startsWith(separator))
            return path.substring(separator.length(), path.length());

        return path;
    }

    /**
     * Removes any trailing separator character (slash or backslash) from the given path and returns the modified path.
     *
     * @param path the path to modify
     * @return the modified path, free of any trailing separator
     */
    public static String removeTrailingSeparator(String path) {
        char lastChar;
        int len = path.length();
        if(len>0 && ((lastChar=path.charAt(len-1))=='/' || lastChar=='\\'))
            return path.substring(0, len-1);

        return path;
    }

    /**
     * Removes any trailing separator character (slash or backslash) from the given path and returns the modified path.
     *
     * @param path the path to modify
     * @param separator the path separator, usually "/" or "\\"
     * @return the modified path, free of any trailing separator
     */
    public static String removeTrailingSeparator(String path, String separator) {
        if(path.endsWith(separator))
            return path.substring(0, path.length()-separator.length());

        return path;
    }
}
