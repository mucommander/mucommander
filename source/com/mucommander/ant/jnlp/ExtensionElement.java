package com.mucommander.ant.jnlp;

import java.util.*;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="extension" category="jnlp"
 */
public class ExtensionElement {
    private String version;
    private String name;
    private String href;
    private Vector downloads;

    public ExtensionElement() {downloads = new Vector();}

    public String getVersion() {return version;}
    public String getName() {return name;}
    public String getHref() {return href;}
    public boolean hasDownloads() {return !downloads.isEmpty();}
    public Iterator downloads() {return downloads.iterator();}
    public void setVersion(String s) {version = s;}
    public void setName(String s) {name = s;}
    public void setHref(String s) {href = s;}
    public ExtDownloadElement createExtDownload() {
        ExtDownloadElement buffer;

        buffer = new ExtDownloadElement();
        downloads.add(buffer);

        return buffer;
    }
}
