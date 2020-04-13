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
package org.icepdf.core.pobjects;

import org.icepdf.core.SecurityCallback;
import org.icepdf.core.application.ProductInfo;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.io.*;
import org.icepdf.core.pobjects.acroform.FieldDictionary;
import org.icepdf.core.pobjects.acroform.InteractiveForm;
import org.icepdf.core.pobjects.annotations.AbstractWidgetAnnotation;
import org.icepdf.core.pobjects.graphics.WatermarkCallback;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.security.SecurityManager;
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.LazyObjectLoader;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Parser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The <code>Document</code> class represents a PDF document and provides
 * access to the hierarchy of objects contained in the body section of the
 * PDF document.  Most of the objects in the hierarchy are dictionaries which
 * contain references to page content and other objects such such as annotations.
 * For more information on the document object hierarchy, see the <i>ICEpdf
 * Developer's Guide</i>.</p>
 * <p/>
 * <p>The <code>Document</code> class also provides access to methods responsible
 * for rendering PDF document content.  Methods are available to capture page
 * content to a graphics context or extract image and text data on a page-by-page
 * basis.</p>
 * <p/>
 * <p>If your PDF rendering application will be accessing encrypted documents,
 * it is important to implement the SecurityCallback.  This interface provides
 * methods for getting password data from a user if needed.<p>
 *
 * @since 1.0
 */
public class Document {

    private static final Logger logger =
            Logger.getLogger(Document.class.toString());

    /**
     * Gets the version number of ICEpdf rendering core.  This is not the version
     * number of the PDF format used to encode this document.
     *
     * @return version number of ICEpdf's rendering core.
     */
    public static String getLibraryVersion() {
        return ProductInfo.PRIMARY + "." + ProductInfo.SECONDARY + "." +
                ProductInfo.TERTIARY + " " + ProductInfo.RELEASE_TYPE;
    }

    private static final String INCREMENTAL_UPDATER =
            "org.icepdf.core.util.IncrementalUpdater";
    public static boolean foundIncrementalUpdater;

