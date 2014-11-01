package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.Common;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.CRC;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT;


public class OutStreamWithCRC extends java.io.OutputStream {
    
    java.io.OutputStream _stream;
    long _size;
    CRC _crc = new CRC();
    boolean _calculateCrc;
    
    public void write(int b) throws java.io.IOException {
        throw new java.io.IOException("OutStreamWithCRC - write() not implemented");
    }
    
    public void write(byte [] data,int off, int  size) throws java.io.IOException {
        if(_stream != null) {
            if (size == 0) {
                throw new java.io.IOException("size = 0");
            } else {
                _stream.write(data, off,size);
            }
        }
        if (_calculateCrc)
            _crc.Update(data,off, size);
        
        _size += size;
    }
    
    public void SetStream(java.io.OutputStream stream) { _stream = stream; }
    
    public void Init() {
        Init(true);
    }
    public void Init(boolean calculateCrc) {
        _size = 0;
        _calculateCrc = calculateCrc;
        _crc.Init();
    }
    public void ReleaseStream() throws java.io.IOException {
        // _stream.Release();
        if (_stream != null) _stream.close();
        _stream = null;
    }
    public long GetSize()  {
        return _size;
    }
    public int GetCRC()  {
        return _crc.GetDigest();
    }
    public void InitCRC() {
        _crc.Init();
    }
}
