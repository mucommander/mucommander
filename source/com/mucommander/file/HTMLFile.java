package com.mucommander.file;

import java.io.*;
import java.net.*;
import java.util.Vector;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * SMBFile represents an SMB file.
 */
public class HTMLFile extends AbstractFile {

	protected URL url;

	/** File separator is '/' for urls */
	private String separator = "/";
	
	private String name = null;
	private String absPath;
//	private long date = -1;
//	private long size = -1;

	private boolean isFolder;
	
	private AbstractFile parent;	
	
	
	/**
	 * Creates a new instance of HTMLFile.
	 */
	public HTMLFile(String fileURL) throws MalformedURLException {
		this(new URL(fileURL));
	}
	 
	protected HTMLFile(String fileURL, URL context) throws MalformedURLException {
		this(new URL(context, fileURL));
	}
		 

	protected void setParent(AbstractFile parent) {
		this.parent = parent;
	}

		 
	protected HTMLFile(URL url) {
		this.url = url;
		
//		this.absPath = url.toString();
		this.absPath = url.toExternalForm();

		// removes the ending separator character (if any)
		this.absPath = absPath.endsWith(separator)?absPath.substring(0,absPath.length()-1):absPath;

		int pos = absPath.lastIndexOf('/');
		
		this.name = absPath.substring(pos<7?7:pos+1, absPath.length());

		this.isFolder = name.endsWith("htm") || name.endsWith("html");
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
		return separator;
	}

	public long getDate() {
		return System.currentTimeMillis();
	}
	
	public long getSize() {
		return 0;
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
		return isFolder;
	}
	
	
	public boolean equals(Object f) {
		if(!(f instanceof HTMLFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile
		
		return ((HTMLFile)f).getAbsolutePath().equals(absPath);
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
		Vector children = new Vector();

	    EditorKit kit = new HTMLEditorKit();
	    Document doc = kit.createDefaultDocument();

	    // The Document class does not yet 
	    // handle charset's properly.
	    doc.putProperty("IgnoreCharsetDirective", 
	      Boolean.TRUE);
	    
		URLConnection conn;
		Reader r = null;
		try {
        // Open connection
        conn = url.openConnection();
			
        // Set custom user-agent
        conn.setRequestProperty("user-agent", "mucommander_"+com.mucommander.Launcher.MUCOMMANDER_VERSION);
			
		  // Create a reader on the HTML content.
	      r = new InputStreamReader(conn.getInputStream());

	      // Parse the HTML.
	      kit.read(r, doc, 0);

	      // Iterate through the elements 
	      // of the HTML document.
	      ElementIterator it = new ElementIterator(doc);
	      javax.swing.text.Element elem;
	      HTMLFile child;
		  String href;
		  while ((elem = it.next()) != null) {
			SimpleAttributeSet s = (SimpleAttributeSet)
				elem.getAttributes().getAttribute(HTML.Tag.A);
			if (s != null) {
			    try {
					href = ""+s.getAttribute(HTML.Attribute.HREF);
					if (href.startsWith("http://")) {
						child = new HTMLFile(href, url);
						child.parent = this;
						if(children.indexOf(child)==-1)
							children.add(child);
					}
				}
				catch(IOException e) {
				}
			}
	      }
	    } catch (Exception e) {
		    if (com.mucommander.Debug.ON) {
				System.out.println("Error while parsing HTML: "+e);
		    }
		}

		try {
			// Try and close URL connection reader
			if(r!=null)
				r.close();
		}
		catch(IOException e) {}

		Object childrenArray[] = new AbstractFile[children.size()];
		return (AbstractFile[])children.toArray(childrenArray);
	}

	public void mkdir(String name) throws IOException {
		throw new IOException();
	}
}