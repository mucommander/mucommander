package com.mucommander.ant.jnlp;

import org.apache.tools.ant.BuildException;
import java.util.*;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="information" category="webstart"
 */
public class InformationElement {
    private String locale;
    private String title;
    private String vendor;
    private String homepage;
    private Vector descriptions;
    private Vector icons;
    private boolean offlineAllowed;

    public InformationElement() {
        icons        = new Vector();
        descriptions = new Vector();
    }

    public void setOffline(boolean b) {offlineAllowed = b;}
    public boolean isOffline() {return offlineAllowed;}
    public void setTitle(String s) {title = s;}
    public String getTitle() {return title;}
    public String getVendor() {return vendor;}
    public void setVendor(String s) {vendor = s;}
    public void setLocale(String s) {locale = s;}
    public String getLocale() {return locale;}
    public void setHomepage(String s) {homepage = s;}
    public String getHomepage() {return homepage;}
    public Iterator descriptions() {return descriptions.iterator();}
    public Iterator icons() {return icons.iterator();}

    public DescriptionElement createDescription() {
        DescriptionElement buffer;

        buffer = new DescriptionElement();
        descriptions.add(buffer);

        return buffer;
    }

    public IconElement createIcon() {
        IconElement buffer;

        buffer = new IconElement();
        icons.add(buffer);

        return buffer;
    }
}
