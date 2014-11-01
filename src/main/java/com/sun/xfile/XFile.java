/*
 * Copyright (c) 1999, 2007 Sun Microsystems, Inc. 
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

import java.io.*;
import java.util.Vector;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * 
 * Instances of this class represent the name of a file or directory.
 * Since only the name of the file is represented, the file itself
 * need not exist.
 * <p>
 * The XFile object is functionally equivalent to the java.io.File
 * object with the ability to handle not only native pathnames
 * but also URL-style pathnames. URL pathnames have some advantages
 * over native pathnames:
 * <p>
 *   <ul>
 *   <li> <b>The name is platform-independent.</b><br>
 *        You can use the same name to reference a file
 *        independent of the pathname syntax supported
 *        by the underlying operating system.  The
 *        component separator in a URL pathname is always
 *        a forward slash.
 *        <p>
 *   <li> <b>The name can be global in scope.</b><br>
 *        For instance, a URL name can refer to a file
 *        anywhere on the Internet, e.g.
 *        <p>
 *        <code>nfs://santa.northpole.org/toys/catalog</code>
 *        <p>
 *   <li> <b>The name can refer explicitly to an access scheme.</b><br>
 *        For example:
 *        <ul>
 *        <li><code>file:///C|/java/bin</code> (a local directory)
 *        <li><code>nfs://myserver/home/ed</code>
 *     (directory on NFS server)
 *        <li><code>ftp://ftpsrv/pub/pkg.zip</code>
 *     (file on FTP server)
 *        </ul>
 *        This property makes possible the dynamic loading
 *        of new filesystem accessors.
 *        <p>
 *   <li> <b>Consistent rules for composition of relative names.</b><br>
 *        URLs support a well defined set of rules for the use
 *        of names relative to a "base" URL described in 
 *        <a href=http://ds.internic.net/rfc/rfc1808.txt>RFC 1808</a>.
 *        <br> For instance:
 *        <p><center>
 *        <table border cellpadding=5>
 *        <tr>
 *          <th>Base</th><th>Relative</th><th>Composition</th>
 *        </tr> <tr>
 *          <td><code>file:///a/b/c</code></td>
 *          <td><code>x</code></td>
 *          <td><code>file:///a/b/c/x</code></td>
 *        </tr> <tr>
 *          <td><code>nfs://server/a/b/c</code></td>
 *          <td><code>/y</code></td>
 *          <td><code>nfs://server/y</code></td>
 *        </tr> <tr>
 *          <td><code>nfs://server/a/b/c</code></td>
 *          <td><code>../z</code></td>
 *          <td><code>nfs://server/a/b/z</code></td>
 *        </tr> <tr>
 *          <td><code>file:///a/b/c</code></td>
 *          <td><code>d/.</code></td>
 *          <td><code>nfs://server/a/b/c/d</code></td>
 *        </tr> <tr>
 *          <td><code>file:///a/b/c</code></td>
 *          <td><code>nfs://srv/x</code></td>
 *          <td><code>nfs://srv/x</code></td>
 *        </tr>
 *        </table>
 *        </center>
 *        <p>
 *   <li> <b>Will support Universal Resource Names.</b><br>
 *        Although URLs are necessarily location dependent,
 *        location indepent Universal Resource Names (URN)
 *        names can be used within the same structure (see
 *        <a href=ftp://ftp.isi.edu/in-notes/rfc2141.txt>
 *        RFC 2141</a>.
 *        <p>
 *   </ul>
 *   <p>
 *
 * Pathnames that are not represented as URL names will be
 * assumed to represent "native" names and XFile will present
 * the same semantics as the java.io.File class.
 */
public class XFile {

    /**
     * File Accessor that implements the underlying filesystem
     */
    private XFileAccessor xfa;

    /**
     * The url of the file.
     */
    private XFurl url;
    private String urlStr;
    private File nativeFile;
    private boolean bound;

