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

package com.sun.xfile;

import java.io.IOException;

/**
 * The XFileAccessor interface is implemented by filesystems that
 * need to be accessed via the XFile API.
 *
 * Classes that implement this interface must be associated
 * with a URL scheme that is structured according to the
 * Common Internet Scheme syntax described in
 * <a href=http://ds.internic.net/rfc/rfc1738.txt>RFC 1738</a>;
 * an optional location part followed by a hierarchical set
 * of slash separated directories.
 * <p>
 * A class file that implements this interface must be named
 * "XFileAccessor" and be installed in a directory named after
 * the URL scheme that it implements, for instance, an XFileAccessor
 * that provides file access through the HTTP protocol would be
 * associated with the "http" URL and its class file would be
 * called:
 * <p><blockquote><code>
 * 	http.XFileAccessor
 * </code></blockquote><p>
 * A class prefix is added to this name.  The default prefix is
 * <code>com.sun</code> and this composite name is located by the
 * classLoader via the CLASSPATH.
 * For instance, Sun's "nfs" XFileAccessor is installed as:
 * <p><blockquote><code>
 * 	com.sun.nfs.XFileAccessor
 * </code></blockquote><p>
 * The default class prefix <code>com.sun</code> can be changed by
 * setting the System property <b><code>java.protocol.xfile</code></b>
 * to any desired prefix or a list of prefixes separated by
 * vertical bars. Each prefix in the list will be used to
 * construct a package name and the classLoader will attempt
 * to load that package via the CLASSPATH.  This process will
 * continue until the XFileAccessor is successfully loaded.
 * <p>
 * For instance, if you want to use the "ftp"
 * XFileAccessor from Acme, Inc and the "nfs" XFileAccessor
 * from "ABC Inc." then you can set the system property as
 * follows:
 * <p><blockquote><code>
 * 	<b>java.protocol.xfile=com.acme|com.abc</b>
 * </code></blockquote>
 * When an "ftp" URL is used, the following package names will
 * be constructed:
 * <code<blockquote><pre>
 *    com.acme.ftp.XFileAccessor
 *    com.abc.ftp.XFileAccessor
 *    com.sun.ftp.XFileAccessor
 * </pre></blockquote></code>
 * (the default "com.sun" prefix is automatically added to
 * the end of the property list)
 * <p>
 * The class loader attempts to load each of the constructed
 * package names in turn relative to the CLASSPATH until it is
 * successful.
 * <p>
 * A subsequent reference to an "nfs" URL will result in
 * the following list of candidate package names:
 * <code<blockquote><pre>
 *    com.acme.nfs.XFileAccessor
 *    com.abc.nfs.XFileAccessor
 *    com.sun.nfs.XFileAccessor
 * </pre></blockquote></code>
 * In this case the "nfs" XFileAccessor from ABC, Inc. will
 * be loaded in preference to Sun's NFS.
 *
 *
 * @author  Brent Callaghan
 * @version 1.0, 04/08/98
 * @see     com.sun.xfile.XFile
 */
public interface XFileAccessor {


    /**
     * Open a file in this filesystem.
     *
     * This method is called before any other method.
     * It may be used to open the <i>real</i> file.
     *
     * @param xf The XFile for the file to be accessed
     * 		The URL will be of the form
     * 		<p>
     * 		    &lt;proto&gt;://&lt;location&gt;/&lt;path&gt;
     * 		<p>
     * 		where &lt;proto&gt; is the name of the filesystem,
     *		e.g. "nfs" and &lt;location&gt; is the location of
     *		the filesystem.  For nfs this is the network name of
     *		a server.  The &lt;path&gt; is a pathname that locates
     *		the file within &lt;location&gt;. As required by
     *		RFC 1738, the component delimiters in the pathname
     *		are as for URL syntax: forward slashes only.
     * @param serial true if serial access; false if random access
     * @param readOnly true if read only; false if read/write
     */
    boolean open(XFile xf, boolean serial, boolean readOnly);

    /**
     *  Return the XFile for this Accessor
     */
    XFile getXFile();


