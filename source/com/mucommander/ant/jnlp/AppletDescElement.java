/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ant.jnlp;

import java.util.*;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="appletdesc" category="webstart"
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
