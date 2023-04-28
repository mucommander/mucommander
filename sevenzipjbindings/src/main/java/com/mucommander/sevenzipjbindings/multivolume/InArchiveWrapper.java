/**
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.sevenzipjbindings.multivolume;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.IOutUpdateArchive;
import net.sf.sevenzipjbinding.IOutUpdateArchive7z;
import net.sf.sevenzipjbinding.IOutUpdateArchiveBZip2;
import net.sf.sevenzipjbinding.IOutUpdateArchiveGZip;
import net.sf.sevenzipjbinding.IOutUpdateArchiveTar;
import net.sf.sevenzipjbinding.IOutUpdateArchiveZip;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.PropertyInfo;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;

public class InArchiveWrapper implements IInArchive {

    private static final Logger LOGGER = LoggerFactory.getLogger(InArchiveWrapper.class);

    private final IInArchive archive;

    private final Closeable[] closeables;

    public InArchiveWrapper(IInArchive archive, Closeable... closeables) {
        this.archive = archive;
        this.closeables = closeables;
    }

    @Override
    public int getNumberOfItems() throws SevenZipException {
        return getMainArchive().getNumberOfItems();
    }

    @Override
    public Object getProperty(int index, PropID propID) throws SevenZipException {
        return getMainArchive().getProperty(index, propID);
    }

    @Override
    public String getStringProperty(int index, PropID propID) throws SevenZipException {
        return getMainArchive().getStringProperty(index, propID);
    }

    @Override
    public void extract(int[] indices, boolean testMode, IArchiveExtractCallback extractCallback) throws SevenZipException {
        getMainArchive().extract(indices, testMode, extractCallback);
    }

    @Override
    public ExtractOperationResult extractSlow(int index, ISequentialOutStream outStream) throws SevenZipException {
        return getMainArchive().extractSlow(index, outStream);
    }

    @Override
    public ExtractOperationResult extractSlow(int index, ISequentialOutStream outStream, String password) throws SevenZipException {
        return getMainArchive().extractSlow(index, outStream, password);
    }

    @Override
    public Object getArchiveProperty(PropID propID) throws SevenZipException {
        return getMainArchive().getArchiveProperty(propID);
    }

    @Override
    public String getStringArchiveProperty(PropID propID) throws SevenZipException {
        return getMainArchive().getStringArchiveProperty(propID);
    }

    @Override
    public int getNumberOfProperties() throws SevenZipException {
        return getMainArchive().getNumberOfProperties();
    }

    @Override
    public PropertyInfo getPropertyInfo(int index) throws SevenZipException {
        return getMainArchive().getPropertyInfo(index);
    }

    @Override
    public int getNumberOfArchiveProperties() throws SevenZipException {
        return getMainArchive().getNumberOfArchiveProperties();
    }

    @Override
    public PropertyInfo getArchivePropertyInfo(int index) throws SevenZipException {
        return getMainArchive().getArchivePropertyInfo(index);
    }

    @Override
    public ISimpleInArchive getSimpleInterface() {
        return getMainArchive().getSimpleInterface();
    }

    @Override
    public ArchiveFormat getArchiveFormat() {
        return getMainArchive().getArchiveFormat();
    }

    @Override
    public IOutUpdateArchive<IOutItemAllFormats> getConnectedOutArchive() throws SevenZipException {
        return getMainArchive().getConnectedOutArchive();
    }

    @Override
    public IOutUpdateArchive7z getConnectedOutArchive7z() throws SevenZipException {
        return getMainArchive().getConnectedOutArchive7z();
    }

    @Override
    public IOutUpdateArchiveZip getConnectedOutArchiveZip() throws SevenZipException {
        return getMainArchive().getConnectedOutArchiveZip();
    }

    @Override
    public IOutUpdateArchiveTar getConnectedOutArchiveTar() throws SevenZipException {
        return getMainArchive().getConnectedOutArchiveTar();
    }

    @Override
    public IOutUpdateArchiveGZip getConnectedOutArchiveGZip() throws SevenZipException {
        return getMainArchive().getConnectedOutArchiveGZip();
    }

    @Override
    public IOutUpdateArchiveBZip2 getConnectedOutArchiveBZip2() throws SevenZipException {
        return getMainArchive().getConnectedOutArchiveBZip2();
    }

    @Override
    public void close() throws SevenZipException {
        getMainArchive().close();
        if (closeables != null) {
            for (Closeable c : closeables) {
                try {
                    c.close();
                } catch (Exception e) {
                    LOGGER.warn("Error closing: {}", c, e);
                }
            }
        }

    }

    private IInArchive getMainArchive() {
        return archive;
    }
}
