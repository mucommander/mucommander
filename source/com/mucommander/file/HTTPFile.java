package com.mucommander.file;

import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.swing.text.*;
import javax.swing.text.html.*;


/**
 *
 * @author Maxence Bernard
 */
public class HTTPFile extends AbstractFile implements RemoteFile {

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";
	
	private String name;
	private String absPath;
	private long date;
	private long size;
	
	private URL url;
	protected AbstractFile parent;
	
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

		// Determine file name (URL-encoded)
		int urlLen = absPath.length();
		int pos = absPath.lastIndexOf('/');
		this.name = URLDecoder.decode(absPath.substring(pos<7?7:pos+1, absPath.endsWith(SEPARATOR)?urlLen-1:urlLen));

		// Get URLConnection instance
		URLConnection conn = url.openConnection();
		
		// Set user-agent header
		conn.setRequestProperty("user-agent", com.mucommander.Launcher.MUCOMMANDER_APP_STRING);

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
		if(contentType!=null && contentType.equals("text/html"))
			isHTML = true;

System.out.println("isHTML ="+isHTML); 
	}

	protected HTTPFile(String fileURL, URL context) throws IOException {
		this(new URL(context, fileURL));
	}

	
	protected void setParent(AbstractFile parent) {
		this.parent = parent;
	}

	
	public String getName() {
		return name;
	}

	/**
	 * Returns a String representation of this AbstractFile which is the name as returned by getName().
	 */
	public String toString() {
		return getName();
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
//		if(file==null)
//			return null;
//		
//		String parent = file.getParent();
//        // SmbFile.getParent() never returns null
//		if(parent.equals("smb://"))
//            return null;
//        
//		return new SMBFile(parent);
		
		return null;
	}
	
	
	public boolean exists() {
		try {
			url.openStream().close();
			return true;
		}
		catch(Exception e) {
			return false;
		}
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
	
	public boolean equals(Object f) {
		if(!(f instanceof HTTPFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile
		
		return ((HTTPFile)f).getAbsolutePath().equals(absPath);
	}
	
	
	public InputStream getInputStream() throws IOException {
		return url.openStream();
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return null;
	}
		
	public boolean moveTo(AbstractFile dest) throws IOException  {
		return false;
	}

	public void delete() throws IOException {
		throw new IOException();
	}

	public AbstractFile[] ls() throws IOException {
		if(!isHTML)
			throw new IOException();
		else {

System.out.println("parsing + "+getAbsolutePath()); 
			Vector children = new Vector();

			EditorKit kit = new HTMLEditorKit();
			Document doc = kit.createDefaultDocument();

//			// The Document class does not yet 
//			// handle charset's properly.
//			doc.putProperty("IgnoreCharsetDirective", 
//			  Boolean.TRUE);
			
			URLConnection conn = null;
			Reader r = null;
			try {
				// Open connection
				conn = url.openConnection();
				
				// Set custom user-agent
				conn.setRequestProperty("user-agent", com.mucommander.Launcher.MUCOMMANDER_APP_STRING);
				
				// Create a reader on the HTML content.
				r = new InputStreamReader(conn.getInputStream());
				
				// Parse the HTML.
				kit.read(r, doc, 0);
				
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
				try {
					// Try and close URL connection
					if(r!=null)
						r.close();
				}
				catch(IOException e) {}
			}
		}
	}

	public void mkdir(String name) throws IOException {
		throw new IOException();
	}
}