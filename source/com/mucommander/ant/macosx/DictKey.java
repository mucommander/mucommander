package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="dict" category="macosx"
 */
public class DictKey extends NamedInfoElement {
    public DictKey() {setValue(new DictValue());}

    public ArrayKey createArray() {return ((DictValue)getValue()).createArray();}
    public BooleanKey createBoolean() {return ((DictValue)getValue()).createBoolean();}
    public StringKey createString() {return ((DictValue)getValue()).createString();}
    public DictKey createDict() {return ((DictValue)getValue()).createDict();}
    public IntegerKey createInteger() {return ((DictValue)getValue()).createInteger();}
    public RealKey createReal() {return ((DictValue)getValue()).createReal();}
    public DateKey createDate() {return ((DictValue)getValue()).createDate();}
    public DataKey createData() {return ((DictValue)getValue()).createData();}
}
