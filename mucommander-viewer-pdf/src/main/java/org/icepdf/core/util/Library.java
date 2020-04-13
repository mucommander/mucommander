/*
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
package org.icepdf.core.util;

import org.icepdf.core.io.SeekableInput;
import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.acroform.InteractiveForm;
import org.icepdf.core.pobjects.acroform.SignatureHandler;
import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.core.pobjects.fonts.FontDescriptor;
import org.icepdf.core.pobjects.graphics.ICCBased;
import org.icepdf.core.pobjects.graphics.ImagePool;
import org.icepdf.core.pobjects.security.SecurityManager;

import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * <p>The <code>Library</code> class acts a central repository for the access
 * of PDF objects in a document.  The Library class has many utility methods
 * which are designed to access PDF objects as easily as possible.  The
 * <code>Library</code> class has direct access to the PDF file and loads the
 * needed objects from the file system when needed. </p>
 *
 * @since 1.0
 */
public class Library {

    private static final Logger log =
            Logger.getLogger(Library.class.toString());

    protected static ThreadPoolExecutor commonThreadPool;
    protected static ThreadPoolExecutor imageThreadPool;

    protected static int commonPoolThreads;
    protected static int imagePoolThreads;
    private static final long KEEP_ALIVE_TIME = 90;

    static {
        try {
            commonPoolThreads =
                    Defs.intProperty("org.icepdf.core.library.threadPoolSize", 2);
            if (commonPoolThreads < 1) {
                commonPoolThreads = 2;
            }
        } catch (NumberFormatException e) {
            log.warning("Error reading buffered scale factor");
        }

        try {
            // todo make ImageReference call interruptible and then we can get rid of this pool.
            imagePoolThreads =
                    Defs.intProperty("org.icepdf.core.library.imageThreadPoolSize", 2);
            if (imagePoolThreads < 1) {
                imagePoolThreads = 2;
            }
        } catch (NumberFormatException e) {
            log.warning("Error reading buffered scale factor");
        }

        log.fine("Starting ICEpdf Thread Pools: " +
                (commonPoolThreads + imagePoolThreads) +
                " threads.");
        initializeThreadPool();
    }

    // new incremental file loader class.
    private LazyObjectLoader lazyObjectLoader;
    private ConcurrentHashMap<Reference, WeakReference<Object>> refs =
            new ConcurrentHashMap<Reference, WeakReference<Object>>(1024);
    private ConcurrentHashMap<Reference, WeakReference<ICCBased>> lookupReference2ICCBased =
            new ConcurrentHashMap<Reference, WeakReference<ICCBased>>(256);
    // Instead of keeping Names names, Dictionary dests, we keep
    //   a reference to the Catalog, which actually owns them
    private Catalog catalog;

    private SecurityManager securityManager;

    // handles signature validation and signing.
    private SignatureHandler signatureHandler;

    // signature permissions
    private Permissions permissions;

    private SeekableInput documentInput;


    // state manager reference needed by most classes to properly managed state
    // changes and new object creation
    public StateManager stateManager;

    private boolean isEncrypted;
    private boolean isLinearTraversal;
    private ImagePool imagePool;

    /**
     * Sets a document loader for the library.
     *
     * @param lol loader object.
     */
    public void setLazyObjectLoader(LazyObjectLoader lol) {
        lazyObjectLoader = lol;
    }

    /**
     * Gets the document's trailer.
     *
     * @param position byte offset of the trailer in the PDF file.
     * @return trailer dictionary
     */
    public PTrailer getTrailerByFilePosition(long position) {
        if (lazyObjectLoader == null)
            return null;
        return lazyObjectLoader.loadTrailer(position);
    }

    /**
     * Gets the object specified by the reference.
     *
     * @param reference reference to a PDF object in the document structure.
     * @return PDF object dictionary that the reference refers to.  Null if the
     * object reference can not be found.
     */
    public Object getObject(Reference reference) {
        Object ob;
        while (true) {
            WeakReference<Object> obRef = refs.get(reference);
            // check stateManager first to allow for annotations to be injected
            // from a separate file.
            if (stateManager != null) {
                if (stateManager.contains(reference)) {
                    ob = stateManager.getChange(reference);
                    if (ob instanceof PObject){
                        return ((PObject) ob).getObject();
                    }
                    return ob;
                }
            }
            ob = obRef != null ? obRef.get() : null;
            if (ob == null && lazyObjectLoader != null) {
                ob = lazyObjectLoader.loadObject(reference);
            }
            if (ob instanceof PObject) {
                return ((PObject) ob).getObject();
            } else if (ob instanceof Reference) {
                reference = (Reference) ob;
            } else {
                break;
            }
        }
        return ob;
    }