    /**
     * Creates a <code>XFile</code> instance that represents the file 
     * whose pathname is the given url argument. 
     *
     * If the the name argument contains the string "://" then it
     * is assumed to be a URL name.  The characters prior to the
     * colon are assumed to be the filesystem scheme name, e.g.
     * "file", "nfs", etc.  The rest of the URL name is assumed
     * to be structured according to the Common Internet Scheme
     * syntax described in
     * <a href=http://ds.internic.net/rfc/rfc1738.txt>RFC 1738</a>;
     * an optional location part followed by a hierarchical set
     * of slash separated directories.
     * <p>
     * <code>&lt;scheme&gt;://&lt;location&gt;/&lt;path&gt;
     *
     * @param      name   the file url
     * @exception  java.lang.NullPointerException if the file url
     *             is equal to <code>null</code>.
     */
    public XFile(String name) {

        urlStr = name;
	if (name == null)
	    throw new NullPointerException();

        try {
            url = new XFurl(name);
            xfa = loadAccessor(url);
        } catch (Exception e) {
            if (name.startsWith(".:"))
                name = name.substring(2);	// lop off ".:"

            nativeFile = new File(name);
            xfa = makeNative(nativeFile);
        }
    }


    /**
     * Creates a <code>XFile</code> instance that represents the file 
     * with the specified name in the specified directory. 
     *
     * If the <code>dir</code> XFile is <code>null</code>, or if the
     * <code>name</code> string is a full URL, then the single-arg
     * constructor is used on the <code>name</code>.
     * <p>
     * If the <code>dir</code> XFile represents a native file
     * and the <code>name</code> string <code>isAbsolute</code>
     * then the single-arg constructor is used on the <code>name</code>.
     * If the <code>name</code> is not absolute then the resulting
     * path is the simple concatenation of the <code>dir</code>
     * path with the file separator and the <code>name</code> as
     * for the two-arg constructor of the <code>File</code> class.
     * <p>
     * If the <code>dir</code> XFile represents a URL name then
     * the <code>dir</code> is assumed to be a <i>base</i> URL
     * and the <code>name</code> string is evaluated as a
     * <i>relative</i> URL according to the rules described in
     * <a href=http://ds.internic.net/rfc/rfc1808.txt>RFC 1808</a>.
     *
     *        <p><center>
     *        <table border cellpadding=5>
     *        <tr>
     *          <th>Dir</th><th>Name</th><th>Composition</th>
     *        </tr> <tr>
     *          <td><code>file:///a/b/c</code></td>
     *          <td><code>x</code></td>
     *          <td><code>file:///a/b/c/x</code></td>
     *        </tr> <tr>
     *          <td><code>nfs://server/a/b/c</code></td>
     *          <td><code>/y</code></td>
     *          <td><code>nfs://server/y</code></td>
     *        </tr> <tr>
     *          <td><code>nfs://server/a/b/c</code></td>
     *          <td><code>../z</code></td>
     *          <td><code>nfs://server/a/b/z</code></td>
     *        </tr> <tr>
     *          <td><code>file:///a/b/c</code></td>
     *          <td><code>d/.</code></td>
     *          <td><code>nfs://server/a/b/c/d</code></td>
     *        </tr> <tr>
     *          <td><code>file:///a/b/c</code></td>
     *          <td><code>nfs://srv/x</code></td>
     *          <td><code>nfs://srv/x</code></td>
     *        </tr> <tr>
     *          <td><code>C:\Data\Programs</code></td>
     *          <td><code>myprog.exe</td>
     *          <td><code>C:\Data\Programs\myprog.exe</code></td>
     *        </tr>
     *        </table>
     *        </center>
     *
     * @param   dir   the directory.
     * @param   name  absolute or relative file name or URL
     */
    public XFile(XFile dir, String name) {

        if (name == null)
            throw new NullPointerException();

        try {
            url = new XFurl(name);
            xfa = loadAccessor(url);
        } catch (Exception e) {

            /*
             * If the name starts with ".:" then its
             * a "native" URL - not relative.
             */
            if (name.startsWith(".:")) {
                name = name.substring(2);	// lop off ".:"
                dir = null;
            }

            /*
             * The name is not a URL
             * If dir is not a URL then
             * make a native file.
             */
            if (dir == null) {
                nativeFile = new File(name);
                xfa = makeNative(nativeFile);

            /*
             * Departure from strict java.io semantics here:
             * If name is absolute then ignore the dir.
             */
            } else if (dir.nativeFile != null) {
                if (new File(name).isAbsolute())
                    nativeFile = new File(name);
                else
                    nativeFile = new File(dir.nativeFile, name);
                xfa = makeNative(nativeFile);

            /*
             * If the dir is a URL then evaluate
             * name as a relative URL (see RFC 1808)
             */
            } else {
                try {
                    url = new XFurl(dir.getURL(), name);
                    xfa = loadAccessor(url);
                } catch (Exception ee) {
                    System.out.println("Error: " + dir.getURL()
                        + " " + name);
                }
            }
        }
        urlStr = url.toString();
    }


