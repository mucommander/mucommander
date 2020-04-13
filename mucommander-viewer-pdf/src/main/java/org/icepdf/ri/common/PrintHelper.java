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
package org.icepdf.ri.common;

import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.PageTree;
import org.icepdf.core.util.GraphicsRenderingHints;

import javax.print.*;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The <code>PrintHelper</code> class is utility class to aid developers in
 * printing PDF document content.  The PrintHelper takes advantage of the
 * Pageable and Printable interfaces availabe in Java 2.</p>
 *
 * @since 2.0
 */
public class PrintHelper implements Printable {

    private static final Logger logger =
            Logger.getLogger(PrintHelper.class.toString());

    private PageTree pageTree;
    private Container container;
    private float userRotation;
    private boolean printFitToMargin;
    private int printingCurrentPage;
    private int totalPagesToPrint;
    private boolean paintAnnotation = true;
    private boolean paintSearchHighlight = true;

    private static PrintService[] services;
    private PrintService printService;
    private HashDocAttributeSet docAttributeSet;
    private HashPrintRequestAttributeSet printRequestAttributeSet;

    /**
     * Creates a new <code>PrintHelper</code> instance defaulting the
     * paper size to Letter and the print quality to Draft.
     *
     * @param container parent container used to center print dialogs.
     * @param pageTree  document page tree.
     */
    public PrintHelper(Container container, PageTree pageTree, int rotation) {
        this(container, pageTree, rotation, MediaSizeName.NA_LETTER, PrintQuality.DRAFT);
    }

