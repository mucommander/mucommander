package com.mucommander.commons.file.impl.sevenzip.provider.Common;

public class BoolVector {
    
    protected boolean[] data = new boolean[10];
    int capacityIncr = 10;
    int elt = 0;
    
    public BoolVector() {
    }
    
    public int size() {
        return elt;
    }
    
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            boolean [] oldData = data;
            int newCapacity = oldCapacity + capacityIncr;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            data = new boolean[newCapacity];
            System.arraycopy(oldData, 0, data, 0, elt);
        }
    }
    
    public boolean get(int index) {
        if (index >= elt)
            throw new ArrayIndexOutOfBoundsException(index);
        
        return data[index];
    }
    
    public void Reserve(int s) {
        ensureCapacity(s);
    }
    
    public void add(boolean b) {
        ensureCapacity(elt + 1);
        data[elt++] = b;
    }
    
    public void clear() {
        elt = 0;
    }
    
    public boolean isEmpty() {
        return elt == 0;
    }
}
