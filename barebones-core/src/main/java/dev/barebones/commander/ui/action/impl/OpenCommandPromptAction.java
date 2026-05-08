package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.archive.AbstractArchiveEntryFile;
import dev.barebones.commander.commons.file.protocol.FileProtocols;
import dev.barebones.commander.core.desktop.DesktopManager;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.ActionProperties;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.dialog.InformationDialog;
import dev.barebones.commander.ui.main.MainFrame;

public class OpenCommandPromptAction extends ParentFolderAction {

    public OpenCommandPromptAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setEnabled(DesktopManager.canOpenInFileManager());
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(FileProtocols.FILE)
                && !currentFolder.isArchive()
                && !currentFolder.hasAncestor(AbstractArchiveEntryFile.class));
    }

    @Override
    public void performAction() {
        try {
            DesktopManager.openCommandPrompt(mainFrame.getActivePanel().getCurrentFolder());
        } catch (Exception e) {
            InformationDialog.showErrorDialog(mainFrame.getJFrame());
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        @Override
        public String getId() {
            return ActionType.OpenCommandPrompt.getId();
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        @Override
        public String getLabel() {
            return Translator.get(ActionProperties.getActionLabelKey(ActionType.OpenCommandPrompt));
        }
    }
}
