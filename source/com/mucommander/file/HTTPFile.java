package com.mucommander.file;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.text.*;
import javax.swing.text.html.*;


/**
 *
 * @author Maxence Bernard
 */
//public class HTTPFile extends AbstractFile implements RemoteFile {
public class HTTPFile extends AbstractFile {

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";
	
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
		this(fileURL, new URL(fileURL.getStringRep(true)));
	}

	
	protected HTTPFile(FileURL fileURL, URL url) throws IOException {
		super(fileURL);

		String protocol = fileURL.getProtocol().toLowerCase();
		if((!protocol.equals("http") && !protocol.equals("https")) || fileURL.getHost()==null)
			throw new IOException();
		
		this.url = url;

if(com.mucommander.Debug.ON) System.out.println("HTTPFile(): "+url.toExternalForm()+" content-type guess="+URLConnection.guessContentTypeFromName(url.getFile()));
		
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
		// If based on this test, the file is considered to be an HTML file, use default date (now) and size (-1),
		// and if not (URL points to file with a known mime type), connect to retrieve content-length, date headers, and verify that
		// content-type is indeed not HTML
		if(fileURL.getPath().equals("")
		 || fileURL.getQuery()!=null
		 || ((mimeType=MimeTypes.getMimeType(this))==null || mimeType.equals("text/html"))) {
			date = System.currentTimeMillis();
			size = -1;
			isHTML = true;
		}
		else {
			// Get URLConnection instance
			HttpURLConnection conn = getHttpURLConnection(url);
	
			// Use HEAD instead of GET as we don't need the body
			conn.setRequestMethod("HEAD");
			
			// Open connection
			conn.connect();

if(com.mucommander.Debug.ON) System.out.println("HTTPFile(): response code = "+conn.getResponseCode());
			
			// Resolve date: last-modified header, if not set date header, and if still not set System.currentTimeMillis
			date = conn.getLastModified();
			if(date==0) {
				date = conn.getDate();
				if(date==0)
					date = System.currentTimeMillis();
			}
			
			// Resolve size thru content-length header (-1 if not available)
			size = conn.getContentLength();
			
			// Test if content is HTML
			String contentType = conn.getContentType();
			if(contentType!=null && contentType.trim().startsWith("text/html"))
				isHTML = true;
//System.out.println("contentType= "+contentType+" isHTML ="+isHTML); 
		}
	}
	
	
	private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
		// Get URLConnection instance
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		
		// Set user-agent header
		conn.setRequestProperty("user-agent", com.mucommander.Launcher.USER_AGENT);
	
		return conn;
	}
	
	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
