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

package com.mucommander.ant.macosx;

/**
 * Represents an date key in the property list.
 * @author Nicolas Rinaudo
 * @ant.type name="date" category="macosx"
 */
public class DateKey extends NamedInfoElement {
    public DateKey() {setValue(new DateValue());}
    public void setYear(int i) {((DateValue)getValue()).setYear(i);}
    public void setMonth(int i) {((DateValue)getValue()).setMonth(i);}
    public void setDay(int i) {((DateValue)getValue()).setDay(i);}
    public void setHours(int i) {((DateValue)getValue()).setHours(i);}
    public void setMinutes(int i) {((DateValue)getValue()).setMinutes(i);}
    public void setSeconds(int i) {((DateValue)getValue()).setSeconds(i);}
}
