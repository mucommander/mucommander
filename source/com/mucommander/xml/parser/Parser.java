package com.mucommander.xml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * The class responsible for parsing an XML document and notifying the registered handler of
 * parsing events.
 *
 * <p>There are a number of features that can be statically enabled/disabled by setting the
 * corresponding field to <code>true</code>/<code>false</code>. Disabling unneeded features
 * will yield to a smaller class file as well as faster execution time.
 *
 * @author Maxence Bernard
 * @version 1.0
 */
public class Parser {

	/**
	 * If set to <code>false</code>, attributes will be ignored and a <code>null</code> value for
	 * the attributes parameter will be passed to 
	 * {@link com.mucommander.xml.parser.ContentHandler#startElement(String, String, Hashtable, Hashtable) ContentHandler.startElement()}.
	 * Also when set to <code>false</code>, the corresponding code block will be removed at compile time.
	 * <br>Default value is <code>true</code>.
	 */
	private final static boolean ATTRIBUTE_SUPPORT = true;
	
	/**
	 * If set to <code>true</code>, predefined (amp, apos, gt, lt, quot) and numbered (decimal only)
	 * character entities will be decoded.
	 * <br>Default value is <code>true</code>.
	 */
	private final static boolean CHARACTER_ENTITIES_SUPPORT = true;
	
	/**
	 * If set to <code>true</code>, Namespaces for elements and attributes will be parsed 
	 * and then sent to {@link com.mucommander.xml.parser.ContentHandler#startElement(String, String, Hashtable, Hashtable) ContentHandler.startElement()}.
	 * <br>Default value is <code>true</code>.
	 */
	private final static boolean NAMESPACE_SUPPORT = false;

	/**
	 * If set to <code>true</code>, {@link com.mucommander.xml.parser.ContentHandler#characters(String)
	 * ContentHandler.characters()} will NOT be called for strings that only contain whitespace 
	 * characters.
	 * <br>Default value is <code>false</code>.
	 */
	private final static boolean IGNORE_WHITESPACE_STRINGS = false;
	
	
	/**
	 * Array that maps predefined character entities to their decoded value.
	 */
	private final static String CHAR_MAP[][] =  {
		{"&amp;","&"},
		{"&lt;","<"},
		{"&gt;",">"},
		{"&apos;","'"},
		{"&quot;","\""}
	};
	
	
	/**
	 * Used to read the XML document.
	 */
	private InputStreamReader in;
	
	/**
	 * The registered handler that receives parsing events.
	 */
	private ContentHandler handler;
	
	/**
	 * Maps a namespace prefix to a URI.
	 */
	private Hashtable nsTable;
	
	
	/**
	 * Creates a new instance of <code>Parser</code>.
 	 *
	 * <p>Note: a single instance of this class can be used to parse several XML documents.</p>
	 */
	public Parser()  {
	}
	
	/**
	 * Parses an XML Document.
	 *
	 * The given InputStream will *not* be closed after the parser has finished parsing this file.
	 * This means additional data can be read from this InputStream after a call to this method.
	 *
	 * @param in the <code>InputStream</code> from which the XML document will be read.
	 * @param handler the content handler.
	 * @param encoding use the specified encoding to read the InputStream or the system's default encoding
	 * if a <code>null</code> value was passed.
	 *
	 * @throws IOException if an error occurred while trying to read from the InputStream or if
	 * the end of the stream has been reached prematurely.
	 */
	public void parse(InputStream in, ContentHandler handler, String encoding) throws Exception {
		this.in = encoding==null?new InputStreamReader(in):new InputStreamReader(in, encoding);
		this.handler = handler;

		// Creates Namespace table if needed
		if(NAMESPACE_SUPPORT)
			nsTable = new Hashtable();

		try {
			// Notifies the ContentHandler that document starts.
			handler.startDocument();

			// Skips prolog (if any)
			int c;
			while(readChar()!='<' || (c=readChar())=='?' || c=='!'); 

			// Parses the root element recursively
			parseElement((char)c, "");
		}
		catch(Exception e) {
			// Notifies the ContentHandler that document ends.
			handler.endDocument();
			throw e;
		}

		// Notifies the ContentHandler that document ends.
		handler.endDocument();
		
		// Explicitely set those variables to null so that they can be GCd 		
		in = null;
		handler = null;
		
		if(NAMESPACE_SUPPORT)
			nsTable = null;
	}

	/**
	 * Reads and returns the next char from the InputStream.
	 * This method ensures that the parser will never get stuck receiving -1 char if the EOF
	 * has been reached unexpectedly.
	 *
	 * @return the next char from the InputStream
	 * @throws IOException if an error occurred while trying to read from the InputStream or if EOF
	 * has been reached.
	 */
	private char readChar() throws IOException {
		int c = in.read();
		if(c==-1)
			throw new IOException();		
		return (char)c;
	}

	/**
	 * Returns the next token from the InputStream using the given delimiter.
	 *
	 * @param delim the delimiter character
	 * @return the next token from the InputStream
	 * @throws IOException if an error occurred while trying to read from the InputStream.
	 */
	private String nextToken(char delim) throws IOException {
		StringBuffer sb = new StringBuffer();
		char ch;
		while((ch=(char)readChar())!=delim)
			sb.append(ch);
		return sb.toString();	
	}

