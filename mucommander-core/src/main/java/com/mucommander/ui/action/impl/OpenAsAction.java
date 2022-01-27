/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.action.impl;

import java.io.IOException;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * Open a file as if it has the specified file extension.
 * @author Arik Hadas
*/
public class OpenAsAction extends OpenAction {

    private String extension;

    public OpenAsAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        extension = (String) properties.get("extension");
    }

    /**
     * Opens the currently selected file in the active folder panel.
     */
    @Override
    public void performAction() {
        // Retrieves the currently selected file,
        // Note: a CachedFile instance is retrieved to avoid blocking the event thread.
        AbstractFile file = mainFrame.getActiveTable().getSelectedFile(true, true);

        // Aborts if none.
        if (file == null)
            return;

        AbstractFile resolvedFile;
        if (file.isSymlink()) {
            resolvedFile = resolveSymlink(file);
            if (resolvedFile == null) {
                InformationDialog.showErrorDialog(mainFrame, Translator.get("cannot_open_cyclic_symlink"));
                return;
            }
        }
        else
            resolvedFile = file;

        try {
            resolvedFile = FileFactory.wrapArchive(resolvedFile, extension);
            resolvedFile.setCustomExtension(extension.substring(1));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Opens the currently selected file.
        open(resolvedFile, mainFrame.getActivePanel());

    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "OpenAs";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return null; }
    }

}