    /**
     * Utility method for displaying debug info related to PDF object loading.
     *
     * @param ob object to show debug information for
     */
    private void printObjectDebug(Object ob) {
        if (ob == null) {
            log.finer("null object found");
        } else if (ob instanceof PObject) {
            PObject tmp = (PObject) ob;
            log.finer(tmp.getReference() + " " + tmp.toString());
        } else if (ob instanceof Dictionary) {
            Dictionary tmp = (Dictionary) ob;
            log.finer(tmp.getPObjectReference() + " " + tmp.toString());
        } else {
            log.finer(ob.getClass() + " " + ob.toString());
        }
    }

    /**
     * Gets the PDF object specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the object that the reference
     * points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return PDF object that the key references.
     * @see #getObjectReference(java.util.HashMap, Name)
     */
    public Object getObject(HashMap dictionaryEntries, Name key) {
        if (dictionaryEntries == null) {
            return null;
        }
        Object o = dictionaryEntries.get(key);
        if (o == null)
            return null;
        if (o instanceof Reference) {
            o = getObject((Reference) o);
        }
        return o;
    }

    /**
     * Test to see if the given key is a reference and not an inline dictinary
     *
     * @param dictionaryEntries dictionary to test
     * @param key               dictionary key
     * @return true if the key value exists and is a reference, false if the
     * dictionaryEntries are null or the key references an inline dictionary
     */
    public boolean isReference(HashMap dictionaryEntries, Name key) {
        return dictionaryEntries != null &&
                dictionaryEntries.get(key) instanceof Reference;

    }

    /**
     * Gets the reference association of the key if any.  This method is usual
     * used in combination with #isReference to get and assign the Reference
     * for a given PObject.
     *
     * @param dictionaryEntries dictionary to search in.
     * @param key               key to search for in dictionary.
     * @return reference of the object that key points if any.  Null if the key
     * points to an inline dictionary and not a reference.
     */
    public Reference getReference(HashMap dictionaryEntries, Name key) {
        Object ref = dictionaryEntries.get(key);
        if (ref instanceof Reference) {
            return (Reference) ref;
        } else {
            return null;
        }
    }

    /**
     * Gets the state manager class which keeps track of changes PDF objects.
     *
     * @return document state manager
     */
    public StateManager getStateManager() {
        return stateManager;
    }

    /**
     * Sets the document state manager so that all object can access the
     * state manager via the central library instance.
     *
     * @param stateManager reference to the state change class
     */
    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    /**
     * Gets the PDF object that the <code>referenceObject</code> references.
     *
     * @param referenceObject reference object.
     * @return PDF object that <code>referenceObject</code> references.  If
     * <code>referenceObject</code> is not an instance of a Reference, the
     * origional <code>referenceObject</code> is returned.
     */
    public Object getObject(Object referenceObject) {
        if (referenceObject instanceof Reference) {
            return getObject((Reference) referenceObject);
        }
        return referenceObject;
    }

    /**
     * Tests if the given key will return a non-null PDF object from the
     * specified dictionary entries.  A null PDF object would result if no
     * PDF object could be found with the specified key.
     *
     * @param dictionaryEntries dictionary entries
     * @param key               dictionary key
     * @return true, if the key's value is non-null PDF object; false, otherwise.
     */
    public boolean isValidEntry(HashMap dictionaryEntries, Name key) {
        if (dictionaryEntries == null) {
            return false;
        }
        Object o = dictionaryEntries.get(key);
        return o != null && (!(o instanceof Reference) || isValidEntry((Reference) o));
    }

