package com.mucommander.file;

import java.io.*;
import java.net.*;
import java.util.Vector;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 */
public class HTMLFile extends HTTPFile implements ArchiveFile {

	
	/**
	 * Creates a new instance of HTMLFile.
	 */
	public HTMLFile(String fileURL) throws MalformedURLException {
		super(fileURL);
	}
	  
//	protected HTMLFile(URL url) {
//		super(url);
//	} 

	public HTMLFile(HTTPFile file) {
		super(file.url);
	}
	
	protected HTMLFile(String fileURL, URL context) throws MalformedURLException {
		super(new URL(context, fileURL));
	}

			
	public boolean isBrowsable() {
		return true;
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

}