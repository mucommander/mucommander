package com.mucommander.ant.macosx;

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;
import java.util.*;

/**
 *
 */
public class ArrayValue implements InfoElement {
    public static final String ELEMENT_ARRAY     = "array";
    private Vector keys;

    public ArrayValue() {keys = new Vector();}

    public BooleanValue createBoolean() {
        BooleanValue value;

        keys.add(value = new BooleanValue());

        return value;
    }

    public StringValue createString() {
        StringValue value;

        keys.add(value = new StringValue());

        return value;
    }

    public DictValue createDict() {
        DictValue value;

        keys.add(value = new DictValue());

        return value;
    }

    public ArrayValue createArray() {
        ArrayValue value;

        keys.add(value = new ArrayValue());

        return value;
    }

    public IntegerValue createInteger() {
        IntegerValue value;

        keys.add(value = new IntegerValue());

        return value;
    }

    public RealValue createReal() {
        RealValue value;

        keys.add(value = new RealValue());

        return value;
    }

    public void write(XmlWriter out) throws BuildException {
        Iterator iterator;

        out.startElement(ELEMENT_ARRAY);
        out.println();

        iterator = keys.iterator();
        while(iterator.hasNext())
            ((InfoElement)iterator.next()).write(out);

        out.endElement(ELEMENT_ARRAY);
    }
}
