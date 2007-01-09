package com.mucommander.file;

import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.io.Base64OutputStream;
import com.mucommander.io.RandomAccessInputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;


/**
 *
 *
 * @author Maxence Bernard
 */
//public class HTTPFile extends AbstractFile implements RemoteFile {
public class HTTPFile extends AbstractFile {

    private String name;
    private long date;
    private long size;
	
    private URL url;
    //	private String urlString;
    //	private FileURL fileURL;

    private boolean parentValSet;
    protected AbstractFile parent;
	
    /** True if the URL looks like */
    private boolean isHTML;
	
	
    /**
     * Creates a new instance of HTTPFile.
     */
    public HTTPFile(FileURL fileURL) throws IOException {
        this(fileURL, new URL(fileURL.getStringRep(false)));
    }

	
    protected HTTPFile(FileURL fileURL, URL url) throws IOException {
        super(fileURL);

        String protocol = fileURL.getProtocol().toLowerCase();
        if((!protocol.equals(FileProtocols.HTTP) && !protocol.equals(FileProtocols.HTTPS)) || fileURL.getHost()==null)
            throw new IOException();

        this.url = url;

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(url.toExternalForm());
		
        //		// urlString is url-encoded
        //		this.fileURL = new FileURL(urlString);
        //		this.urlString = url.toExternalForm();

        // Determine file name (URL-decoded)
        this.name = fileURL.getFilename(true);
        // Name may contain '/' or '\' characters once decoded, let's remove them
        if(name!=null) {
            name = name.replace('/', ' ');
            name = name.replace('\\', ' ');
        }

        String mimeType;
        // Test if based on the URL, the file looks like an HTML file :
        //  - URL contains no path after hostname (e.g. http://google.com)
        //  - URL points to dynamic content (e.g. http://lulu.superblog.com?param=hola&val=...), even though dynamic scripts do not always return HTML
        //  - No filename with a known mime type can be extracted from the last part of the URL (e.g. NOT http://mucommander.com/download/mucommander-0_7.tgz)
        if(fileURL.getPath().equals("/")  || fileURL.getQuery()!=null || ((mimeType=MimeTypes.getMimeType(this))==null || mimeType.equals("text/html"))) {
            isHTML = true;
            size = -1;
            date = System.currentTimeMillis();
        }
        else {
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
        }
    }

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
        //		try {
        //			url.openStream().close();
        return true;
        //		}
        //		catch(Exception e) {
        //			return false;
        //		}
    }
	
    public boolean canRead() {
        return true;
    }
	
    public boolean canWrite() {
        return false;
    }

    public boolean canExecute() {
        return false;
    }

    public boolean setReadable(boolean readable) {
        return false;
    }

    public boolean setWritable(boolean writable) {
        return false;
    }

    public boolean setExecutable(boolean executable) {
        return false;
    }

    public boolean canSetPermissions() {
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

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        // No random access for HTTP files unfortunately
        throw new IOException();
    }

    /**
     * Not available, always throws an <code>IOException</code>.
     */
    public OutputStream getOutputStream(boolean append) throws IOException {
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
	

    public AbstractFile[] ls() throws IOException {
        //			EditorKit kit = new HTMLEditorKit();
        //			Document doc = kit.createDefaultDocument();

        //			// The Document class does not yet handle charsets properly.
        //			doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		
        //			Reader r = null;
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


            // Extract encoding information (if any)
            String contentType = conn.getContentType();
            if(contentType==null || !contentType.trim().startsWith("text/html"))
                throw new IOException("Content type is not text/html");  // Todo: localize this message
			
            int pos;
            String enc = null;
            if((pos=contentType.indexOf("charset"))!=-1 || (pos=contentType.indexOf("Charset"))!=-1) {
                StringTokenizer st = new StringTokenizer(contentType.substring(pos, contentType.length()));
                enc = st.nextToken();
            }
			
            // Create a reader on the HTML content with the proper encoding.
            // Use default encoding
            InputStream in = conn.getInputStream();
            InputStreamReader ir;
            // Use specified encoding
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
			
            while((tokenType=st.nextToken())!=StreamTokenizer.TT_EOF) {
                token = st.sval;
                //					if(st.ttype!=StreamTokenizer.TT_WORD)
                if(token==null)
                    continue;
				
                if(tokenType=='\'' || tokenType=='"') {
                    try {
                        //							if(token.toLowerCase().startsWith("http://") || (!token.equals("") && token.charAt(0)=='/')) {
                        if((prevToken.equalsIgnoreCase("href") || prevToken.equalsIgnoreCase("src")) && !(token.startsWith("mailto") || token.startsWith("MAILTO") || token.startsWith("#"))) {
                            if(!childrenURL.contains(token)) {
                                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating child "+token+" context="+contextURL);
                                childURL = new URL(contextURL, token);
                                childFileURL = new FileURL(childURL.toExternalForm(), fileURL);
                                child = new HTTPFile(childFileURL, childURL);
                                // Recycle this file for parent whenever possible
                                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("recycle parent="+child.fileURL.equals(this.fileURL));
                                if(childFileURL.equals(this.fileURL))
                                    child.setParent(this);

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
            /*
            // Parse the HTML.
            kit.read(br, doc, 0);
			
            // Iterate through the elements 
            // of the HTML document.
            ElementIterator it = new ElementIterator(doc);
            javax.swing.text.Element elem;
            HTTPFile child;
            String att;
            AttributeSet atts;
            SimpleAttributeSet s;
            while ((elem = it.next()) != null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Parsing "+elem.getName());
            att = null;
            //					try {
            atts = elem.getAttributes();
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("atts "+atts);
            if ((s=(SimpleAttributeSet)atts.getAttribute(HTML.Tag.A)) != null)
            att = ""+s.getAttribute(HTML.Attribute.HREF);
            else if ((s=(SimpleAttributeSet)atts.getAttribute(HTML.Tag.IMG)) != null)
            att = ""+s.getAttribute(HTML.Attribute.SRC);
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("att="+att);

            //					}
            //					catch(IOException e) {
            //						if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error while parsing HTML element: "+e);
            //					}

            if (att!=null && att.toLowerCase().startsWith("http://")) {
            try {
            child = new HTTPFile(att, url);
            child.parent = this;
            if(children.indexOf(child)==-1)
            children.add(child);
            }
            catch(IOException e) {
            if (com.mucommander.Debug.ON) {
            com.mucommander.Debug.trace("Unable to create child file : "+att+" "+e);
            e.printStackTrace();
            }
            }
            }
            }
            */
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

    public boolean isHidden() {
        return false;
    }


    public boolean isBrowsable() {
        return isHTML;
    }


//    public boolean equals(Object f) {
//        if(!(f instanceof HTTPFile))
//            return super.equals(f);		// could be equal to a ZipArchiveFile
//
//        return ((HTTPFile)f).getAbsolutePath(false).equals(getAbsolutePath(false));
//    }


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