    /*
     * Extract the protocol scheme from the url and
     * load a class for an XFileAccessor or XFileExtensionAccessor
     *
     * The accessor is located by constructing a package
     * name that contains the scheme string which is then
     * located relative to the CLASSPATH.
     *
     * The package name is constructed from three components:
     *
     *    <package prefix> . <url scheme string> . <suffix>
     *
     * The default package prefix is "com.sun" however this may
     * changed by setting of the System property "java.protocol.xfile"
     * to the value of one or more alternative prefixes delimited
     * by vertical bars. The list is searched until the XFileAccessor
     * is found. This method of locating accessors is similar to that
     * of java.net.URL protocol handlers. The suffix is either
     * "XFileAccessor" or "XFileExtensionAccessor".
     *
     * The hash table is used as a cache for previously loaded classes.
     */
    private Class loadClass(String proto, String suffix, Hashtable ht)
        throws ClassNotFoundException, IllegalAccessException {

        /*
         * Check if there's a cached class for this protocol
         */
        Class cl = (Class)ht.get(proto);
        if (cl != null)
            return cl;

        String prefixList = null;

        try {	// applets will get a security exception here
            prefixList = System.getProperty("java.protocol.xfile");
        } catch (SecurityException e) {};

        if (prefixList == null)
            prefixList = "";
        else
            prefixList += "|";
        prefixList += "com.sun";	// always the default

        StringTokenizer pkgs = new StringTokenizer(prefixList, "|");

        while (cl == null && pkgs.hasMoreTokens()) {
            String prefix = pkgs.nextToken().trim();
            String clname = prefix + "." + proto + "." + suffix;
            try {
                cl = Class.forName(clname);
            } catch (Exception e) {};
        }

        if (cl == null)
            throw new ClassNotFoundException();

        ht.put(proto, cl);

        return (cl);
    }
    

    /*
     * A table of cached XFileAccessors
     */
    static Hashtable cachedAccessors = new Hashtable();

    /*
     * Load an XFileAccessor
     */
    private XFileAccessor loadAccessor(XFurl url)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {

        Class cl = loadClass(url.getProtocol(), "XFileAccessor",
                        cachedAccessors);
        if (cl == null)
            return null;

        return (XFileAccessor)cl.newInstance();
    }

    
    /*
     * Return a file accessor that corresponds
     * to a native file.
     */
    private XFileAccessor makeNative(File f) {
        char sep = f.separatorChar;
        try {
            url = new XFurl("file:///" + f.getPath().replace(sep, '/'));
            return loadAccessor(url);
        } catch (Exception e) {
            System.out.println("Error: makenative:" + f.getPath());
            return null;
        }
    }


    /*
     * Check that the file is open.
     * The open() method must be called before
     * any other methods in the Accessor.
     * This makes it easier for Accessors to
     * centralize initialization code in one place.
     */
    private boolean bind() {
        if (bound)
            return true;

        bound = xfa.open(this, false, false);

        return bound;
    }


    /*
     * Return the bound status
     */
    private boolean getBound() {
        return bound;
    }


    /*
     * Get the native file
     */
    private File getNative() {
        return nativeFile;
    }

    /*
     * Get a new XFileAccessor for this object
     */
    protected XFileAccessor newAccessor() {
        try {
            return loadAccessor(url);
        } catch (Exception e) {
            return makeNative(nativeFile);
        }
    }

    /*
     * Get the XFileAccessor for this Object
     *
     * @return XFileAccessor
     */
    private XFileAccessor getAccessor() {
        return xfa;
    }


    /*
     * A table of cached XFileExtensionAccessors
     */
    static Hashtable cachedExtensionAccessors = new Hashtable();

