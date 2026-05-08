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

package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.FolderPanel;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.table.FileTable;

/**
 * Opens browsable files in the inactive panel.
 * <p>
 * This action is only enabled if the current selection is browsable as defined by
 * {@link dev.barebones.commander.commons.file.AbstractFile#isBrowsable()}.
 * </p>
 * 
 * @author Nicolas Rinaudo
 */
public class OpenInOtherPanelAction extends SelectedFileAction {
    /**
     * Creates a new <code>OpenInOtherPanelAction</code> with the specified parameters.
     * 
     * @param mainFrame
     *            frame to which the action is attached.
     * @param properties
     *            action's properties.
     */
    public OpenInOtherPanelAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void activePanelChanged(FolderPanel folderPanel) {
        super.activePanelChanged(folderPanel);

        if (mainFrame.getInactivePanel().getTabs().getCurrentTab().isLocked())
            setEnabled(false);
    }

    /**
     * This method is overridden to enable this action when the parent folder is selected.
     */
    @Override
    protected boolean getFileTableCondition(FileTable fileTable) {
        AbstractFile selectedFile = fileTable.getSelectedFile(true, true);

        return selectedFile != null && selectedFile.isBrowsable();
    }

    /**
     * Opens the currently selected file in the inactive folder panel.
     */
    @Override
    public void performAction() {
        AbstractFile file;

        // Retrieves the currently selected file, aborts if none (should not normally happen).
        if ((file = mainFrame.getActiveTable().getSelectedFile(true, true)) == null || !file.isBrowsable())
            return;

        // Opens the currently selected file in the inactive panel.
        mainFrame.getInactivePanel().tryChangeCurrentFolder(file);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.OpenInOtherPanel.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }
    }
}
