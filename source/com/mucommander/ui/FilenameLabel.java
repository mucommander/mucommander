package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.icon.FileIcons;

import javax.swing.*;

/**
 * A simple JLabel that displays the filename and file type icon of a given file.
 * The file icon appears on the left side of the label, before the filename.
 *
 * @author Maxence Bernard
 */
public class FilenameLabel extends JLabel {

    public FilenameLabel(AbstractFile file) {
        String filename = file.getName();
        setText(filename);
        setToolTipText(filename);

        setIcon(FileIcons.getFileIcon(file));
    }
}
