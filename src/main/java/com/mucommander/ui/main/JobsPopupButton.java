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

import com.mucommander.job.FileJob;
import com.mucommander.job.JobListener;
import com.mucommander.job.JobsManager;
import com.mucommander.ui.button.PopupButton;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.*;

/**
 * JobsPopupButton is a button that allows to interact with {@link FileJob}s that are running in the background.
 * When the button is clicked, a popup menu is displayed, showing the operations that are currently running
 * int the background. Clicking on a job displays its progress dialog and removes it from the list of jobs
 * that are running in the background (the job switches to blocking mode).
 * Note that this button will only be functional when there are jobs running in the background.
 *
 * @author Arik Hadas
 */
class JobsPopupButton extends PopupButton implements JobListener {

    /**
     * Holds a reference to the RolloverButtonAdapter instance so that it doesn't get garbage-collected
     */
    private RolloverButtonAdapter rolloverButtonAdapter;

    JobsPopupButton() {
        setContentAreaFilled(false);
        setIcon(IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, "waiting.png"));

        // Rollover-enable the button and hold a reference to the RolloverButtonAdapter instance so that it doesn't
        // get garbage-collected
        rolloverButtonAdapter = new RolloverButtonAdapter();
        RolloverButtonAdapter.setButtonDecoration(this);
        addMouseListener(rolloverButtonAdapter);

        setVisible(false);
        JobsManager.getInstance().addJobListener(this);
    }

    @Override
    public Insets getInsets() {
        return new Insets(2, 2, 2, 2);
    }

    @Override
    public void jobAdded(FileJob source) {
        int nbBackgroundJobs = JobsManager.getInstance().getBackgroundJobs().size();
        setVisible(nbBackgroundJobs != 0);
    }

    @Override
    public void jobRemoved(FileJob source) {
        int nbBackgroundJobs = JobsManager.getInstance().getBackgroundJobs().size();
        setVisible(nbBackgroundJobs != 0);
        if (!isVisible() && isPopupMenuVisible())
            popupMenu.setVisible(false);
    }

    @Override
    public JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JobsManager jobsManager = JobsManager.getInstance();
        for (FileJob job : jobsManager.getBackgroundJobs()) {
            JMenuItem jobItem = new JMenuItem();

            jobItem.setText(String.format("%s (%s%%)",
                    job.getProgressDialog().getTitle(),
                    job.getJobProgress().getTotalPercentInt()));

            jobItem.addActionListener(e -> {
                job.setRunInBackground(false);
                job.getProgressDialog().showDialog();
            });

            jobsManager.addJobListener(new JobListener() {
                @Override
                public void jobProgress(FileJob source, boolean fullUpdate) {
                    if (source == job) {
                        jobItem.setText(String.format("%s (%s%%)",
                                job.getProgressDialog().getTitle(),
                                job.getJobProgress().getTotalPercentInt()));
                    }
                }
            });

            popupMenu.add(jobItem);
        }

        return popupMenu;
    }

}
