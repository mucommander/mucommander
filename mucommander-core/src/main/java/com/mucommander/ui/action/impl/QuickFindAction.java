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

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.search.SearchProtocolProvider;
import com.mucommander.search.SearchUtils;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.InvokesDialog;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.LocationTextField;
import com.mucommander.ui.main.MainFrame;

/**
 * This action pops up the Search dialog that is used to execute a file search.
 *
 * @author Arik Hadas
 */
@InvokesDialog
public class QuickFindAction extends MuAction {

    public QuickFindAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        AbstractFile currentFile = mainFrame.getActivePanel().getCurrentFolder();
        FileURL searchURL = SearchUtils.toSearchURL(currentFile);
        LocationTextField locationTextField = mainFrame.getActivePanel().getLocationTextField();
        locationTextField.setText(searchURL.toString());
        locationTextField.requestFocus();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new QuickFindAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "QuickFind";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.FILES; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK  | KeyEvent.SHIFT_DOWN_MASK); }
    }
}