    /**
     * Get the XFileExtensionAccessor
     *
     * @return instance of XFileExtensionAccessor or null
     *         if there is no XFileExtensionAccessor.
     */
    public XFileExtensionAccessor getExtensionAccessor() {
        try {

            /*
             * XXX The following ugly code avoids a ClassCastException
             * for old apps using the deprecated
             * nfsXFileExtensionAccessor class.
             */
            String suffix;

            if (url.getProtocol().equals("nfs"))
                suffix = "nfsXFileExtensionAccessor";
            else
                suffix = "XFileExtensionAccessor";

            Class cl = loadClass(url.getProtocol(), suffix,
                        cachedExtensionAccessors);
    
            Constructor con = cl.getConstructor(new Class[]{this.getClass()});
            return (XFileExtensionAccessor)con.newInstance(new Object[]{this});
    
        } catch (Exception e) {
            return null;
        }
    }
    

    /*
     * Get the XFile URL
     */
    private XFurl getURL() {
        return url;
    }


    /**
     * Returns the name of the filesystem, the string before
     * the colon of the URL. 
     *
     * If this XFile represents a native path then the
     * "file" filesystem will be returned.
     *
     * @return  the name of the filesystem.
     */
    public String getFileSystemName() {
	return url.getProtocol();
    }


    /**
     * Returns the name of the file represented by this object.
     *
     * The name is the last component of the pathname.
     * For a URL this is the last, non-terminating slash.
     * For a native file it is the portion of the pathname after
     * the last occurrence of the separator character. 
     *
     * @return  the name of the file (without any directory components)
     *          represented by this <code>XFile</code> object.
     */
    public String getName() {
        if (nativeFile != null)
            return nativeFile.getName();

        return url.getName();
    }


    /**
     * Returns the pathname of the file represented by this object.
     *
     * @return the pathname represented by this <code>XFile</code>
     *          object.
     *          <p>
     *          If the object is a URL type, the path is the part
     *          of the URL following the location, e.g.
     *          <p>
     *          <code>new XFile("nfs://location/a/b/c").getPath()
     *          == "a/b/c"</code>
     *          <p>
     *          <code>new XFile("file:///a/b/c").getPath()
     *          == "a/b/c"</code>
     *          <p>
     *          <code>new XFile("nfs://server/").getPath()
     *          == ""</code>
     */
    public String getPath() {
        if (nativeFile != null)
            return nativeFile.getPath();

	return url.getPath();
    }


    /**
     * Returns the absolute pathname of the file represented by this
     * object.
     *
     * If this object is represented by a native pathname and is an
     * absolute pathname, then return the pathname. Otherwise, return
     * a pathname that is a concatenation of the current user
     * directory, the separator character, and the pathname of this
     * file object. 
     * The system property <code>user.dir</code> contains the current 
     * user directory. 
     * <p>
     * If the object is represented by a URL then return the entire URL
     * string.
     *
     * @return a system-dependent absolute pathname for this
     *         <code>XFile</code>.
     */
    public String getAbsolutePath() {
        if (nativeFile != null)
            return nativeFile.getAbsolutePath();

	return urlStr;
    }


    /**
     * Returns the canonical form of this <code>XFile</code> object's
     * pathname.
     *
     * If the object is represented by a URL name then the full
     * URL is always returned.  URL names are always canonical.
     * <p>
     * For native paths the precise definition of canonical form
     * is system-dependent, but it usually specifies an absolute
     * pathname in which all relative references and references
     * to the current user directory have been completely
     * resolved.  The canonical form of a pathname of a nonexistent
     * file may not be defined.
     *
     * @return the canonical path of the object
     * @exception java.io.IOException If an I/O error occurs, which
     *            is possible because the construction of the
     *            canonical path may require filesystem queries.
     */
    public String getCanonicalPath() throws IOException {
        if (nativeFile != null)
            return nativeFile.getCanonicalPath();

	return urlStr;
    }


    /**
     * Returns the parent part of the pathname of this
     * <code>XFile</code> object, or <code>null</code> if the name
     * has no parent part.
     *
     * If the name is a URL then the parent part is the URL with
     * the last component of the pathname removed.  If the URL
     * has no pathname part, then the URL is returned unchanged.
     * <p>
     * For native paths the parent part is generally everything
     * leading up to the last occurrence of the  separator character,
     * although the precise definition is system dependent.
     * On UNIX, for example, the parent part of <code>"/usr/lib"</code>
     * is <code>"/usr"</code> whose parent part is <code>"/"</code>,
     * which in turn has no parent.
     * On Windows platforms, the parent part of <code>"c:\java"</code>
     * is <code>"c:\"</code>, which in turn has no parent.
     *
     * @return the name of the parent directory
     */
    public String getParent() {
        if (nativeFile != null)
            return nativeFile.getParent();

	return url.getParent();
    }


