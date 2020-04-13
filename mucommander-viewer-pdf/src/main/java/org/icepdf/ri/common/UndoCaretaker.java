/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.common;

import org.icepdf.core.Memento;
import org.icepdf.core.util.Defs;

import java.util.ArrayList;

/**
 * Undo caretaker implementation for the Viewer RI.  Currently only annotation
 * can be manipulate but this class can easily handle any class that implements
 * the Memento interfce.
 *
 * @since 4.0
 */
public class UndoCaretaker {

    // max number of object to store in undo list.
    private static int maxHistorySize;

    static {
        // enables interactive annotation support.
        maxHistorySize =
                Defs.sysPropertyInt(
                        "org.icepdf.ri.viewer.undo.size", 25);
    }

    private ArrayList<Memento> mementoStateHistory;
    private int cursor;

    public UndoCaretaker() {
        mementoStateHistory = new ArrayList<Memento>(maxHistorySize);
        cursor = 0;
    }

    /**
     * Undo the last state change.  Only possible if there are items in the
     * undo history list.
     */
    public void undo() {
        if (isUndo()) {
            // move the point reference
            cursor = cursor - 1;
            Memento tmp = mementoStateHistory.get(cursor);
            // restore the old state
            tmp.restore();
        }
    }

    /**
     * Gets the status of the undo command.
     *
     * @return true if an undo command is possible, false if undo can not be done.
     */
    public boolean isUndo() {
        return mementoStateHistory.size() > 0 && cursor > 0;
    }

    /**
     * Redo the last state change.  ONly possible if there have been previous
     * undo call.
     */
    public void redo() {
        if (isRedo()) {
            // move the pointer
            cursor = cursor + 1;
            Memento tmp = mementoStateHistory.get(cursor);
            // restore the old state
            tmp.restore();
        }
    }

    /**
     * Gets the status of the redo command.
     *
     * @return true if an redo command is possible, false if the redo can not be done.
     */
    public boolean isRedo() {
        // check for at least one history state in the next index.
        return cursor + 1 < mementoStateHistory.size();
    }

    /**
     * Adds the give states to the history list.
     *
     * @param previousState previous state
     * @param newState      new state.
     */
    public void addState(Memento previousState, Memento newState) {
        // first check history bounds, if we are in an none
        if (cursor >= maxHistorySize) {
            // get rid of first index.
            mementoStateHistory.remove(0);
            mementoStateHistory.remove(1);
            cursor = mementoStateHistory.size() - 1;
        }
        // check to see if we are in a possible redo state, if so we clear
        // all states from the current pointer.
        if (isRedo()) {
            for (int i = cursor + 1, max = mementoStateHistory.size(); i < max; i++) {
                mementoStateHistory.remove(cursor + 1);
            }
        }
        // first entry is special case, add them as is.
        if (mementoStateHistory.size() == 0) {
            mementoStateHistory.add(previousState);
            mementoStateHistory.add(newState);
            cursor = 1;
        }
        // we do an offset add
        else {
            mementoStateHistory.set(cursor, previousState);
            mementoStateHistory.add(newState);
            cursor++;
        }
    }
}
