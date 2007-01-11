package com.mucommander.ui.dnd;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * This class represents a Transferable file set and is used for Drag and Drop transfers initiated by muCommander
 * (dragged from a muCommander UI component).
 *
 * <p>The actual file set data can be fetched using one of those 3 DataFlavors :
 * <ul>
 * <li>FileSetDataFlavor (as returned by {@link #getFileSetDataFlavor()}): data returned as a {@link com.mucommander.file.util.FileSet}.
 * This flavor is used for local file transfers (within the application) only. In particular, this DataFlavor cannot
 * be used to transfer data to the clipboard because the data (FileSet) cannot be serialized.
 * In this case, the {@link #setFileSetDataFlavorSupported(boolean)} method should be used to disable FileSet DataFlavor.
 * <li>DataFlavor.javaFileListFlavor : data returned as a java.util.Vector of <code>java.io.File</code> files.
 * This flavor is used for file transfers to and from external applications.
 * <li>DataFlavor.stringFlavor: data returned as a String representing file path(s) separated by \n characters.
 * This alternate flavor is used for file transfers to and from external applications that do not support
 * DataFlavor.javaFileListFlavor but text only (plain text editors for example)
 * </ul>
 *
 * @author Maxence Bernard
 */
public class TransferableFileSet implements Transferable {

    /** Transferred FileSet */
    private FileSet fileSet;

    /** Is FileSet DataFlavor supported ? */
    private boolean fileSetFlavorSupported = true;

    /** Is DataFlavor.javaFileListFlavor supported ? */
    private boolean javaFileListFlavorSupported = true;

    /** Is DataFlavor.stringFlavor supported ? */
    private boolean stringFlavorSupported = true;

    /** Does DataFlavor.stringFlavor transfer the files' full paths or filenames only ? */
    private boolean stringFlavourTransfersFilename = false;


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
     * Creates a new Transferable file set with support for all DataFlavors enabled.
     *
     * @param fileSet the files to be transferred
     */
    public TransferableFileSet(FileSet fileSet) {
        this.fileSet = fileSet;
    }


    /**
     * Sets whether or not the FileSet <code>DataFlavor</code> (as returned by {@link #getFileSetDataFlavor()}
     * should be supported by this Transferable (supported by default).
     */
    public void setFileSetDataFlavorSupported(boolean supported) {
        this.fileSetFlavorSupported = supported;
    }

    /**
     * Sets whether or not the <code>DataFlavor.javaFileListFlavor</code> should be supported by this Transferable
     * (supported by default).
     */
    public void setJavaFileListDataFlavorSupported(boolean supported) {
        this.javaFileListFlavorSupported = supported;
    }

    /**
     * Sets whether or not the <code>DataFlavor.stringFlavor</code> should be supported by this Transferable
     * (supported by default).
     */
    public void setStringDataFlavorSupported(boolean supported) {
        this.stringFlavorSupported = supported;
    }


    /**
     * Sets whether the files' full path or just the filenames should be returned when
     * {@link #getTransferData(java.awt.datatransfer.DataFlavor)} is called with <code>DataFlavor.stringFlavor</code>.
     * (*not* enabled by default)
     *
     * @param b if <code>true</code>, DataFlavor.stringFlavor returns filenames only, full file paths otherwise.
     */
    public void setStringDataFlavourTransfersFilename(boolean b) {
        this.stringFlavourTransfersFilename = b;
    }

    /**
     * Returns whether the files' full path or just the filenames will be returned when
     * {@link #getTransferData(java.awt.datatransfer.DataFlavor)} is called with <code>DataFlavor.stringFlavor</code>.
     * Returns <code>false</code> unless {@link #setStringDataFlavourTransfersFilename(boolean)} has been called.
     */
    public boolean getStringDataFlavourTransfersFilename() {
        return this.stringFlavourTransfersFilename;
    }


    /**
     * Returns an instance of the custom FileSet DataFlavor used to transfer files locally.
     */
    public static DataFlavor getFileSetDataFlavor() {
        return FILE_SET_DATA_FLAVOR;
    }


    /**
     * Returns the files contained by the specified Transferable as a {@link com.mucommander.file.util.FileSet},
     * or <code>null</code> if no file was present in the Transferable or if an error occurred.
     *
     * <p>3 types of dropped data flavors are supported and used in this order of priority:
     * <ul>
     * <li>FileSet: the local DataFlavor used when files are transferred from muCommander
     * <li>File list: used when files are transferred from an external application
     * <li>File paths: alternate flavor used when some text representing one or several file paths is dragged
     * from an external application
     * </ul>
     *
     * @param transferable a Transferable instance that contains the files to be retrieved
     * @return the files contained by the specified Transferable as a FileSet, or <code>null</code> if no file
     * was present or if an error occurred
     */
    public static FileSet getTransferFiles(Transferable transferable) {
        FileSet files;
        AbstractFile file;

        try {
            // FileSet DataFlavor
            if(transferable.isDataFlavorSupported(FILE_SET_DATA_FLAVOR)) {
                files = (FileSet)transferable.getTransferData(FILE_SET_DATA_FLAVOR);
            }
            // File list DataFlavor
            else if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List fileList = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);

                int nbFiles = fileList.size();
                files = new FileSet();
                for(int i=0; i<nbFiles; i++) {
                    file = FileFactory.getFile(((File)fileList.get(i)).getAbsolutePath());

                    if(file!=null)
                        files.add(file);
                }
            }
            // Text plain DataFlavor: assume that lines designate file paths
            else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                BufferedReader br = new BufferedReader(DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(transferable));

                // Read input line by line and try to create AbstractFile instances
                String path;
                files = new FileSet();
                while((path=br.readLine())!=null) {
                    // Try to create an AbstractFile instance, returned instance may be null
                    file = FileFactory.getFile(path);

                    // Safety precaution: if at least one line doesn't resolve as a file, stop reading
                    // and return null. This is to avoid any nasty effect that could arise if a random
                    // piece of text (let's say an email contents) was inadvertently pasted or dropped to muCommander.
                    if(file==null)
                        return null;

                    files.add(file);
                }

                br.close();
            }
            else {
                return null;
            }
        }
        catch(Exception e) {
            // Catch UnsupportedFlavorException, IOException
            if(Debug.ON) Debug.trace("Caught exception while processing transferable: "+e);

            return  null;
        }

        return files;
    }


    //////////////////////////
    // Transferable methods //
    //////////////////////////

    public DataFlavor[] getTransferDataFlavors() {
        Vector supportedDataFlavorsV = new Vector();

        if(fileSetFlavorSupported)
            supportedDataFlavorsV.add(FILE_SET_DATA_FLAVOR);

        if(javaFileListFlavorSupported)
            supportedDataFlavorsV.add(DataFlavor.javaFileListFlavor);

        if(stringFlavorSupported)
            supportedDataFlavorsV.add(DataFlavor.stringFlavor);

        DataFlavor supportedDataFlavors[] = new DataFlavor[supportedDataFlavorsV.size()];
        supportedDataFlavorsV.toArray(supportedDataFlavors);

        return supportedDataFlavors;
    }


    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        if(dataFlavor.equals(FILE_SET_DATA_FLAVOR))
            return fileSetFlavorSupported;
        else if(dataFlavor.equals(DataFlavor.javaFileListFlavor))
            return javaFileListFlavorSupported;
        else if(dataFlavor.equals(DataFlavor.stringFlavor))
            return stringFlavorSupported;

        return false;
    }


    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
