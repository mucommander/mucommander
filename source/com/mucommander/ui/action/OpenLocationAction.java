package com.mucommander.ui.action;

import com.mucommander.bonjour.BonjourService;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action opens a specified location in the current active FileTable. The location can be designated by either a
 * FileURL, path, or AbstractFile.
 *
 * @author Maxence Bernard
 */
public class OpenLocationAction extends MucoAction {

    private FileURL url;
    private AbstractFile file;
    private String path;


    /**
     * Creates a new OpenLocationAction instance using the provided url's string representation
     * (with credentials stripped out) as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, FileURL url) {
        this(mainFrame, properties, url, url.getProtocol().equals(FileProtocols.FILE)?url.getPath():url.toString(false));
    }

    /**
     * Creates a new OpenLocationAction instance using the provided FileURL and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, FileURL url, String label) {
        super(mainFrame, properties, false);

        this.url = url;
        setLabel(label);
        setToolTipText(url.getProtocol().equals(FileProtocols.FILE)?url.getPath():url.toString(false));
    }


    /**
     * Creates a new OpenLocationAction instance using the filename of the provided AbstractFile 
     * as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, AbstractFile file) {
        this(mainFrame, properties, file, file.getName());
    }

    /**
     * Creates a new OpenLocationAction instance using the provided AbstractFile and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, AbstractFile file, String label) {
        super(mainFrame, properties, false);

        this.file = file;
        setLabel(label);
        setToolTipText(file.getAbsolutePath());
    }


    /**
     * Creates a new OpenLocationAction instance using the provided path as label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, String path) {
        this(mainFrame, properties, path, path);
    }

    /**
     * Creates a new OpenLocationAction instance using the provided path and label.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, String path, String label) {
        super(mainFrame, properties, false);

        this.path = path;
        setLabel(label);
        setToolTipText(path);
    }


    /**
     * Convenience constructor, same effect as calling {@link #OpenLocationAction(MainFrame, Hashtable, String, String)} with
     * {@link Bookmark#getLocation()} and {@link Bookmark#getName()}.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, Bookmark bookmark) {
        this(mainFrame, properties, bookmark.getLocation(), bookmark.getName());
    }


    /**
     * Convenience constructor, same effect as calling {@link #OpenLocationAction(MainFrame, Hashtable, FileURL, String)} with
     * {@link BonjourService#getURL()} and {@link BonjourService#getNameWithProtocol()} ()}.
     */
    public OpenLocationAction(MainFrame mainFrame, Hashtable properties, BonjourService bonjourService) {
        this(mainFrame, properties, bonjourService.getURL(), bonjourService.getNameWithProtocol());
    }


    public void performAction() {
        FolderPanel folderPanel = mainFrame.getActiveTable().getFolderPanel();
        if(url!=null) {
            folderPanel.tryChangeCurrentFolder(url);
        }
        else if(file!=null) {
            folderPanel.tryChangeCurrentFolder(file);
        }
        else if(path!=null) {
            folderPanel.tryChangeCurrentFolder(path);
        }
    }
}
