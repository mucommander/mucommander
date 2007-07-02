package com.mucommander.file.impl.trash;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.process.ProcessRunner;

import java.util.Vector;

/**
 * KDETrash provides access to the Konqueror trash. Only local files (or locally mounted files) can be moved
 * to the trash.
 *
 * <p>
 * <b>Implementation notes:</b><br>
 * <br>
 * This trash is implemented as a {@link QueuedTrash} as it spawns a process to move a file to the trash and it
 * is thus more effective to group files to be moved instead of spawning multiple processes.<br>
 * The <code>ktrash</code> and <code>kfmclient</code> commands are used to interact with the Konqueror trash.
 * </p>
 *
 * @author Maxence Bernard
 */
public class KDETrash extends QueuedTrash {

    /** Command that empties the trash */
    private final static String EMPTY_TRASH_COMMAND = "ktrash --empty";

    /** Command that reveals the trash in Konqueror */ 
    private final static String REVEAL_TRASH_COMMAND = "kfmclient openURL trash:/";


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
            if(Debug.ON) Debug.trace("Caught exception: "+e);
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
            if(Debug.ON) Debug.trace("Caught exception: "+e);
            return false;
        }
    }

    //////////////////////////////////
    // AbstractTrash implementation //
    //////////////////////////////////

    /**
     * Implementation notes: returns <code>true</code> only for local files that are not archive entries.
     */
    public boolean canMoveToTrash(AbstractFile file) {
        return file.getTopAncestor() instanceof LocalFile;
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    public boolean canEmptyTrash() {
        return true;
    }

    public boolean emptyTrash() {
        return executeAndWait(EMPTY_TRASH_COMMAND);
    }

    public boolean isTrashFile(AbstractFile file) {
        return (file.getTopAncestor() instanceof LocalFile)
                && file.getAbsolutePath(true).contains("/.local/share/Trash/");
    }

    /**
     * Implementation notes: always returns <code>-1</code> (information not available).
     */
    public int getTrashItemCount() {
        return -1;
    }

    public void revealTrash() {
        executeAndWait(REVEAL_TRASH_COMMAND);
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    public boolean canRevealTrash() {
        return true;
    }


    ////////////////////////////////
    // QueuedTrash implementation //
    ////////////////////////////////

    protected boolean moveToTrash(Vector queuedFiles) {
        int nbFiles = queuedFiles.size();
        String tokens[] = new String[nbFiles+3];

        tokens[0] = "kfmclient";
        tokens[1] = "move";

        for(int i=0; i<nbFiles; i++) {
            tokens[i+2] = ((AbstractFile)queuedFiles.elementAt(i)).getAbsolutePath();
        }

        tokens[nbFiles+2] = "trash:/";

        return executeAndWait(tokens);
    }
}
