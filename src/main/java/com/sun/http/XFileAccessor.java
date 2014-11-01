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

package com.sun.http;

import com.sun.xfile.*;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;

/**
 * The XFileAccessor interface is implemented by filesystems that
 * need to be accessed via the XFile API.
 *
 * @author  Brent Callaghan
 * @version 1.0, 04/08/98
 * @see     com.sun.xfile.XFile
 */
public class XFileAccessor implements com.sun.xfile.XFileAccessor {

    XFile xf;
    URL url;
    URLConnection urlConn;
    InputStream  iStream;
    OutputStream oStream;
    long fp;	// file pointer

    /**
     * Open this file object
     *
     * @param xf the XFile for this file
     * @param serial   true if serial access
     * @param readOnly true if read only
     */
    public boolean open(XFile xf, boolean serial, boolean readOnly) {

        if (! serial)
            return false;

        this.xf = xf;

        try {
            url = new URL(xf.toString());
            urlConn = url.openConnection();
    
            urlConn.setDoInput(readOnly);
            urlConn.setDoOutput(! readOnly);
    
    	    urlConn.connect();

            return true;

        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Get the XFile for this Accessor
     *
     * @return XFile for this object
     */
    public XFile getXFile() {
        return xf;
    }

    /**
     * Tests if this XFileAccessor object exists. 
     *
     * @return <code>true</code> if the file specified by this object
     *         exists; <code>false</code> otherwise.
     */
    public boolean exists() {
        try {
            if (iStream == null)
                iStream = urlConn.getInputStream();

            return true;

        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Tests if the application can write to this file. 
     *
     * @return <code>true</code> if the application is allowed to
     *         write to a file whose name is specified by this
     *         object; <code>false</code> otherwise.
     */
    public boolean canWrite() {
        try {
            if (oStream == null)
                oStream = urlConn.getOutputStream();

             return true;

        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Tests if the application can read from the specified file. 
     *
     * @return <code>true</code> if the file specified by this
     *         object exists and the application can read the file;
     *         <code>false</code> otherwise.
     */
    public boolean canRead() {
        return exists();
    }

    /**
     * Tests if the file represented by this
     * object is a "normal" file. 
     * <p>
     * A file is "normal" if it is not a directory and, in 
     * addition, satisfies other system-dependent criteria. Any 
     * non-directory file created by a Java application is
     * guaranteed to be a normal file. 
     *
     * @return <code>true</code> if the file specified by this
     *         <code>XFile</code> object exists and is a "normal"
     *         file; <code>false</code> otherwise.
     */
    public boolean isFile() {
        return true;
    }


    /**
     * Tests if the file represented by this XFileAccessor
     * object is a directory. 
     *
     * @return <code>true</code> if this XFileAccessor object
     *         exists and is a directory; <code>false</code>
     *         otherwise.
     */
    public boolean isDirectory() {
        return false;
    }


    /**
     * Returns the time that the file represented by this 
     * <code>XFile</code> object was last modified. 
     * <p>
     * The return value is system dependent and should only be
     * used to compare with other values returned by last modified.
     * It should not be interpreted as an absolute time. 
     *
     * @return the time the file specified by this object was last
     *         modified, or <code>0L</code> if the specified file
     *         does not exist.
     */
    public long lastModified() {
        return urlConn.getLastModified();
    }


    /**
     * Returns the length of the file represented by this 
     * XFileAccessor object. 
     *
     * @return the length, in bytes, of the file specified by
     *         this object, or <code>0L</code> if the specified
     *         file does not exist.
     */
    public long length() {
        long len = urlConn.getContentLength();

        return len < 0 ? 0 : len;
    }


    /**
     * Creates a file whose pathname is specified by this 
     * XFileAccessor object. 
     *
     * @return <code>true</code> if the file could be created;
     *         <code>false</code> otherwise.
     */
    public boolean mkfile() {
        try {
            if (oStream == null)
                oStream = urlConn.getOutputStream();

            return true;

        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Creates a directory whose pathname is specified by this 
     * XFileAccessor object. 
     *
     * @return <code>true</code> if the directory could be created;
     *         <code>false</code> otherwise.
     */
    public boolean mkdir() {
        return false;
    }


    /**
     * Renames the file specified by this XFileAccessor object to 
     * have the pathname given by the XFileAccessor object argument. 
     *
     * @param  dest   the new filename.
     * @return <code>true</code> if the renaming succeeds;
     *         <code>false</code> otherwise.
     */
    public boolean renameTo(XFile dest) {
        return false;
    }


    /**
     * Returns a list of the files in the directory specified by
     * this XFileAccessor object. 
     *
     * @return an array of file names in the specified directory.
     *         This list does not include the current directory or
     *         the parent directory ("<code>.</code>" and
     *         "<code>..</code>" on Unix systems).
     */
    public String[] list() {
        return new String[0];
    }


    /**
     * Deletes the file specified by this object.  If the target
     * file to be deleted is a directory, it must be empty for
     * deletion to succeed.
     *
     * @return <code>true</code> if the file is successfully deleted;
     *         <code>false</code> otherwise.
     */
    public boolean delete() {
        return false;
    }


    /** 
     * Reads a subarray as a sequence of bytes. 
     *
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @param foff the offset into the file
     * @return number of bytes read; -1 if EOF
     * @exception IOException If an I/O error has occurred. 
     */ 
    public int read(byte b[], int off, int len, long foff)
        throws IOException {

        int c;

        if (iStream == null)
            iStream = urlConn.getInputStream();

        if (foff > fp) {
            iStream.skip(foff - fp);
            fp = foff;
        }

        c = iStream.read(b, off, len);

        if (c > 0)
            fp += c;

        return (c);
    }


    /**
     * Writes a sub array as a sequence of bytes.
     *
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @param foff the offset into the file
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte b[], int off, int len, long foff)
        throws IOException {

        if (oStream == null)
            oStream = urlConn.getOutputStream();

        oStream.write(b, off, len);

        fp += len;
    }


    /**
     * Forces any buffered output bytes to be written out. 
     * <p>
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
        if (oStream != null)
            oStream.flush();
    }


    /**
     * Close the Streams
     *
     * @exception IOException If an I/O error has occurred.
     */
    public void close() throws IOException {

        if (iStream != null) {
            iStream.close();
            iStream = null;
        }
        if (oStream != null) {
            oStream.close();
            oStream = null;
        }
    }
    

    /**
     * Returns a string representation of this object. 
     *
     * @return a string giving the pathname of this object. 
     */
    public String toString() {
        return url.toString();
    }
}
