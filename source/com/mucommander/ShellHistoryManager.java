package com.mucommander;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.PlatformManager;

import java.io.*;
import java.util.*;

/**
 * Used to manage shell history.
 * <p>
 * Using this class is fairly basic: you can add elements to the shell history through
 * {@link #add(String)} and browse it through {@link #getHistoryIterator()}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ShellHistoryManager {
    // - History configuration -----------------------------------------------
    // -----------------------------------------------------------------------
    /** Default history size. */
    private static final int    DEFAULT_HISTORY_SIZE = 100;
    /** Name of the configuration variable that stores the history size. */
    private static final String HISTORY_SIZE_VAR  = "prefs.shell.history_size";
    /** File in which to store the shell history. */
    private static final String HISTORY_FILE      = "history.txt";



    // - Class fields ---------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /** Stores the shell history. */
    private static String[] history;
    /** Index of the first element of the history. */
    private static int      historyStart;
    /** Index of the last element of the history. */
    private static int      historyEnd;
    /** Path to the history file. */
    private static String   historyFile;



    // - Initialisation -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Prevents instantiations of the class.
     */
    private ShellHistoryManager() {}

    /**
     * Initialises and history.
     */
    static {history = new String[ConfigurationManager.getVariableInt(HISTORY_SIZE_VAR, DEFAULT_HISTORY_SIZE)];}



    // - History access -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Returns a <b>non thread-safe</code> iterator on the history.
     * @return an iterator on the history.
     */
    public static Iterator getHistoryIterator() {return new HistoryIterator();}

    /**
     * Adds the specified command to shell history.
     * @param command command to add to the shell history.
     */
    public static void add(String command) {
        // Ignores empty commands.
        if(command.trim().equals(""))
            return;

        // Updates the history buffer.
        history[historyEnd] = command;
        historyEnd++;

        // Wraps around the history buffer.
        if(historyEnd == history.length)
            historyEnd = 0;

        // Clears items from the begining of the buffer if necessary.
        if(historyEnd == historyStart) {
            if(++historyStart == history.length)
                historyStart = 0;
        }
    }



    // - History saving / loading ---------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Sets the path of the shell history file.
     * @param path where to load the shell history from.
     */
    public static void setHistoryFile(String path) {historyFile = path;}

    /**
     * Returns the path to the history file.
     * @return the path to the history file.
     */
    public static File getHistoryFile() {
        // If an history file was specified, use it.
        if(historyFile != null)
            return new File(historyFile);

        // Otherwise use the default history file.
        return new File(PlatformManager.getPreferencesFolder(), HISTORY_FILE);
    }

    /**
     * Writes the shell history to hard drive.
     */
    public static void writeHistory() {
        PrintStream out;      // Where to write the history to.
        Iterator    iterator; // Iterator on the history.

        out = null;
        try {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Writing history file: " + getHistoryFile());

            // Writes the content of the shell history.
            out      = new PrintStream(new FileOutputStream(getHistoryFile()));
            iterator = getHistoryIterator();
            while(iterator.hasNext())
                out.println(iterator.next());

        }
        catch(Exception e) {if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("muCommander was unable to write history: " + e);}

        // Cleans up.
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Loads the shell history.
     */
    public static void loadHistory() {
        BufferedReader in;      // Where to read the history from.
        String         command; // Last command read from the history file.

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Loading shell history from: " + getHistoryFile());

        in = null;
        try {
            // Reads the content of the history file.
            in = new BufferedReader(new InputStreamReader(new FileInputStream(getHistoryFile())));
            while((command = in.readLine()) != null)
                add(command);
        }
        catch(Exception e) {if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Failed to load shell history: " + e);}

        // Cleans up.
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }

    }

    /**
     * Loads the shell history from the specified path.
     * @param path where to load shell history from.
     */
    public static void loadHistory(String path) {
        setHistoryFile(path);
        loadHistory();
    }

    /**
     * Iterator used to browse history.
     * @author Nicolas Rinaudo
     */
    static class HistoryIterator implements Iterator {
        /** Index in the history. */
        private int index;

        /**
         * Creates a new history iterator.
         */
        public HistoryIterator() {index = ShellHistoryManager.historyStart;}

        /**
         * Returns <code>true</code> if there are more elements to iterate through.
         * @return <code>true</code> if there are more elements to iterate through, <code>false</code> otherwise.
         */
        public boolean hasNext() {return index != ShellHistoryManager.historyEnd;}

        /**
         * Returns the next element in the history.
         * @return the next element in the history.
         */
        public Object next() throws NoSuchElementException {
            String value;

            if(!hasNext())
                throw new NoSuchElementException();

            value = ShellHistoryManager.history[index];
            if(++index == ShellHistoryManager.history.length)
                index = 0;
            return value;
        }

        /**
         * Operation not supported.
         */
        public void remove() throws UnsupportedOperationException {throw new UnsupportedOperationException();}
    }

}
