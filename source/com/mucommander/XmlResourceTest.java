/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.ResourceLoader;
import junit.framework.TestCase;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

/**
 * This test case checks all XML documents that are embedded with muCommander and ensures they are well-formed.
 *
 * @author Maxence Bernard
 */
public class XmlResourceTest extends TestCase {

    /**
     * This test case checks all XML documents that are embedded with muCommander and ensures they are well-formed.
     * The test fails when an error is encountered in one of the XML files, even a recoverable one.
     * All XML files located in muCommander's base classpath are tested, whether this test case is invoked from the
     * application's JAR file or from a regular directory. Archive files found in the application's path will also be
     * searched for XML files.
     *
     * @throws IOException if a file or a folder couldn't be accessed
     * @throws SAXException if an error was found in one of the XML documents
     * @throws ParserConfigurationException if 'a serious configuration error' occurred in the XML parser
     */
    public void testXmlResources() throws SAXException, IOException, ParserConfigurationException {
        testXMLFiles(ResourceLoader.getRootPackageAsFile(XmlResourceTest.class));
    }


    /////////////////////
    // Support methods //
    /////////////////////

    /**
     * Looks for XML files in the specified folder recursively and tests them for well-formedness.
     * A <code>org.xml.sax.SAXException</code> is thrown when an error is encountered in one of the XML files,
     * even a recoverable one. Any archive contained in the folder will be searched for XML files.
     *
     * @param folder the folder in which to look for XML files recursively.
     * @throws IOException if a file or a folder couldn't be accessed
     * @throws SAXException if an error was found in one of the XML documents
     * @throws ParserConfigurationException if 'a serious configuration error' occurred in the XML parser
     */
    private void testXMLFiles(AbstractFile folder) throws SAXException, IOException, ParserConfigurationException {
        AbstractFile children[] = folder.ls();
        for(int i=0; i<children.length; i++) {
            if(children[i].isBrowsable())
                testXMLFiles(children[i]);
            else if("xml".equals(children[i].getExtension()))
                testXMLDocument(children[i]);
        }
    }

    /**
     * Checks the specified XML file that are embedded with muCommander for well-formedness. The test fails
     * whenever an error is encountered in one of the XML files, even a recoverable one.
     *
     * @param file the file to parse and check for well-formedness.
     * @throws IOException if there was an error reading the file
     * @throws SAXException if an error was found in XML document
     * @throws ParserConfigurationException 'a serious configuration error' occurred in the XML parser
     */
    private void testXMLDocument(AbstractFile file) throws SAXException, IOException, ParserConfigurationException {
        System.out.println("Parsing "+file.getAbsolutePath());

        SAXParserFactory.newInstance().newSAXParser().parse(file.getInputStream(), new SAXErrorHandler());
    }

    /**
     * This SAX handler reports errors that are found in the parsed XML document on the standard ouput and throws
     * exceptions to the parser. 
     */
    private static class SAXErrorHandler extends DefaultHandler {

        public void warning(SAXParseException e) throws SAXException {
            printSAXError(e, "warning");
            throw e;
        }

        public void error(SAXParseException e) throws SAXException {
            printSAXError(e, "error");
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            printSAXError(e, "fatal error");
            throw e;
        }

        private void printSAXError(SAXParseException e, String errorType) {
            System.out.println("SAX "+errorType+" at line "+e.getLineNumber()+", column "+e.getColumnNumber()+" : "+e);
        }
    }

}
