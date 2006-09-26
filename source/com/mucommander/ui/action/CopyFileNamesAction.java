package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.file.FileSet;
import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.*;

/**
 * This action copies the filename(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyFileNamesAction extends SelectedFilesAction {

    public CopyFileNamesAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        copyFilenamesToClipboard(mainFrame, false);
    }

    /**
     * Copies selected/marked filenames or file paths (depending on the specified parameter)
     * to the clipboard.
     *
     * @param mainFrame current mainFrame
     * @param copyFullPath if <code>true</code>, full paths will be copied instead of names
     */
    public static void copyFilenamesToClipboard(MainFrame mainFrame, boolean copyFullPath) {
        // Get selected file OR marked files (if any), returned FileSet should never be empty
        FileSet selectedFiles = mainFrame.getLastActiveTable().getSelectedFiles();
        int nbFiles = selectedFiles.size();
        if(nbFiles==0)
            return;

        // Iterate on all selected/marked files
        StringBuffer clipboardText = new StringBuffer();
        // If shift down, copy full paths instead of names
        AbstractFile file;
        for(int i=0; i<nbFiles; i++) {
            file = selectedFiles.fileAt(i);
            clipboardText.append(copyFullPath?file.getAbsolutePath():file.getName());
            if(i!=nbFiles-1)
                clipboardText.append('\n');
        }

        // Set clipboard's content
        StringSelection stringSelection = new StringSelection(clipboardText.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
    }
}