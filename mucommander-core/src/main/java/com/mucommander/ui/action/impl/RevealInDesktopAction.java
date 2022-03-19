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

import static com.mucommander.core.desktop.DesktopManager.openInFileManager;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.AbstractArchiveEntryFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This action reveals the currently selected file or folder in the native Desktop's file manager (e.g. Finder for
 * macOS, Explorer for Windows, etc...).
 *
 * @author Maxence Bernard
 */
public class RevealInDesktopAction extends ParentFolderAction {

    public RevealInDesktopAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setEnabled(DesktopManager.canOpenInFileManager());
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(LocalFile.SCHEMA)
                && !currentFolder.isArchive()
                && !currentFolder.hasAncestor(AbstractArchiveEntryFile.class));
    }

    @Override
    public void performAction() {
        AbstractFile selectedFile = mainFrame.getActiveTable().getSelectedFile();
        if (selectedFile != null) {
            try {
                CompletionStage<Optional<String>> completionStage = openInFileManager(selectedFile);
                InformationDialog.showErrorDialogIfNeeded(getMainFrame(), completionStage);
            } catch (Exception e) {
                InformationDialog.showErrorDialog(mainFrame);
            }
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "RevealInDesktop";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
        }

        @Override
        public String getLabel() {
            return Translator.get(ActionProperties.getActionLabelKey(RevealInDesktopAction.Descriptor.ACTION_ID),
                    DesktopManager.canOpenInFileManager() ? DesktopManager.getFileManagerName()
                            : Translator.get("file_manager"));
        }
    }
}
