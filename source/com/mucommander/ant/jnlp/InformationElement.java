/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ant.jnlp;

import org.apache.tools.ant.BuildException;

import java.util.Iterator;
import java.util.Vector;

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
