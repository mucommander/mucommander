
package com.mucommander;

import com.mucommander.ui.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;

import java.util.Random;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


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
		MainFrame mainFrame = WindowManager.getInstance().getCurrentMainFrame();
		FolderPanel folderPanel1 = mainFrame.getFolderPanel1();
		FolderPanel folderPanel2 = mainFrame.getFolderPanel2();
		
		while(stressThread!=null) {
			FolderPanel folderPanel = random.nextInt(2)==0?folderPanel1:folderPanel2;
			AbstractFile currentFolder = folderPanel.getCurrentFolder();
			AbstractFile parentFolder = currentFolder.getParent();
			try {
				AbstractFile children[] = currentFolder.ls();
				// 1 in 3 chance to go up if folder has children
				if(children.length==0 || (random.nextInt(3)==0 && parentFolder!=null)) {
					folderPanel.trySetCurrentFolder(parentFolder, true);
				}
				else {
					AbstractFile randomChild = children[random.nextInt(children.length)];
					if(!randomChild.isBrowsable())
						continue;
					// Try to ls() in RandomChild to trigger an IOException if folder is not readable
					// so that no error dialog pops up when calling trySetCurrentFolder()
					randomChild.ls();
					folderPanel.getFileTable().selectFile(randomChild);
					folderPanel.trySetCurrentFolder(randomChild, true);
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