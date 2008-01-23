/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.command;

/**
 * Defines the structure of a custom associations XML file.
 * <p>
 * This interface is only meant as a convenient way of sharing the XML
 * file format between the {@link AssociationWriter} and {@link AssociationReader}. It will be removed
 * at bytecode optimisation time.
 * </p>
 * <p>
 * Associations XML files must match the following DTD:
 * <pre>
 * &lt;!ELEMENT associations (association*)&gt;
 * 
 * &lt;!ELEMENT association (filename?,symlink?,hidden?,readable?,writable?,executable?)&gt;
 * &lt;!ATTLIST association command CDATA #REQUIRED&gt;
 *
 * &lt;!ELEMENT filename EMPTY&gt;
 * &lt;!ATTLIST filename value CDATA                 #REQUIRED&gt;
 * &lt;!ATTLIST filename case_sensitive (true|false) #IMPLIED&gt;
 *
 * &lt;!ELEMENT symlink EMPTY&gt;
 * &lt;!ATTLIST symlink value (true|false) #REQUIRED&gt;
 *
 * &lt;!ELEMENT hidden EMPTY&gt;
 * &lt;!ATTLIST hidden value (true|false) #REQUIRED&gt;
 *
 * &lt;!ELEMENT readable EMPTY&gt;
 * &lt;!ATTLIST readable value (true|false) #REQUIRED&gt;
 *
 * &lt;!ELEMENT writable EMPTY&gt;
 * &lt;!ATTLIST writable value (true|false) #REQUIRED&gt;
 *
 * &lt;!ELEMENT executable EMPTY&gt;
 * &lt;!ATTLIST executable value (true|false) #REQUIRED&gt;
 * </pre>
 * </p>
 * @see AssociationReader
 * @see AssociationWriter
 * @author Nicolas Rinaudo
 */
interface AssociationsXmlConstants {
    // - XML elements ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Root element. */
    public static final String ELEMENT_ROOT          = "associations";
    /** Custom association definition element. */
    public static final String ELEMENT_ASSOCIATION   = "association";
    public static final String ELEMENT_MASK          = "filename";
    public static final String ELEMENT_IS_SYMLINK    = "symlink";
    public static final String ELEMENT_IS_HIDDEN     = "hidden";
    public static final String ELEMENT_IS_READABLE   = "readable";
    public static final String ELEMENT_IS_WRITABLE   = "writable";
    public static final String ELEMENT_IS_EXECUTABLE = "executable";



    // - Custom association structure ------------------------------------------
    // -------------------------------------------------------------------------
    /** Name of the attribute containing the alias of the command to execute in this association. */
    public static final String ATTRIBUTE_COMMAND        = "command";
    public static final String ATTRIBUTE_VALUE          = "value";
    public static final String ATTRIBUTE_CASE_SENSITIVE = "case_sensitive";
    public static final String VALUE_TRUE               = "true";
    public static final String VALUE_FALSE              = "false";
}
