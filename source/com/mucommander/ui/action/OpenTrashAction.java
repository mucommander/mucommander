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

package com.mucommander.ui.action;

import com.mucommander.file.AbstractTrash;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * Opens the trash in the default file manager of the current OS/Desktop manager. This action is enabled only
 * if the current platform has an {@link com.mucommander.file.AbstractTrash} implementation and if it is capable
 * of opening the trash, as reported by {@link com.mucommander.file.AbstractTrash#canOpen()}.
 *
 * @author Maxence Bernard
 */
public class OpenTrashAction extends MuAction {

    public OpenTrashAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        AbstractTrash trash = FileFactory.getTrash();
        setEnabled(trash!=null && trash.canOpen());
    }

    public void performAction() {
        FileFactory.getTrash().open();
    }
}
