package com.mucommander.sevenzipjbindings.multivolume;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.sevenzipjbindings.SignatureCheckedRandomAccessFile;

import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback;
import net.sf.sevenzipjbinding.ICryptoGetTextPassword;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISeekableStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class SevenZipMultiVolumeCallbackHandler implements IArchiveOpenVolumeCallback, ICryptoGetTextPassword, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipMultiVolumeCallbackHandler.class);

    private final Map<String, IInStream> fileCache = new HashMap<>();

    private final AbstractFile firstFile;

    private final byte[] signature;

    private final String password;

    private boolean isFirstFile = true;

    public SevenZipMultiVolumeCallbackHandler(byte[] signature, AbstractFile firstFile, String password) {
        this.signature = signature;
        this.firstFile = firstFile;
        this.password = password;
    }

    @Override
    public Object getProperty(PropID propID) {
        switch (propID) {
            case NAME:
                return firstFile.getAbsolutePath();
        }
        return null;
    }

    @Override
    public IInStream getStream(String filename) {
        try {
            IInStream stream = fileCache.get(filename);
            if (stream != null) {
                stream.seek(0, ISeekableStream.SEEK_SET);
            } else {
                AbstractFile abstractFile = FileFactory.getFile(filename);
                if (isFirstFile) {
                    // Only first file starts with magic number
                    stream = new SignatureCheckedRandomAccessFile(abstractFile, signature);
                    isFirstFile = false;
                } else {
                    stream = new RandomAccessFileInStream(new RandomAccessFile(filename, "r"));
                }
                fileCache.put(filename, stream);
            }
            return stream;
        } catch (FileNotFoundException e) {
            // There is no way to know ahead of time if we reached the last file,
            // So it is safe to ignore this Exception
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        for (IInStream f : fileCache.values()) {
            try {
                f.close();
            } catch (Exception e) {
                LOGGER.error("Error closing IInStream", e);
            }
        }
    }

    @Override
    public String cryptoGetTextPassword() throws SevenZipException {
        if (password == null) {
            throw new SevenZipException("No password was provided for opening protected archive.");
        }
        return password;
    }
}
