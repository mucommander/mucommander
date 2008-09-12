/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.file;

import com.mucommander.Debug;
import com.mucommander.auth.Credentials;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.runtime.OsFamilies;

import java.net.MalformedURLException;
import java.util.Vector;

/**
 * This class provides a default {@link SchemeParser} implementation. Certain scheme-specific features of the parser
 * can be turned on or off in the constructor, allowing to use this parser to be used most schemes.
 *
 * <p>This parser can not only parse URLs but also local absolute paths and UNC paths. Upon parsing, these paths are
 * turned into equivalent, fully qualified URLs.</p>
 *
 * <h3>Local paths</h3>
 * <p>
 * Local absolute paths are turned into corresponding 'file' URLs. Local paths are system-dependent, their form and
 * path separator vary from one OS to the other. Only native paths are supported, i.e. Windows-style paths are supported
 * only when running on Windows (or OS/2), Unix-style paths only on an OS that uses them natively...
 * Here are a couple example of how local paths are parsed and turned into FileURL instances:   
 * <ul>
 *  <li>Under Windows or OS/2, <code>C:\Windows\System32\</code> will be parsed and turned into a FileURL whose path
 * separator is "\" and representation <code>file://localhost/C:\Windows\System32\</code></li>
 *  <li>Under a Unix-style OS (Linux, Mac OS X, Solaris...), <code>C:\Windows\System32\</code> will be parsed and turned
 * into a FileURL whose path separator is "\" and representation <code>file://localhost/C:\Windows\System32\</code></li>
 * </ul>
 * </p>
 *
 * <h3>UNC paths</h3>
 * <p>
 * Windows-style UNC paths such as <code>\\Server\Volume\File</code> are supported on all OSes but the FileURL
 * resulting from the parsing varies will not be the same whether they are created on a Windows environment or
 * on another:
 * <ul>
 *  <li>On Windows (any version), <code>\\Server\Volume\File</code> will be turned into a FileURL whose string
 * representation is <code>file://Server/\Volume\File</code></li>
 *  <li>On any other kind of OS, <code>\\Server\Volume\File</code> will be turned into a FileURL whose string
 * representation is <code>smb://Server/Volume/File</code></li>
 * </ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public class DefaultSchemeParser implements SchemeParser {

    /** Path separator */
    protected String separator;

    /** True if query should be parsed and not considered as part of the path */
    protected boolean parseQuery;

    /** The string replacement for '~' path fragraments, null for no tilde replacement */
    protected String tildeReplacement;

    /** String designating the localhost. */
    protected final static String LOCALHOST = "localhost";

    /** Local user home folder */
    protected final static String LOCAL_USER_HOME = System.getProperty("user.home");


    /**
     * Creates a DefaultSchemeParser with a <code>"/"</code> path separator, query parsing and tilde replacement
     * disabled.
     */
    public DefaultSchemeParser() {
        this("/", false, null);
    }

    /**
     * Creates a DefaultSchemeParser using the specified path separator and tilde replacement (<code>null</code> to
     * disable tilde replacement), and query parsing enabled if the corresponding parameter is set to <code>true</code>.
     *
     * @param separator the path separator that delimits path fragments
     * @param parseQuery if <code>true</code>, the query part (delimited by the '?' character) will be extracted and not
     * considered as being part of the path
     * @param tildeReplacement if not <code>null</code>, path fragments equal to '~' will be replaced by this string.
     * <code>null</code> disables tilde replacement
     *
     */
    public DefaultSchemeParser(String separator, boolean parseQuery, String tildeReplacement) {
        this.separator = separator;
        this.parseQuery = parseQuery;
        this.tildeReplacement = tildeReplacement;
    }

    /**
     * Returns a canonical value of the given path, where '.' and '..' path fragments are factored out, and '~' replaced
     * by the specified value (if not <code>null</code>).
     *
     * @param path the path to canonize
     * @param separator the path separator to use that delimits path fragments.
     * @param tildeReplacement if the specified value is not null, it is used to replace path fragments equal to '~'
     * @return the canonized path
     * @throws MalformedURLException if the path is invalid
     */
    protected String canonizePath(String path, String separator, String tildeReplacement) throws MalformedURLException {
        // Todo: use PathTokenizer?

        if(!path.equals("/")) {
            int pos;	    // position of current path separator
            int pos2 = 0;	// position of next path separator
            String dir;		// Current directory
            String dirWS;	// Current directory without trailing slash
            Vector pathV = new Vector();	// Will contain directory hierachy
            while((pos=pos2)!=-1) {
                // Get the index of the next path separator occurrence
                pos2 = path.indexOf(separator, pos);

                if(pos2==-1) {	// Last dir (or empty string)
                    dir = path.substring(pos);
                    dirWS = dir;
                }
                else {
                    dir = path.substring(pos, ++pos2);		// Dir name includes trailing slash
                    dirWS = dir.substring(0, dir.length()-1);
                }

                // Discard '.' and empty directories
                if((dirWS.equals("") && pathV.size()>0) || dirWS.equals(".")) {
                    continue;
                }
                // Remove last directory
                else if(dirWS.equals("..")) {
                    if(pathV.size()==0)
                        throw new MalformedURLException();
                    pathV.removeElementAt(pathV.size()-1);
                    continue;
                }
                // Replace '~' by the provided replacement string, only if one was specified
                else if(dirWS.equals("~") && (tildeReplacement!=null)) {
                    path = path.substring(0, pos) + tildeReplacement + path.substring(pos+1);
                    // Will perform another pass at the same position
                    pos2 = pos;
                    continue;
                }

                // Add directory to the end of the list
                pathV.add(dir);
            }

            // Reconstruct path from directory list
            path = "";
            int nbDirs = pathV.size();
            for(int i=0; i<nbDirs; i++)
                path += pathV.elementAt(i);

            // We now have a path free of '.' and '..'
        }

        return path;
    }


    /////////////////////////////////
    // SchemeParser implementation //
    /////////////////////////////////

    public void parse(String url, FileURL fileURL) throws MalformedURLException {
        try {
            int pos;
            int schemeDelimPos = url.indexOf("://");
            int urlLen = url.length();

            // If the given url contains no scheme, consider that it is a local path and transform it into a file:// URL
            if(schemeDelimPos==-1) {
                // Treat the URL as local file path if it starts with:
                // - '/' and OS doesn't use root drives (Unix-style path)
                // - a drive letter and OS uses root drives (Windows-style)
                // - a ~ character (refers to the user home folder)
                if((!LocalFile.USES_ROOT_DRIVES && url.startsWith("/")) || (LocalFile.USES_ROOT_DRIVES && url.indexOf(":\\")==1) || url.startsWith("~")) {
                    fileURL.setScheme(FileProtocols.FILE);
                    fileURL.setHost(LOCALHOST);
                    String separator = LocalFile.SEPARATOR;
                    fileURL.setPath(canonizePath(url, separator, LOCAL_USER_HOME));

                    // All done, return
                    return;
                }

                // Handle Windows-style UNC network paths ( \\hostname\path ):
                // - under Windows, transform it into a URL in the file://hostname/path form,
                //   LocalFile constructor will translate it back into an UNC network path
                // - under other OS, conveniently transform it into smb://hostname/path to be nice with folks
                //   who've spent too much time using Windows
                else if(url.startsWith("\\\\") && urlLen>2) {
                    if(OsFamilies.WINDOWS.isCurrent()) {
                        pos = url.indexOf('\\', 2);
                        if(pos==-1)
                            url =  FileProtocols.FILE+"://"+url.substring(2);
                        else
                            url = FileProtocols.FILE+"://"+url.substring(2, pos)+"/"+(pos==urlLen-1?"":url.substring(pos+1));

                        // Update scheme delimiter position
                        schemeDelimPos = FileProtocols.FILE.length();
                    }
                    else {
                        url = FileProtocols.SMB+"://"+url.substring(2).replace('\\', '/');

                        // Update scheme delimiter position
                        schemeDelimPos = FileProtocols.SMB.length();
                    }

                    // Update URL's length
                    urlLen = url.length();
                }
                // This doesn't look like a valid path, throw an MalformedURLException
                else {
                    throw new MalformedURLException("Path not absolute or malformed: "+url);
                }
            }

            // Start URL parsing

            String scheme = url.substring(0, schemeDelimPos);
            fileURL.setScheme(scheme);
            // Advance string index
            pos = schemeDelimPos+3;

            int separatorPos = url.indexOf('/', pos);

            // The question mark character (if any) marks the beginning of the query part, only if it should be parsed.
            int questionMarkPos = parseQuery?url.indexOf('?', pos):-1;
            int hostEndPos;         // Contains the position of the beginning of the path/query part
            if(separatorPos!=-1)    // Separator is necessarily before question mark
                hostEndPos = separatorPos;
            else if(questionMarkPos !=-1)
                hostEndPos = questionMarkPos;
            else
                hostEndPos = urlLen;

            // URL part before path/query part and without scheme://
            String urlBP = url.substring(pos, hostEndPos);
            pos = 0;

            // Parse login and password if they have been specified in the URL
            // Login/password may @ characters, so consider the last '@' occurrence (if any) as the host delimiter
            // Note that filenames may contain @ characters, but that's OK here since path is not contained in the String
            int atPos = urlBP.lastIndexOf('@');
            int colonPos;
            // Filenames may contain @ chars, so atPos must be lower than next separator's position (if any)
            if(atPos!=-1 && (separatorPos==-1 || atPos<separatorPos)) {
                colonPos = urlBP.indexOf(':');
                String login = urlBP.substring(0, colonPos==-1?atPos:colonPos);
                String password;
                if(colonPos!=-1)
                    password = urlBP.substring(colonPos+1, atPos);
                else
                    password = null;

                if(!"".equals(login) || !(password==null || "".equals(password)))
                    fileURL.setCredentials(new Credentials(login, password));

                // Advance string index
                pos = atPos+1;
            }

            // Parse host and port (if specified)
            colonPos = urlBP.indexOf(':', pos);

            String host;
            if(colonPos!=-1) {
                host = urlBP.substring(pos, colonPos);
                fileURL.setPort(Integer.parseInt(urlBP.substring(colonPos+1)));
            }
            else {
                host = urlBP.substring(pos);
            }

            if(host.equals(""))
                host = null;

            fileURL.setHost(host);

            // Parse path part excluding query part
            pos = hostEndPos;
            String path = url.substring(pos, questionMarkPos==-1?urlLen:questionMarkPos);

            // Empty path means '/'
            if(path.equals(""))
                path = "/";

            // Canonize path: factor out '.' and '..' and replace '~' by the replacement string (if any)
            fileURL.setPath(canonizePath(path, separator, tildeReplacement));

            if(Debug.ON && path.trim().equals("")) Debug.trace("Warning: path should not be empty, url="+url);

            // Parse query part (if any)
            if(questionMarkPos!=-1)
                fileURL.setQuery(url.substring(questionMarkPos));
        }
        catch(MalformedURLException e) {
            throw e;
        }
        catch(Exception e2) {
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Unexpected exception in FileURL() with "+url+" : "+e2);
                e2.printStackTrace();
            }
            throw new MalformedURLException();
        }
    }
}
