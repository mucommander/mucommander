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

package com.mucommander.ui.action.impl;

import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.NoIcon;
import com.mucommander.ui.main.MainFrame;

/**
 * This action opens the parent folder of the selected file and keeps the latter file selected.
 * 
 * @author Arik Hadas
 */
public class ShowInEnclosingFolderAction extends SelectedFileAction {

    public ShowInEnclosingFolderAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        AbstractFile selectedFile = mainFrame.getActiveTable().getSelectedFile(true, true);
        if (selectedFile == null)
            return;

        AbstractFile enclosingFolder = selectedFile.getParent();
        mainFrame.getActivePanel().tryChangeCurrentFolder(enclosingFolder, selectedFile, true);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.ShowInEnclosingFolder.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }
    }
}
