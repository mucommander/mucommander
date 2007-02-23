package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public class ChangeFileAttributesJob extends FileJob {

    private boolean recurseOnDirectories;

    private int permissions = -1;
    private long date = -1;


    public ChangeFileAttributesJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, int permissions, boolean recurseOnDirectories) {
        super(progressDialog, mainFrame, files);

        this.permissions = permissions;
        this.recurseOnDirectories = recurseOnDirectories;
    }


    public ChangeFileAttributesJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet files, long date, boolean recurseOnDirectories) {
        super(progressDialog, mainFrame, files);

        this.date = date;
        this.recurseOnDirectories = recurseOnDirectories;
    }


    ////////////////////////////
    // FileJob implementation //
    ////////////////////////////

    protected boolean processFile(AbstractFile file, Object recurseParams) {
        // Stop if interrupted
        if(getState()==INTERRUPTED)
            return false;

        if(recurseOnDirectories && file.isDirectory()) {
            do {		// Loop for retries
                try {
                    AbstractFile children[] = file.ls();
                    int nbChildren = children.length;

                    for(int i=0; i<nbChildren && getState()!=INTERRUPTED; i++) {
                        // Notify job that we're starting to process this file (needed for recursive calls to processFile)
                        nextFile(children[i]);
                        processFile(children[i], null);
                    }

                    break;
                }
                catch(IOException e) {
                    // Unable to open source file
                    int ret = showErrorDialog("", Translator.get("cannot_read_folder", file.getName()));
                    // Retry loops
                    if(ret==RETRY_ACTION)
                        continue;
                    // Cancel, skip or close dialog return false
                    return false;
                }
            }
            while(true);
        }

        if(permissions!=-1)
            return file.setPermissions(permissions);

//        if(date!=-1)
        return file.changeDate(date);
    }

    public String getStatusString() {
        return Translator.get("progress_dialog.processing_file", getCurrentFileInfo());
    }

    // This job modifies the FileSet's base folder and potentially its subfolders
    protected boolean hasFolderChanged(AbstractFile folder) {
        return files.getBaseFolder().isParentOf(folder);
    }
}
