package com.mucommander.xml;

import java.io.*;
import java.util.Iterator;

/**
 * Used to write pretty-printed XML content.
 * <p>
 * Writing XML content with this class is meant to be just about as easy and straightforward
 * as possible:<br/>
 * - open your XML stream ({@link #XmlWriter(File)} or {@link #XmlWriter(OutputStream)}).<br/>
 * - open and close tags ({@link #openTag(String)} and {@link #closeTag(String)}).<br/>
 * - add any CDATA you need ({@link #writeCData(String)}).
 * </p>
 * <p>
 * It's important to realize that no coherency checking whatsoever is performed.
 * There's nothing to prevent developers from closing tags they haven't opened yet, or
 * duplicating attribute names, or pretty much any other silly thing you can think of in XML.
 * </p>
 * @author Nicolas Rinaudo
 */
public class XmlWriter {
    // - Constants -------------------------------------------------------
    // -------------------------------------------------------------------
    /** Number of space characters used for one level of indentation. */
    private static final int OFFSET_INCREMENT       = 4;
    /** Identifier for publicly accessible objects. */
    public  static final String AVAILABILITY_PUBLIC = "PUBLIC";
    /** Identifier for system resources. */
    public  static final String AVAILABILITY_SYSTEM = "SYSTEM";



    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Where to write the XML content to. */
    private PrintStream out;
    /** Current indentation offset. */
    private int         offset;
    /** Whether the next tag opening or closing operation should be indented. */
    private boolean     printIndentation;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates an XmlWriter that will write to the specified file.
     * <p>
     * The generated XML content will be encoded using <code>UTF-8</code>.
     * </p>
     * @param     file                         where the XmlWriter should write its content to.
     * @exception FileNotFoundException        thrown if <code>file</code> could not be found.
     * @exception UnsupportedEncodingException thrown if <code>UTF-8</code> is not supported.
     */
    public XmlWriter(File file) throws FileNotFoundException, UnsupportedEncodingException {this(new FileOutputStream(file));}

    /**
     * Creates an XmlWriter that will write to the specified file using the specified encoding.
     * @param     file                         where the XmlWriter should write its content to.
     * @param     encoding                     encoding to use when writing the XML content.
     * @exception FileNotFoundException        thrown if <code>file</code> could not be found.
     * @exception UnsupportedEncodingException thrown if <code>encoding</code> is not supported.
     */
    public XmlWriter(File file, String encoding) throws FileNotFoundException, UnsupportedEncodingException {
        this(new FileOutputStream(file), encoding);
    }

    /**
     * Creates an XmlWriter that will write to the specified output stream.
     * <p>
     * The generated XML content will be encoded using <code>UTF-8</code>.
     * </p>
     * @param     stream                       where the XmlWriter should write its content to.
     * @exception FileNotFoundException        thrown if <code>file</code> could not be found.
     * @exception UnsupportedEncodingException thrown if <code>UTF-8</code> is not supported.
     */
    public XmlWriter(OutputStream stream) throws FileNotFoundException, UnsupportedEncodingException {
        this(stream, "UTF-8");
    }

    /**
     * Creates an XmlWriter that will write to the specified stream.
     * @param     stream                       where the XmlWriter should write its content to.
     * @param     encoding                     encoding to use when writing the XML content.
     * @exception FileNotFoundException        thrown if <code>file</code> could not be found.
     * @exception UnsupportedEncodingException thrown if <code>encoding</code> is not supported.
     */
    public XmlWriter(OutputStream stream, String encoding) throws UnsupportedEncodingException {
        out = new PrintStream(stream, true, encoding);

        // XML header
        out.print("<?xml version=\"1.0\" encoding=\"");
        out.print(encoding);
        out.println("\"?>");
    }



