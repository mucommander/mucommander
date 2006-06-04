package com.mucommander.ant.macosx;

/**
 * Represents an integer key in the property list.
 * @author Nicolas Rinaudo
 */
public class IntegerKey extends NamedInfoElement {
    /**
     * Allows Ant to set the property's value.
     * @param i integer property's value.
     */
    public void setValue(int i) {setValue(new IntegerValue(i));}
}
