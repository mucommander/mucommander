package com.mucommander.ant.macosx;

import org.apache.tools.ant.BuildException;

public class RootDictionary extends AbstractDictionary {
    private String version;

    public void setVersion(String s) {version = s;}
    public String getVersion() {return version;}
    public void check() throws BuildException {
        if(version == null)
            throw new BuildException("No Info.plist version specified. Please fill in the version attribute.");
    }
}