    /**
     * Tests if this XFile object exists. 
     *
     * @return <code>true</code> if the file specified by this object
     *         exists; <code>false</code> otherwise.
     */
    boolean exists();


    /**
     * Tests if the application can write to this file. 
     *
     * @return <code>true</code> if the application is allowed to
     *         write to a file whose name is specified by this
     *         object; <code>false</code> otherwise.
     */
    boolean canWrite();


    /**
     * Tests if the application can read from the specified file. 
     *
     * @return <code>true</code> if the file specified by this
     *         object exists and the application can read the file;
     *         <code>false</code> otherwise.
     */
    boolean canRead();

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
     *         object exists and is a "normal"
     *         file; <code>false</code> otherwise.
     */
    boolean isFile();


    /**
     * Tests if the file represented by this XFileAccessor
     * object is a directory. 
     *
     * @return <code>true</code> if this XFileAccessor object
     *         exists and is a directory; <code>false</code>
     *         otherwise.
     */
    boolean isDirectory();


    /**
     * Returns the time that the file represented by this 
     * <code>XFile</code> object was last modified. 
     * It is measured as the time in milliseconds since
     * midnight, January 1, 1970 UTC.
     * <p>
     * @return the time the file specified by this object was last
     *         modified, or <code>0L</code> if the specified file
     *         does not exist.
     */
    long lastModified();


    /**
     * Returns the length of the file represented by this 
     * XFileAccessor object. 
     *
     * @return the length, in bytes, of the file specified by
     *         this object, or <code>0L</code> if the specified
     *         file does not exist.
     */
    long length();


    /**
     * Creates an empty file whose pathname is specified by this 
     * XFileAccessor object. 
     *
     * @return <code>true</code> if the file was created;
     *         <code>false</code> otherwise.
     */
    boolean mkfile();


    /**
     * Creates a directory whose pathname is specified by this 
     * XFileAccessor object. 
     *
     * @return <code>true</code> if the directory could be created;
     *         <code>false</code> otherwise.
     */
    boolean mkdir();


    /**
     * Renames the file specified by this XFileAccessor object to 
     * have the pathname given by the XFileAccessor object argument. 
     *
     * The destination XFile object will be of the same URL
     * scheme as this object. The change of name must not
     * affect the existence or accessibility of this object.
     *
     * @param  dest the new filename.
     * @return <code>true</code> if the renaming succeeds;
     *         <code>false</code> otherwise.
     */
    boolean renameTo(XFile dest);


    /**
     * Deletes the file specified by this object.  If the target
     * file to be deleted is a directory, it must be empty for deletion
     * to succeed.
     *
     * @return     <code>true</code> if the file is successfully deleted;
     *             <code>false</code> otherwise.
     */
    boolean delete();


    /**
     * Returns a list of the files in the directory specified by
     * this XFileAccessor object. 
     *
     * @return an array of file names in the specified directory.
     *         This list does not include the current directory or
     *         the parent directory ("<code>.</code>" and
     *         "<code>..</code>" on Unix systems).
     */
    String[] list();


    /** 
     * Reads a subarray as a sequence of bytes. 
     *
     * @param b the buffer into which the data is read
     * @param off the start offset in the data buffer
     * @param len the maximum number of bytes to be read
     * @param foff the offset into the file
     * @return number of bytes read - zero if none.
     * @exception java.io.IOException If an I/O error has occurred. 
     */ 
    int read(byte b[], int off, int len, long foff) throws IOException;


    /**
     * Writes a sub array as a sequence of bytes.
     *
     * @param b the data to be written
     * @param off the start offset in the data in the buffer
     * @param len the number of bytes that are written
     * @param foff the offset into the file
     * @exception java.io.IOException If an I/O error has occurred.
     */
    void write(byte b[], int off, int len, long foff) throws IOException;


    /**
     * Forces any buffered output bytes to be written out. 
     * <p>
     *
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    void flush() throws IOException;


    /**
     * Close the file.
     *
     * Closes this file and releases any system resources 
     * associated with the file.
     *
     * After the file is closed further I/O operations may
     * throw IOException.
     *
     * @exception java.io.IOException If an I/O error has occurred.
     */
    void close() throws IOException;
}
