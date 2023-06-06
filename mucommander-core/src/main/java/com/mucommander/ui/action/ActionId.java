/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.ui.action;

import java.util.Objects;

/**
 * A class representing ActionId for shortcuts and actions, including custom commands,
 * commandbar, toolbar and terminal.
 */
public final class ActionId {

    enum ActionType {
        GENERIC,
        COMMAND,
        COMMANDBAR,
        TOOLBAR,
        TERMINAL
    }

    private final String actionId;
    private final ActionType type;

    private ActionId(String actionId, ActionType type) {
        this.actionId = actionId;
        this.type = type;
    }

    public static ActionId asGenericAction(String actionId) {
        return new ActionId(actionId, ActionType.GENERIC);
    }

    public static ActionId asCommandAction(String actionId) {
        return new ActionId(actionId, ActionType.COMMAND);
    }

    public static ActionId asCommandBarAction(String actionId) {
        return new ActionId(actionId, ActionType.COMMANDBAR);
    }

    public static ActionId asToolBarAction(String actionId) {
        return new ActionId(actionId, ActionType.TOOLBAR);
    }

    public static ActionId asTerminalAction(String actionId) {
        return new ActionId(actionId, ActionType.TERMINAL);
    }

    public String getActionId() {
        return actionId;
    }

    public ActionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ActionId actionId1 = (ActionId) o;
        return Objects.equals(actionId, actionId1.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }

    @Override
    public String toString() {
        return actionId;
    }
}
