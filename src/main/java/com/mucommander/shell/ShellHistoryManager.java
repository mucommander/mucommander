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

package com.mucommander.shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.io.backup.BackupInputStream;
import com.mucommander.io.backup.BackupOutputStream;

/**
 * Used to manage shell history.
 * <p>
 * Using this class is fairly basic: you can add elements to the shell history through
 * {@link #add(String)} and browse it through {@link #getHistoryIterator()}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ShellHistoryManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShellHistoryManager.class);
	
    // - History configuration -----------------------------------------------
    // -----------------------------------------------------------------------
    /** File in which to store the shell history. */
    private static final String DEFAULT_HISTORY_FILE_NAME = "shell_history.xml";



    // - Class fields ---------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /** List of shell history registered listeners. */
    private static WeakHashMap<ShellHistoryListener, ?> listeners;
    /** Stores the shell history. */
    private static String[]                             history;
    /** Index of the first element of the history. */
    private static int                                  historyStart;
    /** Index of the last element of the history. */
    private static int                                  historyEnd;
    /** Path to the history file. */
    private static AbstractFile                         historyFile;



    // - Initialisation -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Prevents instantiations of the class.
     */
    private ShellHistoryManager() {}

    /**
     * Initialises history.
     */
    static {
        history   = new String[MuConfigurations.getPreferences().getVariable(MuPreference.SHELL_HISTORY_SIZE, MuPreferences.DEFAULT_SHELL_HISTORY_SIZE)];
        listeners = new WeakHashMap<ShellHistoryListener, Object>();
    }



    // - Listener code --------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Registers a listener to changes in the shell history.
     * @param listener listener to register.
     */
    public static void addListener(ShellHistoryListener listener) {listeners.put(listener, null);}

    /**
     * Propagates shell history events to all registered listeners.
     * @param command command that was added to the shell history.
     */
    private static void triggerEvent(String command) {
        for(ShellHistoryListener listener : listeners.keySet())
            listener.historyChanged(command);
    }



    // - History access -------------------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Completely empties the shell history.
     */
    public static void clear() {
        // Empties history.
        historyStart = 0;
        historyEnd   = 0;

        // Notifies listeners.
        for(ShellHistoryListener listener : listeners.keySet())
            listener.historyCleared();
    }

    /**
     * Returns a <b>non thread-safe</b> iterator on the history.
     * @return an iterator on the history.
     */
    public static Iterator<String> getHistoryIterator() {return new HistoryIterator();}

    /**
     * Adds the specified command to shell history.
     * @param command command to add to the shell history.
     */
    public static void add(String command) {
        // Ignores empty commands.
        if(command.trim().equals(""))
            return;
        // Ignores the command if it's the same as the last one.
        // There is no last command if history is empty.
        if(historyEnd != historyStart) {
            int lastIndex;

            // Computes the index of the previous command.
            if(historyEnd == 0)
                lastIndex = history.length - 1;
            else
                lastIndex = historyEnd - 1;

            if(command.equals(history[lastIndex]))
                return;
        }

        LOGGER.debug("Adding  " + command + " to shell history.");

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

        // Propagates the event.
        triggerEvent(command);
    }



    // - History saving / loading ---------------------------------------------------
    // ------------------------------------------------------------------------------
    /**
     * Sets the path of the shell history file.
     * @param     path                  where to load the shell history from.
     * @exception FileNotFoundException if <code>path</code> is not accessible.
     * @see                             #getHistoryFile()
     * @see                             #setHistoryFile(File)
     * @see                             #setHistoryFile(AbstractFile)
     */
    public static void setHistoryFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setHistoryFile(new File(path));
        else
            setHistoryFile(file);
    }

    /**
     * Sets the path of the shell history file.
     * @param     file                  where to load the shell history from.
     * @exception FileNotFoundException if <code>path</code> is not accessible.
     * @see                             #getHistoryFile()
     * @see                             #setHistoryFile(AbstractFile)
     * @see                             #setHistoryFile(String)
     */
    public static void setHistoryFile(File file) throws FileNotFoundException {setHistoryFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path of the shell history file.
     * @param     file                  where to load the shell history from.
     * @exception FileNotFoundException if <code>path</code> is not accessible.
     * @see                             #getHistoryFile()
     * @see                             #setHistoryFile(File)
     * @see                             #setHistoryFile(String)
     */
    public static void setHistoryFile(AbstractFile file) throws FileNotFoundException {
        // Makes sure file can be used as a shell history file.
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        historyFile = file;
    }

    /**
     * Returns the path to the shell history file.
     * <p>
     * This method cannot guarantee the file's existence, and it's up to the caller
     * to deal with the fact that the user might not actually have created a history file yet.
     * </p>
     * <p>
     * This method's return value can be modified through {@link #setHistoryFile(String)}.
     * If this wasn't called, the default path will be used: {@link #DEFAULT_HISTORY_FILE_NAME}
     * in the {@link com.mucommander.PlatformManager#getPreferencesFolder() preferences} folder.
     * </p>
     * @return             the path to the shell history file.
     * @throws IOException if an error occured while locating the default shell history file.
     * @see                #setHistoryFile(File)
     * @see                #setHistoryFile(String)
     * @see                #setHistoryFile(AbstractFile)
     */
    public static AbstractFile getHistoryFile() throws IOException {
        if(historyFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_HISTORY_FILE_NAME);
        return historyFile;
    }

    /**
     * Writes the shell history to hard drive.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeHistory() throws IOException {
        BackupOutputStream out;

        out = null;
        try {ShellHistoryWriter.write(out = new BackupOutputStream(getHistoryFile()));}
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Loads the shell history.
     * @throws Exception if an error occurs.
     */
    public static void loadHistory() throws Exception {
        BackupInputStream in;

        in = null;
        try {ShellHistoryReader.read(in = new BackupInputStream(getHistoryFile()));}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e2) {}
            }
        }
    }

    /**
     * Iterator used to browse history.
     * @author Nicolas Rinaudo
     */
    static class HistoryIterator implements Iterator<String> {
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
        public String next() throws NoSuchElementException {
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
