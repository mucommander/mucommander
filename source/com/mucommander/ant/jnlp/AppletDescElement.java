package com.mucommander.ant.jnlp;

import java.util.*;

/**
 * @author Nicolas Rinaudo
 */
public class AppletDescElement {
    private String documentBase;
    private String mainClass;
    private String name;
    private int    width;
    private int    height;
    private Vector params;

    public AppletDescElement() {params = new Vector();}

    public String getDocumentBase() {return documentBase;}
    public String getMain() {return mainClass;}
    public String getName() {return name;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public boolean hasParams() {return !params.isEmpty();}
    public Iterator params() {return params.iterator();}
    public void setDocumentBase(String s) {documentBase = s;}
    public void setMain(String s) {mainClass = s;}
    public void setName(String s) {name = s;}
    public void setWidth(int i) {width = i;}
    public void setHeight(int i) {height = i;}
    public PropertyElement createParam() {
        PropertyElement buffer;

        buffer = new PropertyElement();
        params.add(buffer);

        return buffer;
    }
}