    /**
     * Tests if there exists a cross-reference entry for this reference.
     *
     * @param reference reference to a PDF object in the document structure.
     * @return true, if a cross-reference entry exists for this reference; false, otherwise.
     */
    public boolean isValidEntry(Reference reference) {
        WeakReference<Object> ob = refs.get(reference);
        return (ob != null && ob.get() != null) ||
                lazyObjectLoader != null &&
                        lazyObjectLoader.haveEntry(reference);
    }

    /**
     * Gets a Number specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the Number object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return Number object if a valid key;  null, if the key does not point
     * to Number or is invalid.
     */
    public Number getNumber(HashMap dictionaryEntries, Name key) {
        Object o = getObject(dictionaryEntries, key);
        if (o instanceof Number)
            return (Number) o;
        return null;
    }

    /**
     * Gets a Boolean specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the Boolean object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return Number object if a valid key;  null, if the key does not point
     * to Number or is invalid.
     */
    public Boolean getBoolean(HashMap dictionaryEntries, Name key) {
        Object o = getObject(dictionaryEntries, key);
        if (o instanceof String)
            return Boolean.valueOf((String) o);
        else return o instanceof Boolean && (Boolean) o;
    }

    /**
     * Gets a float specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return float value if a valid key;  null, if the key does not point
     * to a float or is invalid.
     */
    public float getFloat(HashMap dictionaryEntries, Name key) {
        Number n = getNumber(dictionaryEntries, key);
        return (n != null) ? n.floatValue() : 0.0f;
    }

    /**
     * Gets an int specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return int value if a valid key,  null if the key does not point
     * to an int or is invalid.
     */
    public int getInt(HashMap dictionaryEntries, Name key) {
        Number n = getNumber(dictionaryEntries, key);
        return (n != null) ? n.intValue() : 0;
    }

    /**
     * Gets a float specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return float value if a valid key;  null, if the key does not point
     * to a float or is invalid.
     */
    public long getLong(HashMap dictionaryEntries, Name key) {
        Number n = getNumber(dictionaryEntries, key);
        return (n != null) ? n.longValue() : 0L;
    }

    /**
     * Gets a Name specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the Name object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return Name object if a valid key;  null, if the key does not point
     * to Name or is invalid.
     */
    public Name getName(HashMap dictionaryEntries, Name key) {
        Object o = getObject(dictionaryEntries, key);
        if (o != null) {
            if (o instanceof Name) {
                return (Name) o;
            }
        }
        return null;
    }

    /**
     * Gets a text string specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the string object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return string object if a valid key;  null, if the key does not point
     * to Name or is invalid.
     */
    public String getString(HashMap dictionaryEntries, Name key) {
        Object o = getObject(dictionaryEntries, key);
        if (o != null) {
            if (o instanceof String) {
                return ((String) o);
            } else if (o instanceof StringObject) {
                return ((StringObject) o).getDecryptedLiteralString(securityManager);
            } else if (o instanceof Name) {
                return ((Name) o).getName();
            }
        }
        return null;
    }

    /**
     * Gets a dictionary specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference, the dictionary object that the
     * reference points to is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return dictionary object if a valid key;  null, if the key does not point
     * to dictionary or is invalid.
     */
    @SuppressWarnings("unchecked")
    public HashMap getDictionary(HashMap dictionaryEntries, Name key) {
        Object o = getObject(dictionaryEntries, key);
        if (o instanceof HashMap) {
            return (HashMap) o;
        } else if (o instanceof List) {
            List v = (List) o;
            HashMap h1 = new HashMap();
            for (Object o1 : v) {
                if (o1 instanceof HashMap) {
                    h1.putAll((HashMap) o1);
                }
            }
            return h1;
        }
        return null;
    }

    public List getArray(HashMap dictionaryEntries, Name key) {
        Object o = getObject(dictionaryEntries, key);
        if (o instanceof List) {
            return (List) o;
        }
        return null;
    }

    /**
     * Gets a rectangle specified by the key.  The rectangle is already
     * in the coordinate system of Java2D, and thus must be used carefully.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return rectangle in Java2D coordinate system.
     */
    public Rectangle2D.Float getRectangle(HashMap dictionaryEntries, Name key) {
        List v = (List) getObject(dictionaryEntries, key);
        if (v != null) {
            // s by default contains data in the Cartesian plain.
            return new PRectangle(v).toJava2dCoordinates();
        } else {
            return null;
        }
    }

