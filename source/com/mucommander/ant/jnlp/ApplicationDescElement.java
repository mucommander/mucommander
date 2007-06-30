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

import java.util.Iterator;
import java.util.Vector;

/**    ˜
 * @author Nicolas Rinaudo
 * @ant.type name="applicationdesc" category="webstart"
 */
public class ApplicationDescElement {
    private String mainClass;
    private Vector arguments;

    public ApplicationDescElement() {arguments = new Vector();}

    public void setMain(String s) {mainClass = s;}
    public String getMain() {return mainClass;}
    public boolean hasArguments() {return !arguments.isEmpty();}
    public Iterator arguments() {return arguments.iterator();}
    public ArgumentElement createArgument() {
        ArgumentElement buffer;

        buffer = new ArgumentElement();
        arguments.add(buffer);

        return buffer;
    }
}