    static {
        // check class bath for NFont library, and declare results.
        try {
            Class.forName(INCREMENTAL_UPDATER);
            foundIncrementalUpdater = true;
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "PDF write support was not found on the class path");
        }
    }

    // optional watermark callback
    private WatermarkCallback watermarkCallback;

    // core catalog, root of the document hierarchy.
    private Catalog catalog;

    // We used to keep the document main PTrailer's PInfo,
    //  but now that's lazily loaded, so instead we keep the
    //  PTrailer itself, which can get us the PInfo whenever
    private PTrailer pTrailer;

    // state manager for tracking object that have been touched in some way
    // for editing purposes,
    private StateManager stateManager;

    // This is the original file or url path of where the PDF document was load
    // from
    private String origin;

    // This is the location of the file when it is saved to the hard drive.  This
    // is usually only different from the origin if the the PDF document
    // was loaded from a URL
    private String cachedFilePath;

    // callback for password dialogs, or command line access.
    private SecurityCallback securityCallback;

    // disable/enable file caching, overrides fileCachingSize.
    private static boolean isCachingEnabled;
    private static boolean isFileCachingEnabled;
    private static int fileCacheMaxSize;

    // repository of all PDF object associated with this document.
    private Library library = null;
    private SeekableInput documentSeekableInput;

    static {
        // sets if file caching is enabled or disabled.
        isCachingEnabled =
                Defs.sysPropertyBoolean("org.icepdf.core.streamcache.enabled",
                        false);

        isFileCachingEnabled = Defs.sysPropertyBoolean("org.icepdf.core.filecache.enabled",
                true);
        fileCacheMaxSize = Defs.intProperty("org.icepdf.core.filecache.size", 200000000);
    }

    /**
     * Creates a new instance of a Document.  A Document class represents
     * one PDF document.
     */
    public Document() {
    }

    /**
     * Sets a page watermark implementation to be painted on top of the page
     * content.  Watermark can be specified for each page or once by calling
     * document.setWatermark().
     *
     * @param watermarkCallback watermark implementation.
     */
    public void setWatermarkCallback(WatermarkCallback watermarkCallback) {
        this.watermarkCallback = watermarkCallback;
    }

    /**
     * Utility method for setting the origin (filepath or URL) of this Document
     *
     * @param o new origin value
     * @see #getDocumentOrigin()
     */
    private void setDocumentOrigin(String o) {
        origin = o;
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config(
                    "MEMFREE: " + Runtime.getRuntime().freeMemory() + " of " +
                            Runtime.getRuntime().totalMemory());
            logger.config("LOADING: " + o);
        }
    }

    /**
     * Sets the cached file path in the case of opening a file from a URL.
     *
     * @param o new cached file path value
     * @see #getDocumentCachedFilePath
     */
    private void setDocumentCachedFilePath(String o) {
        cachedFilePath = o;
    }

    /**
     * Returns the cached file path in the case of opening a file from a URL.
     *
     * @return file path
     */
    private String getDocumentCachedFilePath() {
        return cachedFilePath;
    }

    /**
     * Load a PDF file from the given path and initiates the document's Catalog.
     *
     * @param filepath path of PDF document.
     * @throws PDFException         if an invalid file encoding.
     * @throws PDFSecurityException if a security provider cannot be found
     *                              or there is an error decrypting the file.
     * @throws IOException          if a problem setting up, or parsing the file.
     */
    public void setFile(String filepath)
            throws PDFException, PDFSecurityException, IOException {
        setDocumentOrigin(filepath);
        File file = new File(filepath);
        FileInputStream inputStream = new FileInputStream(file);
        int fileLength = inputStream.available();
        if (isFileCachingEnabled && file.length() > 0 && fileLength <= fileCacheMaxSize) {
            // copy the file contents into byte[], for direct memory mapping.
            byte[] data = new byte[fileLength];
            inputStream.read(data);
            setByteArray(data, 0, fileLength, filepath);
        } else {
            RandomAccessFileInputStream rafis =
                    RandomAccessFileInputStream.build(new File(filepath));
            setInputStream(rafis);
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    /**
     * Load a PDF file from the given URL and initiates the document's Catalog.
     * If the system property org.icepdf.core.streamcache.enabled=true, the file
     * will be cached to a temp file; otherwise, the complete document stream will
     * be stored in memory.
     *
     * @param url location of file.
     * @throws PDFException         an invalid file encoding.
     * @throws PDFSecurityException if a security provider can not be found
     *                              or there is an error decrypting the file.
     * @throws IOException          if a problem downloading, setting up, or parsing the file.
     */
    public void setUrl(URL url)
            throws PDFException, PDFSecurityException, IOException {
        InputStream in = null;
        try {
            // make a connection
            URLConnection urlConnection = url.openConnection();

            // Create a stream on the URL connection
            in = urlConnection.getInputStream();

            String pathOrURL = url.toString();

            setInputStream(in, pathOrURL);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Load a PDF file from the given input stream and initiates the document's Catalog.
     * If the system property org.icepdf.core.streamcache.enabled=true, the file
     * will be cached to a temp file; otherwise, the complete document stream will
     * be stored in memory.
     *
     * @param in        input stream containing PDF data
     * @param pathOrURL value assigned to document origin
     * @throws PDFException         an invalid stream or file encoding
     * @throws PDFSecurityException if a security provider can not be found
     *                              or there is an error decrypting the file.
     * @throws IOException          if a problem setting up, or parsing the SeekableInput.
     */
    public void setInputStream(InputStream in, String pathOrURL)
            throws PDFException, PDFSecurityException, IOException {
        setDocumentOrigin(pathOrURL);

        if (!isCachingEnabled) {
//System.out.println("Started  downloading PDF to memory : " + (new java.util.Date()));
            // read into memory first
            ConservativeSizingByteArrayOutputStream byteArrayOutputStream =
                    new ConservativeSizingByteArrayOutputStream(100 * 1024);

            // write the bytes.
            byte[] buffer = new byte[4096];
            int length;
//                int pdfFileSize = 0;
            // in.read will block until the end of the file is read.
            while ((length = in.read(buffer, 0, buffer.length)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
//                    pdfFileSize += length;
            }
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            int size = byteArrayOutputStream.size();
            byteArrayOutputStream.trim();
            byte[] data = byteArrayOutputStream.relinquishByteArray();
//System.out.println("Finished downloading PDF to memory : " + (new java.util.Date()) + "  pdfFileSize: " + pdfFileSize);

            // finally read the cached file
            SeekableByteArrayInputStream byteArrayInputStream =
                    new SeekableByteArrayInputStream(data, 0, size);
            setInputStream(byteArrayInputStream);
        }
        // if caching is allowed cache the url to file
        else {
//System.out.println("Started  downloading PDF to disk : " + (new java.util.Date()));
            // create tmp file and write bytes to it.
            File tempFile = File.createTempFile(
                    "ICEpdfTempFile" + getClass().hashCode(),
                    ".tmp");
            // Delete temp file on exit
            tempFile.deleteOnExit();

            // Write the data to the temp file.
            FileOutputStream fileOutputStream =
                    new FileOutputStream(tempFile.getAbsolutePath(), true);

            // write the bytes.
            byte[] buffer = new byte[4096];
            int length;
//                int pdfFileSize = 0;
            while ((length = in.read(buffer, 0, buffer.length)) > 0) {
                fileOutputStream.write(buffer, 0, length);
//                    pdfFileSize += length;
            }
            fileOutputStream.flush();
            fileOutputStream.close();
//System.out.println("Finished downloading PDF to disk : " + (new java.util.Date()) + "  pdfFileSize: " + pdfFileSize);

            setDocumentCachedFilePath(tempFile.getAbsolutePath());

            // finally read the cached file
            RandomAccessFileInputStream rafis =
                    RandomAccessFileInputStream.build(tempFile);
            setInputStream(rafis);
        }
    }

    /**
     * Load a PDF file from the given byte array and initiates the document's Catalog.
     * If the system propertyorg.icepdf.core.streamcache.enabled=true, the file
     * will be cached to a temp file; otherwise, the complete document stream will
     * be stored in memory.
     * The given byte array is not necessarily copied, and will try to be directly
     * used, so do not modify it after passing it to this method.
     *
     * @param data      byte array containing PDF data
     * @param offset    the index into the byte array where the PDF data begins
     * @param length    the number of bytes in the byte array belonging to the PDF data
     * @param pathOrURL value assigned to document origin
     * @throws PDFException         an invalid stream or file encoding
     * @throws PDFSecurityException if a security provider can not be found
     *                              or there is an error decrypting the file.
     * @throws IOException          if a problem setting up, or parsing the SeekableInput.
     */
    public void setByteArray(byte[] data, int offset, int length, String pathOrURL)
            throws PDFException, PDFSecurityException, IOException {
        setDocumentOrigin(pathOrURL);

        if (!isCachingEnabled) {
            // finally read the cached file
            SeekableByteArrayInputStream byteArrayInputStream =
                    new SeekableByteArrayInputStream(data, offset, length);
            setInputStream(byteArrayInputStream);
        }
        // if caching is allowed cache the url to file
        else {
//System.out.println("Started  downloading PDF to disk : " + (new java.util.Date()));
            // create tmp file and write bytes to it.
            File tempFile = File.createTempFile(
                    "ICEpdfTempFile" + getClass().hashCode(),
                    ".tmp");
            // Delete temp file on exit
            tempFile.deleteOnExit();

            // Write the data to the temp file.
            FileOutputStream fileOutputStream =
                    new FileOutputStream(tempFile.getAbsolutePath(), true);

            // write the bytes.
//                int pdfFileSize = 0;
            fileOutputStream.write(data, offset, length);
//                pdfFileSize += length;
            fileOutputStream.flush();
            fileOutputStream.close();
//System.out.println("Finished downloading PDF to disk : " + (new java.util.Date()) + "  pdfFileSize: " + pdfFileSize);

            setDocumentCachedFilePath(tempFile.getAbsolutePath());

            // finally read the cached file
            RandomAccessFileInputStream rafis =
                    RandomAccessFileInputStream.build(tempFile);
            setInputStream(rafis);
        }
    }

    /**
     * Load a PDF file from the given SeekableInput stream and initiates the
     * document's Catalog.
     *
     * @param in        input stream containing PDF data
     * @param pathOrURL value assigned to document origin
     * @throws PDFException         an invalid stream or file encoding
     * @throws PDFSecurityException if a security provider can not be found
     *                              or there is an error decrypting the file.
     * @throws IOException          if a problem setting up, or parsing the SeekableInput.
     */
    public void setInputStream(SeekableInput in, String pathOrURL)
            throws PDFException, PDFSecurityException, IOException {
        setDocumentOrigin(pathOrURL);
        setInputStream(in);
    }

    /**
     * Sets the input stream of the PDF file to be rendered.
     *
     * @param in inputStream containing PDF data stream
     * @throws PDFException         if error occurs
     * @throws PDFSecurityException security error
     * @throws IOException          io error during stream handling
     */
    private void setInputStream(final SeekableInput in)
            throws PDFException, PDFSecurityException, IOException {
        try {
            documentSeekableInput = in;

            // create library to hold all document objects
            library = new Library();

            // reference the stream and origin with library so we can handle verification and writing of signatures.
            library.setDocumentInput(documentSeekableInput);

            // if interactive show visual progress bar
            //ProgressMonitorInputStream monitor = null;

            boolean loaded = false;
            try {
                loadDocumentViaXRefs(in);

                // initiate the catalog, build the outline for the document
                // this is the best test to see if everything is in order.
                if (catalog != null) {
                    catalog.init();
                }

                loaded = true;
            } catch (PDFException e) {
                throw e;
            } catch (PDFSecurityException e) {
                throw e;
            } catch (Exception e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Cross reference deferred loading failed, will fall back to linear reading.");
                }
            }

            if (!loaded) {
                // Cleanup any bits left behind by the failed xref loading
                if (catalog != null) {
                    catalog = null;
                }
                if (library != null) {
                    library = null;
                }
                library = new Library();
                pTrailer = null;

                in.seekAbsolute(0L);
                loadDocumentViaLinearTraversal(in);

                // initiate the catalog, build the outline for the document
                if (catalog != null) {
                    catalog.init();
                }
            }

            // create new instance of state manager and add it to the library
            stateManager = new StateManager(pTrailer);
            library.setStateManager(stateManager);
        } catch (PDFException e) {
            logger.log(Level.FINE, "Error loading PDF file during linear parse.", e);
            dispose();
            throw e;
        } catch (PDFSecurityException e) {
            dispose();
            throw e;
        } catch (IOException e) {
            dispose();
            throw e;
        } catch (Exception e) {
            dispose();
            logger.log(Level.SEVERE, "Error loading PDF Document.", e);
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Uitility method for loading the documents objects from the Xref table.
     *
     * @param in input stream to parse
     * @throws IOException          an i/o problem
     * @throws PDFException         an invalid stream or file encoding
     * @throws PDFSecurityException if a security provider can not be found
     *                              or there is an error decrypting the file.
     */
    private void loadDocumentViaXRefs(SeekableInput in)
            throws PDFException, PDFSecurityException, IOException {
        //if( true ) throw new RuntimeException("Fallback to linear traversal");
        int offset = skipPastAnyPrefixJunk(in);
        long xrefPosition = getInitialCrossReferencePosition(in) + offset;
        PTrailer documentTrailer = null;
        if (xrefPosition > 0L) {
            in.seekAbsolute(xrefPosition);

            Parser parser = new Parser(in);
            Object obj = parser.getObject(library);
            if (obj instanceof PObject)
                obj = ((PObject) obj).getObject();
            PTrailer trailer = (PTrailer) obj;
            //PTrailer trailer = (PTrailer) parser.getObject( library );
            if (trailer == null)
                throw new RuntimeException("Could not find trailer");
            if (trailer.getPrimaryCrossReference() == null)
                throw new RuntimeException("Could not find cross reference");
            trailer.setPosition(xrefPosition);

            documentTrailer = trailer;
            // any prev/next trails are loaded lazily
        }
        if (documentTrailer == null)
            throw new RuntimeException("Could not find document trailer");
        if (offset > 0) {
            // mark the offset, so that it can be correct for later during
            // object retrieval.
            documentTrailer.getCrossReferenceTable().setOffset(offset);
        }

        LazyObjectLoader lol = new LazyObjectLoader(
                library, in, documentTrailer.getPrimaryCrossReference());
        library.setLazyObjectLoader(lol);

        pTrailer = documentTrailer;
        catalog = documentTrailer.getRootCatalog();
        library.setCatalog(catalog);

        if (catalog == null)
            throw new NullPointerException("Loading via xref failed to find catalog");

        boolean madeSecurityManager = makeSecurityManager(documentTrailer);
        if (madeSecurityManager) {
            attemptAuthorizeSecurityManager();
        }
        // setup a signature permission dictionary
        configurePermissions();
    }

    private long getInitialCrossReferencePosition(SeekableInput in) throws IOException {
        in.seekEnd();

        long endOfFile = in.getAbsolutePosition();
        long currentPosition = endOfFile - 1;
        long afterStartxref = -1;
        String startxref = "startxref";
        int startxrefIndexToMatch = startxref.length() - 1;

        while (currentPosition >= 0 && (endOfFile - currentPosition) < 2048) {
            in.seekAbsolute(currentPosition);
            int curr = in.read();
            if (curr < 0)
                throw new EOFException("Could not find startxref at end of file");
            if (curr == startxref.charAt(startxrefIndexToMatch)) {
                // If we've matched the whole string
                if (startxrefIndexToMatch == 0) {
                    afterStartxref = currentPosition + startxref.length();
                    break;
                }
                startxrefIndexToMatch--;
            } else
                startxrefIndexToMatch = startxref.length() - 1;
            currentPosition--;
        }
        if (afterStartxref < 0)
            throw new EOFException("Could not find startxref near end of file");

        in.seekAbsolute(afterStartxref);
        Parser parser = new Parser(in);
        Number xrefPositionObj = (Number) parser.getToken();
        if (xrefPositionObj == null)
            throw new RuntimeException("Could not find ending cross reference position");
        return xrefPositionObj.longValue();
    }

    /**
     * Uitily method for parsing a PDF documents object.  This should only be
     * called when the xref lookup fails or the file is being loaded
     * via byte input because file caching is not enabled.
     *
     * @param seekableInput stream representing whole pdf document
     * @throws PDFException         an invalid stream or file encoding
     * @throws PDFSecurityException if a security provider can not be found
     *                              or there is an error decrypting the file.
     */
    private void loadDocumentViaLinearTraversal(SeekableInput seekableInput)
            throws PDFException, PDFSecurityException, IOException {

        InputStream in = seekableInput.getInputStream();

        int objectsOffset = skipPastAnyPrefixJunk(in);

        library.setLinearTraversal();

        // NOTE: when we implement linerized document we should be able to
        //       rework this method.
        Parser parser = new Parser(in);

        // document Trailer, holds encryption info
        PTrailer documentTrailer = null;

        // Loop through all objects that where parsed from the data stream
        List<PObject> documentObjects = new ArrayList<PObject>();
        Object pdfObject;
        while (true) {
            // parse all of the objects in the stream,  objects are added
            // to the library object.
            pdfObject = parser.getObject(library);

            // eof or io error result in break
            if (pdfObject == null) {
                break;
            }

            // unwrap pObject for catalog and ptrailer lookups.
            if (pdfObject instanceof PObject) {
                PObject tmp = (PObject) pdfObject;
                // apply the offset value of the object.
                tmp.setLinearTraversalOffset(objectsOffset + parser.getLinearTraversalOffset());
                // store reference so we can rebuild the xref table.
                documentObjects.add(tmp);
                Object obj = tmp.getObject();
                if (obj != null)
                    pdfObject = obj;
            }

            // find the catalog which has information on outlines
            // which is need by the gui
            if (pdfObject instanceof Catalog) {
                catalog = (Catalog) pdfObject;
            }

            // Find the trailer object so that we can get the encryption information
            // trailer information is not a PObject and thus there should
            if (pdfObject instanceof PTrailer) {
                if (documentTrailer == null) {
                    documentTrailer = (PTrailer) pdfObject;
                } else {
                    // add more trailer data to the original
                    PTrailer nextTrailer = (PTrailer) pdfObject;
                    if (nextTrailer.getPrev() > 0) {
                        documentTrailer.addNextTrailer(nextTrailer);
                        documentTrailer = nextTrailer;
                    }
                }
            }
        }

        // apply the new object offset values so that the object can be retrieved
        // using the actual index in the file
        CrossReference refs = documentTrailer.getPrimaryCrossReference();
        Object entry;
        for (PObject pObject : documentObjects) {
            entry = refs.getEntryForObject(pObject.getReference().getObjectNumber());
            if (entry != null && entry instanceof CrossReference.UsedEntry) {
                ((CrossReference.UsedEntry) entry).setFilePositionOfObject(
                        pObject.getLinearTraversalOffset());
            } else {
                refs.addUsedEntry(pObject.getReference().getObjectNumber(),
                        pObject.getLinearTraversalOffset(),
                        pObject.getReference().getGenerationNumber());
            }
        }

        if (logger.isLoggable(Level.FINER)) {
            for (PObject pobjects : documentObjects) {
                // display object information in debug mode
                logger.finer(pobjects.getClass().getName() + " " +
                        pobjects.getLinearTraversalOffset() + " " +
                        pobjects);
            }
        }


        // The LazyObjectLoader is used for both reading from a SeekableInput,
        //  and also accessing ObjectStreams.
        // So, even with linear traversal, we still need it for PDF 1.5 documents
        if (documentTrailer != null) {
            LazyObjectLoader lol = new LazyObjectLoader(
                    library, seekableInput, documentTrailer.getPrimaryCrossReference());
            library.setLazyObjectLoader(lol);
        }

        pTrailer = documentTrailer;
        library.setCatalog(catalog);

        // Add Document information object to catalog
        if (documentTrailer != null) {
            boolean madeSecurityManager = makeSecurityManager(documentTrailer);
            if (madeSecurityManager)
                attemptAuthorizeSecurityManager();
        }

        // setup a signature handler
        configurePermissions();
    }

    /**
     * Typically, if we're doing a linear traversal, it's because the PDF file
     * is corrupted, usually by junk being appended to it, or the ending
     * being truncated, or, in this case, from junk being inserted into the
     * beginning of the file, skewing all the xref object offsets.
     * <p/>
     * We're going to look for the "%PDF-1." string that most PDF files start
     * with. If we do find it, then leave the InputStream after the next
     * whitespace, else rewind back to the beginning, in case the file was
     * never encoded with the PDF version comment.
     *
     * @param in InputStream derived from SeekableInput.getInputStream()
     */
    private int skipPastAnyPrefixJunk(InputStream in) {
        if (!in.markSupported())
            return 0;
        try {
            final int scanLength = 2048;
            final String scanFor = "%PDF-";
            final int scanForLength = scanFor.length();
            int scanForIndex = 0;
            boolean scanForWhiteSpace = false;
            in.mark(scanLength);
            for (int i = 0; i < scanLength; i++) {
                int data = in.read();
                if (data < 0) {
                    in.reset();
                    return 0;
                }
                // scan to the end of the comment line and return the offset
                if (scanForWhiteSpace) {
                    scanForIndex++;
                    if (Parser.isWhitespace((char) data)) {
                        return scanForIndex;
                    }
                } else {
                    if (data == scanFor.charAt(scanForIndex)) {
                        scanForIndex++;
                        if (scanForIndex == scanForLength) {
                            // Now read until we find white space
                            scanForWhiteSpace = true;
                        }
                    } else
                        scanForIndex = 0;
                }
            }
            // Searched through scanLength number of bytes and didn't find it,
            //  so reset, in case it was never there to find
            in.reset();
        } catch (IOException e) {
            try {
                in.reset();
            } catch (IOException e2) {
                // forget about it.
            }
        }
        return 0;
    }

    /**
     * Skips junk and keeps track of the offset so that later corrections can
     * be made for object seeks.
     *
     * @param in input stream to parse.
     * @return 0 if file header is well formed, otherwise the offset to where
     * the document header starts.
     */
    private int skipPastAnyPrefixJunk(SeekableInput in) {
        if (!in.markSupported())
            return 0;
        try {
            final int scanLength = 2048;
            final String scanFor = "%PDF-1.";
            int scanForIndex = 0;
            in.mark(scanLength);
            for (int i = 0; i < scanLength; i++) {
                int data = in.read();
                if (data < 0) {
                    in.reset();
                    return 0;
                }
                if (data == scanFor.charAt(scanForIndex)) {
                    return i;
                } else {
                    scanForIndex = 0;
                }
            }
            // Searched through scanLength number of bytes and didn't find it,
            //  so reset, in case it was never there to find
            in.reset();
        } catch (IOException e) {
            try {
                in.reset();
            } catch (IOException e2) {
                // forget about it.
            }
        }
        return 0;
    }


    /**
     * Utility method for building the SecurityManager if the document
     * contains a crypt entry in the PTrailer.
     *
     * @param documentTrailer document trailer
     * @return Whether or not a SecurityManager was made, and set in the Library
     * @throws PDFSecurityException if there is an issue finding encryption libraries.
     */
    private boolean makeSecurityManager(PTrailer documentTrailer) throws PDFSecurityException {
        /**
         * Before a security manager can be created or needs to be created
         * we need the following
         *      1.  The trailer object must have an encrypt entry
         *      2.  The trailer object must have an ID entry
         */
        boolean madeSecurityManager = false;
        HashMap<Object, Object> encryptDictionary = documentTrailer.getEncrypt();
        List fileID = documentTrailer.getID();
        // check for a missing file ID.
        if (fileID == null) {
            // we have a couple malformed documents that don't specify a FILE ID.
            // but proving two empty string allows the document to be decrypted.
            fileID = new ArrayList(2);
            fileID.add(new LiteralStringObject(""));
            fileID.add(new LiteralStringObject(""));
        }

        if (encryptDictionary != null && fileID != null) {
            // create new security manager
            library.setSecurityManager(new SecurityManager(
                    library, encryptDictionary, fileID));
            madeSecurityManager = true;
        }
        return madeSecurityManager;
    }

    /**
     * Initializes permission object as it is uses with encrypt permission to define
     * document characteristics at load time.
     *
     * @return true if permissions where found, false otherwise.
     */
    private boolean configurePermissions() {
        if (catalog != null) {
            Permissions permissions = catalog.getPermissions();
            if (permissions != null) {
                library.setPermissions(permissions);
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Document perms dictionary found and configured. ");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * If the document has a SecurityManager it is encrypted and as a result the
     * following method is used with the SecurityCallback to prompt a user for
     * a password if needed.
     *
     * @throws PDFSecurityException error during authorization manager setup
     */
    private void attemptAuthorizeSecurityManager() throws PDFSecurityException {
        // check if pdf is password protected, by passing in black
        // password
        if (!library.getSecurityManager().isAuthorized("")) {
            // count password tries
            int count = 1;
            // store temporary password
            String password;

            // Give user 3 chances to type the correct password
            // before throwing security exceptions
            while (true) {
                // Display password dialog
                // make sure a callback has been set.
                if (securityCallback != null) {
                    password = securityCallback.requestPassword(this);
                    if (password == null) {
                        throw new PDFSecurityException("Encryption error");
                    }
                } else {
                    throw new PDFSecurityException("Encryption error");
                }

                // Verify new password,  proceed if authorized,
                //    fatal exception otherwise.
                if (library.getSecurityManager().isAuthorized(password)) {
                    break;
                }
                count++;
                // after 3 tries throw the the error.
                if (count > 3) {
                    throw new PDFSecurityException("Encryption error");
                }
            }
        }

        // set the encryption flag on catalog
        library.setEncrypted(true);
    }

    /**
     * Gets the page dimension of the indicated page number using the specified
     * rotation factor.
     *
     * @param pageNumber   Page number for the given dimension.  The page
     *                     number is zero-based.
     * @param userRotation Rotation, in degrees, that has been applied to page
     *                     when calculating the dimension.
     * @return page dimension for the specified page number
     * @see #getPageDimension(int, float, float)
     */
    public PDimension getPageDimension(int pageNumber, float userRotation) {
        Page page = catalog.getPageTree().getPage(pageNumber);
        return page.getSize(userRotation);
    }

    /**
     * Gets the page dimension of the indicated page number using the specified
     * rotation and zoom settings.  If the page does not exist then a zero
     * dimension is returned.
     *
     * @param pageNumber   Page number for the given dimension.  The page
     *                     number is zero-based.
     * @param userRotation Rotation, in degrees, that has been applied to page
     *                     when calculating the dimension.
     * @param userZoom     Any deviation from the page's actual size, by zooming in or out.
     * @return page dimension for the specified page number.
     * @see #getPageDimension(int, float)
     */
    public PDimension getPageDimension(int pageNumber, float userRotation, float userZoom){
        Page page = catalog.getPageTree().getPage(pageNumber);
        if (page != null) {
            return page.getSize(userRotation, userZoom);
        } else {
            return new PDimension(0, 0);
        }
    }

    /**
     * Returns the origin (filepath or URL) of this Document.  This is the original
     * location of the file where the method getDocumentLocation returns the actual
     * location of the file.  The origin and location of the document will only
     * be different if it was loaded from a URL or an input stream.
     *
     * @return file path or URL
     * @see #getDocumentLocation
     */
    public String getDocumentOrigin() {
        return origin;
    }

    /**
     * Returns the file location or URL of this Document. This location may be different
     * from the file origin if the document was loaded from a URL or input stream.
     * If the file was loaded from a URL or input stream the file location is
     * the path to where the document content is cached.
     *
     * @return file path
     * @see #getDocumentOrigin()
     */
    public String getDocumentLocation() {
        if (cachedFilePath != null)
            return cachedFilePath;
        return origin;
    }

    /**
     * Gets an instance of the the document state manager which stores references
     * of object that need to be written to file.
     *
     * @return stateManager instance for this document.
     */
    public StateManager getStateManager() {
        return stateManager;
    }

    /**
     * Returns the total number of pages in this document.
     *
     * @return number of pages in the document
     */
    public int getNumberOfPages() {
        try {
            return catalog.getPageTree().getNumberOfPages();
        } catch (Exception e) {
            logger.log(Level.FINE, "Error getting number of pages.", e);
        }
        return 0;
    }

    /**
     * Paints the contents of the given page number to the graphics context using
     * the specified rotation, zoom, rendering hints and page boundary.
     *
     * @param pageNumber     Page number to paint.  The page number is zero-based.
     * @param g              graphics context to which the page content will be painted.
     * @param renderHintType Constant specified by the GraphicsRenderingHints class.
     *                       There are two possible entries, SCREEN and PRINT, each with configurable
     *                       rendering hints settings.
     * @param pageBoundary   Constant specifying the page boundary to use when
     *                       painting the page content.
     * @param userRotation   Rotation factor, in degrees, to be applied to the rendered page.
     * @param userZoom       Zoom factor to be applied to the rendered page.
     */
    public void paintPage(int pageNumber, Graphics g, final int renderHintType,
                          final int pageBoundary, float userRotation, float userZoom) throws InterruptedException {
        Page page = catalog.getPageTree().getPage(pageNumber);
        page.init();
        PDimension sz = page.getSize(userRotation, userZoom);
        int pageWidth = (int) sz.getWidth();
        int pageHeight = (int) sz.getHeight();

        Graphics gg = g.create(0, 0, pageWidth, pageHeight);
        page.paint(gg, renderHintType, pageBoundary, userRotation, userZoom);

        gg.dispose();
    }

    /**
     * Dispose of Document, freeing up all used resources.
     */
    public void dispose() {

        if (documentSeekableInput != null) {
            try {
                documentSeekableInput.close();
            } catch (IOException e) {
                logger.log(Level.FINE, "Error closing document input stream.", e);
            }
            documentSeekableInput = null;
        }

        String fileToDelete = getDocumentCachedFilePath();
        if (fileToDelete != null) {
            File file = new File(fileToDelete);
            boolean success = file.delete();
            if (!success && logger.isLoggable(Level.WARNING)) {
                logger.warning("Error deleting URL cached to file " + fileToDelete);
            }
        }
    }

    /**
     * Takes the internal PDF data, which may be in a file or in RAM,
     * and write it to the provided OutputStream.
     * The OutputStream is not flushed or closed, in case this method's
     * caller requires otherwise.
     *
     * @param out OutputStream to which the PDF file bytes are written.
     * @return The length of the PDF file copied
     * @throws IOException if there is some problem reading or writing the PDF data
     */
    public long writeToOutputStream(OutputStream out) throws IOException {
        long documentLength = documentSeekableInput.getLength();
        SeekableInputConstrainedWrapper wrapper = new SeekableInputConstrainedWrapper(
                documentSeekableInput, 0L, documentLength);
        try {


            byte[] buffer = new byte[4096];
            int length;
            while ((length = wrapper.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (Throwable e) {
            logger.log(Level.FINE, "Error writing PDF output stream.", e);
            throw new IOException(e.getMessage());
        } finally {
            try {
                wrapper.close();
            } catch (IOException e) {
                // forget about it.
            }
        }
        return documentLength;
    }

    /**
     * Copies the pre-existing PDF file, and appends an incremental update for
     * any edits, to the specified OutputStream. For the pre-existing PDF
     * content copying, writeToOutputStream(OutputStream out) is used.
     *
     * @param out OutputStream to which the PDF file bytes are written.
     * @return The length of the PDF file saved
     * @throws IOException if there is some problem reading or writing the PDF data
     */
    public long saveToOutputStream(OutputStream out) throws IOException {
        long documentLength = writeToOutputStream(out);
        if (foundIncrementalUpdater) {
            try {
                Class<?> incrementalUpdaterClass = Class.forName(INCREMENTAL_UPDATER);
                Object[] argValues = {this, out, documentLength};
                Method method = incrementalUpdaterClass.getDeclaredMethod(
                        "appendIncrementalUpdate",
                        new Class[]{Document.class, OutputStream.class, Long.TYPE});
                long appendedLength = (Long) method.invoke(null, argValues);
                return documentLength + appendedLength;
            } catch (Throwable e) {
                logger.log(Level.FINE, "Could not call incremental updater.", e);
            }
        }
        return documentLength;
    }

    /**
     * Gets an Image of the specified page.  The image size is automatically
     * calculated given the page boundary, user rotation and zoom.  The rendering
     * quality is defined by GraphicsRenderingHints.SCREEN.
     *
     * @param pageNumber     Page number of the page to capture the image rendering.
     *                       The page number is zero-based.
     * @param renderHintType Constant specified by the GraphicsRenderingHints class.
     *                       There are two possible entries, SCREEN and PRINT each with configurable
     *                       rendering hints settings.
     * @param pageBoundary   Constant specifying the page boundary to use when
     *                       painting the page content. Typically use Page.BOUNDARY_CROPBOX.
     * @param userRotation   Rotation factor, in degrees, to be applied to the rendered page.
     *                       Arbitrary rotations are not currently supported for this method,
     *                       so only the following values are valid: 0.0f, 90.0f, 180.0f, 270.0f.
     * @param userZoom       Zoom factor to be applied to the rendered page.
     * @return an Image object of the current page.
     */
    public Image getPageImage(int pageNumber,
                              final int renderHintType, final int pageBoundary,
                              float userRotation, float userZoom) throws InterruptedException {
        Page page = catalog.getPageTree().getPage(pageNumber);
        page.init();
        PDimension sz = page.getSize(pageBoundary, userRotation, userZoom);

        int pageWidth = (int) sz.getWidth();
        int pageHeight = (int) sz.getHeight();

        BufferedImage image = ImageUtility.createCompatibleImage(pageWidth, pageHeight);
        Graphics g = image.createGraphics();

        page.paint(g, renderHintType,
                pageBoundary, userRotation, userZoom);
        g.dispose();

        return image;
    }

    /**
     * Exposes a page's PageText object which can be used to get text with
     * in the PDF document.  The PageText.toString() is the simplest way to
     * get a pages text.  This utility call does not parse the whole stream
     * and is best suited for text extraction functionality as it faster then
     * #getPageViewText(int).
     *
     * @param pageNumber Page number of page in which text extraction will act on.
     *                   The page number is zero-based.
     * @return page PageText data Structure.
     * @see #getPageViewText(int).
     */
    public PageText getPageText(int pageNumber) throws InterruptedException {
        PageTree pageTree = catalog.getPageTree();
        if (pageNumber >= 0 && pageNumber < pageTree.getNumberOfPages()) {
            Page pg = pageTree.getPage(pageNumber);
            return pg.getText();
        } else {
            return null;
        }
    }

    /**
     * Exposes a page's PageText object which can be used to get text with
     * in the PDF document.  The PageText.toString() is the simplest way to
     * get a pages text.  The pageText hierarchy can be used to search for
     * selected text or used to set text as highlighted.
     *
     * @param pageNumber Page number of page in which text extraction will act on.
     *                   The page number is zero-based.
     * @return page PageText data Structure.
     */
    public PageText getPageViewText(int pageNumber) throws InterruptedException{
        PageTree pageTree = catalog.getPageTree();
        if (pageNumber >= 0 && pageNumber < pageTree.getNumberOfPages()) {
            Page pg = pageTree.getPage(pageNumber);
            return pg.getViewText();
        } else {
            return null;
        }
    }

    /**
     * Gets the security manager for this document. If the document has no
     * security manager null is returned.
     *
     * @return security manager for document if available.
     */
    public SecurityManager getSecurityManager() {
        return library.getSecurityManager();
    }

    /**
     * Sets the security callback to be used for this document.  The security
     * callback allows a mechanism for prompting a user for a password if the
     * document is password protected.
     *
     * @param securityCallback a class which implements the SecurityCallback
     *                         interface.
     */
    public void setSecurityCallback(SecurityCallback securityCallback) {
        this.securityCallback = securityCallback;
    }

    /**
     * Gets the document's information as specified in the PTrailer in the document
     * hierarchy.
     *
     * @return document information
     * @see org.icepdf.core.pobjects.PInfo for more information.
     */
    public PInfo getInfo() {
        if (pTrailer == null)
            return null;
        return pTrailer.getInfo();
    }

    /**
     * Enables or disables the form widget annotation highlighting.  Generally not use for print but can be very
     * useful for highlight input fields in a Viewer application.
     *
     * @param highlight true to enable highlight mode, otherwise; false.
     */
    public void setFormHighlight(boolean highlight) {
        // iterate over the document annotations and set the appropriate highlight value.
        if (catalog != null && catalog.getInteractiveForm() != null) {
            InteractiveForm interactiveForm = catalog.getInteractiveForm();
            ArrayList<Object> widgets = interactiveForm.getFields();
            if (widgets != null) {
                for (Object widget : widgets) {
                    descendFormTree(widget, highlight);
                }
            }
        }
    }

    /**
     * Recursively set highlight on all the form fields.
     *
     * @param formNode root form node.
     */
    private void descendFormTree(Object formNode, boolean highLight) {
        if (formNode instanceof AbstractWidgetAnnotation) {
            ((AbstractWidgetAnnotation) formNode).setEnableHighlightedWidget(highLight);
        } else if (formNode instanceof FieldDictionary) {
            // iterate over the kid's array.
            FieldDictionary child = (FieldDictionary) formNode;
            formNode = child.getKids();
            if (formNode != null) {
                ArrayList kidsArray = (ArrayList) formNode;
                for (Object kid : kidsArray) {
                    if (kid instanceof Reference) {
                        kid = library.getObject((Reference) kid);
                    }
                    if (kid instanceof AbstractWidgetAnnotation) {
                        ((AbstractWidgetAnnotation) kid).setEnableHighlightedWidget(highLight);
                    } else if (kid instanceof FieldDictionary) {
                        descendFormTree(kid, highLight);
                    }
                }
            }

        }
    }

    /**
     * Gets a vector of Images where each index represents an image  inside
     * the specified page.  The images are returned in the size in which they
     * where embedded in the PDF document, which may be different than the
     * size displayed when the complete PDF page is rendered.
     *
     * @param pageNumber page number to act on.  Zero-based page number.
     * @return vector of Images inside the current page
     */
    public List<Image> getPageImages(int pageNumber) throws InterruptedException {
        Page pg = catalog.getPageTree().getPage(pageNumber);
        pg.init();
        return pg.getImages();
    }

    /**
     * Gets the Document Catalog's PageTree entry as specified by the Document
     * hierarchy.  The PageTree can be used to obtain detailed information about
     * the Page object which makes up the document.
     *
     * @return PageTree specified by the document hierarchy. Null if the document
     * has not yet loaded or the catalog can not be found.
     */
    public PageTree getPageTree() {
        if (catalog != null) {
            PageTree pageTree = catalog.getPageTree();
            if (pageTree != null) {
                pageTree.setWatermarkCallback(watermarkCallback);
            }
            return pageTree;
        } else {
            return null;
        }
    }

    /**
     * Gets the Document's Catalog as specified by the Document hierarchy. The
     * Catalog can be used to traverse the Document's hierarchy.
     *
     * @return document's Catalog object; null, if one does not exist.
     */
    public Catalog getCatalog() {
        return catalog;
    }

    /**
     * Sets the caching mode when handling file loaded by an URI.  If enabled
     * URI streams will be cached to disk, otherwise they will be stored in
     * memory. This method must be set before a call to setByteArray() or
     * setInputStream() is called.
     *
     * @param cachingEnabled true to enable, otherwise false.
     */
    public static void setCachingEnabled(boolean cachingEnabled) {
        isCachingEnabled = cachingEnabled;
    }
}
