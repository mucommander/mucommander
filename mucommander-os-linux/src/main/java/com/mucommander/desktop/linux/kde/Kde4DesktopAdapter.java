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

package com.mucommander.desktop.linux.kde;

import com.mucommander.desktop.TrashProvider;

/**
 * @author Maxence Bernard
 */
abstract class Kde4DesktopAdapter extends KdeDesktopAdapter {

    static String BASE_COMMAND = "kioclient";

    static String TRASH_EMPTY_COMMAND = "ktrash --empty";

    @Override
    protected String getFileManagerName() {
        return "Dolphin";
    }

    @Override
    protected String getBaseCommand() {
        return BASE_COMMAND;
    }

    @Override
    public TrashProvider getTrash() {
        return new Kde4TrashProvider();
    }
}
