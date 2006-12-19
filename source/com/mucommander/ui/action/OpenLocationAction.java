package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FolderPanel;
import com.mucommander.file.FileURL;
import com.mucommander.file.AbstractFile;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bonjour.BonjourService;

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
    public OpenLocationAction(MainFrame mainFrame, FileURL url) {
        this(mainFrame, url, url.getStringRep(false));
    }

    /**
     * Creates a new OpenLocationAction instance using the provided FileURL and label.
     */
    public OpenLocationAction(MainFrame mainFrame, FileURL url, String label) {
        super(mainFrame, false);

        this.url = url;
        setLabel(label);
        setToolTipText(url.getStringRep(false));
    }


    /**
     * Creates a new OpenLocationAction instance using the filename of the provided AbstractFile 
     * as label.
     */
    public OpenLocationAction(MainFrame mainFrame, AbstractFile file) {
        this(mainFrame, file, file.getName());
    }

    /**
     * Creates a new OpenLocationAction instance using the provided AbstractFile and label.
     */
    public OpenLocationAction(MainFrame mainFrame, AbstractFile file, String label) {
        super(mainFrame, false);

        this.file = file;
        setLabel(label);
        setToolTipText(file.getAbsolutePath());
    }


    /**
     * Creates a new OpenLocationAction instance using the provided path as label.
     */
    public OpenLocationAction(MainFrame mainFrame, String path) {
        this(mainFrame, path, path);
    }

    /**
     * Creates a new OpenLocationAction instance using the provided path and label.
     */
    public OpenLocationAction(MainFrame mainFrame, String path, String label) {
        super(mainFrame, false);

        this.path = path;
        setLabel(label);
        setToolTipText(path);
    }


    /**
     * Convenience constructor, same effect as calling {@link #OpenLocationAction(MainFrame, String, String)} with
     * {@link Bookmark#getLocation()} and {@link Bookmark#getName()}.
     */
    public OpenLocationAction(MainFrame mainFrame, Bookmark bookmark) {
        this(mainFrame, bookmark.getLocation(), bookmark.getName());
    }


    /**
     * Convenience constructor, same effect as calling {@link #OpenLocationAction(MainFrame, FileURL, String)} with
     * {@link BonjourService#getURL()} and {@link BonjourService#getNameWithProtocol()} ()}.
     */
    public OpenLocationAction(MainFrame mainFrame, BonjourService bonjourService) {
        this(mainFrame, bonjourService.getURL(), bonjourService.getNameWithProtocol());
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
