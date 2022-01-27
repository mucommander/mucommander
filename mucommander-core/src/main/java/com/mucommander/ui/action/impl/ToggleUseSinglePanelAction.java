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

import javax.swing.KeyStroke;

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;

/**
 * Toggles isVisible state of the right panel, imitating single/two panel view switch.
 */
public class ToggleUseSinglePanelAction extends MuAction {

    private static final float TWO_PANELS_DEFAULT_RATIO = 0.5f;
    private float previousRatio = TWO_PANELS_DEFAULT_RATIO;

    public ToggleUseSinglePanelAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    private void hideInactivePanel() {
        mainFrame.getInactivePanel().setVisible(false);
    }

    private void showInactivePanel() {
        mainFrame.getInactivePanel().setVisible(true);
    }


    @Override
    public void performAction() {
        // we want to restore old two panel ratio
        boolean isSinglePanelViewNow = mainFrame.toggleSinglePanel();

        if (isSinglePanelViewNow) {
            previousRatio = mainFrame.getSplitPane().getSplitRatio();
            hideInactivePanel();

        } else {
            mainFrame.getSplitPane().setSplitRatio(previousRatio);
            showInactivePanel();
        }
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ToggleSinglePanel";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.VIEW;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return null;
        }
    }
}
