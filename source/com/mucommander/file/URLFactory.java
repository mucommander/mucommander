package com.mucommander.file;

import com.mucommander.cache.LRUCache;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;

import java.net.MalformedURLException;

/**
 * Provides static methods for retrieving FileURL instances.
 * An LRU cache is used to cache the most frequently accessed FileURL.
 *
 * @author Maxence Bernard
 */
public class URLFactory {

    /** Static LRUCache instance that caches frequently accessed FileURL instances */
    private static LRUCache urlCache = LRUCache.createInstance(ConfigurationManager.getVariableInt(ConfigurationVariables.URL_CACHE_CAPACITY, ConfigurationVariables.DEFAULT_URL_CACHE_CAPACITY));


    /**
     * Creates and returns a FileURL using the given url (or path), or null if the FileURL could not be created
     * (MalformedURLException was thrown).
     *
     * <p>A lookup is first made to the LRU cache to look for an existing FileURL intance,
     * and only if one wasn't found, a new FileURL instance is created and added to the cache.
     *
     * @param url the URL (or local path) to wrap into a FileURL instance
     */
    public static FileURL getFileURL(String url) {
        try {
            return getFileURL(url, null, false);
        }
        catch(MalformedURLException e) {
            // Should never happen
            return null;
        }
    }


    /**
     * Creates and returns a FileURL using the given url and parent, or null if the FileURL could not be created
     * (path or url malformed).
     *
     * <p>A lookup is first made to the LRU cache to look for an existing FileURL intance,
     * and only if one wasn't found, a new FileURL instance is created and added to the cache.
     *
     * @param url the URL (or local path) to wrap into a FileURL instance
     * @param parentURL the parent FileURL to use as new FileURL's parent
     */
    public static FileURL getFileURL(String url, FileURL parentURL) {
        try {
            return getFileURL(url, parentURL, false);
        }
        catch(MalformedURLException e) {
            // Should never happen
            return null;
        }
    }


    /**
     * Creates and returns a FileURL using the given url and parent. If the {@param throwException} parameter is set
     * to true, a MalformedURLException is thrown if the given URL or path is malformed. If not, null is simply returned.
     *
     * <p>A lookup is first made to the LRU cache to look for an existing FileURL intance,
     * and only if one wasn't found, a new FileURL instance is created and added to the cache.
     *
     * @param url the URL (or local path) to wrap into a FileURL instance
     * @param parentURL the parent FileURL to use as new FileURL's parent
     */
    public static FileURL getFileURL(String url, FileURL parentURL, boolean throwException) throws MalformedURLException {
        try {
            // First, try and find a cached FileURL instance
            FileURL fileURL = (FileURL)urlCache.get(url);

            // FileURL not in cache, let's create it and add it to the cache
            if(fileURL==null) {
                // A MalformedURLException if the provided URL/path is malformed
                fileURL = new FileURL(url, parentURL);		// Reuse parent file's FileURL (if any)

                // FileURL cache is not used for protocols other than 'file' as FileURL are mutable
                // (setLogin, setPassword, setPort) and it may cause some weird side effects
                if(fileURL.getProtocol().equals("file"))
                    urlCache.add(url, fileURL);

            }

//            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("url cache hits/misses: "+urlCache.getHitCount()+"/"+urlCache.getMissCount()+" size="+urlCache.size());
            return fileURL;
        }
        catch(MalformedURLException e) {
            if(throwException)
                throw e;

            return null;
        }
    }
}
