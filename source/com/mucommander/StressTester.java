
package com.mucommander;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;
import com.mucommander.ui.table.FileTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;


public class StressTester implements Runnable, ActionListener {

    private static Thread stressThread;

    public StressTester() {
        (stressThread = new Thread(this)).start();
    }

    public static void stop() {
        stressThread = null;
    }

    public void run() {
        Random random = new Random();
        MainFrame mainFrame = WindowManager.getCurrentMainFrame();
        FolderPanel folderPanel1 = mainFrame.getFolderPanel1();
        FolderPanel folderPanel2 = mainFrame.getFolderPanel2();
		
        while(stressThread!=null) {
            FolderPanel folderPanel = random.nextInt(2)==0?folderPanel1:folderPanel2;
            AbstractFile currentFolder = folderPanel.getCurrentFolder();
            AbstractFile parentFolder = currentFolder.getParent();

            mainFrame.toBack();
            mainFrame.toFront();
			
            try {
                AbstractFile children[] = currentFolder.ls();
                FileTable fileTable = folderPanel.getFileTable();
                // 1 in 3 chance to go up if folder has children
                if(children.length==0 || (random.nextInt(3)==0 && parentFolder!=null)) {
                    fileTable.selectRow(0);
                    fileTable.enterAction(false);
                    //					folderPanel.trySetCurrentFolder(parentFolder, true);
                }
                else {
                    AbstractFile randomChild = children[random.nextInt(children.length)];
                    if(!randomChild.isBrowsable())
                        continue;
                    // Try to ls() in RandomChild to trigger an IOException if folder is not readable
                    // so that no error dialog pops up when calling trySetCurrentFolder()
                    randomChild.ls();
                    fileTable.selectFile(randomChild);
                    fileTable.enterAction(false);
                    //					folderPanel.trySetCurrentFolder(randomChild, true);
                }
            }
            catch(Exception e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught Exception: "+e);
            }

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Sleeping for a bit...");		
            try { stressThread.sleep(100+random.nextInt(3000)); }
            catch(InterruptedException e) { if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Caught InterruptedException "+e);}
        }
    }

    public void actionPerformed(ActionEvent e) {
        stop();
    }

    public static void main(String args[]) {
        Launcher.main(args);

        StressTester instance = new StressTester();
        JDialog stopDialog = new JDialog();
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(instance);
        stopDialog.getContentPane().add(stopButton);
        stopDialog.setSize(new Dimension(80, 60));
        stopDialog.setVisible(true);
    }
}
