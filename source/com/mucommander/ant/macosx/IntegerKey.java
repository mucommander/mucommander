package com.mucommander.ant.macosx;

/**
 * Ant representation of an <code>integer</code> key.
 * @author Nicolas Rinaudo
 * @ant.type name="integer" category="macosx"
 */
public class IntegerKey extends NamedInfoElement {
    /**
     * Creates an integer key.
     */
    public IntegerKey() {}

    /**
     * Sets the value of the integer key.
     * @param i value of the integer key.
     */
    public void setValue(int i) {setValue(new IntegerValue(i));}
}
