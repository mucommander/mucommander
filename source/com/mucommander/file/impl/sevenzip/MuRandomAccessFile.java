package com.mucommander.file.impl.sevenzip;

import java.io.IOException;
import java.io.InputStream;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.impl.sevenzip.provider.SevenZip.IInStream;

public class MuRandomAccessFile  extends IInStream {
	
	private AbstractFile file;
	
	private InputStream stream;
	
	private long position;
	
	public MuRandomAccessFile(AbstractFile file) {
		super();
		position = 0;
		this.file = file;
		try {
			stream = file.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public long Seek(long offset, int seekOrigin) throws IOException {
		if (seekOrigin == STREAM_SEEK_SET) {
            stream = file.getInputStream();
            stream.skip(offset);
            position = offset;
        }
        else if (seekOrigin == STREAM_SEEK_CUR) {
            stream.skip(offset);
            position += offset;
        }
        return position;
	}

	public int read() throws IOException {
		int read = stream.read();
		position += read;
		return read;
	}

	public int read(byte [] data, int off, int size) throws java.io.IOException {
        int read = stream.read(data, off, size);
        position += read;
        return read;
    }
        
    public int read(byte [] data, int size) throws java.io.IOException {
        int read = stream.read(data,0,size);
        position += read;
        return read;
    }
    
    public void close() throws java.io.IOException {
        stream.close();
    }
    
    public long skip(long offset) {
    	try {
			return stream.skip(offset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
    }
    
    public int available () {
    	try {
			return stream.available();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
    }
}
