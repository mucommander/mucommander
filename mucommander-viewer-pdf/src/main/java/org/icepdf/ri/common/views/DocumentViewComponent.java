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

package org.icepdf.ri.common.views;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.icepdf.core.util.Library;
import org.icepdf.ri.viewer.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Each document in the collection will be represented by a DocumentViewComponent.
 * When double click the respective document will be opened in a new viewer
 * window if the file name ends width ".pdf".
 *
 * @since 5.1.0
 */
public class DocumentViewComponent extends JComponent implements MouseListener, Runnable {

    private static final long serialVersionUID = -8881023489246309889L;
   
    private Library parentLibrary;
    private Reference fileReference;
    private String fileName;
    private boolean isPdfDocument;

    public static final String PDF_EXTENSION = ".pdf";

    private static int minimumThumbHeight = 110;
    private static int minimumThumbWidth = 85;

    private Dimension pageSize = new Dimension(minimumThumbWidth, minimumThumbHeight);
    private SoftReference<BufferedImage> documentThumbNail =
            new SoftReference<BufferedImage>(null);

    public DocumentViewComponent(Library parentLibrary, String fileName, Reference fileReference) {
        this.parentLibrary = parentLibrary;
        this.fileName = fileName;
        this.fileReference = fileReference;

        addMouseListener(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // grab  thread from the library and capture the first page of the document
        // and then clean up, as we might never access the document again
        isPdfDocument = fileName.toLowerCase().endsWith(PDF_EXTENSION);
        if (isPdfDocument) {
            Library.execute(this);
        }
    }

    /**
     * Kick off the rendering of the first page for preview purposes, document
     * is closed after the thumbnail is created.
     */
    public void run() {

        try {
            Stream fileStream = (Stream) parentLibrary.getObject(fileReference);
            InputStream fileInputStream = fileStream.getDecodedByteArrayInputStream();
            Document embeddedDocument = new Document();
            embeddedDocument.setInputStream(fileInputStream, fileName);
            // capture the first page.
            Page page = embeddedDocument.getPageTree().getPage(0);
            page.init();

            // calculate how big/small the thumbnail needs to be
            PDimension defaultSize = page.getSize(Page.BOUNDARY_CROPBOX, 0, 1f);
            float scale = minimumThumbHeight / (float) defaultSize.getHeight();
            pageSize = page.getSize(Page.BOUNDARY_CROPBOX, 0, scale).toDimension();

            int pageWidth = (int) pageSize.getWidth();
            int pageHeight = (int) pageSize.getHeight();

            BufferedImage image = ImageUtility.createTranslucentCompatibleImage(pageWidth, pageHeight);
            Graphics g = image.createGraphics();

            page.paint(g, GraphicsRenderingHints.PRINT,
                    Page.BOUNDARY_CROPBOX, 0, scale);
            g.dispose();

            documentThumbNail = new SoftReference<BufferedImage>(image);

            // que the repaint.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    revalidate();
                    repaint();
                }
            });

            // close the document.
            embeddedDocument.dispose();

        } catch (Throwable ex) {
            isPdfDocument = false;
        }
    }

    public void paintComponent(Graphics gg) {
        // paint the thumbnail if any
        BufferedImage pageThumbNail = documentThumbNail.get();
        if (pageThumbNail != null) {
            Graphics2D g = (Graphics2D) gg;
            g.drawImage(pageThumbNail, 0, 0, null);
        }
    }

    public Dimension getPreferredSize() {
        return pageSize;
    }

    /**
     * On a mouse double click we attempt to lod the PDF document in a new
     * viewer window.
     *
     * @param e mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && isPdfDocument) {
            try {
                Stream fileStream = (Stream) parentLibrary.getObject(fileReference);
                InputStream fileInputStream = fileStream.getDecodedByteArrayInputStream();
                Document embeddedDocument = new Document();
                embeddedDocument.setInputStream(fileInputStream, fileName);
                WindowManager.getInstance().newWindow(embeddedDocument, fileName);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }


}