	/**
	 * Decodes predefined and numbered character entities.
	 *
	 * @param s the String to be decoded (with &xxxx; entities)
	 * @return the decoded String (without &xxxx; entities)
	 */
	private String decode(String s) {
		char c;
		int pos = 0;
		int pos2;
		
		// Decodes numbered character entities
		while ((pos=s.indexOf("&#",pos))!=-1) {
			pos2 = s.indexOf(';',pos+1);
			// hexadecimal encoded numbers begin with an 'x' character
			c = (char)(s.charAt(pos+2)=='x'?Integer.parseInt(s.substring(pos+3,pos2), 16):Integer.parseInt(s.substring(pos+2,pos2)));
			s = s.substring(0,pos)+ c +s.substring(pos2+1);
			pos++;
		}

		// Decodes predefined character entities
		for(int i=0; i<CHAR_MAP.length; i++) {
			pos = 0;
			while ((pos=s.indexOf(CHAR_MAP[i][0], pos))!=-1) {
				s = s.substring(0,pos)+CHAR_MAP[i][1]+s.substring(pos+CHAR_MAP[i][0].length());
				pos ++;
			}
		}
	
		return s;
	}
	
	/**
	 * Parses an element recursively.
	 *
	 * @param c the first character after the begin element character '<'
	 * @param defaultNsURI the default namespace prefix inherited by this element
	 *
	 * @throws Exception a parsing exception or an exception thrown by the ContentHandler
	 */
	private void parseElement(char c, String defaultNsURI) throws Exception {
		/* Temporary variables */
		String s="";
		String name;
		String uri="";
		StringBuffer sb;
		Hashtable attValues = new Hashtable();
		Hashtable attURIs = new Hashtable();
		int pos;
		int len;
		boolean emptyElement = false;

		/* Handles comments, processing instructions and CDATA */
		if (c=='!')  {	// CDATA section
			if (readChar()=='[') {	
				// skips CDATA[
				in.skip(6);
				// looks for the ]]> string
				while(!(s+=readChar()).endsWith("]]>"));
				// 
				handler.characters(s.substring(0,s.length()-3));
			}
			else	// Comment section
				while(readChar()!='-' || readChar()!='-' || readChar()!='>');
			return;
		}
		else if (c=='?') {	// Processing instruction
			// 
			while(readChar()!='?' || readChar()!='>');
			return;
		}

		/* Element name parsing */
		s = c+nextToken('>');
		len = s.length();
		
		if (s.charAt(len-1)=='/') {
			s = s.substring(0,--len);
			emptyElement = true;
		}
		sb = new StringBuffer();
		pos = 0;
		while (pos!=len&&(c=s.charAt(pos++))!=' '&&(c<9||c>13))
			sb.append(c);
		name = sb.toString();

		/* Attributes parsing */
		if (ATTRIBUTE_SUPPORT) {		// if false, the following code block will be removed at compile time
				String attName;
				String attValue;
				
				while (true) {
					// Skips whitespace chars
					while (pos<len&&((c=s.charAt(pos++))==' '||(c>8 && c<14)));
					if(pos==len)
						break;

					// Parses attribute name
					attName = s.substring(pos-1,(pos=s.indexOf('=',pos))).trim();
					
					// Skips whitespace chars
					while (++pos!=len&&((c=s.charAt(pos))==' '||(c>8 && c<14)));
					
					// Parses and decodes (if support enabled) attribute value
					attValue = s.substring(pos+1, (pos=s.indexOf(c,pos+1)));
					attValue = CHARACTER_ENTITIES_SUPPORT?decode(attValue):attValue;

					// Adds the new attribute to the Hashtable
					attValues.put(
						attName,
						attValue
					);
					
					if (NAMESPACE_SUPPORT)  {
						int colonPos = attName.indexOf(':');
												
						// Checks for xmlns attribute
						if (attName.indexOf("xmlns")==0)  {
							if(attName.equals("xmlns"))	// ns defaulting
								defaultNsURI = attValue;
							else						// ns declaration
								nsTable.put(attName.substring(colonPos+1, attName.length()), attValue);
						}
						
						// Parses the ns prefix (if any) and retrieves the corresponding URI
						uri = colonPos==-1?"":(String)nsTable.get(attName.substring(0, colonPos));
						// Maps the att name to the ns URI
						attURIs.put(attName, uri==null?"":uri);
					}
					
					pos++;
				}
		}

		// Resolves URI for this element if Namespace support is enabled
		if (NAMESPACE_SUPPORT) {
			pos = name.indexOf(':');
			uri = pos==-1?defaultNsURI:(String)nsTable.get(name.substring(0, pos));
		}
	
		// Notifies the ContentHandler that this element has started
		handler.startElement(uri, name, attValues, attURIs);

		// If the element is empty, notifies the ContentHandler that this element has ended and return
		if (emptyElement) {
			handler.endElement(uri, name);
			return;
		}
		
		// Parses character data and children elements recursively
		while(true) {
			s = nextToken('<');

			// If DECODE_CHARACTERS is set to false, the following code block will be removed at compile time
			if (CHARACTER_ENTITIES_SUPPORT)  {
				s = decode(s);
			}
			
			// Sends character data to the ContentHandler
			if(!(IGNORE_WHITESPACE_STRINGS && s.trim().equals("")))
				handler.characters(s);

			c=(char)readChar();
			// Element ends
			if (c=='/')  {
				// Notifies the ContentHandler that this element ends
				handler.endElement(uri, name);
				while(readChar()!='>');
				return;
			}
			
			// parses child element
			parseElement(c, defaultNsURI);
		}				
	}
}
