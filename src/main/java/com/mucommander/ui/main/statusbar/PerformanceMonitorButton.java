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

package com.mucommander.ui.main.statusbar;

import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.impl.TogglePerformanceMonitorAction;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

import java.awt.*;

/**
 * PerformanceMonitorButton is a button that shows performance monitor dialog with information about memory consumption and CPU utilization
 *
 * @author Mikhail Tikhomirov
 */
class PerformanceMonitorButton extends NonFocusableButton {

    PerformanceMonitorButton(MainFrame mainFrame) {
        super(ActionManager.getActionInstance(TogglePerformanceMonitorAction.Descriptor.ACTION_ID, mainFrame));
        setIcon(IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, "chart_bar.png"));
        RolloverButtonAdapter.decorateButton(this);
        putClientProperty("JButton.buttonType", "square");
    }

    @Override
    public void setText(String text) {
        super.setText(null);
    }

    /**
     * Replace the default insets to be exactly (2,2,2,2).
     */
    @Override
    public Insets getInsets() {
        return new Insets(2, 2, 2, 2);
    }

}
