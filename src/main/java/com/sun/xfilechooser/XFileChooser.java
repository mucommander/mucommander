/*
 * Copyright (c) 1998, 2007 Sun Microsystems, Inc. 
 * All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
 * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.xfilechooser;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.beans.*;

import java.io.File;
import com.sun.xfile.*;

/**
 * XFileChooser is an extension of the JFileChooser.  It
 * provides a simple mechanism for the user to choose a file locally
 * and remotely via NFS URLs.  Instead of returning a File Object
 * which is done in JFileChooser it returns and XFile Object.
 * The XFile object allows access to local and remote files.
 *
 * @beaninfo
 *   attribute: isContainer false
 * @see #javax.swing.JFileChooser
 * @version 1.0 
 * @author Agnes Jacob
 *
 */
public class XFileChooser extends JFileChooser implements PropertyChangeListener {
    private XFile currentXDirectory = new XFile(System.getProperty("user.home"));
    private XFile selectedXFile = null;
    
    private XFile[] selectedXFiles = null;
    private XFileInputStream selectedXFileInputStream = null;
    private XFileOutputStream selectedXFileOutputStream = null;

    /*    private File currentDirectory; */
    
    /** Identifies user's directory change. */
    public static final String XDIRECTORY_CHANGED_PROPERTY = "XdirectoryChanged";
    /** Identifes change in user's single-file selection. */
    public static final String SELECTED_XFILE_CHANGED_PROPERTY = "SelectedXFileChangedProperty";
    /** Identifes change in user's multiple-file selection. */
    public static final String SELECTED_XFILES_CHANGED_PROPERTY = "SelectedXFilesChangedProperty";

    /*
     * XFileChooser Constructors
     */

    /**
     * Creates a XFileChooser pointing to the user's home directory.
     * Initializes some of the private variables needed for bean's editor.
     */
    public XFileChooser() {
	super(XFileSystemView.getFileSystemView());
	setApproveButtonToolTipText("");
	setApproveButtonText("");
	setApproveButtonMnemonic(0);
	setDialogTitle("");
	setFileSelectionMode(FILES_ONLY);
	addPropertyChangeListener(this);
    }
    
    /**
     * Creates a XFileChooser using the given path. Passing in a null
     * string causes the file chooser to point to the users home directory.
     *
     * @param currentDirectoryPath  a String giving the path to a file or directory
     */
    public XFileChooser(String currentDirectoryPath) {
	super(currentDirectoryPath, XFileSystemView.getFileSystemView());
	addPropertyChangeListener(this);
    }
    
    /**
     * Creates a XFileChooser using the given XFile as the path. Passing
     * in a null file causes the file chooser to point to the users's
     * home directory.
     *
     * @param currentDirectory  a XFile object specifying the path to a file 
     *                   or directory
     */
    public XFileChooser(XFile currentDirectory) {
	this((currentDirectory == null) ? ((String) null) : currentDirectory.getAbsolutePath());
	addPropertyChangeListener(this);
    }

    /**
     * This method gets called when certain bound properties has changed
     * on the associated JFileChooser. The properties it will
     * listen to include: DIRECTORY_CHANGED_PROPERTY,
     * SELECTED_FILE_CHANGED_PROPERTY, SELECTED_FILES_CHANGED_PROPERTY.
     * This method is needed to create the corresponding XFile for
     * the File object of the selected file or directory.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
	Object obj = e.getNewValue();
	if ((obj == null) || (! (obj instanceof BeanXFile)))
            return;

	BeanXFile bf = (BeanXFile) obj;
	
        if(prop == JFileChooser.DIRECTORY_CHANGED_PROPERTY) {
	    XFile oldValue = this.currentXDirectory;
			       
	    this.currentXDirectory = new XFile(((BeanXFile) obj).getAbsolutePath()); 
	    firePropertyChange(XDIRECTORY_CHANGED_PROPERTY, oldValue, 
			       this.currentXDirectory);

	} else if (prop == JFileChooser.SELECTED_FILE_CHANGED_PROPERTY) {
	    XFile oldValue = this.selectedXFile;
	    this.selectedXFile = new XFile(((File) obj).getAbsolutePath());
	    firePropertyChange(SELECTED_XFILES_CHANGED_PROPERTY, oldValue,
			       this.selectedXFile);
	} else if (prop == JFileChooser.SELECTED_FILES_CHANGED_PROPERTY) {
	    XFile[] oldValue = this.selectedXFiles;
	    File[] sfiles = (File[]) obj;
	    
	    for (int i=0; i < sfiles.length; i++)
		this.selectedXFiles[i] = new XFile(sfiles[i].getAbsolutePath());
	    firePropertyChange(SELECTED_XFILES_CHANGED_PROPERTY, oldValue, 
			       this.selectedXFiles);
	}

    }

    /**
     * Returns XFile Object of the current directory.
     *
     * @return the XFile object of the current directory
     * @see #setCurrentXDirectory    */
    public XFile getCurrentXDirectory() {
	return(currentXDirectory);
    }

