/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dnd;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.util.FileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *   <li>FileSetDataFlavor (as returned by {@link #getFileSetDataFlavor()}): data returned as a {@link com.mucommander.commons.file.util.FileSet}.
 * This flavor is used for local file transfers (within the application) only. In particular, this DataFlavor cannot
 * be used to transfer data to the clipboard because the data (FileSet) cannot be serialized.
 * In this case, the {@link #setFileSetDataFlavorSupported(boolean)} method should be used to disable FileSet DataFlavor.
 *   </li>
 *   <li>DataFlavor.javaFileListFlavor : data returned as a java.util.Vector of <code>java.io.File</code> files.
 * This flavor is used for file transfers to and from external applications.
 *   </li>
 *   <li>text/uri-list (RFC 2483): an alternative flavor supported by Gnome and KDE where the data is returned as
 * a String containing file URIs separated by '\r\n'. This flavor is as of today the only one supported by GNOME and
 * KDE. See bug #45 and http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4899516 .
 *   </li>
 *   <li>DataFlavor.stringFlavor: data returned as a String containing files paths separated by \n characters.
 * This other alternative flavor is used for file transfers to and from external applications that do not support
 * either of DataFlavor.javaFileListFlavor and text/uri-list but text only (plain text editors for example).
 *   </li>
 * </ul>
 *
 * @author Maxence Bernard, Xavi Miró
 */
public class TransferableFileSet implements Transferable {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransferableFileSet.class);
	
    /** Transferred FileSet */
    private FileSet fileSet;

    /** Is FileSet DataFlavor supported ? */
    private boolean fileSetFlavorSupported = true;

    /** Is DataFlavor.javaFileListFlavor supported ? */
    private boolean javaFileListFlavorSupported = true;

    /** Is DataFlavor.stringFlavor supported ? */
    private boolean stringFlavorSupported = true;

    /** Is text/uri-list (RFC 2483) flavor supported ? */
    private boolean textUriFlavorSupported = true;

    /** Does DataFlavor.stringFlavor transfer the files' full paths or filenames only ? */
    private boolean stringFlavourTransfersFilename = false;

    /** Does DataFlavor.stringFlavor transfer the files' filenames with extension or without ? */
    private boolean stringFlavourTransfersFileBaseName = false;
    
	/** DataFlavor used for GNOME/KDE transfers */
    private static DataFlavor TEXT_URI_FLAVOR;

    /** Custom FileSet DataFlavor used for local transfers */
    private static DataFlavor FILE_SET_DATA_FLAVOR;

    static {
        // Create a single custom DataFlavor instance that designates the FileSet class to transfer data
        try {
            FILE_SET_DATA_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType+";class="+FileSet.class.getName());
	        TEXT_URI_FLAVOR =  new DataFlavor("text/uri-list;class="+String.class.getName());
        }
        catch(ClassNotFoundException e) {
            // That should never happen
            LOGGER.debug("FileSet DataFlavor could not be instantiated", e);
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
     *
     * @param supported <code>true</code> to support the flavor
     */
    public void setFileSetDataFlavorSupported(boolean supported) {
        this.fileSetFlavorSupported = supported;
    }

    /**
     * Sets whether or not the <code>DataFlavor.javaFileListFlavor</code> should be supported by this Transferable
     * (supported by default).
     *
     * @param supported <code>true</code> to support the flavor
     */
    public void setJavaFileListDataFlavorSupported(boolean supported) {
        this.javaFileListFlavorSupported = supported;
    }

    /**
     * Sets whether or not the <code>DataFlavor.stringFlavor</code> should be supported by this Transferable
     * (supported by default).
     *
     * @param supported <code>true</code> to support the flavor
     */
    public void setStringDataFlavorSupported(boolean supported) {
        this.stringFlavorSupported = supported;
    }

    /**
     * Sets whether or not the <code>text/uri-list</code> (RFC 2483) should be supported by this Transferable
     * (supported by default).
     *
     * @param supported <code>true</code> to support the flavor
     */
    public void setTextUriFlavorSupported(boolean supported) {
        this.textUriFlavorSupported = supported;
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
     *
     * @return whether the files' full path or just the filenames will be returned when
     * {@link #getTransferData(java.awt.datatransfer.DataFlavor)} is called with <code>DataFlavor.stringFlavor</code>
     */
    public boolean getStringDataFlavourTransfersFilename() {
        return this.stringFlavourTransfersFilename;
    }

    /**
     * Sets whether the files' base name (without file extension) should be returned when
     * {@link #getTransferData(java.awt.datatransfer.DataFlavor)} is called with <code>DataFlavor.stringFlavor</code>.
     * (*not* enabled by default)
     *
     * @param b if <code>true</code>, DataFlavor.stringFlavor returns filenames without extension, full file name otherwise.
     */
	public void setStringDataFlavourTransfersFileBaseName(boolean b) {
		this.stringFlavourTransfersFileBaseName = b;
	}

    /**
     * Returns whether the files' base name (without file extension) should be returned when
     * {@link #getTransferData(java.awt.datatransfer.DataFlavor)} is called with <code>DataFlavor.stringFlavor</code>.
     * Returns <code>false</code> unless {@link #setStringDataFlavourTransfersFileBaseName(boolean)} has been called.
     *
     * @return whether the files'  base name (without file extension) should be returned when
     * {@link #getTransferData(java.awt.datatransfer.DataFlavor)} is called with <code>DataFlavor.stringFlavor</code>
     */
    public boolean getStringDataFlavourTransfersFileBaseName() {
		return stringFlavourTransfersFileBaseName;
	}

    /**
     * Returns an instance of the custom FileSet DataFlavor used to transfer files locally.
     *
     * @return an instance of the custom FileSet DataFlavor used to transfer files locally
     */
    public static DataFlavor getFileSetDataFlavor() {
        return FILE_SET_DATA_FLAVOR;
    }


    /**
     * Returns the files contained by the specified Transferable as a {@link com.mucommander.commons.file.util.FileSet},
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
                List<File> fileList = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);

                int nbFiles = fileList.size();
                files = new FileSet();
                for(int i=0; i<nbFiles; i++) {
                    file = FileFactory.getFile(fileList.get(i).getAbsolutePath());

                    if(file!=null)
                        files.add(file);
                }
            }
            // Text plain DataFlavor: assume that lines designate file paths
            else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                BufferedReader br;

                br = null;
                try {
                    br = new BufferedReader(DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(transferable));

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
                }
                // Documentation is not explicit on whether DataFlavor streams need to be closed, we might as well
                // do so just to be sure.
                finally {
                    if(br != null) {
                        try {br.close();}
                        catch(IOException e) {}
                    }
                }
            }
            else {
                return null;
            }
        }
        catch(Exception e) {
            // Catch UnsupportedFlavorException, IOException
            LOGGER.debug("Caught exception while processing transferable", e);

            return  null;
        }

        return files;
    }


    //////////////////////////
    // Transferable methods //
    //////////////////////////

    public DataFlavor[] getTransferDataFlavors() {
        List<DataFlavor> supportedDataFlavorsV = new Vector<DataFlavor>();

        if(fileSetFlavorSupported)
            supportedDataFlavorsV.add(FILE_SET_DATA_FLAVOR);

        if(javaFileListFlavorSupported)
            supportedDataFlavorsV.add(DataFlavor.javaFileListFlavor);

        if(stringFlavorSupported)
            supportedDataFlavorsV.add(DataFlavor.stringFlavor);

        if(textUriFlavorSupported)
            supportedDataFlavorsV.add(TEXT_URI_FLAVOR);

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
        else if(dataFlavor.equals(TEXT_URI_FLAVOR))
            return textUriFlavorSupported;

        return false;
    }


    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
        int nbFiles = fileSet.size();

        // Return files stored in a FileSet instance (the one that was passed to the constructor)
        if(dataFlavor.equals(FILE_SET_DATA_FLAVOR) && fileSetFlavorSupported) {
            return fileSet;
        }
        // Return files stored in a java.util.Vector instance
        else if(dataFlavor.equals(DataFlavor.javaFileListFlavor) && javaFileListFlavorSupported) {
            List<File> fileList = new Vector<File>(nbFiles);

            for(int i=0; i<nbFiles; i++) {
                AbstractFile file = fileSet.elementAt(i);
                fileList.add(new File(file.getAbsolutePath()));
            }
            return fileList;
        }
//        // Return an InputStream formatted in a specified Unicode charset that contains file paths separated by '\n' characters
//        else if(dataFlavor.equals(DataFlavor.getTextPlainUnicodeFlavor())) {
//            String mimeType = dataFlavor.getMimeType();
//            String charset = mimeType.substring(mimeType.indexOf("charset=")+8, mimeType.length());
//
//            StringBuilder sb = new StringBuilder();
//            for(int i=0; i<nbFiles; i++) {
//                sb.append(fileSet.fileAt(i).getAbsolutePath());
//                if(i!=nbFiles-1)
//                    sb.append('\n');
//            }
//
//            return new ByteArrayInputStream(sb.toString().getBytes(charset));
//        }
        // Return a String with file paths or names
        else if(dataFlavor.equals(DataFlavor.stringFlavor) && stringFlavorSupported) {
            StringBuilder sb = new StringBuilder();
            AbstractFile file;
            for(int i=0; i<nbFiles; i++) {
                file = fileSet.elementAt(i);
                // Check if to return string with filenames
                if (stringFlavourTransfersFilename) {
                	// Append just base name or file name with extension
                	sb.append(stringFlavourTransfersFileBaseName ? file.getBaseName() : file.getName());
                } else {
                	sb.append(file.getAbsolutePath());
                }
                                	
                if(i!=nbFiles-1)
                    sb.append('\n');
            }

            return sb.toString();
        }
        // Return a String with file URLs, as specified by RFC 2483
        else if(dataFlavor.equals(TEXT_URI_FLAVOR) && textUriFlavorSupported) {
            StringBuilder sb = new StringBuilder();
            AbstractFile file;
            for(int i=0; i<nbFiles; i++) {
                file = fileSet.elementAt(i);
                String url = file.getURL().getScheme().equals(FileProtocols.FILE)
                    // Use java.io.File.toURI() for local files (e.g. file:/mnt/music), this is the format expected by
                    // Gnome/Nautilus.
                    ?new File(file.getAbsolutePath()).toURI().toString()
                    // Use standard URL format (e.g. smb://host/share/file) for other file types
                    :file.getAbsolutePath();

                sb.append(url);
                if(i!=nbFiles-1){
                    sb.append("\r\n");
                }
            }

            return sb.toString();
        }
        // Any other requested DataFlavor will thrown an UnsupportedFlavorException
        else {
            throw new UnsupportedFlavorException(dataFlavor);
        }
    }
}
