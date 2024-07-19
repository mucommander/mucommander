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

import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.NoIcon;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.tabs.ActiveTabListener;

/**
 * Pops up the DrivePopupButton (the drop down button that allows to quickly select a volume or bookmark) of the left
 * FolderPanel.
 *
 * @author Maxence Bernard
 */
public class PopupLeftDriveButtonAction extends MuAction implements ActiveTabListener {

    public PopupLeftDriveButtonAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        mainFrame.getLeftPanel().getTabs().addActiveTabListener(this);

        activeTabChanged();
    }

    /**
     * Enables or disables this action based on the current tab is not locked, this action will be enabled, if not it
     * will be disabled.
     */
    public void activeTabChanged() {
        setEnabled(!mainFrame.getLeftPanel().getTabs().getCurrentTab().isLocked());
    }

    @Override
    public void performAction() {
        mainFrame.getLeftPanel().getDriveButton().popupMenu();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.PopupLeftDriveButton.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }
    }
}
