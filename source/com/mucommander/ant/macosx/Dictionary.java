package com.mucommander.ant.macosx;

import org.apache.tools.ant.BuildException;

public class Dictionary extends AbstractDictionary {
    private String name;

    public void setName(String s) {name = s;}
    public String getName() {return name;}
    public void check() throws BuildException {
        if(name == null)
            throw new BuildException("No dictionary name specified. Please fill in the name attribute.");
    }
}
