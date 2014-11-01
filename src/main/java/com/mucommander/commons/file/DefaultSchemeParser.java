/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file;

import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URLDecoder;

/**
 * This class provides a default {@link SchemeParser} implementation. Certain scheme-specific features of the parser
 * can be turned on or off in the constructor, allowing this parser to be used with most schemes.
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
 * @see PathCanonizer
 * @author Maxence Bernard
 */
public class DefaultSchemeParser implements SchemeParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSchemeParser.class);

    /** True if query should be parsed and not considered as part of the path */
    protected boolean parseQuery;

    /** <code>PathCanonizer</code> instance to be used for canonizing the path part */
    protected PathCanonizer pathCanonizer;


    /**
     * Creates a DefaultSchemeParser with a {@link DefaultPathCanonizer} that uses the operating system's default 
     * path separator as the path separator and no tilde replacement, and query parsing disabled.
     */
    public DefaultSchemeParser() {
        this(false);
    }

    /**
     * Creates a DefaultSchemeParser with a {@link DefaultPathCanonizer} that uses that uses the operating system's
	 * default path separator as the path separator and no tilde replacement.
     * If <code>parseQuery</code> is <code>true</code>, any query part (delimited by '?') will be parsed as such,
     * or considered as part of the path otherwise.
     *
     * @param parseQuery <code>true</code>, any query part (delimited by '?') will be parsed as such, or considered
     * as part of the path otherwise
     */
    public DefaultSchemeParser(boolean parseQuery) {
        this(new DefaultPathCanonizer(System.getProperty("file.separator"), null), parseQuery);
    }

    /**
     * Creates a DefaultSchemeParser using the specified {@link PathCanonizer} for canonizing the path part.
     * If <code>parseQuery</code> is <code>true</code>, any query part (delimited by '?') will be parsed as such,
     * or considered as part of the path otherwise.
     *
     * @param pathCanonizer <code>PathCanonizer</code> instance to be used for canonizing the path part
     * @param parseQuery <code>true</code>, any query part (delimited by '?') will be parsed as such, or considered
     * as part of the path otherwise
     */
    public DefaultSchemeParser(PathCanonizer pathCanonizer, boolean parseQuery) {
        this.parseQuery = parseQuery;
        this.pathCanonizer = pathCanonizer;
    }

    /**
     * Handles the parsing of the given local file URL.
     *
     * @param url the URL to parse
     * @param fileURL the FileURL instance in which to set the different parsed parts
     */
    private void handleLocalFilePath(String url, FileURL fileURL) {
        SchemeHandler handler = FileURL.getRegisteredHandler(FileProtocols.FILE);
        SchemeParser parser = handler.getParser();

        fileURL.setHandler(handler);
        fileURL.setScheme(FileProtocols.FILE);
        fileURL.setHost(FileURL.LOCALHOST);
        fileURL.setPath((parser instanceof DefaultSchemeParser?((DefaultSchemeParser)parser).getPathCanonizer():pathCanonizer).canonize(url));
    }

    /**
     * Returns the {@link PathCanonizer} instance that is used by this {@link DefaultSchemeParser}.
     *
     * @return the {@link PathCanonizer} instance that is used by this {@link DefaultSchemeParser}
     */
    public PathCanonizer getPathCanonizer() {
        return pathCanonizer;
    }



    /////////////////////////////////
    // SchemeParser implementation //
    /////////////////////////////////

    public void parse(String url, FileURL fileURL) throws MalformedURLException {
        // The general form of a URI is:

        //      foo://example.com:8042/over/there?name=ferret#nose
        //      \_/   \______________/\_________/ \_________/ \__/
        //       |           |            |            |        |
        //    scheme     authority       path        query   fragment
        //       |   _____________________|__
        //      / \ /                        \
        //      urn:example:animal:ferret:nose


        // See http://labs.apache.org/webarch/uri/rfc/rfc3986.html for full specs

        try {
            int pos;
            int schemeDelimPos = url.indexOf("://");
            int urlLen = url.length();

            // If the given url contains no scheme, consider that it is a local path and transform it into a file:// URL
            if(schemeDelimPos==-1) {
                // Treat the URL as local file path if it starts with:
                // - '/' and OS doesn't use root drives (Unix-style path)
                // - a drive letter and OS uses root drives (Windows-style) [support both C:\ and C:/ style]
                // - a ~ character (refers to the user home folder)
                if((!LocalFile.USES_ROOT_DRIVES && url.startsWith("/")) || url.startsWith("~")) {
                    handleLocalFilePath(url, fileURL);

                    // All done, return
                    return;
                }
                else if (LocalFile.USES_ROOT_DRIVES && (url.indexOf(":\\")==1 || url.indexOf(":/")==1)) {
                    // Turn forward slash-separated paths into their backslash-separated counterparts.
                    if(url.charAt(2)=='/')
                        url = url.replace('/', '\\');

                    handleLocalFilePath(url, fileURL);

                    // All done, return
                    return;
                }

                // Handle Windows-style UNC network paths ( \\hostname\path ):
                // - under Windows, transform it into a URL in the file://hostname/path form,
                //   LocalProtocolProvider will translate it back into an UNC network path
                // - under other OS, conveniently transform it into smb://hostname/path to be nice with folks
                //   who've spent too much time using Windows
                else if(url.startsWith("\\\\") && urlLen>2) {
                    if(OsFamily.WINDOWS.isCurrent()) {
                        pos = url.indexOf('\\', 2);
                        url = FileProtocols.FILE+"://"+ 
                				(pos==-1?url.substring(2):url.substring(2, pos)+"/"+(pos==urlLen-1?"":url.substring(pos+1)));

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

            // The authority part is the one between scheme:// and the path/query. It includes the user information
            // (login/password), host and port. 
            String authority = url.substring(pos, hostEndPos);
            pos = 0;

            // Parse login and password (if specified).
            // They may contain non-URL safe characters that are decoded here, and re-encoded by FileURL#toString.
            int atPos = authority.lastIndexOf('@');
            int colonPos;
            // Filenames may contain @ chars, so atPos must be lower than next separator's position (if any)
            if(atPos!=-1 && (separatorPos==-1 || atPos<separatorPos)) {
                colonPos = authority.indexOf(':');
                String login = URLDecoder.decode(authority.substring(0, colonPos==-1?atPos:colonPos), "UTF-8");
                String password;
                if(colonPos!=-1)
                    password = URLDecoder.decode(authority.substring(colonPos+1, atPos), "UTF-8");
                else
                    password = null;

                if(!"".equals(login) || !(password==null || "".equals(password)))
                    fileURL.setCredentials(new Credentials(login, password));

                // Advance string index
                pos = atPos+1;
            }

            // Parse host and port (if specified)
            colonPos = authority.indexOf(':', pos);

            String host;
            if(colonPos!=-1) {
                host = authority.substring(pos, colonPos);
                String portString = authority.substring(colonPos+1);
                if(!portString.equals("")) {        // Tolerate an empty port part (e.g. http://mucommander.com:/)
                    try {
                        fileURL.setPort(Integer.parseInt(portString));
                    }
                    catch(NumberFormatException e) {
                        throw new MalformedURLException("URL contains an invalid port");
                    }
                }
            }
            else {
                host = authority.substring(pos);
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
            fileURL.setPath(pathCanonizer.canonize(path));

            LOGGER.info("Warning: path should not be empty, url={}", url);

            // Parse query part (if any)
            if(questionMarkPos!=-1)
                fileURL.setQuery(url.substring(questionMarkPos+1));     // Do not include the question mark
        }
        catch(MalformedURLException e) {
            throw e;
        }
        catch(Exception e2) {
            LOGGER.info("Unexpected exception in FileURL() with "+url, e2);

            throw new MalformedURLException();
        }
    }
}
