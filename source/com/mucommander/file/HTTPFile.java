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
	private String absPath;
	private long date;
	private long size;
	
	private URL url;
	private FileURL fileURL;
	private boolean parentValSet;
	protected AbstractFile parent;
	
	/** True if the URL looks like */
	private boolean isHTML;
	
	
	/**
	 * Creates a new instance of HTTPFile.
	 */
	public HTTPFile(String absPath) throws IOException {
		this(new URL(absPath));
	}

	
	protected HTTPFile(URL url) throws IOException {
		this.url = url;
		this.absPath = url.toExternalForm();
		
		this.fileURL = new FileURL(absPath);
		int urlLen = absPath.length();

		if(!fileURL.getProtocol().toLowerCase().equals("http") || fileURL.getHost().equals(""))
			throw new IOException();
		
		// Remove trailing / (if any)
		if(absPath.endsWith(SEPARATOR))
			absPath = absPath.substring(0, --urlLen);

		// Determine file name (URL-encoded)
		this.name = fileURL.getFilename();

		String mimeType;
//System.out.println("MIME type = "+mimeType);
		// Test if based on the URL, the file looks like an HTML :
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
			HttpURLConnection conn = getHttpURLConnection();
	
			// Use HEAD instead of GET as we don't need the body
			conn.setRequestMethod("HEAD");
			
			// Open connection
			conn.connect();
			
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
	
	
	protected HTTPFile(String fileURL, URL context) throws IOException {
		this(new URL(context, fileURL));
	}

	private HttpURLConnection getHttpURLConnection() throws IOException {
		// Get URLConnection instance
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		
		// Set user-agent header
		conn.setRequestProperty("user-agent", com.mucommander.Launcher.USER_AGENT);
	
		return conn;
	}
	
	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
	public String getProtocol() {
		return "HTTP";
	}

	public String getName() {
		return name;
	}

	public String getAbsolutePath() {
		return absPath;
	}

	public String getSeparator() {
		return SEPARATOR;
	}

	public long getDate() {
		return date;
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
				try { this.parent = new HTTPFile(parentURL.getURL(false)); }
				catch(IOException e) {} // No problem, no parent that's all
			}
			this.parentValSet = true;
		}
		
		return this.parent;
	}
	

	protected void setParent(AbstractFile parent) {
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
		
		return ((HTTPFile)f).getAbsolutePath().equals(absPath);
	}
	
	
	public InputStream getInputStream() throws IOException {
		HttpURLConnection conn = getHttpURLConnection();
		conn.connect();
		return conn.getInputStream();
	}

	/** 
	 * Overrides AbstractFile's getInputStream(long) method to provide a more efficient implementation : 
	 * use the HTTP 1.1 header that resumes file transfer and skips a number of bytes.
	 */
	public InputStream getInputStream(long skipBytes) throws IOException {
		HttpURLConnection conn = getHttpURLConnection();
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
			// Open connection
			URLConnection conn = url.openConnection();
			
			// Set custom user-agent
			conn.setRequestProperty("user-agent", com.mucommander.Launcher.USER_AGENT);
			
			// Establish connection
			conn.connect();
			
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
			
			// Create a reader on the HTML content.
//				r = new InputStreamReader();
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
			while((tokenType=st.nextToken())!=StreamTokenizer.TT_EOF) {
				token = st.sval;
//System.out.println("token= "+token+" "+st.ttype+" "+(st.ttype==st.TT_WORD)+" prevToken="+prevToken);
//					if(st.ttype!=StreamTokenizer.TT_WORD)
				if(token==null)
					continue;
				
				if(tokenType=='\'' || tokenType=='"') {
					try {
//							if(token.toLowerCase().startsWith("http://") || (!token.equals("") && token.charAt(0)=='/')) {
						if(prevToken.equals("href") || prevToken.equals("src")) {
							if(!childrenURL.contains(token)) {
								child = new HTTPFile(token, this.url);
//									child = new HTTPFile(token);
								child.setParent(this);
								children.add(child);
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
//					e.printStackTrace();
			}
			throw new IOException();
		}
		finally {
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

}