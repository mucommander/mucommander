package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Common;

import java.io.IOException;

import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT;

public class SequentialOutStreamImp2 extends java.io.OutputStream {
    byte []_buffer;
    int _size;
    int _pos;
    public void Init(byte [] buffer, int size) {
        _buffer = buffer;
        _pos = 0;
        _size = size;
    }
    
    public void write(int b) throws java.io.IOException {
        throw new java.io.IOException("SequentialOutStreamImp2 - write() not implemented");
    }
    
    public void write(byte [] data,int off, int size) throws java.io.IOException {
        for(int i = 0 ; i < size ; i++) {
            if (_pos < _size) {
                _buffer[_pos++] = data[off + i];
            } else {
                throw new java.io.IOException("SequentialOutStreamImp2 - can't write");
            }
        }
    }
}