    // - Tag operations --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the document type declaration of the XML file.
     * <p>
     * For the generated XML content to be valid, developers should make sure that this
     * is the very first method they call. This class doesn't ensure coherency and won't
     * complain if the <code>DOCTYPE</code> statement is the last in the file.
     * </p>
     * <p>
     * Both <code>description</code> and <code>url</code> can be set to <code>null</code>.
     * If so, they will just be ignored.
     * </p>
     * @param topElement   label of the top element in the XML file.
     * @param availability availability of the document (expected to be either {@link #AVAILABILITY_PUBLIC}
     *                     or {@link #AVAILABILITY_SYSTEM}, but this is not enforced).
     * @param description  description of the file (see DOCTYPE specifications for more information).
     * @param url          URL at which the DTD of the XML file can be downloaded.
     */
    public void writeDocType(String topElement, String availability, String description, String url) {
        // Writes the compulsory bits.
        out.print("<!DOCTYPE ");
        out.print(topElement);
        out.print(' ');
        out.print(availability);

        // Writes the description if present.
        if(description != null) {
            out.print(' ');
            out.print('\"');
            out.print(description);
            out.print('\"');
        }

        // Writes the DTD url if present.
        if(url != null) {
            out.print(' ');
            out.print('\"');
            out.print(url);
            out.print('\"');
        }
        out.println('>');
    }

    /**
     * Writes a tag opening sequence.
     * <p>
     * Tags opened using this method will not have any attribute, and will
     * need to be closed using a {@link #closeTag(String)} call.
     * </p>
     * @param name name of the tag to open.
     * @see #openTag(XmlAttributes)
     * @see @writeStandAloneTag(String)
     * @see @writeStandAloneTag(String,XmlAttributes)
     */
    public void openTag(String name) {openTag(name, false, null);}

    /**
     * Writes a stand-alone tag.
     * <p>
     * Tags opened using this method will not have any attributes, and will be
     * closed immediately.
     * </p>
     * @param name name of the tag to write.
     * @see #openTag(String,XmlAttributes)
     * @see #openTag(String)
     * @see @writeStandAloneTag(String)
     */
    public void writeStandAloneTag(String name) {openTag(name, true, null);}

    /**
     * Writes a tag opening sequence.
     * <p>
     * Tags opened using this method will need to be closed using a {@link #closeTag(String)} call.
     * </p>
     * @param name       name of the tag to open.
     * @param attributes attributes that this tag will have.
     * @see #openTag(String)
     * @see @writeStandAloneTag(String)
     * @see @writeStandAloneTag(String,XmlAttributes)
     */
    public void openTag(String name, XmlAttributes attributes) {openTag(name, false, attributes);}

    /**
     * Writes a stand-alone tag.
     * <p>
     * Tags opened using this method will not need to be closed
     * </p>
     * @param name       name of the tag to write.
     * @param attributes attributes that this tag will be closed immediately.
     * @see #openTag(String)
     * @see #openTag(String,XmlAttributes)
     * @see @writeStandAloneTag(String)
     */
    public void writeStandAloneTag(String name, XmlAttributes attributes) {openTag(name, true, attributes);}

    /**
     * Writes a tag opening sequence.
     * @param name         name of the tag to open.
     * @param isStandAlone whether or not this tag should be closed immediately.
     * @param attributes   XML attributes for this tag.
     */
    private void openTag(String name, boolean isStandAlone, XmlAttributes attributes) {
        // Prints indentation if necessary.
        indent();

        // Opens the tag.
        out.print('<');
        out.print(name);

        // Writes attributes, if any.
        if(attributes != null) {
            Iterator names;
            String   attName;

            names = attributes.names();
            while(names.hasNext()) {
                attName = (String)names.next();
                out.print(' ');
                out.print(attName);
                out.print("=\"");
                out.print(attributes.getValue(attName));
                out.print("\"");
            }
        }

        // Closes the tag if necessary.
        if(isStandAlone)
            out.print('/');
        else
            offset += OFFSET_INCREMENT;

        // Finishes the tag opening sequence.
        out.print('>');
    }

    /**
     * Writes a tag closing sequence.
     * @param name name of the tag to close.
     */
    public void closeTag(String name) {
        // Updates the indentation, and prints it if necessary.
        offset -= OFFSET_INCREMENT;
        indent();

        // Writes the tag closing sequence.
        out.print("</");
        out.print(name);
        out.print('>');
        println();
    }



    // - CDATA handling --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the specified CDATA to the XML stream.
     * <p>
     * Note that you can use this method whenever you want. While this might seem obvious,
     * it means that you can use it, for example, to add XML headers before you even opened
     * your first tag.
     * </p>
     * @param cdata content to write to the XML stream.
     */
    public void writeCData(String cdata) {
        indent();
        out.print(cdata);
    }



    // - Indentation handling --------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Prints a line break.
     * <p>
     * The next line will be indented.
     * </p>
     */
    public void println() {
        out.println();
        printIndentation = true;
    }

    /**
     * If necessary, prints indentation.
     */
    private void indent() {
        if(printIndentation) {
            for(int i = 0; i < offset; i++)
                out.print(' ');
            printIndentation = false;
        }
    }



    // - Misc. -----------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Closes the XML stream.
     */
    public void close() throws IOException {out.close();}
}
