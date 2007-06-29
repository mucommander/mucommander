/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