    /**
     * Tests if the file represented by this <code>XFile</code>
     * object is an absolute pathname.
     *
     * If the object is represented by a URL then <code>true</code>
     * is always returned.<br>
     * If the <code>XFile</code> represents a native name then
     * the definition of an absolute pathname is system 
     * dependent. For example, on UNIX, a pathname is absolute if its 
     * first character is the separator character.
     * On Windows platforms, 
     * a pathname is absolute if its first character is an ASCII
     * '&#92;' or '/', or if it begins with a letter followed by
     * a colon. 
     *
     * @return  <code>true</code> if the pathname indicated by the
     *          <code>XFile</code> object is an absolute pathname;
     *          <code>false</code> otherwise.
     */
    public boolean isAbsolute() {
        if (nativeFile != null)
            return nativeFile.isAbsolute();

        return true;
    }


    /**
     * Tests if this <code>XFile</code> exists. 
     *
     * @return <code>true</code> if the file specified by this object
     *         exists; <code>false</code> otherwise.
     */
    public boolean exists() {
        if (!bind())
            return false;

	return xfa.exists();
    }


    /**
     * Tests if the application can write to this file. 
     *
     * @return  <code>true</code> if the application is allowed to
     *          write to a file whose name is specified by this object;
     *          <code>false</code> otherwise.
     */
    public boolean canWrite() {
        if (!bind())
            return false;

	return xfa.canWrite();
    }


    /**
     * Tests if the application can read from the specified file. 
     *
     * @return <code>true</code> if the file specified by this
     *         object exists and the application can read the file;
     *         <code>false</code> otherwise.
     */
    public boolean canRead() {
        if (!bind())
            return false;

	return xfa.canRead();
    }


    /**
     * Tests if the file represented by this <code>XFile</code> 
     * object is a "normal" file. 
     *
     * A file is "normal" if it is not a directory and, in 
     * addition, satisfies other system-dependent criteria. Any 
     * non-directory file created by a Java application is guaranteed
     * to be a normal file. 
     *
     * @return <code>true</code> if the file specified by this object
     *         exists and is a "normal" file; <code>false</code>
     *         otherwise.
     */
    public boolean isFile() {
        if (!bind())
            return false;

	return xfa.isFile();
    }


    /**
     * Tests if the file represented by this <code>XFile</code> 
     * object is a directory. 
     *
     * @return <code>true</code> if this <code>XFile</code> exists
     *         and is a directory; <code>false</code> otherwise.
     */
    public boolean isDirectory() {
        if (!bind())
            return false;

	return xfa.isDirectory();
    }



    /**
     * Returns the time that the file represented by this 
     * <code>XFile</code> object was last modified. 
     * <p>
     * The return value is system dependent and should only be used to 
     * compare with other values returned by last modified. It should
     * not be interpreted as an absolute time. 
     *
     * @return the time the file specified by this object was last
     *         modified, or <code>0L</code> if the specified file
     *         does not exist.
     */
    public long lastModified() {
        if (!bind())
            return 0L;;

	return xfa.lastModified();
    }


    /**
     * Returns the length of the file represented by this 
     * <code>XFile</code> object. 
     *
     * @return the length, in bytes, of the file specified by this
     *         object, or <code>0L</code> if the specified file does
     *         not exist. The length constitutes the number of bytes
     *         readable via an InputStream. The length value for
     *         a directory is undefined.
     */
    public long length() {
        if (!bind())
            return 0L;

	return xfa.exists() ? xfa.length() : 0L;
    }