    /**
     * Creates a new <code>PrintHelper</code> instance using the specified
     * media sized and print quality.
     *
     * @param container     parent container uses to center print dialog.
     * @param pageTree      document page tree.
     * @param rotation      rotation at witch to paint document.
     * @param paperSizeName MediaSizeName constant of paper size to print to.
     * @param printQuality  quality of the print job, draft, quality etc.
     */
    public PrintHelper(Container container, PageTree pageTree,
                       final float rotation,
                       final MediaSizeName paperSizeName,
                       final PrintQuality printQuality) {
        this.container = container;
        this.pageTree = pageTree;
        this.userRotation = rotation;

        // find available printers
        services = lookForPrintServices();

        // default printing properties.
        // Print and document attributes sets.
        printRequestAttributeSet =
                new HashPrintRequestAttributeSet();
        docAttributeSet = new HashDocAttributeSet();

        // assign print quality.
        printRequestAttributeSet.add(printQuality);

        // change paper
        printRequestAttributeSet.add(paperSizeName);
        docAttributeSet.add(paperSizeName);

        // setting margins to full paper size as PDF have their own margins
        MediaSize mediaSize =
                MediaSize.getMediaSizeForName(paperSizeName);
        float[] size = mediaSize.getSize(MediaSize.INCH);
        printRequestAttributeSet
                .add(new MediaPrintableArea(0, 0, size[0], size[1],
                        MediaPrintableArea.INCH));
        docAttributeSet.add(new MediaPrintableArea(0, 0, size[0], size[1],
                MediaPrintableArea.INCH));

        // default setup, all pages, shrink to fit and no dialog.
        setupPrintService(0, this.pageTree.getNumberOfPages(), 1, true, false);

        // display paper size.
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Paper Size: " + paperSizeName.getName() +
                    " " + size[0] + " x " + size[1]);
        }
    }

    /**
     * Creates a new <code>PrintHelper</code> instance using the specified
     * doc and print attribute sets.  This constructor offers the most flexibility
     * as it allows the attributes sets to be pre configured.  This method
     * should only be used by advanced users.
     *
     * @param container                parent container uses to center print dialog.
     * @param pageTree                 document page tree.
     * @param userRotation             rotation of view
     * @param docAttributeSet          MediaSizeName constant of paper size to print to.
     * @param printRequestAttributeSet quality of the print job, draft, quality etc.
     */
    public PrintHelper(Container container, PageTree pageTree,
                       float userRotation,
                       HashDocAttributeSet docAttributeSet,
                       HashPrintRequestAttributeSet printRequestAttributeSet) {
        this.container = container;
        this.pageTree = pageTree;
        this.userRotation = userRotation;
        // blindly assign doc and print attribute sets.
        this.docAttributeSet = docAttributeSet;
        this.printRequestAttributeSet = printRequestAttributeSet;
        // find available printers
        services = lookForPrintServices();
        // default setup, all pages, shrink to fit and no dialog.
        setupPrintService(0, this.pageTree.getNumberOfPages(), 1, true, false);
    }

    /**
     * Configures the PrinterJob instance with the specified parameters.
     *
     * @param startPage             start of page range, zero-based index.
     * @param endPage               end of page range, one-based index.
     * @param copies                number of copies of pages in print range.
     * @param shrinkToPrintableArea true, to enable shrink to fit printable area;
     *                              false, otherwise.
     * @param showPrintDialog       true, to display a print setup dialog when this method
     *                              is initiated; false, otherwise.  This dialog will be shown after the
     *                              page dialog if it is visible.
     * @return true if print setup should continue, false if printing was cancelled
     * by user interaction with optional print dialog.
     */
    public boolean setupPrintService(int startPage,
                                     int endPage,
                                     int copies,
                                     boolean shrinkToPrintableArea,
                                     boolean showPrintDialog) {
        // make sure our printable doc knows how many pages to print
        // Has to be set before printerJob.printDialog(), so it can show to
        // the user which pages it can print
        printFitToMargin = shrinkToPrintableArea;

        // set the number of pages
        printRequestAttributeSet.add(new PageRanges(startPage + 1, endPage + 1));
        // setup number of
        printRequestAttributeSet.add(new Copies(copies));

        // show the print dialog, return false if the user cancels/closes the
        // dialog.
        if (showPrintDialog) {
            printService = getSetupDialog();
            return printService != null;
        } else {// no dialog and thus printing will continue.
            return true;
        }
    }

    /**
     * Configures the PrinterJob instance with the specified parameters.
     *
     * @param printService          print service to print document to.
     * @param startPage             start of page range, zero-based index.
     * @param endPage               end of page range, one-based index.
     * @param copies                number of copies of pages in print range.
     * @param shrinkToPrintableArea true, to enable shrink to fit printable area;
     *                              false, otherwise.
     */
    public void setupPrintService(PrintService printService,
                                  int startPage,
                                  int endPage,
                                  int copies,
                                  boolean shrinkToPrintableArea) {

        // make sure our printable doc knows how many pages to print
        // Has to be set before printerJob.printDialog(), so it can show to
        //  the user which pages it can print
        printFitToMargin = shrinkToPrintableArea;

        // set the number of pages
        printRequestAttributeSet.add(new PageRanges(startPage + 1, endPage + 1));
        // setup number of
        printRequestAttributeSet.add(new Copies(copies));

        this.printService = printService;
    }

    /**
     * Configures the PrinterJob instance with the specified parameters.  this
     * method should only be used by advanced users.
     *
     * @param printService             print service to print document to.
     * @param printRequestAttributeSet print jobt attribute set.
     * @param shrinkToPrintableArea    true, to enable shrink to fit printable area;
     *                                 false, otherwise.
     */
    public void setupPrintService(PrintService printService,
                                  HashPrintRequestAttributeSet printRequestAttributeSet,
                                  boolean shrinkToPrintableArea) {
        printFitToMargin = shrinkToPrintableArea;
        this.printRequestAttributeSet = printRequestAttributeSet;
        this.printService = printService;
    }

    /**
     * Utility for showing print dialog for the current printService.  If no
     * print service is assigned the first print service is used to create
     * the print dialog.
     */
    public void showPrintSetupDialog() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        if (printService == null && services != null &&
                services.length > 0 && services[0] != null) {
            printService = services[0];
        }
        try {
            pj.setPrintService(printService);
            // Step 2: Pass the settings to a page dialog and print dialog.
            pj.pageDialog(printRequestAttributeSet);
        } catch (Throwable e) {
            logger.log(Level.FINE, "Error creating page setup dialog.", e);
        }
    }

    /**
     * Gets the page number of the page currently being spooled by the Printable
     * interface.
     *
     * @return current page being spooled by printer.
     */
    public int getCurrentPage() {
        return printingCurrentPage;
    }

    /**
     * Number of total pages being printed.
     *
     * @return total pages being printed.
     */
    public int getNumberOfPages() {
        return totalPagesToPrint;
    }

    /**
     * Gets the fit to margin property.  If enabled the page is scaled to fit
     * the paper size maxing out on the smallest paper dimension.
     *
     * @return true if fit to margin is enabled.
     */
    public boolean isPrintFitToMargin() {
        return printFitToMargin;
    }

    /**
     * Users rotation specified for the print job.
     *
     * @return float value representing rotation, 0 is 0 degrees.
     */
    public float getUserRotation() {
        return userRotation;
    }

    /**
     * Gets the document attributes currently in use.
     *
     * @return current document attributes.
     */
    public HashDocAttributeSet getDocAttributeSet() {
        return docAttributeSet;
    }

    /**
     * Gets the print request attribute sets.
     *
     * @return attribute set
     */
    public HashPrintRequestAttributeSet getPrintRequestAttributeSet() {
        return printRequestAttributeSet;
    }

    /**
     * Gets the currently assigned print service.
     *
     * @return current print service, can be null.
     */
    public PrintService getPrintService() {
        return printService;
    }

    /**
     * Prints the page at the specified index into the specified
     * java.awt.Graphics context in the specified format.
     *
     * @param printGraphics paper graphics context.
     * @param pageFormat    print attributes translated from PrintService
     * @param pageIndex     page to print, zero based.
     * @return A status code of Printable.NO_SUCH_PAGE or Printable.PAGE_EXISTS
     */
    public int print(Graphics printGraphics, PageFormat pageFormat, int pageIndex) {

        // update the pageCount
        if (printingCurrentPage != pageIndex) {
            printingCurrentPage = pageIndex + 1;
        }

        // Throws NO_SUCH_PAGE to printable interface,  out of page range
        if (pageIndex < 0 || pageIndex >= pageTree.getNumberOfPages()) {
            return Printable.NO_SUCH_PAGE;
        }
        try {

            // Initiate the Page to print, not adding to the pageTree cache purposely,
            // after we finish using it we'll dispose it.
            Page currentPage = pageTree.getPage(pageIndex);
            currentPage.init();
            PDimension pageDim = currentPage.getSize(userRotation);

            // Grab default page width and height
            float pageWidth = (float) pageDim.getWidth();
            float pageHeight = (float) pageDim.getHeight();

            // Default zoom factor
            float zoomFactor = 1.0f;

            Point imageablePrintLocation = new Point();

            // detect if page is being drawn in landscape, if so then we should
            // should be rotating the page so that it prints correctly
            float rotation = userRotation;
            boolean isDefaultRotation = true;
            if ((pageWidth > pageHeight &&
                    pageFormat.getOrientation() == PageFormat.PORTRAIT)
                // auto rotation for landscape.
//            (pageHeight > pageFormat.getImageableWidth() &&
//                pageFormat.getOrientation() == PageFormat.LANDSCAPE )
                    ) {
                // rotate clockwise 90 degrees
                isDefaultRotation = false;
                rotation -= 90;
            }

            // if true, we want to shrink out page to the new area.
            if (printFitToMargin) {

                // Get location of imageable area from PageFormat object
                Dimension imageablePrintSize;
                // correct scale to fit calculation for a possible automatic
                // rotation.
                if (isDefaultRotation) {
                    imageablePrintSize = new Dimension(
                            (int) pageFormat.getImageableWidth(),
                            (int) pageFormat.getImageableHeight());
                } else {
                    imageablePrintSize = new Dimension(
                            (int) pageFormat.getImageableHeight(),
                            (int) pageFormat.getImageableWidth());

                }
                float zw = imageablePrintSize.width / pageWidth;
                float zh = imageablePrintSize.height / pageHeight;
                zoomFactor = Math.min(zw, zh);
                imageablePrintLocation.x = (int) pageFormat.getImageableX();
                imageablePrintLocation.y = (int) pageFormat.getImageableY();
            }
            // apply imageablePrintLocation, normally (0,0)
            printGraphics.translate(imageablePrintLocation.x,
                    imageablePrintLocation.y);

            // Paint the page content
            currentPage.paint(printGraphics,
                    GraphicsRenderingHints.PRINT,
                    Page.BOUNDARY_CROPBOX,
                    rotation, zoomFactor, paintAnnotation, paintSearchHighlight);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Printing: Page initialization and painting was interrupted", e);
        }

        // Paint content to page buffer to reduce spool size but quality will suffer.
//        Image image = viewController.getDocument().getPageImage(pageIndex,
//                GraphicsRenderingHints.PRINT,
//                Page.BOUNDARY_CROPBOX,
//                rotation, zoomFactor);
//        printGraphics.drawImage(image,0,0,null);
//        image.flush();

        return Printable.PAGE_EXISTS;
    }

    /**
     * Print a range of pages from the document as specified by #setupPrintService.
     *
     * @throws PrintException if a default printer could not be found or some
     *                        other printing related error.
     */
    public void print() throws PrintException {

        // make sure we have a service, if not we assign the default printer 
        if (printService == null && services != null &&
                services.length > 0 && services[0] != null) {
            printService = services[0];
        }

        if (printService != null) {

            // calculate total pages being printed
            calculateTotalPagesToPrint();

            printService.createPrintJob().print(
                    new SimpleDoc(this,
                            DocFlavor.SERVICE_FORMATTED.PRINTABLE,
                            null),
                    printRequestAttributeSet);
        } else {
            logger.fine("No print could be found to print to.");
        }

    }

    public CancelablePrintJob cancelablePrint() throws PrintException {

        // make sure we have a service, if not we assign the default printer
        if (printService == null && services != null &&
                services.length > 0 && services[0] != null) {
            printService = services[0];
        }

        if (printService != null) {

            // calculate total pages being printed
            calculateTotalPagesToPrint();

            DocPrintJob printerJob = printService.createPrintJob();
            printerJob.print(
                    new SimpleDoc(this,
                            DocFlavor.SERVICE_FORMATTED.PRINTABLE,
                            null),
                    printRequestAttributeSet);

            return (CancelablePrintJob) printerJob;
        } else {
            return null;
        }
    }

    public void print(PrintJobWatcher printJobWatcher) throws PrintException {

        // make sure we have a service, if not we assign the default printer
        if (printService == null && services != null &&
                services.length > 0 && services[0] != null) {
            printService = services[0];
        }

        if (printService != null) {

            // calculate total pages being printed
            calculateTotalPagesToPrint();

            DocPrintJob printerJob = printService.createPrintJob();
            printJobWatcher.setPrintJob(printerJob);

            printerJob.print(
                    new SimpleDoc(this,
                            DocFlavor.SERVICE_FORMATTED.PRINTABLE,
                            null),
                    printRequestAttributeSet);

            printJobWatcher.waitForDone();
        } else {
            logger.fine("No print could be found to print to.");
        }

    }

    /**
     * Utility for creating a print setup dialog.
     *
     * @return print service selected by the user, or null if the user
     * cancelled the dialog.
     */
    private PrintService getSetupDialog() {
        final int offset = 50;
        // find graphic configuration for the window the viewer is in.
        Window window = SwingUtilities.getWindowAncestor(
                container);
        GraphicsConfiguration graphicsConfiguration =
                window == null ? null : window.getGraphicsConfiguration();
        // try and trim the services list.
//        services = new PrintService[]{services[0]};

        return ServiceUI.printDialog(graphicsConfiguration,
                container.getX() + offset,
                container.getY() + offset,
                services, services[0],
                DocFlavor.SERVICE_FORMATTED.PRINTABLE,
                printRequestAttributeSet);
    }

    private void calculateTotalPagesToPrint() {
        // iterate over page ranges to find out how many pages are to
        // be printed
        PageRanges pageRanges = (PageRanges)
                printRequestAttributeSet.get(PageRanges.class);
        totalPagesToPrint = 0;
        // we need to loop over the multiple ranges as commas can be used
        // to specify more then one range.  Make sure the specified pages
        // fall with in the range allowed by the document.
        int start, end;
        for (int[] ranges : pageRanges.getMembers()) {
            start = ranges[0];
            end = ranges[1];
            if (start < 1) {
                start = 1;
            }
            if (end > pageTree.getNumberOfPages()) {
                end = pageTree.getNumberOfPages();
            }
            totalPagesToPrint += end - start + 1;
        }
    }

    private PrintService[] lookForPrintServices() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(
                DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        // check for a default service and make sure it is at index 0. the lookupPrintServices does not
        // aways put the default printer first in the array.
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultService != null && services.length > 1) {
            PrintService printService;
            for (int i = 1, max = services.length; i < max; i++) {
                printService = services[i];
                if (printService.equals(defaultService)) {
                    // found the default printer, now swap it with the first index.
                    PrintService tmp = services[0];
                    services[0] = defaultService;
                    services[i] = tmp;
                    break;
                }
            }
        }
        return services;
    }

    /**
     * Are page annotations going to be printed?
     *
     * @return true if annotation are to be printed, false otherwise
     */
    public boolean isPaintAnnotation() {
        return paintAnnotation;
    }

    /**
     * Manually enable or disable the printing of annotation for a print job
     *
     * @param paintAnnotation true to paint annotation; otherwise false.
     */
    public void setPaintAnnotation(boolean paintAnnotation) {
        this.paintAnnotation = paintAnnotation;
    }

    /**
     * Are page search highlight's going to be printed?
     *
     * @return true if highlights are to be printed, false otherwise
     */
    public boolean isPaintSearchHighlight() {
        return paintSearchHighlight;
    }

    /**
     * Manually enable or disable the printing of search highlights for a print job
     *
     * @param paintSearchHighlight true to paint search highlights; otherwise false.
     */
    public void setPaintSearchHighlight(boolean paintSearchHighlight) {
        this.paintSearchHighlight = paintSearchHighlight;
    }
}
