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
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.terminal.TerminalIntegration;

/**
 * This action shows built-in terminal (it mimics the behavior of Midnight Commander Ctrl-O command
 * that originates back from Norton Commander).
 */
public class ToggleTerminalAction extends ActiveTabAction {

    /**
     * A reference to Terminal Integration. Made static (and volatile) to ensure
     * that TerminalIntegration is initialized only once, otherwise, for some unknown
     * to me reason even-though ToggleTerminalAction constructor is called once, the
     * TerminalIntegration is called twice as if there was another instance of
     * ToggleTerminalAction with terminalIntegration = null - but there's only one instance (!).
     */
    private volatile static TerminalIntegration terminalIntegration = null;
    private final static Object LOCK = new Object();

    public ToggleTerminalAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        setEnabled(DesktopManager.canOpenInFileManager());
    }

    @Override
    public void performAction() {
        ensureIntegrationIsInitialized();
        terminalIntegration.toggleTerminal();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = ActionType.ToggleTerminal.getId();

        @Override
        public String getId() {
            return ACTION_ID;
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        @Override
        public String getLabel() {
            return Translator.get(ACTION_ID);
        }
    }

    @Override
    protected void toggleEnabledState() {
        ensureIntegrationIsInitialized();
        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        setEnabled(currentFolder.getURL().getScheme().equals(LocalFile.SCHEMA));
    }

    private void ensureIntegrationIsInitialized() {
        var verticalSplit = mainFrame.getVerticalSplitPane();
        if (terminalIntegration == null && verticalSplit != null) {
            // doing it lazy, because in c-tor main frame might not be fully built (no vertical split pane yet)
            synchronized (LOCK) {
                if (terminalIntegration == null) {
                    terminalIntegration = new TerminalIntegration(mainFrame, verticalSplit);
                }
            }
        }
    }
}
