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

package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessRunner;


/**
 * This class is a gateway to the <code>chmod</code> UNIX command. It provides static methods that allow to change a
 * file's permissions, overcoming the limitations of <code>java.io.File</code>.
 *
 * <p>The <code>chmod</code> command is available only under {@link com.mucommander.runtime.OsFamily#isUnixBased() UNIX-based}
 * systems -- a call to any of this class' methods under other OS will likely fail.</p>
 *
 * @author Maxence Bernard
 * @see com.mucommander.file.FilePermissions
 */
public class Chmod {

    /**
     * Attemps to change the permissions of the given file and returns <code>true</code> if the <code>chmod</code>
     * command reported a success.
     *
     * @param file the file whose permissions are to be changed
     * @param permissions the new permissions
     * @see com.mucommander.file.FilePermissions
     * @return true if the <code>chmod</code> command reported a success
     */
    public static boolean chmod(AbstractFile file, int permissions) {
        return chmod(new AbstractFile[]{file}, Integer.toOctalString(permissions));
    }

    /**
     * Attemps to change the permissions of the given file and returns <code>true</code> if the <code>chmod</code>
     * command reported a success.
     *
     * @param file the file whose permissions are to be changed
     * @param permissions the new permissions, in any form accepted by the chmod command
     * @return true if the <code>chmod</code> command reported a success
     */
    public static boolean chmod(AbstractFile file, String permissions) {
        return chmod(new AbstractFile[]{file}, permissions);
    }

    /**
     * Attemps to change the permissions of the given files and returns <code>true</code> if the <code>chmod</code>
     * command reported a success.
     *
     * @param files the files whose permissions are to be changed
     * @param permissions the new permissions
     * @see com.mucommander.file.FilePermissions
     * @return true if the <code>chmod</code> command reported a success
     */
    public static boolean chmod(AbstractFile files[], int permissions) {
        return chmod(files, Integer.toOctalString(permissions));
    }

    /**
     * Attemps to change the permissions of the given files and returns <code>true</code> if the <code>chmod</code>
     * command reported a success.
     *
     * @param files the files whose permissions are to be changed
     * @param permissions the new permissions, in any form accepted by the chmod command
     * @return true if the <code>chmod</code> command reported a success
     */
    public static boolean chmod(AbstractFile files[], String permissions) {
        AbstractProcess process;

        // Create the command token array
        String[] tokens = new String[files.length+2];
        tokens[0] = "chmod";
        tokens[1] = permissions;
        int fileIndex = 0;
        for(int i=2; i<tokens.length; i++)
            tokens[i] = files[fileIndex++].getAbsolutePath();

        try {
            process = ProcessRunner.execute(tokens);
            process.waitFor();
            return process.exitValue()==0;
        }
        catch(Exception e) {        // IOException, InterruptedException
            return false;
        }
    }
}
