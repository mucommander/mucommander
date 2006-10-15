package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

/**
 * Filter used to filter out System folders.
 *
 * <p>At the moment, this filter only supports Mac OS X top-level system folders (those hidden by Finder)
 * and thus this filter should only be used under Mac OS X.
 *
 * @author Maxence Bernard
 */
public class SystemFoldersFilter extends FileFilter {

    /**
     * Top-level Mac OS X system folders hidden by Finder.
     */
    private final static String SYSTEM_FOLDERS[]= {
        // Mac OS X system folders
        "/.Trashes",
        "/.vol",
        "/dev",
        "/automount",
        "/bin",
        "/cores",
        "/etc",
        "/lost+found",    
        "/Network",
        "/private",
        "/sbin",
        "/tmp",
        "/usr",
        "/var",
//        "/Volumes",
        "/mach.sym",
        "/mach_kernel",
        "/mach",
        "/Desktop DB",
        "/Desktop DF",
        "/.hotfiles.btree",
        "/.Spotlight-V100",
        "/.hidden",     // Used by Mac OS X up to 10.3, not in 10.4
        System.getProperty("user.home")+"/.Trash",  // User trash folder
        // Mac OS 9 system folders 
        "/AppleShare PDS",
        "/Cleanup At Startup",
        "/Desktop Folder",
        "/Network Trash Folder",
        "/Shutdown Check",
        "/Temporary Items",
        System.getProperty("user.home")+"/Temporary Items",  // User trash folder
        "/TheFindByContentFolder",
        "/TheVolumeSettingsFolder",
        "/Trash",
        "/VM Storage"
    };

    public boolean accept(AbstractFile file) {
        String path = file.getAbsolutePath(false);

        int nbSystemFolders = SYSTEM_FOLDERS.length;
        for(int i=0; i<nbSystemFolders; i++)
            if(path.equals(SYSTEM_FOLDERS[i]))
                return false;

        return true;
    }
}
