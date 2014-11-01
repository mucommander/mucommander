/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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


package com.mucommander;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.table.FileTable;

/**
 * Used to start muCommander in stress-test mode.
 * @author Maxence Bernard
 */
public class StressTester implements Runnable, ActionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(StressTester.class);
	
    private boolean run;

    public StressTester() {
        run = true;
    }

    /**
     * Stops the current stress test.
     */
    public void stop() {
        run = false;
    }

    public void run() {
        Random random = new Random();
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();

        while(run) {
            if(random.nextInt(2)==0)
                ActionManager.performAction(com.mucommander.ui.action.impl.SwitchActiveTableAction.Descriptor.ACTION_ID, mainFrame);    

            FolderPanel folderPanel = mainFrame.getActivePanel();
            FileTable fileTable = mainFrame.getActiveTable();
            AbstractFile currentFolder = folderPanel.getCurrentFolder();

            try {
                AbstractFile parentFolder = currentFolder.getParent();
                AbstractFile children[] = currentFolder.ls();
                // 1 in 3 chance to go up if folder has children
                if(children.length==0 || (random.nextInt(3)==0 && parentFolder!=null)) {
                    fileTable.selectRow(0);
                    ActionManager.performAction(com.mucommander.ui.action.impl.OpenAction.Descriptor.ACTION_ID, mainFrame);
                }
                else {
                    AbstractFile randomChild = children[random.nextInt(children.length)];
                    if(!randomChild.isBrowsable())
                        continue;
                    // Try to ls() in RandomChild to trigger an IOException if folder is not readable
                    // so that no error dialog pops up when calling tryChangeCurrentFolder()
                    randomChild.ls();
                    fileTable.selectFile(randomChild);
                    ActionManager.performAction(com.mucommander.ui.action.impl.OpenAction.Descriptor.ACTION_ID, mainFrame);
                    //					folderPanel.tryChangeCurrentFolder(randomChild, true);
                }
            }
            catch(Exception e) {
                LOGGER.debug("Caught Exception", e);
            }

            LOGGER.trace("Sleeping for a bit...");
            try {
                Thread.sleep(100+random.nextInt(200));
            }
            catch(InterruptedException e) {
                LOGGER.debug("Caught InterruptedException", e);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        stop();
    }

    /**
     * Method used to start the stress tester.
     * @param args command line arguments.
     * @throws IOException if an unrecoverable error occurred during startup
     */
    public static void main(String args[]) throws IOException {
        Launcher.main(args);

        StressTester instance = new StressTester();
        JDialog stopDialog = new JDialog();
        JButton stopButton = new JButton("Stop");
        new Thread(instance).start();
        stopButton.addActionListener(instance);
        stopDialog.getContentPane().add(stopButton);
        stopDialog.setSize(new Dimension(80, 60));
        stopDialog.setVisible(true);
    }
}