    /**
     * The Reference is to the Stream from which the ICC color space data
     * is to be parsed. So, without this method, we would be making and
     * initializing a new ICCBased object every time one was needed, since
     * the Reference is not for the ICCBased object itself.
     *
     * @param ref Reference to Stream containing ICC color space data
     * @return ICCBased color model object for the given reference
     */
    public ICCBased getICCBased(Reference ref) {
        ICCBased cs = null;

        WeakReference<ICCBased> csRef = lookupReference2ICCBased.get(ref);
        if (csRef != null) {
            cs = csRef.get();
        }

        if (cs == null) {
            Object obj = getObject(ref);
            if (obj instanceof Stream) {
                Stream stream = (Stream) obj;
                cs = new ICCBased(this, stream);
                lookupReference2ICCBased.put(ref, new WeakReference<ICCBased>(cs));
            }
        }
        return cs;
    }

    @SuppressWarnings("unchecked")
    public Resources getResources(HashMap dictionaryEntries, Name key) {
        if (dictionaryEntries == null)
            return null;
        Object ob = dictionaryEntries.get(key);
        if (ob == null)
            return null;
        else if (ob instanceof Resources)
            return (Resources) ob;
        else if (ob instanceof Reference) {
            Reference reference = (Reference) ob;
            return getResources(reference);
        } else if (ob instanceof HashMap) {
            HashMap ht = (HashMap) ob;
            Resources resources = new Resources(this, ht);
            dictionaryEntries.put(key, resources);
            return resources;
        }
        return null;
    }

    public Resources getResources(Reference reference) {
        Object object = getObject(reference);
        if (object instanceof Resources) {
            return (Resources) object;
        } else if (object instanceof HashMap) {
            HashMap ht = (HashMap) object;
            Resources resources = new Resources(this, ht);
            addObject(resources, reference);
            return resources;
        }
        return null;
    }

    /**
     * Adds a PDF object and its respective object reference to the library.
     *
     * @param object          PDF object to add.
     * @param objectReference PDF object reference object.
     */
    public void addObject(Object object, Reference objectReference) {
        refs.put(objectReference, new WeakReference<Object>(object));
    }

    /**
     * Removes an object from from the library.
     *
     * @param objetReference object reference to remove to library
     */
    public void removeObject(Reference objetReference) {
        if (objetReference != null) {
            refs.remove(objetReference);
        }
    }

    /**
     * Creates a new instance of a Library.
     */
    public Library() {
        // set Catalog memory Manager and cache manager.
        imagePool = new ImagePool();
        signatureHandler = new SignatureHandler();
    }

    /**
     * Sets a pointer to the orginal document input stream
     *
     * @param documentInput seekable inputstream.
     */
    public void setDocumentInput(SeekableInput documentInput) {
        this.documentInput = documentInput;
    }

    /**
     * Gets the SeekableInput of the document underlying bytes.
     *
     * @return document bytes.
     */
    public SeekableInput getDocumentInput() {
        return documentInput;
    }

    /**
     * Gets the PDF object specified by the <code>key</code> in the dictionary
     * entries.  If the key value is a reference it is returned.
     *
     * @param dictionaryEntries the dictionary entries to look up the key in.
     * @param key               string value representing the dictionary key.
     * @return the Reference specified by the PDF key.  If the key is invalid
     * or does not reference a Reference object, null is returned.
     * @see #getObject(java.util.HashMap, Name)
     */
    public Reference getObjectReference(HashMap dictionaryEntries,
                                        Name key) {
        if (dictionaryEntries == null) {
            return null;
        }
        Object o = dictionaryEntries.get(key);
        if (o == null)
            return null;
        Reference currentRef = null;
        while (o != null && (o instanceof Reference)) {
            currentRef = (Reference) o;
            o = getObject(currentRef);
        }
        return currentRef;
    }

    /**
     * Indicates that document is encrypted using Adobe Standard Encryption.
     *
     * @return true if the document is encrypted, false otherwise.
     */
    public boolean isEncrypted() {
        return isEncrypted;
    }

    /**
     * Gets the document's security manger.
     *
     * @return document's security manager if the document is encrypted, null
     * otherwise.
     */
    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public SignatureHandler getSignatureHandler() {
        return signatureHandler;
    }

