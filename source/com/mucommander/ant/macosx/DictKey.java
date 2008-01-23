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

package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="dict" category="macosx"
 */
public class DictKey extends NamedInfoElement {
    public DictKey() {setValue(new DictValue());}

    public ArrayKey createArray() {return ((DictValue)getValue()).createArray();}
    public BooleanKey createBoolean() {return ((DictValue)getValue()).createBoolean();}
    public StringKey createString() {return ((DictValue)getValue()).createString();}
    public DictKey createDict() {return ((DictValue)getValue()).createDict();}
    public IntegerKey createInteger() {return ((DictValue)getValue()).createInteger();}
    public RealKey createReal() {return ((DictValue)getValue()).createReal();}
    public DateKey createDate() {return ((DictValue)getValue()).createDate();}
    public DataKey createData() {return ((DictValue)getValue()).createData();}
}
