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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.sevenzipjbindings.SignatureCheckedRandomAccessFile;

import net.sf.sevenzipjbinding.IArchiveOpenCallback;
import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback;
import net.sf.sevenzipjbinding.ICryptoGetTextPassword;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISeekableStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;

public class SevenZipRarMultiVolumeCallbackHandler implements
        IArchiveOpenVolumeCallback, IArchiveOpenCallback, ICryptoGetTextPassword, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipRarMultiVolumeCallbackHandler.class);

    private Map<String, SignatureCheckedRandomAccessFile> fileCache = new HashMap<>();

    private String lastFileName;

    private final byte[] signature;

    private final String password;

    public SevenZipRarMultiVolumeCallbackHandler(byte[] signature, String password) {
        this.signature = signature;
        this.password = password;
    }

    @Override
    public void setTotal(Long files, Long bytes) throws SevenZipException {
        // NO-OP
    }

    @Override
    public void setCompleted(Long files, Long bytes) throws SevenZipException {
        // NO-OP
    }

    @Override
    public Object getProperty(PropID propID) throws SevenZipException {
        switch (propID) {
            case NAME:
                return lastFileName;
        }
        return null;
    }

    @Override
    public IInStream getStream(String filename) throws SevenZipException {
        try {
            SignatureCheckedRandomAccessFile stream = fileCache.get(filename);
            if (stream != null) {
                stream.seek(0, ISeekableStream.SEEK_SET);
            } else {
                AbstractFile abstractFile = FileFactory.getFile(filename);
                stream = new SignatureCheckedRandomAccessFile(abstractFile, signature);
                fileCache.put(filename, stream);
            }

            lastFileName = filename;
            return stream;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String cryptoGetTextPassword() throws SevenZipException {
        if (password == null) {
            throw new SevenZipException("No password was provided for opening protected archive.");
        }
        return password;
    }

    @Override
    public void close() throws IOException {
        for (SignatureCheckedRandomAccessFile f : fileCache.values()) {
            try {
                f.close();
            } catch (Exception e) {
                LOGGER.error("Error closing SignatureCheckedRandomAccessFile", e);
            }
        }
    }

}
