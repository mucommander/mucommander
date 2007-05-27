package com.mucommander.file.impl.http;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.*;
import com.mucommander.file.util.PathTokenizer;
import com.mucommander.io.Base64OutputStream;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;


/**
 * HTTPFile provides access to files located on an HTTP/HTTPS server.
 *
 * <p>The associated {@link FileURL} protocols are {@link FileProtocols#HTTP} and {@link FileProtocols#HTTPS}.
 * The host part of the URL designates the HTTP server. Credentials can be specified in the login and password parts
 * and will be used for HTTP Basic Authentication.
 *
 * <p>Here are a few examples of valid HTTP URLs:
 * <code>
 * http://www.mucommander.com/index.html<br>
 * http://www.mucommander.com/index.php?<br>
 * http://john:p4sswd@www.mucommander.com/restricted_area/<br>
 * </code>
 *
 * A notable feature of HTTPFile is that it handles HTML files as archives: when any of the {@link #ls()} methods is
 * called, the HTML file is parsed and any link found in the code is considered as a file:
 * <ul>
 *  <li>If the link looks like a link to an HTML file, the child HTTPFile will be 'browsable' ({@link #isBrowsable()}
 * will return <code>true</code>).
 *  <li>If not, the file will just be a regular file.
 * </ul>
 *
 * <p>In order to avoid the cost of having to perform a HEAD request for each file, some guessing based on the URL and
 * its filename is performed to determine if the file is an HTML file (content-type text/html) or not.
 * In practice, this works quite well for most sites but the algorithm will be confused by some non-conventional
 * file naming, for instance if an HTML file ends with the '.gif' extension.
 * <br>A HEAD request is then issued only for non-HTML files, to determine their size and last modified date.
 * HTML files will thus have a size returned by {@link #getSize()} of <code>-1</code> (undetermined), and a date
 * returned by {@link #getDate()} corresponding to 'now' (current time).
 *
 * <p>Access to HTTP files is provided by the <code>java.net</code> API. The {@link #getUnderlyingFileObject()} method
 * allows to retrieve a <code>java.net.URL</code> instance corresponding to this HTTPFile.
 *
 * @author Maxence Bernard
 */
public class HTTPFile extends AbstractFile {

    private URL url;

    private String absPath;

//    private String name;
    private long date;
    private long size;

    private boolean parentValSet;
    protected AbstractFile parent;
	
    /** True if the URL looks like */
    private boolean isHTML;

    /** True if file has been resolved on the remote HTTP server, either successfully or unsuccessfully */
    private boolean fileResolved;

    /** True if the file could be successfully resolved on the remote HTTP server */
	private boolean exists;


    /**
     * Creates a new instance of HTTPFile.
     */
    public HTTPFile(FileURL fileURL) throws IOException {
        this(fileURL, new URL(fileURL.toString(false)), fileURL.toString(false));
    }

	
    protected HTTPFile(FileURL fileURL, URL url, String absPath) throws IOException {
        super(fileURL);

        String protocol = fileURL.getProtocol().toLowerCase();
        if((!protocol.equals(FileProtocols.HTTP) && !protocol.equals(FileProtocols.HTTPS)) || fileURL.getHost()==null)
            throw new IOException();

        this.url = url;
        this.absPath = absPath;

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(url.toExternalForm());
		
//        // Determine file name (URL-decoded)
//        this.name = fileURL.getFilename(true);
//        // Name may contain '/' or '\' characters once decoded, let's remove them
//        if(name!=null) {
//            name = name.replace('/', ' ');
//            name = name.replace('\\', ' ');
//        }

        String mimeType;
        // Test if based on the URL, the file looks like an HTML file :
        //  - URL contains no path after hostname (e.g. http://google.com)
        //  - URL points to dynamic content (e.g. http://lulu.superblog.com?param=hola&val=...), even though dynamic scripts do not always return HTML
        //  - No filename with a known mime type can be extracted from the last part of the URL (e.g. NOT http://mucommander.com/download/mucommander-0_7.tgz)
        if(fileURL.getPath().equals("/")  || fileURL.getQuery()!=null || ((mimeType= MimeTypes.getMimeType(this))==null || mimeType.equals("text/html"))) {
            isHTML = true;
            size = -1;
            date = System.currentTimeMillis();
        }
        else {
            resolveFile();
        }
    }


