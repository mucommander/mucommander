package com.mucommander.ui.action.impl;

import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.AbstractArchiveEntryFile;
import com.mucommander.commons.file.protocol.FileProtocols;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;

public class OpenCommandPromptAction extends ParentFolderAction {

    public OpenCommandPromptAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        setEnabled(DesktopManager.canOpenInFileManager());
    }

    @Override
    protected void toggleEnabledState() {
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(FileProtocols.FILE)
                && !currentFolder.isArchive()
                && !currentFolder.hasAncestor(AbstractArchiveEntryFile.class)
        );
    }

    @Override
    public void performAction() {
        try {
            DesktopManager.openCommandPrompt(mainFrame.getActivePanel().getCurrentFolder());
        }
        catch(Exception e) {
            InformationDialog.showErrorDialog(mainFrame.getJFrame());
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        @Override
        public String getId() { return ActionType.OpenCommandPrompt.getId(); }

        @Override
        public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

        @Override
        public String getLabel() {
            return Translator.get(ActionProperties.getActionLabelKey(ActionType.OpenCommandPrompt));
        }
    }
}