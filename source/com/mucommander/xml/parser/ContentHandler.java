package com.mucommander.xml.parser;

import java.util.Hashtable;

/**
 * Receives notification of the logical content of a document.
 *
 * <p>This is the main interface that applications implement: if the application needs to be informed
 * of basic parsing events, it implements this interface and registers an instance with the parser.
 * The parser uses the instance to report basic document-related events like the start and end of
 * elements and character data.</p>
 *
 * <p>The order of events in this interface is very important, and mirrors the order of information
 * in the document itself. For example, all of an element's content (character data and/or subelements)
 * will appear, in order, between the <code>startElement event and the corresponding </code>endElement event.</p>
 *
 * <p>Depending on the contents received by the parser, the application may wish to throw an exception.
 * The parser will then immediately stop parsing the document, call the endDocument method and throw 
 * the exception back to the caller of the 
 * {@link com.mucommander.xml.parser.Parser#parse(java.io.InputStream,ContentHandler,String) Parser.parse} method.
 * The application may also wish to filter the exceptions thrown by the ContentHandler from those
 * thrown directly by the parser. This can be done by creating a custom Exception class and throwing
 * it in the ContentHandler.
 *
 * @author Maxence Bernard
 * @version 1.0
 */
public interface ContentHandler  {

	/**
	 * Receives notification of the beginning of a document.
	 *
	 * <p>The parser will invoke this method only once, before any other event callbacks.</p>
	 *
	 * @throws Exception any exception. If an exception is thrown the parser will stop parsing
	 * the current document, call the {@link #endDocument() endDocument} method and throw this
	 * exception back to the caller of the
	 * {@link com.mucommander.xml.parser.Parser#parse(java.io.InputStream,ContentHandler,String) Parser.parse} method.
	 */
	public void startDocument() throws Exception;

	/**
	 * Receives notification of the end of a document.
	 *
	 * <p>The parser will invoke this method only once, and it will be the last method invoked
	 * during the parse. The parser shall not invoke this method until it has either abandoned parsing
	 * (because of an unrecoverable error) or reached the end of input.</p>
	 *
	 * @throws Exception any exception. If an exception is thrown the parser will throw this
	 * exception back to the caller of the
	 * {@link com.mucommander.xml.parser.Parser#parse(java.io.InputStream,ContentHandler,String) Parser.parse} method.
	 */
	public void endDocument() throws Exception;

	/**
	 * Receives notification of the beginning of an element.
	 *
	 * <p>The parser will invoke this method at the beginning of every element in the XML document; 
	 * there will be a corresponding endElement event for every startElement event 
	 * (even when the element is empty). All of the element's content will be reported, in order, 
	 * before the corresponding endElement event.</p>
	 *
	 * <p>None of the parameters passed by the parser will contain <code>null</code> values.
	 *
	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI 
	 * or if Namespace support has been disabled in the parser.
	 *
	 * @param name element name. The name may appear as a qualified name (i.e. with a colon separating
	 * the Namespace prefix from the local name) if a Namespace prefix has been specified for 
	 * this element.
	 *
	 * @param attValues an Hashtable that maps the attributes name to their value. If there are no 
	 * attributes or attribute support has been disabled, an empty an Hashtable will be passed.<br>
	 * Note that Namespace declaration attributes (xmlns* attributes) are included in the Hashtable.
	 *
	 * @param attURIs an Hashtable that maps the attributes name to their URI. If there are no 
	 * attributes or attribute or Namespace support has been disabled, an empty an Hashtable will be
	 * passed.
	 * Attributes with no Namespace URI will have an empty string value in the Hashtable.<br>
	 * Note that Namespace declaration attributes (xmlns* attributes) are included in the Hashtable,
	 * their URI is an empty string.
	 *
	 * @throws Exception any exception. If an exception is thrown the parser will stop parsing
	 * the current document, call the {@link #endDocument() endDocument} method and throw this
	 * exception back to the caller of the
	 * {@link com.mucommander.xml.parser.Parser#parse(java.io.InputStream,ContentHandler,String) Parser.parse} method.
	 */
	public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception;

	/**
	 * Receives notification of the end of an element.
	 *
	 * <p>The parser will invoke this method at the end of every element in the XML document; 
	 * there will be a corresponding startElement() event for every endElement() event 
	 * (even when the element is empty).</p>
	 *
	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI 
	 * or if Namespace support has been disabled in the parser.
	 *
	 * @param name element name. The name may appear as a qualified name (i.e. with a colon separating
	 * the Namespace prefix from the local name) if a Namespace prefix has been specified for 
	 * this element.
	 *
	 * @throws Exception any exception. If an exception is thrown the parser will stop parsing
	 * the current document, call the {@link #endDocument() endDocument} method and throw this
	 * exception back to the caller of the
	 * {@link com.mucommander.xml.parser.Parser#parse(java.io.InputStream,ContentHandler,String) Parser.parse} method.
	 */
	public void endElement(String uri, String name) throws Exception;
	
	/**
	 * Receives notification of character data.
	 * 
	 * <p>The parser will call this method to report each chunk of character data.
	 * It may return all contiguous character data in a single chunk, or may split it into several
	 * chunks.</p>
	 *
	 * <p>Whitespace will be reported unless it has been disabled in the parser.</p>
	 * 
	 * @param s the characters from the XML document, wrapped in a String object.
	 *
	 * @throws Exception any exception. If an exception is thrown the parser will stop parsing
	 * the current document, call the {@link #endDocument() endDocument} method and throw this
	 * exception back to the caller of the
	 * {@link com.mucommander.xml.parser.Parser#parse(java.io.InputStream,ContentHandler,String) Parser.parse} method.
	 */
	public void characters(String s) throws Exception;
}