    private void resolveFile() throws IOException {
        try {
            // Default values.
            size = -1;
            date = System.currentTimeMillis();

            // Get URLConnection instance
            HttpURLConnection conn = getHttpURLConnection(url);

            // Use HEAD instead of GET as we don't need the body
            conn.setRequestMethod("HEAD");

            // Establish connection
            conn.connect();

            // Check HTTP response code and throw appropriate IOException if request failed
            checkHTTPResponse(conn);

            // Resolve date: use last-modified header, if not set use date header, and if still not set use System.currentTimeMillis
            date = conn.getLastModified();
            if(date==0) {
                date = conn.getDate();
                if(date==0)
                    date = System.currentTimeMillis();
            }

            // Resolve size with content-length header (-1 if not available)
            size = conn.getContentLength();

            // Test if content is HTML
            String contentType = conn.getContentType();
            if(contentType!=null && contentType.trim().startsWith("text/html"))
                isHTML = true;

            // File was successfully resolved on the remote HTTP server and thus exists
            exists = true;
        }
        finally {
            // Mark the file as resolved, even if the request failed
            fileResolved = true;
        }
    }


    /**
     * Returns HttpURLConnection instance for the given URL. If the {@link FileURL} contained by this HTTPFile
     * contains {@link Credentials}, these will be used in the connection for HTTP Basic Authentication.  

     * @throws IOException if the HttpURLConnection could not be opened
     */
    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        // Get URLConnection instance
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();

        // If credentials are contained in this HTTPFile's FileURL, use them for Basic HTTP Authentication
        Credentials credentials = fileURL.getCredentials();
        if(credentials!=null)
            conn.setRequestProperty(
                "Authorization",
                "Basic "+ Base64OutputStream.encode(credentials.getLogin()+":"+credentials.getPassword())
            );

        // Set user-agent header
        conn.setRequestProperty("user-agent", com.mucommander.PlatformManager.USER_AGENT);

