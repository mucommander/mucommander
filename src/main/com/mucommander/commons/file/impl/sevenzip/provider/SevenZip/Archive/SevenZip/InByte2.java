package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;
import java.io.IOException;

class InByte2 {
    byte [] _buffer;
    int _size;
    int _pos;
    
    public void Init(byte [] buffer, int size) {
        _buffer = buffer;
        _size = size;
        _pos = 0;
    }
    public int ReadByte() throws IOException {
        if(_pos >= _size)
            throw new IOException("CInByte2 - Can't read stream");
        return (_buffer[_pos++] & 0xFF);
    }
    
    int ReadBytes2(byte [] data, int size) {
        int processedSize;
        for(processedSize = 0; processedSize < size && _pos < _size; processedSize++)
            data[processedSize] = _buffer[_pos++];
        return processedSize;
    }
    
    boolean ReadBytes(byte [] data, int size) {
        int processedSize = ReadBytes2(data, size);
        return (processedSize == size);
    }
    
    int GetProcessedSize() { return _pos; }
    
    InByte2() {
    }
}
