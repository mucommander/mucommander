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

package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 * @ant type name="array" category="macosx"
 */
public class ArrayKey extends NamedInfoElement {
    public ArrayKey() {setValue(new ArrayValue());}

    public BooleanValue createBoolean() {return ((ArrayValue)getValue()).createBoolean();}
    public StringValue createString() {return ((ArrayValue)getValue()).createString();}
    public DictValue createDict() {return ((ArrayValue)getValue()).createDict();}
    public ArrayValue createArray() {return ((ArrayValue)getValue()).createArray();}
    public IntegerValue createInteger() {return ((ArrayValue)getValue()).createInteger();}
    public RealValue createReal() {return ((ArrayValue)getValue()).createReal();}
    public DateValue createDate() {return ((ArrayValue)getValue()).createDate();}
    public DataValue createData() {return ((ArrayValue)getValue()).createData();}
}