//if(Debug.ON) Debug.trace("called, dataFlavor="+dataFlavor);
        int nbFiles = fileSet.size();

        // Return files stored in a FileSet instance (the one that was passed to the constructor)
        if(dataFlavor.equals(FILE_SET_DATA_FLAVOR) && fileSetFlavorSupported) {
            return fileSet;
        }
        // Return files stored in a java.util.Vector instance
        else if(dataFlavor.equals(DataFlavor.javaFileListFlavor) && javaFileListFlavorSupported) {
            Vector fileList = new Vector(nbFiles);

            for(int i=0; i<nbFiles; i++) {
                AbstractFile file = fileSet.fileAt(i);
//                // Add the file only if it is a local file that is not an archive's entry
//                if(file.getURL().getProtocol().equals(FileProtocols.FILE) && ((file instanceof FSFile) || (file instanceof AbstractArchiveFile && ((AbstractArchiveFile)file).getProxiedFile() instanceof FSFile)))
                fileList.add(new File(file.getAbsolutePath()));
            }
            return fileList;
        }
//        // Return an InputStream formatted in a specified Unicode charset that contains file paths separated by '\n' characters
//        else if(dataFlavor.equals(DataFlavor.getTextPlainUnicodeFlavor())) {
//            String mimeType = dataFlavor.getMimeType();
//            String charset = mimeType.substring(mimeType.indexOf("charset=")+8, mimeType.length());
//
//            StringBuffer sb = new StringBuffer();
//            for(int i=0; i<nbFiles; i++) {
//                sb.append(fileSet.fileAt(i).getAbsolutePath());
//                if(i!=nbFiles-1)
//                    sb.append('\n');
//            }
//
//            return new ByteArrayInputStream(sb.toString().getBytes(charset));
//        }
        else if(dataFlavor.equals(DataFlavor.stringFlavor) && stringFlavorSupported) {
            StringBuffer sb = new StringBuffer();
            AbstractFile file;
            for(int i=0; i<nbFiles; i++) {
                file = fileSet.fileAt(i);
                sb.append(stringFlavourTransfersFilename?file.getName():file.getAbsolutePath());
                if(i!=nbFiles-1)
                    sb.append('\n');
            }

            return sb.toString();
        }
        // Any other requested DataFlavor will thrown an UnsupportedFlavorException
        else {
            throw new UnsupportedFlavorException(dataFlavor);
        }
    }
}
