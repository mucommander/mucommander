/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.viewer.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.PagedData;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.RandomAccess;
import com.mucommander.commons.io.StreamUtils;

/**
 * Class for direct binary access to abstract file.
 */
@ParametersAreNonnullByDefault
public class FileBinaryData implements BinaryData {

    public static final String BROKEN_ABSTRACT_FILE = "Broken abstract file";
    public static final int PAGE_SIZE = 4096;

    private final AbstractFile file;

    private InputStream cacheInputStream = null;
    private long cachePosition = 0;
    private final DataPage[] cachePages = new DataPage[] { new DataPage(), new DataPage() };
    private int nextCachePage = 0;

    public FileBinaryData(AbstractFile file) {
        this.file = file;
    }

    @Override
    public boolean isEmpty() {
        return file.getSize() > 0;
    }

    @Override
    public long getDataSize() {
        return file.getSize();
    }

    @Override
    public synchronized byte getByte(long position) {
        long pageIndex = position / PAGE_SIZE;
        int pageOffset = (int) (position % PAGE_SIZE);

        if (cachePages[0].pageIndex == pageIndex) {
            return cachePages[0].page[pageOffset];
        }
        if (cachePages[1].pageIndex == pageIndex) {
            return cachePages[1].page[pageOffset];
        }
        int usedPage = loadPage(pageIndex);
        return cachePages[usedPage].page[pageOffset];
    }

    @Nonnull
    @Override
    public BinaryData copy() {
        return new FileBinaryData(file);
    }

    @Nonnull
    @Override
    public synchronized BinaryData copy(long startFrom, long length) {
        long pageIndex = startFrom / PAGE_SIZE;
        int pageOffset = (int) (startFrom % PAGE_SIZE);

        PagedData data = new PagedData();
        long dataPosition = 0;
        while (length > 0) {
            int pageLength = length > PAGE_SIZE - pageOffset ? PAGE_SIZE - pageOffset : (int) length;
            copyTo(data, dataPosition, pageIndex, pageOffset, pageLength);
            pageIndex++;
            pageOffset = 0;
            dataPosition += pageLength;
            length -= pageLength;
        }

        return data;
    }

    private void copyTo(PagedData data, long dataPosition, long pageIndex, int pageOffset, int pageLength) {
        if (cachePages[0].pageIndex == pageIndex) {
            data.insert(dataPosition, cachePages[0].page, pageOffset, pageLength);
        } else if (cachePages[1].pageIndex == pageIndex) {
            data.insert(dataPosition, cachePages[1].page, pageOffset, pageLength);
        } else {
            int usedPage = loadPage(pageIndex);
            data.insert(dataPosition, cachePages[usedPage].page, pageOffset, pageLength);
        }
    }

    @Override
    public synchronized void copyToArray(long startFrom, byte[] target, int offset, int length) {
        long pageIndex = startFrom / PAGE_SIZE;
        int pageOffset = (int) (startFrom % PAGE_SIZE);

        int dataPosition = offset;
        while (length > 0) {
            int pageLength = Math.min(length, PAGE_SIZE - pageOffset);
            copyTo(target, dataPosition, pageIndex, pageOffset, pageLength);
            pageIndex++;
            pageOffset = 0;
            dataPosition += pageLength;
            length -= pageLength;
        }
    }

    private void copyTo(byte[] data, int dataPosition, long pageIndex, int pageOffset, int pageLength) {
        if (cachePages[0].pageIndex == pageIndex) {
            System.arraycopy(cachePages[0].page, pageOffset, data, dataPosition, pageLength);
        } else if (cachePages[1].pageIndex == pageIndex) {
            System.arraycopy(cachePages[1].page, pageOffset, data, dataPosition, pageLength);
        } else {
            int usedPage = loadPage(pageIndex);
            System.arraycopy(cachePages[usedPage].page, pageOffset, data, dataPosition, pageLength);
        }
    }

    @Override
    public void saveToStream(OutputStream outputStream) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            StreamUtils.copyStream(inputStream, outputStream);
        }
    }

    @Nonnull
    @Override
    public InputStream getDataInputStream() {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(BROKEN_ABSTRACT_FILE, e);
        }
    }

    @Override
    public void dispose() {
        resetCache();
    }

    public void resetCache() {
        if (cacheInputStream != null) {
            try {
                cacheInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(FileBinaryData.class.getName()).log(Level.SEVERE, null, ex);
            }
            cacheInputStream = null;
        }

        cachePages[0].pageIndex = -1;
        cachePages[1].pageIndex = -1;
    }

    public synchronized void close() {
        resetCache();
    }

    @Nonnull
    private InputStream getInputStream(long position) throws IOException {
        if (cacheInputStream != null && position == cachePosition) {
            return cacheInputStream;
        } else if (cacheInputStream != null && cacheInputStream instanceof RandomAccess) {
            ((RandomAccess) cacheInputStream).seek(position);
            cachePosition = position;
        } else if (cacheInputStream != null && position > cachePosition) {
            StreamUtils.skipFully(cacheInputStream, position - cachePosition);
            cachePosition = position;
        } else {
            if (cacheInputStream != null) {
                cacheInputStream.close();
            }
            cacheInputStream = file.getInputStream();
            if (cacheInputStream instanceof RandomAccess) {
                ((RandomAccess) cacheInputStream).seek(position);
            } else {
                StreamUtils.skipFully(cacheInputStream, position);
            }
            cachePosition = position;
        }

        return cacheInputStream;
    }

    private int loadPage(long pageIndex) {
        int usedPage = nextCachePage;
        long position = pageIndex * PAGE_SIZE;
        long dataSize = getDataSize();
        try {
            InputStream inputStream = getInputStream(position);

            int done = 0;
            int remains = position + PAGE_SIZE > dataSize ? (int) (dataSize - position) : PAGE_SIZE;
            while (remains > 0) {
                int copied = inputStream.read(cachePages[usedPage].page, done, remains);
                if (copied < 0) {
                    throw new IllegalStateException(BROKEN_ABSTRACT_FILE);
                }
                cachePosition += copied;
                remains -= copied;
                done += copied;
            }

            cachePages[usedPage].pageIndex = pageIndex;
            nextCachePage = 1 - nextCachePage;
        } catch (IOException e) {
            throw new IllegalStateException(BROKEN_ABSTRACT_FILE, e);
        }

        return usedPage;
    }

    private static class DataPage {
        long pageIndex = -1;
        byte[] page = new byte[PAGE_SIZE];
    }
}
