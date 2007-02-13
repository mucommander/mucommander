package com.mucommander.ant.jnlp;

import org.apache.tools.ant.BuildException;

/**
 * @author Nicolas Rinaudo
 */
public class Downloadable {
    public static final String EAGER_LABEL    = "eager";
    public static final String LAZY_LABEL     = "lazy";
    public static final int    DOWNLOAD_LAZY  = 1;
    public static final int    DOWNLOAD_EAGER = 0;

    private int download;

    public void setDownload(String s) {
        if(s.equals(LAZY_LABEL))
            download = DOWNLOAD_LAZY;
        else if(s.equals(EAGER_LABEL))
            download = DOWNLOAD_EAGER;
        else
            throw new BuildException("Illegal download type: " + s);
    }

    public int getDownload() {return download;}
}
