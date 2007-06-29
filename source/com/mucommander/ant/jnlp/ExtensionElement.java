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
 * @ant.type name="extension" category="webstart"
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
