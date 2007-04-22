package com.mucommander.file;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;

/**
 * FileProtocolMapping maps a file protocol onto an {@link AbstractFile} class. This class can be used with
 * {@link FileFactory} to register and unregister file protocols at runtime.
 *
 * @see FileFactory, AbstractFile
 * @author Maxence Bernard
 */
public class FileProtocolMapping {

    // This fields have package access to allow FileFactory to access them directly, a little faster than using the
    // accessor methods

    /** the Class associated with the file protocol */
    Class providerClass;
    /** the provider class' constructor that is used to create new file instances */
    Constructor providerConstructor;
    /** the file procotol associated with the provider class */
    String protocol;


    /**
     * Creates a new FileProtocolMapping that associates the given file protocol to the given {@link AbstractFile} class.
     *
     * <p>The class denoted by the specified Class instance must satisfy two conditions:
     * <ul>
     *  <li>it must extend AbstractFile
     *  <li>it must provide a constructor with the {@link AbstractFile#AbstractFile(FileURL)} signature
     * </ul>
     * If any of those 2 conditions are not satisfied, an exception will be thrown.
     *
     * @param abstractFileClass a Class instance denoting a class which extends {@link AbstractFile} and which has
     * a constructor with the {@link AbstractFile#AbstractFile(FileURL)} signature
     * @param protocol the protocol to associate with the specified AbstractFile class (e.g. "ftp")
     * @throws IntrospectionException if the specified class does not extend {@link AbstractFile}
     * @throws NoSuchMethodException if the specified class does not provide a constructor with the {@link AbstractFile#AbstractFile(FileURL)} signature
     * @throws SecurityException if access to the constructor is denied
     */
    public FileProtocolMapping(Class abstractFileClass, String protocol) throws IntrospectionException, NoSuchMethodException, SecurityException {
        this.providerClass = abstractFileClass;
        this.protocol = protocol;

        if(!AbstractFile.class.isAssignableFrom(abstractFileClass))
            throw new IntrospectionException(abstractFileClass.getName()+" does not extends "+AbstractFile.class.getName());

        this.providerConstructor = abstractFileClass.getConstructor(new Class[]{FileURL.class});
    }

    /**
     * Returns the file procotol associated with the provider class.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the Class denoting the {@link AbstractFile} class associated with the file protocol.
     */
    public Class getProviderClass() {
        return providerClass;
    }

    /**
     * Returns the provider class' constructor that is used to create new file instances.
     */
    public Constructor getProviderConstructor() {
        return providerConstructor;
    }

    /**
     * Returns <code>true</code> if the given Object is a FileProtocolMapping instance with the same file protocol
     * and AbstractFile class.
     */
    public boolean equals(Object o) {
        if(!(o instanceof FileProtocolMapping))
            return false;

        FileProtocolMapping fpm = (FileProtocolMapping)o;
        return fpm.providerClass.equals(providerClass) && fpm.protocol.equals(protocol);
    }
}