    /**
     * Renames the file specified by this <code>XFile</code> object to 
     * have the pathname given by the <code>XFile</code> argument. 
     *
     * This object and <code>dest</code> must represent filesystems
     * of the same type.  For instance: both native or both of
     * the same URL scheme.
     *
     * After a successful renameTo, this object continues to
     * be a valid reference to the file.  Only the name
     * is different.
     *
     * If the destination filename already exists, it will be replaced.
     * The application must have permission to modify the source and
     * destination directory.
     *
     * @param      dest the new filename.
     * @return     <code>true</code> if the renaming succeeds;
     *             <code>false</code> otherwise.
     */
    public boolean renameTo(XFile dest) {
        if (dest == null)
            throw new NullPointerException();

        if (! xfa.getClass().isInstance(dest.getAccessor()))
            return false;

        if (!bind())
            return false;

	boolean ok = xfa.renameTo(dest);

        /*
         * Only the name of the file is changed.
         * Its data and state are unaffected.
         * Hence we make this XFile object a clone
         * of the dest XFile.
         */
        if (ok) {
            url = dest.getURL();
            urlStr = dest.getAbsolutePath();
            nativeFile = dest.getNative();
            xfa = dest.getAccessor();
            bound = dest.getBound();
        }

        return ok;
    }


    /**
     * Creates a directory whose pathname is specified by this 
     * <code>XFile</code> object. 
     *
     * If any parent directories in the pathname do not
     * exist, the method will return false.
     *
     * @return <code>true</code> if the directory could be created;
     *         <code>false</code> otherwise.
     */
    public boolean mkdir() {
        bind();

	return xfa.mkdir();
    }


    /**
     * Creates a directory whose pathname is specified by this 
     * <code>XFile</code> object, including any necessary parent
     * directories.
     *
     * @return <code>true</code> if the directory (or directories)
     *         could be created; <code>false</code> otherwise.
     */
    public boolean mkdirs() {
        bind();

	if (exists()) {
	    return false;
	}
	if (mkdir()) {
 	    return true;
 	}

	String parent = getParent();
	return (parent != null) && (new XFile(parent).mkdirs() && mkdir());
    }


    /**
     * Returns a list of the files in the directory specified by this
     * <code>XFile</code> object. 
     *
     * @return an array of file names in the specified directory.
     *         This list does not include the current directory or the
     *         parent directory ("<code>.</code>" and "<code>..</code>"
     *         on Unix systems).
     */
    public String[] list() {
        if (!bind())
            return null;;

	return xfa.list();
    }


    /**
     * Returns a list of the files in the directory specified by this 
     * <code>XFile</code> that satisfy the specified filter. 
     *
     * @param  filter   a filename filter.
     * @return an array of file names in the specified directory.
     *         This list does not include the current directory or the
     *         parent directory ("<code>.</code>" and "<code>..</code>"
     *         on Unix systems).
     * @see    com.sun.xfilenameFilter
     */
    public String[] list(XFilenameFilter filter) {
        if (!bind())
            return null;;

	String names[] = list();

	if (names == null) {
	    return null;
	}

	// Fill in the Vector
	Vector v = new Vector();
	for (int i = 0 ; i < names.length ; i++) {
	    if ((filter == null) || filter.accept(this, names[i])) {
		v.addElement(names[i]);
	    }
	}

	// Create the array
	String files[] = new String[v.size()];
	v.copyInto(files);

	return files;
    }


    /**
     * Deletes the file specified by this object.
     * If the target file to be deleted is a directory, it must be
     * empty for deletion to succeed.
     *
     * @return <code>true</code> if the file is successfully deleted;
     *         <code>false</code> otherwise.
     */
    public boolean delete() {
        if (!bind())
            return false;;

        boolean ok = xfa.delete();

        bound = !ok;

        return ok;
    }


    /**
     * Computes a hashcode for the file.
     *
     * @return a hash code value for this <code>XFile</code> object.
     */
    public int hashCode() {
	return urlStr.hashCode() ^ 1234321;
    }


    /**
     * Compares this object against the specified object.
     *
     * Returns <code>true</code> if and only if the argument is 
     * not <code>null</code> and is a <code>XFile</code> object whose 
     * pathname is equal to the pathname of this object. 
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
	if ((obj == null) || (! (obj instanceof XFile)))
            return false;

	return url.toString().equals(((XFile)obj).getURL().toString());
    }


    /**
     * Returns a string representation of this object. 
     *
     * @return  a string giving the pathname of this object. 
     */
    public String toString() {
        if (nativeFile != null)
            return (nativeFile.toString());

	return urlStr;
    }
}