    /**
     * Sets the current directory. Passing in null sets the filechooser
     * to point to the users's home directory.
     *
     * If the file passed in as currentDirectory is not a directory, the
     * parent of the file will be used as the currentDirectory. If the
     * parent is not traversable, then it will walk up the parent tree
     * until it finds a traversable direcotry, or hits the root of the
     * file system.
     *
     * @beaninfo
     *   preferred: true
     *       bound: true
     * description: the directory that the FileChooser is showing files of
     *
     * @param currentDirectory the XFile object of the
     *		current directory to point to.
     * @see #getCurrentXDirectory
     */
    public void setCurrentXDirectory(XFile currentDirectory) {
	if (currentDirectory != null) {
	    this.currentXDirectory = currentDirectory;
	    setCurrentDirectory(new BeanXFile(currentDirectory.getAbsolutePath()));
	} else 
	    setCurrentDirectory(null);
    }


    /**
     * Returns the XFile object of the selected file. This can be set
     * either by the programmer via setSelectedXFile() or by a user
     * action, such as either typing the filename int the UI or selecting the
     * file from a list in the UI.
     *
     * @see #setSelectedXFile
     * @return the XFile object of the selected file
     */
    public XFile getSelectedXFile() {
	return (selectedXFile);
    }

    /**
     * Sets the XFile object of the selected file.
     * If the file's parent directory is
     * not the current directory, it changed the current directory
     * to be the files parent directory.  This just calls the
     * setSelectedFile() of JFileChooser, passing it the corresponding
     * File object of the selected file.
     *
     * @beaninfo
     *   preferred: true
     *       bound: true
     *
     * @see #getSelectedXFile
     * @param selectedFile the XFile object of the selected file
     */
    public void setSelectedXFile(XFile selectedFile) {
	this.selectedXFile = selectedFile;
	if (selectedFile != null)
	    setSelectedFile(new BeanXFile(selectedFile.getAbsolutePath()));
	else
	    setSelectedFile(null);
    }

    /**
     * Returns a list of selected files if the filechooser is
     * set to allow multi-selection.
     *
     * @see #setSelectedXFiles
     * @return list of XFile objects of the selected files
     */
    public XFile[] getSelectedXFiles() {	
	
	if (selectedXFiles == null) {
	    return new XFile[0];
	} else {
	    return (XFile[])selectedXFiles;
	}
    }

    /**
     * Sets the list of selected files if the filechooser is
     * set to allow multi-selection.
     *
     * @beaninfo
     *       bound: true
     * description: the list of XFile objects of selected files if the chooser is in multi-selection mode
     */
    public void setSelectedXFiles(XFile[] selectedFiles) {
	this.selectedXFiles = selectedFiles;

	if (selectedFiles != null) {
	    BeanXFile[] sfiles = new BeanXFile[selectedFiles.length];

	    for (int i = 0; i < selectedFiles.length; i++) {
		sfiles[i] = new BeanXFile(selectedFiles[i].getAbsolutePath());
	    }
	
	    setSelectedFiles(sfiles);
         } else
	    setSelectedFiles(null);
	    
    }

    /**
     * Returns the XFileInputStream object of the selected file
     *
     * @see #setSelectedXFile
     * @return XFileInputStream of the selected input file
     */
    public XFileInputStream getSelectedXFileInputStream(){
	XFile f = getSelectedXFile();
	try {
	    selectedXFileInputStream = new XFileInputStream(f);
	} catch (Exception e) {
	    selectedXFileInputStream = null;
	}
	return(selectedXFileInputStream);
    }
    
    /**
     * Returns the XFileOutputStream object of the selected file
     *
     * @see #setSelectedXFile
     * @return XFileOutputStream of the selected output file
     */
    public XFileOutputStream getSelectedXFileOutputStream() {
	XFile f = getSelectedXFile();
	try {
	    selectedXFileOutputStream = new XFileOutputStream(f);
	} catch (Exception e) {
	    selectedXFileOutputStream = null;
	}
	return selectedXFileOutputStream;
    }


    /**
     * Make sure that the specified file is viewable, and
     * not hidden.
     *
     * @param f  an XFile object
     */
    public void ensureFileIsVisible(XFile f) {
	if (f != null)
	    ensureFileIsVisible(new File(f.getAbsolutePath()));
    }





    /**
     *  Used only for testing of bean
     */
    public static void main(String args[]) {
	XFileChooser xfb = new XFileChooser();
	xfb.showDialog(new JFrame(), "ajacob");

    }
    

}



