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

package com.mucommander.job;

import com.mucommander.translator.Translator;
import com.mucommander.ui.dialog.DialogAction;

/**
 * Actions on {@code FileJob}
 *
 * @author Arik Hadas
 */
public enum FileJobAction implements DialogAction {

    SKIP("skip"),
    SKIP_ALL("skip_all"),
    RETRY("retry"),
    CANCEL("cancel"),
    APPEND("resume"),
    OK("ok");

    private final String actionName;

    FileJobAction(String actionKey) {
        // here or when in #getActionName
        this.actionName = Translator.get(actionKey);
    }

    @Override
    public String getActionName() {
        return actionName;
    }
}
