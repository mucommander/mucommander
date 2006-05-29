package com.mucommander.ant.macosx;

import org.apache.tools.ant.BuildException;

/**
 * Holds the content of a String key.
 * @author Nicolas Rinaudo
 */
public class InfoString {
    private String name;
    private String value;

    public InfoString() {}
    public void setName(String s) {name = s;}
    public void setValue(String s) {value = s;}

    public String getName() {return name;}
    public String getValue() {return value;}

    public void check() throws BuildException {
        if(name == null || name.trim().length() == 0)
            throw new BuildException("Tried to create a String key without a name.");
        if(value == null || value.trim().length() == 0)
            throw new BuildException("String key " + name + " has no value.");
    }
}
