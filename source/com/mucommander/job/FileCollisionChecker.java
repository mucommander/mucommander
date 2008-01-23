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

package com.mucommander.job;

import com.mucommander.file.AbstractFile;

/**
 * The purpose of this class is to check for collisions between a source and destination file used in a file transfer.
 *
 * <p>Currently, 3 collision types are detected:
 * <ul>
 * <li>{@link #DESTINATION_FILE_ALREADY_EXISTS}: the destination file already exists
 * <li>{@link #SAME_SOURCE_AND_DESTINATION}: source and destination files are the same, according to {@link AbstractFile#equals(Object)}
 * <li>{@link #SOURCE_PARENT_OF_DESTINATION}: source is a folder (as returned by {@link com.mucommander.file.AbstractFile#isBrowsable()}
 * and a parent of destination.
 * </ul>
 *
 * <p>The value returned by {@link #checkForCollision(com.mucommander.file.AbstractFile, com.mucommander.file.AbstractFile)}
 * can be used to create a {@link com.mucommander.ui.dialog.file.FileCollisionDialog} in order to inform the user of the collision
 * and ask him how to resolve it.
 *
 * @see com.mucommander.ui.dialog.file.FileCollisionDialog
 * @author Maxence Bernard
 */
public class FileCollisionChecker {

    /** No collision detected */
    public static final int NO_COLLOSION = 0;

    /** The destination file already exists and is not a directory */
    public static final int DESTINATION_FILE_ALREADY_EXISTS = 1;

    /** Source and destination files are the same */
    public static final int SAME_SOURCE_AND_DESTINATION = 2;

    /** Source and destination are both folders and destination is a subfolder of source */
    public static final int SOURCE_PARENT_OF_DESTINATION = 3;

    /**
     *
     * @param sourceFile source file, can be null in which case the only collision checked against is {@link #DESTINATION_FILE_ALREADY_EXISTS}.
     * @param destFile destination file, cannot be null
     * @return an int describing the collision type, or {@link #NO_COLLOSION} if no collision was detected (see constants)
     */
    public static int checkForCollision(AbstractFile sourceFile, AbstractFile destFile) {

        if(sourceFile!=null) {
            // Source and destination are equal
            if(destFile.equals(sourceFile))
                return SAME_SOURCE_AND_DESTINATION;

            // Both source and destination are folders and destination is a subfolder of source
            if(sourceFile.isParentOf(destFile))
                return SOURCE_PARENT_OF_DESTINATION;
        }

        // File exists in destination
        if(destFile.exists() && !destFile.isDirectory())
            return DESTINATION_FILE_ALREADY_EXISTS;

        return NO_COLLOSION;
    }
}
