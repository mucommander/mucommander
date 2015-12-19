package com.mucommander.commons.file.impl.sevenzip.provider.Common;

public class ByteBuffer {
    int _capacity;
    byte [] _items;
    
    public ByteBuffer() {
        _capacity = 0;
        _items = null;
    }
    
    public byte [] data() { return _items; }
    
    public int GetCapacity() { return  _capacity; }
    
    public void SetCapacity(int newCapacity) {
        if (newCapacity == _capacity)
            return;
        
        byte [] newBuffer;
        if (newCapacity > 0) {
            newBuffer = new byte[newCapacity];
            if(_capacity > 0) {
                int len = _capacity;
                if (newCapacity < len) len = newCapacity;
                
                System.arraycopy(_items,0,newBuffer,0,len); // for (int i = 0 ; i < len ; i++) newBuffer[i] = _items[i];
            }
        } else
            newBuffer = null;
        
        // delete []_items;
        _items = newBuffer;
        _capacity = newCapacity;
    }
}
