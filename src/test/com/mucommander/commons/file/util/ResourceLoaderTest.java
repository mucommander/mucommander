/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.StreamUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A test case for the {@link ResourceLoader} class.
 *
 * @author Maxence Bernard
 */
public class ResourceLoaderTest {

    /**
     * Tests {@link ResourceLoader#getDefaultClassLoader()}.
     */
    @Test
    public void testDefaultClassLoader() {
        assert ResourceLoader.getDefaultClassLoader() != null;
    }

    /**
     * Tests <code>ResourceLoader#getResourceAsURL</code> methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testResourceAsURL() throws IOException {
        assertReadable(ResourceLoader.getResourceAsURL(getExistingResourcePath()));
        assertReadable(ResourceLoader.getResourceAsURL("/"+getExistingResourcePath()));
        assert ResourceLoader.getResourceAsURL(getNonExistingResourcePath()) == null;

        assertReadable(ResourceLoader.getResourceAsURL(getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertReadable(ResourceLoader.getResourceAsURL("/"+getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assert ResourceLoader.getResourceAsURL(getNonExistingResourcePath(), getThisClassLoader(), getRootPackageFile()) == null;
    }

    /**
     * Tests <code>ResourceLoader#getPackageResourceAsURL</code> methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testPackageResourceAsURL() throws IOException {
        assertReadable(ResourceLoader.getPackageResourceAsURL(getThisPackage(), getExistingResourceName()));
        assert ResourceLoader.getPackageResourceAsURL(getThisPackage(), getNonExistingResourceName()) == null;

        assertReadable(ResourceLoader.getPackageResourceAsURL(getThisPackage(), getExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
        assert ResourceLoader.getPackageResourceAsURL(getThisPackage(), getNonExistingResourceName(), getThisClassLoader(), getRootPackageFile()) == null;
    }

    /**
     * Tests <code>ResourceLoader#getResourceAsStream</code> methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testResourceAsStream() throws IOException {
        assertReadable(ResourceLoader.getResourceAsStream(getExistingResourcePath()));
        assertReadable(ResourceLoader.getResourceAsStream("/"+getExistingResourcePath()));
        assert ResourceLoader.getResourceAsStream(getNonExistingResourcePath()) == null;

        assertReadable(ResourceLoader.getResourceAsStream(getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertReadable(ResourceLoader.getResourceAsStream("/"+getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assert ResourceLoader.getResourceAsStream(getNonExistingResourcePath(), getThisClassLoader(), getRootPackageFile()) == null;
    }

    /**
     * Tests <code>ResourceLoader#getPackageResourceAsStream</code> methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testPackageResourceAsStream() throws IOException {
        assertReadable(ResourceLoader.getPackageResourceAsStream(getThisPackage(), getExistingResourceName()));
        assert ResourceLoader.getPackageResourceAsStream(getThisPackage(), getNonExistingResourceName()) == null;

        assertReadable(ResourceLoader.getPackageResourceAsStream(getThisPackage(), getExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
        assert ResourceLoader.getPackageResourceAsStream(getThisPackage(), getNonExistingResourceName(), getThisClassLoader(), getRootPackageFile()) == null;
    }

    /**
     * Tests <code>ResourceLoader#getResourceAsFile</code> methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testResourceAsFile() throws IOException {
        assertReadable(ResourceLoader.getResourceAsFile(getExistingResourcePath()));
        assertReadable(ResourceLoader.getResourceAsFile("/"+getExistingResourcePath()));
        assert ResourceLoader.getResourceAsFile(getNonExistingResourcePath()) == null;

        assertReadable(ResourceLoader.getResourceAsFile(getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assertReadable(ResourceLoader.getResourceAsFile("/"+getExistingResourcePath(), getThisClassLoader(), getRootPackageFile()));
        assert ResourceLoader.getResourceAsFile(getNonExistingResourcePath(), getThisClassLoader(), getRootPackageFile()) == null;
    }

    /**
     * Tests <code>ResourceLoader#getPackageResourceAsFile</code> methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testPackageResourceAsFile() throws IOException {
        assertReadable(ResourceLoader.getPackageResourceAsFile(getThisPackage(), getExistingResourceName()));
        assert ResourceLoader.getPackageResourceAsFile(getThisPackage(), getNonExistingResourceName()) == null;

        assertReadable(ResourceLoader.getPackageResourceAsFile(getThisPackage(), getExistingResourceName(), getThisClassLoader(), getRootPackageFile()));
        assert ResourceLoader.getPackageResourceAsFile(getThisPackage(), getNonExistingResourceName(), getThisClassLoader(), getRootPackageFile()) == null;
    }

    /**
     * Tests <code>ResourceLoader#getRootPackageAsFile</code> methods.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testRootPackageAsFile() throws IOException {
        AbstractFile rootPackageFile = ResourceLoader.getRootPackageAsFile(getClass());
        assert rootPackageFile != null;
        assert rootPackageFile.exists();
        assert rootPackageFile.isBrowsable();

        AbstractFile thisClassFile = rootPackageFile.getChild("com/mucommander/commons/file/util/ResourceLoaderTest.class");
        assertReadable(thisClassFile);
    }

    /**
     * Tests {@link ResourceLoader#getRelativeClassPath(Class)}.
     */
    @Test
    public void testRelativeClassPath() {
        assert "com/mucommander/commons/file/util/ResourceLoaderTest.class".equals(ResourceLoader.getRelativeClassPath(getClass()));
    }

    /**
     * Tests {@link ResourceLoader#getRelativePackagePath(Package)}.
     */
    @Test
    public void testRelativePackagePath() {
        // Returned path does not end with a '/'
        assert "com/mucommander/commons/file/util".equals(ResourceLoader.getRelativePackagePath(getThisPackage()));
    }


    ////////////////////
    // Helper methods //
    ////////////////////

    private void assertReadable(InputStream in) throws IOException {
        assert in != null;
        StreamUtils.readUntilEOF(in);
        in.close();
    }

    private void assertReadable(URL url) throws IOException {
        assert url != null;
        InputStream in = url.openStream();
        StreamUtils.readUntilEOF(in);
        in.close();
    }

    private void assertReadable(AbstractFile file) throws IOException {
        assert file != null;
        assertReadable(file.getInputStream());
        assert file.exists();
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
        return "com/mucommander/commons/file/util/"+getExistingResourceName();
    }

    private String getNonExistingResourceName() {
        return "RubberChicken";
    }

    private String getNonExistingResourcePath() {
        return "com/mucommander/commons/file/util/"+getNonExistingResourceName();
    }
}
