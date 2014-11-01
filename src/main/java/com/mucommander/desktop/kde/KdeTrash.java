/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.desktop.kde;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.desktop.QueuedTrash;
import com.mucommander.process.ProcessRunner;

/**
 * This class provides access to the KDE trash. Only local files (or locally mounted files) can be moved to the trash.
 *
 * <p>
 * <b>Implementation notes:</b><br>
 * <br>
 * This trash is implemented as a {@link com.mucommander.desktop.QueuedTrash} as it spawns a process to move a file to
 * the trash and it is thus more effective to group files to be moved instead of spawning multiple processes.<br>
 * </p>
 *
 * @see Kde3TrashProvider
 * @author Maxence Bernard
 */
class KdeTrash extends QueuedTrash {
	private static final Logger LOGGER = LoggerFactory.getLogger(KdeTrash.class);
	
    /** Command that empties the trash */
    private final static String EMPTY_TRASH_COMMAND = "ktrash --empty";

    /** Command that allows to interact with the trash */
    private String baseCommand;

    /**
     * Creates a new <code>KDETrash</code> instance using the specified command for interacting with the trash.
     *
     * @param baseCommand command that allows to interact with the trash.
     */
    KdeTrash(String baseCommand) {
        this.baseCommand = baseCommand;
    }

    /**
     * Executes the given command and waits for the process termination.
     * Returns <code>true</code> if the command was executed without any error.
     *
     * @param command the command to execute
     * @return true if the command was executed without any error
     */
    private static boolean executeAndWait(String command) {
        try {
            ProcessRunner.execute(command).waitFor();
            return true;
        }
        catch(Exception e) {    // IOException, InterruptedException
            LOGGER.debug("Caught exception", e);
            return false;
        }
    }

    /**
     * Executes the given command and waits for the process termination.
     * Returns <code>true</code> if the command was executed without any error.
     *
     * @param command the command tokens
     * @return true if the command was executed without any error
     */
    private static boolean executeAndWait(String command[]) {
        try {
            ProcessRunner.execute(command).waitFor();
            return true;
        }
        catch(Exception e) {    // IOException, InterruptedException
            LOGGER.debug("Caught exception", e);
            return false;
        }
    }

    //////////////////////////////////
    // AbstractTrash implementation //
    //////////////////////////////////

    /**
     * Implementation notes: returns <code>true</code> only for local files that are not archive entries.
     */
    @Override
    public boolean canMoveToTrash(AbstractFile file) {
        return file.getTopAncestor() instanceof LocalFile;
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    @Override
    public boolean canEmpty() {
        return true;
    }

    @Override
    public boolean empty() {
        return executeAndWait(EMPTY_TRASH_COMMAND);
    }

    @Override
    public boolean isTrashFile(AbstractFile file) {
        return (file.getTopAncestor() instanceof LocalFile)
            && (file.getAbsolutePath(true).indexOf("/.local/share/Trash/") != -1);
    }

    /**
     * Implementation notes: always returns <code>-1</code> (information not available).
     */
    @Override
    public int getItemCount() {
        return -1;
    }

    @Override
    public void open() {
        executeAndWait(baseCommand+" exec trash:/");
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    @Override
    public boolean canOpen() {
        return true;
    }


    ////////////////////////////////
    // QueuedTrash implementation //
    ////////////////////////////////

    @Override
    protected boolean moveToTrash(List<AbstractFile> queuedFiles) {
        int nbFiles = queuedFiles.size();
        String tokens[] = new String[nbFiles+3];

        tokens[0] = baseCommand;
        tokens[1] = "move";

        for(int i=0; i<nbFiles; i++) {
            tokens[i+2] = queuedFiles.get(i).getAbsolutePath();
        }

        tokens[nbFiles+2] = "trash:/";

        return executeAndWait(tokens);
    }
}
