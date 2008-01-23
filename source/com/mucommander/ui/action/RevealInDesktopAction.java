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

import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;


/**
 * This action reveals the currently selected file or folder in the native Desktop's file manager
 * (e.g. Finder for Mac OS X, Explorer for Windows, etc...).
 *
 * @author Maxence Bernard
 */
public class RevealInDesktopAction extends MuAction {

    public RevealInDesktopAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
        setLabel(Translator.get(getClass().getName()+".label", PlatformManager.getFileManagerName()));

        // Disable this action if the platform is not capable of opening files in the default file manager
        if(!PlatformManager.canOpenInFileManager())
            setEnabled(false);
    }

    public void performAction() {
        PlatformManager.openInFileManager(mainFrame.getActiveTable().getFolderPanel().getCurrentFolder());
    }
}
