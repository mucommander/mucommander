package com.mucommander.shell;

/**
 * Interface used to monitor changes to the shell history.
 * @author Nicolas Rinaudo
 */
public interface ShellHistoryListener {
    /**
     * Notifies the listener that a new element has been added to the history.
     * @param command command that was added to the history.
     */
    public void historyChanged(String command);
}