    /**
     * Set a documents permissions for a given certificate of signature, optional.
     * The permission should also be used with the encryption permissions if present
     * to configure the viewer permissions.
     *
     * @return permission object if present, otherwise null.
     */
    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    /**
     * Set the document is encrypted flag.
     *
     * @param flag true, if the document is encrypted; false, otherwize.
     */
    public void setEncrypted(boolean flag) {
        isEncrypted = flag;
    }

    /**
     * When we fail to load the required xref tables or streams that are
     * needed to access the objects in the PDF, then we simply go to the
     * beginning of the file and read in all of the objects into memory,
     * which we call linear traversal.
     */
    public void setLinearTraversal() {
        isLinearTraversal = true;
    }

    /**
     * There are several implications from using linear traversal, which
     * affect how we parse the PDF objects, and maintain them in memory,
     * so those sections of code need to check this flag here.
     *
     * @return If PDF was parsed via linear traversal
     */
    public boolean isLinearTraversal() {
        return isLinearTraversal;
    }

    /**
     * Gets the document's catalog.
     *
     * @return document's catalog.
     */
    public Catalog getCatalog() {
        return catalog;
    }

    /**
     * Sets the document's catalog.  Normally only accessed by the document's parser.
     *
     * @param c document catalog object.
     */
    public void setCatalog(Catalog c) {
        catalog = c;
    }

    /**
     * Checks the Catalog for an interactive Forms dictionary and if found the resources object
     * is used for a font lookup.
     *
     * @param fontName font name to look for.
     * @return font font,  null otherwise.
     */
    public Font getInteractiveFormFont(String fontName) {
        InteractiveForm form = getCatalog().getInteractiveForm();
        if (form != null && form.getResources() != null) {
            Resources resources = form.getResources();
            return resources.getFont(new Name(fontName));
        }
        return null;
    }

    /**
     * Utility/demo functionality to clear all font and font descriptor
     * resources.  The library will re-fetch the font resources in question
     * when needed again.
     */
    public void disposeFontResources() {
        Set<Reference> test = refs.keySet();
        for (Reference ref : test) {
            WeakReference<Object> reference = refs.get(ref);
            Object tmp = reference != null ? reference.get() : null;
            if (tmp instanceof Font || tmp instanceof FontDescriptor) {
                refs.remove(ref);
            }
        }
    }

    public ImagePool getImagePool() {
        return imagePool;
    }

    public static void initializeThreadPool() {

        log.fine("Starting ICEpdf Thread Pool: " + commonPoolThreads + " threads.");
        commonThreadPool = new ThreadPoolExecutor(
                commonPoolThreads, commonPoolThreads, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        // set a lower thread priority
        commonThreadPool.setThreadFactory(new ThreadFactory() {
            public Thread newThread(java.lang.Runnable command) {
                Thread newThread = new Thread(command);
                newThread.setName("ICEpdf-thread-pool");
                newThread.setPriority(Thread.NORM_PRIORITY);
                newThread.setDaemon(true);
                return newThread;
            }
        });

        imageThreadPool = new ThreadPoolExecutor(
                imagePoolThreads, imagePoolThreads, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        // set a lower thread priority
        imageThreadPool.setThreadFactory(new ThreadFactory() {
            public Thread newThread(java.lang.Runnable command) {
                Thread newThread = new Thread(command);
                newThread.setName("ICEpdf-thread-image-pool");
                newThread.setPriority(Thread.NORM_PRIORITY);
                newThread.setDaemon(true);
                return newThread;
            }
        });
    }

    public static void shutdownThreadPool() {
        // do a little clean up.
        commonThreadPool.purge();
        commonThreadPool.shutdownNow();
        imageThreadPool.purge();
        imageThreadPool.shutdownNow();
    }

    public static void execute(Runnable runnable) {
        try {
            commonThreadPool.execute(runnable);
        } catch (RejectedExecutionException e) {
            log.severe("ICEpdf Common Thread Pool was shutdown!");
        }
    }

    public static void executeImage(FutureTask callable) {
        try {
            imageThreadPool.execute(callable);
        } catch (RejectedExecutionException e) {
            log.severe("ICEpdf Common Thread Pool was shutdown!");
        }
    }
}
