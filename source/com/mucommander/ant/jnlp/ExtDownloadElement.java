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

/**
 * @author Nicolas Rinaudo
 * @ant.type name="extdownload" category="webstart"
 */
public class ExtDownloadElement extends Downloadable {
    private String extPart;
    private String part;

    public String getExtPart() {return extPart;}
    public String getPart() {return part;}
    public void setExtPart(String s) {extPart = s;}
    public void setPart(String s) {part = s;}
}
