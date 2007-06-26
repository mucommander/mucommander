package com.mucommander.ant.jnlp;

import org.apache.tools.ant.BuildException;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="icon" category="jnlp"
 */
public class IconElement {
    public static final int KIND_DEFAULT = 0;
    public static final int KIND_SELECTED = 1;
    public static final int KIND_DISABLED = 2;
    public static final int KIND_ROLLOVER = 3;
    private int    kind;
    private String href;
    private int    width;
    private int    height;
    private String version;
    private int    depth;
    private int    size;

    public void setHref(String s) {href = s;}
    public void setVersion(String s) {version = s;}
    public void setWidth(int i) {width = i;}
    public void setHeight(int i) {height = i;}

    public void setKind(String s) throws BuildException {
        if(s.equals("default"))
            kind = KIND_DEFAULT;
        else if(s.equals("selected"))
            kind = KIND_SELECTED;
        else if(s.equals("disabled"))
            kind = KIND_DISABLED;
        else if(s.equals("rollover"))
            kind = KIND_ROLLOVER;
        else
            throw new BuildException("Unknown icon kind: " + s);
    }

    public int getKind() {return kind;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public int getSize() {return size;}
    public int getDepth() {return depth;}
    public String getVersion() {return version;}
    public String getHref() {return href;}

    public void setDepth(int i) {depth = i;}
    public void setSize(int i) {size = i;}
}
