package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 * @ant type name="array" category="macosx"
 */
public class ArrayKey extends NamedInfoElement {
    public ArrayKey() {setValue(new ArrayValue());}

    public BooleanValue createBoolean() {return ((ArrayValue)getValue()).createBoolean();}
    public StringValue createString() {return ((ArrayValue)getValue()).createString();}
    public DictValue createDict() {return ((ArrayValue)getValue()).createDict();}
    public ArrayValue createArray() {return ((ArrayValue)getValue()).createArray();}
    public IntegerValue createInteger() {return ((ArrayValue)getValue()).createInteger();}
    public RealValue createReal() {return ((ArrayValue)getValue()).createReal();}
    public DateValue createDate() {return ((ArrayValue)getValue()).createDate();}
    public DataValue createData() {return ((ArrayValue)getValue()).createData();}
}
