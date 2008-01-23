/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

/**
 * @author Nicolas Rinaudo
 * @ant.type name="downloadable" category="webstart"
 */
public class Downloadable {
    public static final String EAGER_LABEL    = "eager";
    public static final String LAZY_LABEL     = "lazy";
    public static final int    DOWNLOAD_LAZY  = 1;
    public static final int    DOWNLOAD_EAGER = 0;

    private int download;

    public void setDownload(String s) {
        if(s.equals(LAZY_LABEL))
            download = DOWNLOAD_LAZY;
        else if(s.equals(EAGER_LABEL))
            download = DOWNLOAD_EAGER;
        else
            throw new BuildException("Illegal download type: " + s);
    }

    public int getDownload() {return download;}
}
