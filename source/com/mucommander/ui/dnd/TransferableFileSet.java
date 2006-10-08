package com.mucommander.ui.dnd;

import com.mucommander.file.FileSet;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;
import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.Debug;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.util.Vector;

/**
 * This class represents a Transferable file set and is used for Drag and Drop transfers initiated by muCommander
 * (dragged from a muCommander UI component).
 *
 * <p>The actual file set data can be fetched using one of those 3 DataFlavors :
 * <ul>
 * <li>FileSetDataFlavor (as returned by {@link #getFileSetDataFlavor()}): data returned as a {@link com.mucommander.file.FileSet}.
 * This flavor is used for local file transfers (within the application) only.
 * <li>DataFlavor.javaFileListFlavor : data returned as a java.util.Vector. This flavor is used for transfers to and from
 * external applications.
 * <li>Unicode text DataFlavor (as returned by DataFlavor.getTextPlainUnicodeFlavor()) : data returned as a unicode-formatted
 * InputStream, the exact encoding is determined dynamically. This alternate flavor is used for transfers to and from
 * external applications that do not support DataFlavor.javaFileListFlavor but text only (plain text editors for example)
 * </ul>
 *
 * @author Maxence Bernard
 */
public class TransferableFileSet implements Transferable {

    /** Transferred FileSet */
    private FileSet fileSet;

    /** Custom FileSet DataFlavor used for local transfers */
    private static DataFlavor FILE_SET_DATA_FLAVOR;

    static {
        // Create a single custom DataFlavor instance that designates the FileSet class to transfer data
        try {
            FILE_SET_DATA_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class="+FileSet.class.getName());
        }
        catch(ClassNotFoundException e) {
            // That should never happen
            if(Debug.ON) Debug.trace("Error: FileSet DataFlavor could not be instanciated "+e);
        }
    }


    /**
     * Creates a new Transferable file set.

     * @param fileSet the files to be transferred
     */
    public TransferableFileSet(FileSet fileSet) {
        this.fileSet = fileSet;
    }


    /**
     * Returns an instance of the custom FileSet DataFlavor used to transfer files locally.
     */
    public static DataFlavor getFileSetDataFlavor() {
        return FILE_SET_DATA_FLAVOR;
    }

    //////////////////////////
    // Transferable methods //
    //////////////////////////

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FILE_SET_DATA_FLAVOR, DataFlavor.javaFileListFlavor, DataFlavor.getTextPlainUnicodeFlavor()};
    }

    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return dataFlavor.equals(FILE_SET_DATA_FLAVOR)
            || dataFlavor.equals(DataFlavor.javaFileListFlavor)
            || dataFlavor.equals(DataFlavor.getTextPlainUnicodeFlavor());
    }

    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
        int nbFiles = fileSet.size();

        // Return files stored in a FileSet instance (the one that was passed to the constructor)
        if(dataFlavor.equals(FILE_SET_DATA_FLAVOR)) {
            return fileSet;
        }
        // Return files stored in a java.util.Vector instance
        else if(dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
            Vector fileList = new Vector(nbFiles);

            for(int i=0; i<nbFiles; i++) {
                AbstractFile file = fileSet.fileAt(i);
                // Add the file only if it is a local file that is not an archive's entry
                if(file.getURL().getProtocol().equals("file") && ((file instanceof FSFile) || (file instanceof AbstractArchiveFile && ((AbstractArchiveFile)file).getProxiedFile() instanceof FSFile)))
                    fileList.add(new File(file.getAbsolutePath()));
            }
            return fileList;
        }
        // Return an InputStream formatted in a specified Unicode charset that contains file paths separated by '\n' characters
        else if(dataFlavor.equals(DataFlavor.getTextPlainUnicodeFlavor())) {
            String mimeType = dataFlavor.getMimeType();
            String charset = mimeType.substring(mimeType.indexOf("charset=")+8, mimeType.length());

            StringBuffer sb = new StringBuffer();
            for(int i=0; i<nbFiles; i++) {
                sb.append(fileSet.fileAt(i).getAbsolutePath());
                if(i!=nbFiles-1)
                    sb.append('\n');
            }

            return new ByteArrayInputStream(sb.toString().getBytes(charset));
        }
        // Any other requested DataFlavor will thrown an UnsupportedFlavorException
        else {
            throw new UnsupportedFlavorException(dataFlavor);
        }
    }
}
