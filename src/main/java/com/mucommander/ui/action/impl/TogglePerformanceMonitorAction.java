/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

import com.mucommander.text.Translator;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.util.Map;

/**
 * This action shows/hides the current MainFrame's performance monitor dialog depending on its
 * current visible state: if it is visible, hides it, if not shows it.
 * <p>
 * <p>This action's label will be updated to reflect the current visible state.
 * <p>
 *
 * @author Mikhail Tikhomirov
 */
public class TogglePerformanceMonitorAction extends MuAction {

    public TogglePerformanceMonitorAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
        updateLabel(false);
    }

    private void updateLabel(boolean visible) {
        setLabel(Translator.get(visible ? Descriptor.ACTION_ID + ".hide" : Descriptor.ACTION_ID + ".show"));
    }

    @Override
    public void performAction() {
        final boolean visible = !mainFrame.isPerformanceMonitorDialogVisible();
        updateLabel(visible);
        mainFrame.setPerformanceMonitorVisible(visible);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static class Factory implements ActionFactory {

        @Override
        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new TogglePerformanceMonitorAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "TogglePerformanceMonitor";

        @Override
        public String getId() {
            return ACTION_ID;
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.VIEW;
        }

        @Override
        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        @Override
        public KeyStroke getDefaultKeyStroke() {
            return null;
        }

        @Override
        public String getLabelKey() {
            return ACTION_ID + ".show";
        }
    }
}
