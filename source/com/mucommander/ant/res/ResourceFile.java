package com.mucommander.ant.res;

import java.io.File;

/**
 * Used by Ant to hold the description of resource files.
 * @author Nicolas Rinaudo
 */
public class ResourceFile {
    private File in;
    private String out;

    public void setIn(File f) {in = f;}
    public void setOut(String s) {out = s;}

    public File getInputFile() {return in;}
    public String getOutputPath() {return out;}
}
