package com.mucommander.sevenzipjbindings.multivolume;

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

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
