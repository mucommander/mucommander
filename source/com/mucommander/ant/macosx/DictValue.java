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

package com.mucommander.ant.macosx;

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 */
class DictValue implements InfoElement {
    private static final String ELEMENT_DICT = "dict";
    private Vector keys;

    public DictValue() {keys = new Vector();}

    public ArrayKey createArray() {
        ArrayKey value;

        keys.add(value = new ArrayKey());

        return value;
    }

    public BooleanKey createBoolean() {
        BooleanKey value;

        keys.add(value = new BooleanKey());

        return value;
    }

    public StringKey createString() {
        StringKey value;

        keys.add(value = new StringKey());

        return value;
    }

    public DictKey createDict() {
        DictKey value;

        keys.add(value = new DictKey());

        return value;
    }

    public IntegerKey createInteger() {
        IntegerKey value;

        keys.add(value = new IntegerKey());

        return value;
    }

    public RealKey createReal() {
        RealKey value;

        keys.add(value = new RealKey());

        return value;
    }

    public DateKey createDate() {
        DateKey value;

        keys.add(value = new DateKey());

        return value;
    }

    public DataKey createData() {
        DataKey value;

        keys.add(value = new DataKey());

        return value;
    }

    DictKey getDict(String name) {
        Iterator         iterator;
        NamedInfoElement key;

        iterator = keys.iterator();
        while(iterator.hasNext()) {
            key = (NamedInfoElement)iterator.next();
            if(key instanceof DictKey)
                if(key.getName().equals(name))
                    return (DictKey)key;
        }
        return null;
    }

    public void write(XmlWriter out) throws BuildException {
        Iterator iterator;

        try {
            out.startElement(ELEMENT_DICT);
            out.println();

            iterator = keys.iterator();
            while(iterator.hasNext())
                ((InfoElement)iterator.next()).write(out);

            out.endElement(ELEMENT_DICT);
        }
        catch(IOException e) {throw new BuildException(e);}
    }
}
