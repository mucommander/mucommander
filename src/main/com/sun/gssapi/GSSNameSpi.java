/*
 * Copyright (c) 1999, 2007 Sun Microsystems, Inc. 
 * All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
 * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.gssapi;

 
/**
 * This class represents a mechanism specific name element. One or
 * more instances of this class are contained in an instance of
 * a GSSName object. Each GSSNameSpi object represents at most 1
 * mechanism specific name element.
 */

public interface GSSNameSpi {

    /**
     * Initializer for the GSSNameSpi object using a byte array.
     *
     * @param byte[] name bytes which is to be interpreted based
     *    on the nameType
     * @param nameType - oid representing the type of name supplied
     * @exception GSSException The major codes can be BAD_NAMETYPE,
     *    BAD_NAME, and FAILURE.
     * @see #init(String,Oid)
     */
    public void init(byte[] externalName, Oid nameType) throws GSSException;
    
 
    /**
     * Initializer for the GSSNameSpi object using a String.
     *
     * @param name string which is to be interpreted based
     *    on the nameType
     * @param nameType - oid representing the type of name supplied
     * @exception GSSException The major codes can be BAD_NAMETYPE,
     *    BAD_NAME, and FAILURE.
     * @see #init(String,Oid)
     */
    public void init(String name, Oid nameType) throws GSSException;
    

    /**
     * Equal method for the GSSNameSpi objects.
     * If either name denotes an anonymous principal, the call should
     * return false.
     *
     * @param name to be compared with
     * @returns true if they both refer to the same entity, else false
     * @exception GSSException with major codes of BAD_NAMETYPE,
     *    BAD_NAME, FAILURE
     */
    public boolean equals(GSSNameSpi name) throws GSSException;
  

    /**
     * Returns a flat name representation for this object. The name
     * format is defined in RFC 2078.
     *
     * @return the flat name representation for this object
     * @exception GSSException with major codes NAME_NOT_MN, BAD_NAME,
     *    BAD_NAME, FAILURE.   
     */
    public byte[] export() throws GSSException;


    /**
     * Get the mechanism type that this NameElement corresponds to.
     *
     * @return the Oid of the mechanism type
     */
    public Oid getMech();


    /**
     * Returns the name type oid for this name.
     *
     * @return the name type oid for this name
     */
    public Oid getNameType();


    /**
     * Returns a string representation for this name. The printed
     * name type can be obtained by calling getStringNameType().
     *
     * @return string form of this name
     * @see #getStringNameType()
     * @overrides Object#toString
     */
    public String toString();
    

    /**
     * Returns the oid describing the format of the printable name.
     *
     * @return the Oid for the format of the printed name
     */
    public Oid getStringNameType();
  
  
    /**
     * Produces a copy of this object.
     */ 
    public Object clone();
    
    
    /**
     * Indicates if this name object represents an Anonymous name.
     */
    public boolean isAnonymousName();
}
