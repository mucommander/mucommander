/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;
import com.mucommander.io.StreamUtils;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A test case for the {@link ResourceLoader} class.
 *
 * @author Maxence Bernard
 */
public class ResourceLoaderTest extends TestCase {

    /**
     * Tests {@link ResourceLoader#getDefaultClassLoader()}.
     */
    public void testDefaultClassLoader() {
        assertNotNull(ResourceLoader.getDefaultClassLoader());
    }

    /**
     * Tests <code>ResourceLoader#getResourceAsURL</code> methods.
     *
     * @throws IOException should not happen
     */
    public void testResourceAsURL() throws IOException {
        assertReadable(ResourceLoader.getResourceAsURL(getExistingResourcePath()));
        assertReadable(ResourceLoader.getResourceAsURL("/"+getExistingResourcePath()));
        assertNull(ResourceLoader.getResourceAsURL(getNonExistingResourcePath()));

        assertReadable(ResourceLoader.getResourceAsURL(getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertReadable(ResourceLoader.getResourceAsURL("/"+getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertNull(ResourceLoader.getResourceAsURL(getNonExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
    }

    /**
     * Tests <code>ResourceLoader#getPackageResourceAsURL</code> methods.
     *
     * @throws IOException should not happen
     */
    public void testPackageResourceAsURL() throws IOException {
        assertReadable(ResourceLoader.getPackageResourceAsURL(getThisPackage(), getExistingResourceName()));
        assertNull(ResourceLoader.getPackageResourceAsURL(getThisPackage(), getNonExistingResourceName()));

        assertReadable(ResourceLoader.getPackageResourceAsURL(getThisPackage(), getExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
        assertNull(ResourceLoader.getPackageResourceAsURL(getThisPackage(), getNonExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
    }

    /**
     * Tests <code>ResourceLoader#getResourceAsStream</code> methods.
     *
     * @throws IOException should not happen
     */
    public void testResourceAsStream() throws IOException {
        assertReadable(ResourceLoader.getResourceAsStream(getExistingResourcePath()));
        assertReadable(ResourceLoader.getResourceAsStream("/"+getExistingResourcePath()));
        assertNull(ResourceLoader.getResourceAsStream(getNonExistingResourcePath()));

        assertReadable(ResourceLoader.getResourceAsStream(getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertReadable(ResourceLoader.getResourceAsStream("/"+getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertNull(ResourceLoader.getResourceAsStream(getNonExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
    }

    /**
     * Tests <code>ResourceLoader#getPackageResourceAsStream</code> methods.
     *
     * @throws IOException should not happen
     */
    public void testPackageResourceAsStream() throws IOException {
        assertReadable(ResourceLoader.getPackageResourceAsStream(getThisPackage(), getExistingResourceName()));
        assertNull(ResourceLoader.getPackageResourceAsStream(getThisPackage(), getNonExistingResourceName()));

        assertReadable(ResourceLoader.getPackageResourceAsStream(getThisPackage(), getExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
        assertNull(ResourceLoader.getPackageResourceAsStream(getThisPackage(), getNonExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
    }

    /**
     * Tests <code>ResourceLoader#getResourceAsFile</code> methods.
     *
     * @throws IOException should not happen
     */
    public void testResourceAsFile() throws IOException {
        assertReadable(ResourceLoader.getResourceAsFile(getExistingResourcePath()));
        assertReadable(ResourceLoader.getResourceAsFile("/"+getExistingResourcePath()));
        assertNull(ResourceLoader.getResourceAsFile(getNonExistingResourcePath()));

        assertReadable(ResourceLoader.getResourceAsFile(getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertReadable(ResourceLoader.getResourceAsFile("/"+getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertNull(ResourceLoader.getResourceAsFile(getNonExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
    }

    /**
     * Tests <code>ResourceLoader#getPackageResourceAsFile</code> methods.
     *
     * @throws IOException should not happen
     */
    public void testPackageResourceAsFile() throws IOException {
        assertReadable(ResourceLoader.getPackageResourceAsFile(getThisPackage(), getExistingResourceName()));
        assertNull(ResourceLoader.getPackageResourceAsFile(getThisPackage(), getNonExistingResourceName()));

        assertReadable(ResourceLoader.getPackageResourceAsFile(getThisPackage(), getExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
        assertNull(ResourceLoader.getPackageResourceAsFile(getThisPackage(), getNonExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
    }

    /**
     * Tests <code>ResourceLoader#getRootPackageAsFile</code> methods.
     *
     * @throws IOException should not happen
     */
    public void testRootPackageAsFile() throws IOException {
        AbstractFile rootPackageFile = ResourceLoader.getRootPackageAsFile(getClass());
        assertNotNull(rootPackageFile);
        assertTrue(rootPackageFile.exists());
        assertTrue(rootPackageFile.isBrowsable());

        AbstractFile thisClassFile = rootPackageFile.getChild("com/mucommander/file/util/ResourceLoaderTest.class");
        assertReadable(thisClassFile);
    }

    /**
     * Tests {@link ResourceLoader#getRelativeClassPath(Class)}.
     */
    public void testRelativeClassPath() {
        assertEquals("com/mucommander/file/util/ResourceLoaderTest.class", ResourceLoader.getRelativeClassPath(getClass()));
    }

    /**
     * Tests {@link ResourceLoader#getRelativePackagePath(Package)}.
     */
    public void testRelativePackagePath() {
        // Returned path does not end with a '/'
        assertEquals("com/mucommander/file/util", ResourceLoader.getRelativePackagePath(getThisPackage()));
    }


    ////////////////////
    // Helper methods //
    ////////////////////

    private void assertReadable(InputStream in) throws IOException {
        TestCase.assertNotNull(in);
        StreamUtils.readUntilEOF(in);
        in.close();
    }

    private void assertReadable(URL url) throws IOException {
        TestCase.assertNotNull(url);
        InputStream in = url.openStream();
        StreamUtils.readUntilEOF(in);
        in.close();
    }

    private void assertReadable(AbstractFile file) throws IOException {
        assertNotNull(file);
        assertReadable(file.getInputStream());
        assertTrue(file.exists());
    }

    private ClassLoader getThisClassLoader() {
        return getClass().getClassLoader();
    }

    private Package getThisPackage() {
        return getClass().getPackage();
    }

    private AbstractFile getRootPackageFile() {
        return ResourceLoader.getRootPackageAsFile(getClass());
    }

    private String getExistingResourceName() {
        return "ResourceLoaderTest.class";
    }

    private String getExistingResourcePath() {
        return "com/mucommander/file/util/"+getExistingResourceName();
    }

    private String getNonExistingResourceName() {
        return "RubberChicken";
    }

    private String getNonExistingResourcePath() {
        return "com/mucommander/file/util/"+getNonExistingResourceName();
    }
}
