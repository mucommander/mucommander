package com.mucommander.ant.jnlp;

import java.util.*;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="j2se" category="webstart"
 */
public class J2seElement {
    private String version;
    private String href;
    private int    initialHeap;
    private int    maxHeap;
    private Vector resources;

    public J2seElement() {resources = new Vector();}

    public String getVersion() {return version;}
    public String getHref() {return href;}
    public int getInitialHeap() {return initialHeap;}
    public int getMaxHeap() {return maxHeap;}
    public Iterator resources() {return resources.iterator();}
    public boolean hasResources() {return !resources.isEmpty();}
    public void setVersion(String s) {version = s;}
    public void setHref(String s) {href = s;}
    public void setInitialHeap(int i) {initialHeap = i;}
    public void setMaxHeap(int i) {maxHeap = i;}
    public ResourcesElement createResources() {
        ResourcesElement buffer;

        buffer = new ResourcesElement();
        resources.add(buffer);

        return buffer;
    }
}
