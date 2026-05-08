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

import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.dnd.ClipboardSupport;
import dev.barebones.commander.ui.dnd.TransferableFileSet;
import dev.barebones.commander.ui.main.MainFrame;

import java.util.Map;

/**
 * This action copies the file base name(s) (without extension) of the currently selected / marked files(s) to the
 * system clipboard.
 *
 * @author Chen Rozenes
 */
public class CopyFileBaseNamesAction extends SelectedFilesAction {

    public CopyFileBaseNamesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction(FileSet files) {
        // Create a TransferableFileSet and make DataFlavour.stringFlavor (text) the only DataFlavour supported
        TransferableFileSet tfs = new TransferableFileSet(files);

        // Disable unwanted data flavors
        tfs.setJavaFileListDataFlavorSupported(false);
        tfs.setTextUriFlavorSupported(false);
        // Note: not disabling this flavor would throw an exception because the flavor data is not serializable
        tfs.setFileSetDataFlavorSupported(false);

        // Transfer filenames, not file paths
        tfs.setStringDataFlavourTransfersFilename(true);

        // Transfer base names (filename without its extension)
        tfs.setStringDataFlavorSupported(true);
        tfs.setStringDataFlavourTransfersFilename(true);
        tfs.setStringDataFlavourTransfersFileBaseName(true);

        ClipboardSupport.setClipboardContents(tfs);

    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.CopyFileBaseNames.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.SELECTION;
        }
    }

}