//	public String getProtocol() {
//		return "HTTP";
//	}

	public String getName() {
		if(name==null)
			return fileURL.getHost();
		return name;
	}

	public String getAbsolutePath() {
//		return urlString;
		return fileURL.getStringRep(true);
	}

	public String getSeparator() {
		return SEPARATOR;
	}

	public long getDate() {
		return date;
	}
	
	public boolean changeDate(long date) {
		// File is read-only, return false
		return false;
	}
	
	public long getSize() {
		return size;
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
	
	public boolean isHidden() {
		return false;
	}

	public boolean isDirectory() {
		return false;
	}
	
	public boolean isBrowsable() {
		return isHTML;
	}
	
	public boolean isSymlink() {
		return false;
	}

	public boolean equals(Object f) {
		if(!(f instanceof HTTPFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile
		
		return ((HTTPFile)f).getAbsolutePath(false).equals(getAbsolutePath(false));
	}
	
	
	public InputStream getInputStream() throws IOException {
		HttpURLConnection conn = getHttpURLConnection(this.url);
		conn.connect();
		return conn.getInputStream();
	}

	/** 
	 * Overrides AbstractFile's getInputStream(long) method to provide a more efficient implementation : 
	 * use the HTTP 1.1 header that resumes file transfer and skips a number of bytes.
	 */
	public InputStream getInputStream(long skipBytes) throws IOException {
		HttpURLConnection conn = getHttpURLConnection(this.url);
		// Set header that allows to resume transfer
		conn.setRequestProperty("Range", "bytes="+skipBytes+"-");
		conn.connect();
		return conn.getInputStream();
	}
	
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		throw new IOException();
	}
		
	public boolean moveTo(AbstractFile dest) throws IOException  {
		return false;
	}

	public void delete() throws IOException {
		throw new IOException();
	}

	public AbstractFile[] ls() throws IOException {
//System.out.println("parsing "+getAbsolutePath()); 
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
				// Open connection
				conn = getHttpURLConnection(contextURL);
				
				// Disable automatic redirections to track URL change
				conn.setInstanceFollowRedirects(false);

				// Establish connection
				conn.connect();
if(com.mucommander.Debug.ON) System.out.println("HTTPFile.ls(): response code = "+conn.getResponseCode());

				// Test if reponse code is in the 3xx range and if 'Location' field is set
				int responseCode = conn.getResponseCode();
				String locationField = conn.getHeaderField("Location");
				if(responseCode>=300 && responseCode<400 && locationField!=null) {
					// Redirect to Location field and remember context url
if(com.mucommander.Debug.ON) System.out.println("HTTPFile.ls(): Location header = "+conn.getHeaderField("Location"));
					contextURL = new URL(locationField);
					// One more time
					continue;
				}

				break;
			} while(true);

			
			// Extract encoding information (if any)
			String contentType = conn.getContentType();
			if(contentType==null || !contentType.trim().startsWith("text/html"))
				throw new IOException();
			
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
//System.out.println("token= "+token+" "+st.ttype+" "+(st.ttype==st.TT_WORD)+" prevToken="+prevToken);
//					if(st.ttype!=StreamTokenizer.TT_WORD)
				if(token==null)
					continue;
				
				if(tokenType=='\'' || tokenType=='"') {
					try {
//							if(token.toLowerCase().startsWith("http://") || (!token.equals("") && token.charAt(0)=='/')) {
						if((prevToken.equalsIgnoreCase("href") || prevToken.equalsIgnoreCase("src")) && !(token.startsWith("mailto") || token.startsWith("MAILTO") || token.startsWith("#"))) {
							if(!childrenURL.contains(token)) {
if(com.mucommander.Debug.ON) System.out.println("HTTPFile.ls(): creating child "+token+" context="+contextURL);
								childURL = new URL(contextURL, token);
								childFileURL = new FileURL(childURL.toExternalForm());
								child = new HTTPFile(childFileURL, childURL);
								// Recycle this file for parent whenever possible
if(com.mucommander.Debug.ON) System.out.println("HTTPFile.ls(): recycle_parent="+child.fileURL.equals(this.fileURL));
								if(childFileURL.equals(this.fileURL))
									child.setParent(this);

								children.add(AbstractFile.wrapArchive(child));
								childrenURL.add(token);
							}
						}
					}
					catch(IOException e) {
						if (com.mucommander.Debug.ON) {
							System.out.println("Cannot create child : "+token+" "+e);
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
System.out.println("Parsing "+elem.getName());
				att = null;
//					try {
					atts = elem.getAttributes();
System.out.println("atts "+atts);
					if ((s=(SimpleAttributeSet)atts.getAttribute(HTML.Tag.A)) != null)
						att = ""+s.getAttribute(HTML.Attribute.HREF);
					else if ((s=(SimpleAttributeSet)atts.getAttribute(HTML.Tag.IMG)) != null)
						att = ""+s.getAttribute(HTML.Attribute.SRC);
System.out.println("att="+att);

//					}
//					catch(IOException e) {
//						if (com.mucommander.Debug.ON) System.out.println("Error while parsing HTML element: "+e);
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
							System.out.println("Unable to create child file : "+att+" "+e);
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
			if (com.mucommander.Debug.ON) {
				System.out.println("Error while parsing HTML: "+e);
					e.printStackTrace();
			}
			throw new IOException();
		}
		finally {
if(com.mucommander.Debug.ON) System.out.println("HTTPFile.ls(): ends");

			try {
				// Try and close URL connection
				if(br!=null)
					br.close();
			}
			catch(IOException e) {}
		}
	}

	public void mkdir(String name) throws IOException {
		throw new IOException();
	}
	
	
	
	/**
	 * Reveals a problem (?) with URL class, where a URL has to have a trailing '/' 
	 * to be considered as a folder.
	 */
	public static void main(String args[]) throws IOException {
		URL url1 = new URL("http://www.braingames.getput.com/mog");
		System.out.println(""+url1);
		
		URL url2 = new URL(url1, "downloads/MoG-datafiles.zip");
		System.out.println(""+url2);

		url1 = new URL("http://www.braingames.getput.com/mog/");
		System.out.println(""+url1);
		
		url2 = new URL(url1, "downloads/MoG-datafiles.zip");
		System.out.println(""+url2);
	}

}