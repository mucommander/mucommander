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
package com.mucommander.ui.main;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import com.mucommander.job.FileJob;
import com.mucommander.job.JobListener;
import com.mucommander.job.JobsManager;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.icon.IconManager;

/**
 * JobsPopupButton is a button that allows to interact with {@link FileJob}s that are running in the background.
 * {@link com.mucommander.desktop.DesktopManager#getTrash()}.
 * Note that this button will only be functional when there are jobs running in the background. 
 *
 * @author Arik Hadas
 */
class JobsPopupButton extends /*PopupButton*/ JButton implements JobListener {

    /** Holds a reference to the RolloverButtonAdapter instance so that it doesn't get garbage-collected */
    private RolloverButtonAdapter rolloverButtonAdapter;

    JobsPopupButton() {
        setContentAreaFilled(false);
        setIcon(IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, "waiting.png"));

        // Rollover-enable the button and hold a reference to the RolloverButtonAdapter instance so that it doesn't
        // get garbage-collected
//        rolloverButtonAdapter = new RolloverButtonAdapter();
//        RolloverButtonAdapter.setButtonDecoration(this);
//        addMouseListener(rolloverButtonAdapter);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        setVisible(false);
        JobsManager.getInstance().addJobListener(this);
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public void jobAdded(FileJob source) {
        setVisible(JobsManager.getInstance().getJobCount() != 0);
    }

    @Override
    public void jobRemoved(FileJob source) {
        setVisible(JobsManager.getInstance().getJobCount() != 0);
    }

}
