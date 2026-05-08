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

import java.io.File;
import com.sun.xfile.*;
import java.io.IOException;

/**
 *  The BeanXFile class is the interface that makes an XFile object
 *  look like a File object.  This class is needed to support the
 *  UI of the JFileChooser which accesses file objects.
 *  Thus all the methods would call the corresponding XFile methods.
 *
 *  @see #XFile
 */
public class BeanXFile extends File {

    private XFile beanXF;
    
    /*
     * BeanXFile constructors which mirror the File I/O constructors.
     */
    public BeanXFile(String path) {
	super(path);
	beanXF = new XFile(path);
    }
    
    public BeanXFile(File dir, String name) {
	super(dir, name);
	    
	XFile parentXF = new XFile(dir.getAbsolutePath());
	beanXF = new XFile(parentXF, name);
    }
    
    /*
     * XFile Methods that can be accessed.
     */
    public String getPath() {
	String path = beanXF.getPath();

	// For nfs URLs, if the url is nfs://<server_name>, path is ""
	if (path == "")
	    path = beanXF.getAbsolutePath();

	return path;
    }

    public String getAbsolutePath() {
	return beanXF.getAbsolutePath();
    }

    public String getCanonicalPath() {
	try {
	    String path = beanXF.getCanonicalPath();
	    return path;
	} catch (IOException e) {
	    String path = beanXF.getAbsolutePath();
	    return path;
	}

    }
    
    public String getName() {
	String fname = beanXF.getName();
	if (fname == null)
	    return(beanXF.getAbsolutePath());
	else
	    return(fname);
    }

    public boolean renameTo(File dest) {
	XFile tmpFile = new XFile(dest.getAbsolutePath());
	return (beanXF.renameTo(tmpFile));
    }

    public String getParent() {
	return beanXF.getParent();
    }

    public boolean exists() {
	return beanXF.exists();
    }

    public boolean canWrite(){
	return beanXF.canWrite();
    }

    public boolean canRead() {
	return beanXF.canRead();
    }

    public boolean isFile() {
	return beanXF.isFile();
    }

    public boolean isDirectory() {
	return beanXF.isDirectory();
    }

    public boolean isAbsolute() {
       // For nfs urls: isAbsolute is always true 
       return beanXF.isAbsolute();
    }

    public boolean equals(Object obj) {
	/*
	 * Need to pass the XFile object to *.equals because
	 * it checks for instance of XFile
	 */
	XFile xf = new XFile(((File)obj).getAbsolutePath());
	return beanXF.equals(xf);
    }

    public long lastModified() {
	return beanXF.lastModified();
    }

    public long length() {
	return beanXF.length();
    }

    public boolean mkdir() {
	return beanXF.mkdir();
    }
    
    public boolean mkdirs() {
	return beanXF.mkdirs();
    }
    
    public String[] list() {
	return beanXF.list();
    }
    
    public String toString() {
	return beanXF.toString();
    }

    public boolean delete() {
	return beanXF.delete();
    }
    
}
