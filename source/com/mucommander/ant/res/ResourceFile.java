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

package com.mucommander.ant.res;

import java.io.File;

/**
 * Used by Ant to hold the description of resource files.
 * @author Nicolas Rinaudo
 * @ant.type name="file" category="util"
 */
public class ResourceFile {
    private File in;
    private String out;

    public void setIn(File f) {in = f;}
    public void setOut(String s) {out = s;}

    public File getInputFile() {return in;}
    public String getOutputPath() {return out;}
}