        return conn;
    }


    /**
     * Checks the response code of the given HttpURLConnection and :
     * <ul>
     *  <li>throws an {@link AuthException} if the response code is 401 (Unauthorized)
     *  <li>throws an IOException if the response code is not in the 2xx - 3xx range (not a positive response)
     *  <li>does nothing otherwise
     *
     * @param conn the HttpURLConnection connection to examine
     */
    private void checkHTTPResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("response code = "+responseCode);

        // If we got a 401 (Unauthorized) response, throw an AuthException to ask for credentials
        if(responseCode==401)
            throw new AuthException(fileURL, conn.getResponseMessage());

        if(responseCode<200 || responseCode>=400)
            throw new IOException(conn.getResponseMessage());
    }

	
    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////
	
    public long getDate() {
        return date;
    }
	
    public boolean changeDate(long date) {
        // File is read-only, return false
        return false;
    }
	
    public long getSize() {
        return size;	// Size == -1 if not known
    }
	
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentURL = fileURL.getParent();
            if(parentURL==null)
                this.parent = null;
            else {
                try { this.parent = new HTTPFile(parentURL); }
                catch(IOException e) {} // No problem, no parent that's all
            }
            this.parentValSet = true;
        }
		
        return this.parent;
    }
	

    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValSet = true;
    }

    public boolean exists() {
        if(!fileResolved) {
            // Note: file will only be resolved once, even if the request failed
            try { resolveFile(); }
            catch(IOException e) {}
        }

        return exists;
    }

    public boolean getPermission(int access, int permission) {
        return permission==READ_PERMISSION;
    }

    public boolean setPermission(int access, int permission, boolean enabled) {
        return false;
    }

    public boolean canGetPermission(int access, int permission) {
        return false;   // permissions should not be taken into acount
    }

    public boolean canSetPermission(int access, int permission) {
        return false;
    }


    public boolean isDirectory() {
        return false;
    }
	
    public boolean isSymlink() {
        return false;
    }

    public InputStream getInputStream() throws IOException {
        HttpURLConnection conn = getHttpURLConnection(this.url);

        // Establish connection
        conn.connect();

        // Check HTTP response code and throw appropriate IOException if request failed
        checkHTTPResponse(conn);

        return conn.getInputStream();
    }

    /**
     * Not available, always throws an <code>IOException</code>.
     */
    public OutputStream getOutputStream(boolean append) throws IOException {
        throw new IOException();
    }
    
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        // No random access for HTTP files unfortunately
        throw new IOException();
    }

    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        // No random access for HTTP files unfortunately
        throw new IOException();
    }

    /**
     * Not available, always throws an <code>IOException</code>.
     */
    public void delete() throws IOException {
        throw new IOException();
    }

    /**
     * Not available, always throws an <code>IOException</code>.
     */
    public void mkdir(String name) throws IOException {
        throw new IOException();
    }

    /**
     * Not available, always returns <code>-1</code>.
     */
    public long getFreeSpace() {
        // This information is obviously not available over HTTP, return -1
        return -1;
    }

    /**
     * Not available, always returns <code>-1</code>.
     */
    public long getTotalSpace() {
        // This information is obviously not available over HTTP, return -1
        return -1;
    }	
	
    /**
     * Returns a <code>java.net.URL</code> instance corresponding to this file.
     */
    public Object getUnderlyingFileObject() {
        return url;
    }

    public boolean canRunProcess() {
        return false;
    }

    public com.mucommander.process.AbstractProcess runProcess(String[] tokens) throws IOException {
        throw new IOException();
    }


    public AbstractFile[] ls() throws IOException {
        // Implementation note: javax.swing.text.html.HTMLEditorKit isn't quite powerful enough to be used

        BufferedReader br = null;
        try {
            URL contextURL = this.url;
            HttpURLConnection conn;
            do {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("contextURL="+contextURL+" hostname="+contextURL.getHost());
                // Get a connection instance
                conn = getHttpURLConnection(contextURL);

                // Disable automatic redirections to track URL change
                conn.setInstanceFollowRedirects(false);

                // Establish connection
                conn.connect();

                // Check HTTP response code and throw appropriate IOException if request failed
                checkHTTPResponse(conn);

                int responseCode = conn.getResponseCode();

                // Test if reponse code is in the 3xx range (redirection) and if 'Location' field is set
                String locationHeader = conn.getHeaderField("Location");
                if(responseCode>=300 && responseCode<400 && locationHeader!=null) {
                    // Redirect to Location field and remember context url
                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Location header = "+conn.getHeaderField("Location"));
                    contextURL = new URL(contextURL, locationHeader);
                    // One more time
                    continue;
                }

                break;
            } while(true);

            // Retrieve content type and throw an IOException if it is not text/html
            String contentType = conn.getContentType();
            if(contentType==null || !contentType.trim().startsWith("text/html"))
                throw new IOException("Content type is not text/html");  // Todo: localize this message
			
            int pos;
            String enc = null;
            // Extract content type information (if any)
            if((pos=contentType.indexOf("charset"))!=-1 || (pos=contentType.indexOf("Charset"))!=-1) {
                StringTokenizer st = new StringTokenizer(contentType.substring(pos, contentType.length()));
                enc = st.nextToken();
            }
			
            // Use the encoding reported in HTTP header if there was one, otherwise just use the default encoding
            InputStream in = conn.getInputStream();
            InputStreamReader ir;
            if(enc==null)
                ir = new InputStreamReader(in);
            else {
                try {
                    ir = new InputStreamReader(in, enc);
                }
                catch(UnsupportedEncodingException e) {
                    ir = new InputStreamReader(in);
                }
            }

            br = new BufferedReader(ir);

            Vector children = new Vector();
            // List that contains children URL, a TreeSet for fast (log(n)) search operations
            TreeSet childrenURL = new TreeSet();
            StreamTokenizer st = new StreamTokenizer(br);
            String token;
            String prevToken = "";
            int tokenType;
            HTTPFile child;
            URL childURL;
            FileURL childFileURL;
            Credentials credentials = fileURL.getCredentials();

            String parentPath = fileURL.getPath();
            if(!parentPath.endsWith("/"))
                parentPath += "/";

            String parentHost = fileURL.getHost();

            FileURL tempChildURL = (FileURL)fileURL.clone();

            while((tokenType=st.nextToken())!=StreamTokenizer.TT_EOF) {
                token = st.sval;
                if(token==null)
                    continue;
				
                if(tokenType=='\'' || tokenType=='"') {
                    try {
                        if((prevToken.equalsIgnoreCase("href") || prevToken.equalsIgnoreCase("src")) && !(token.startsWith("mailto") || token.startsWith("MAILTO") || token.startsWith("#"))) {
                            if(!childrenURL.contains(token)) {
                                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating child "+token+" context="+contextURL);
                                childURL = new URL(contextURL, token);

                                // Extract the filename from the child URL
                                PathTokenizer pt = new PathTokenizer(childURL.getPath(), "/", false);
                                String filename = null;
                                while(pt.hasMoreFilenames())
                                    filename = pt.nextFilename();

                                // If filename is null (For example if path is '/'), use host instead
                                if(filename==null)
                                    filename = url.getHost();

                                // Create the child FileURL instance
                                childFileURL = new FileURL(childURL.toExternalForm());
                                // Keep the parent's credentials (HTTP basic authentication), only if the host is the same.
                                // It would otherwise constitue a security issue.
                                if(parentHost.equals(childFileURL.getHost()))
                                    childFileURL.setCredentials(credentials);

                                // Important note: URL and absolute path may differ. If for instance,
                                // http://mucommander.com contains a link to http://java.com, the child file's
                                // absolute path will be http://mucommander.com/java.com whereas its URL (and canonical path)
                                // will be http://java.com .
                                // This is done to ensure that every children listed have this file as a parent.
                                tempChildURL.setPath(parentPath+filename);
                                child = new HTTPFile(childFileURL, childURL, tempChildURL.toString());

                                if(Debug.ON) Debug.trace("childFileURL="+child.getURL()+" absPath="+child.getAbsolutePath()+" parent="+child.getParent());

                                children.add(FileFactory.wrapArchive(child));
                                childrenURL.add(token);
                            }
                        }
                    }
                    catch(IOException e) {
                        if (com.mucommander.Debug.ON) {
                            com.mucommander.Debug.trace("Cannot create child : "+token+" "+e);
                        }
                    }
                }
                prevToken = token==null?"":token.toLowerCase();
            }

            Object childrenArray[] = new AbstractFile[children.size()];
            return (AbstractFile[])children.toArray(childrenArray);
        }
        catch (Exception e) {
            if (com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught while parsing HTML:"+e+", throwing IOException");

            if(e instanceof IOException)
                throw (IOException)e;

            throw new IOException();
        }
        finally {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("ends");

            try {
                // Try and close URL connection
                if(br!=null)
                    br.close();
            }
            catch(IOException e) {}
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getAbsolutePath() {
        return absPath;
    }

    public String getCanonicalPath() {
        return url.toExternalForm();
    }

    public boolean isHidden() {
        return false;
    }


    public boolean isBrowsable() {
        return isHTML;
    }


    /**
     * Overrides AbstractFile's getInputStream(long) method to provide a more efficient implementation:
     * use the HTTP 1.1 header to resume and skip the specified number of bytes.
     */
    public InputStream getInputStream(long skipBytes) throws IOException {
        HttpURLConnection conn = getHttpURLConnection(this.url);

        // Set header that allows to resume transfer
        conn.setRequestProperty("Range", "bytes="+skipBytes+"-");

        // Establish connection
        conn.connect();

        // Check HTTP response code and throw appropriate IOException if request failed
        checkHTTPResponse(conn);

        return conn.getInputStream();
    }
}
