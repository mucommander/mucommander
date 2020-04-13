/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.util;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.w3c.dom.DOMImplementation;

import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>SVG</code> class is a utility for writing PDF content to SVG
 * format.
 *
 * @since 1.0
 */
public class SVG {

    private static final Logger logger =
            Logger.getLogger(SVG.class.toString());

    /**
     * Creates a SVG character stream for the given <code>Document</code> and
     * <code>PageNumber</code>.
     *
     * @param pdfDocument Document containing the PDF data
     * @param pageNumber  page number of PDF content that will be rendered to SVG.
     *                    Zero-based index
     * @param out         character stream that the SVG data will be written to
     */
    public static void createSVG(Document pdfDocument, int pageNumber, Writer out) {
        try {

            if (pdfDocument != null &&
                    (pageNumber >= 0 && pageNumber < pdfDocument.getNumberOfPages())) {
                // Get a DOMImplementation
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                // Create an instance of org.w3c.dom.Document
                org.w3c.dom.Document document = domImpl.createDocument(null, "svg",
                        null);
                // Create an instance of the SVG Generator
                SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                // Ask the test to render into the SVG Graphics2D implementation

                float userRotation = 0;
                float userZoom = 1;
                PDimension pdfDimension = pdfDocument.getPageDimension(pageNumber, userRotation, userZoom);
                svgGenerator.setSVGCanvasSize(pdfDimension.toDimension());

                pdfDocument.paintPage(pageNumber, svgGenerator,
                        GraphicsRenderingHints.PRINT,
                        Page.BOUNDARY_CROPBOX,
                        userRotation, userZoom);

                // Finally, stream out SVG to the standard output using UTF-8
                // character to byte encoding
                boolean useCSS = true;              // we want to use CSS style attribute
                //            File f=new File("a.svg");
                //            Writer out = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
                svgGenerator.stream(out, useCSS);
            }
        } catch (org.apache.batik.svggen.SVGGraphics2DIOException e) {
            logger.log(Level.SEVERE, "Error creating svg document.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Error creating svg document.", e);
        }
    }
}
