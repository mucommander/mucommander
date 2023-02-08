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

import java.util.Vector;
import java.util.Enumeration;

/**
 * An object of this class encapsulates a single GSS-API principal entity.
 * Different name formats and their definitions are identified with
 * universal Object Identifiers (Oids). The format of the names can be
 * derived based on the unique oid of each name type.
 * <p>
 * JGSS distinguishes between the following name representations:
 * <ul>
 * <li>Internal Form
 *    - A name representation which may contain name elements
 *        from different mechanisms.
 * <li>Mechanism Name (MN)
 *    - A name representation containing one and only one
 *        mechanism name element.
 * <li>Flat Name
 *    - A contiguous octet stream representation of a MN.
 * </ul>
 * <DL><DT><b>RFC 2078</b>
 * <DD>This class implements the following RFC 2078 functions:
 * <ul><li>gss_compare_name
 * <li>gss_display_name
 * <li>gss_import_name
 * <li>gss_release_name</ul>
 *</DL> 
 * @see Oid
 */

public class GSSName {

    /**
     * Name type used to indicate a host-based service name form. It
     * is used to represent services associated with host computers.
     * This name form is constructed using two elements, "service" and
     * "hostname", as follows:    service@hostname
     * <br>
     * Values for the "service" element are registered with the IANA.
     * It represents the following value: 
     * <p>{ 1(iso), 3(org), 6(dod), 1(internet), 5(security),
     * 6(nametypes), 2(gss-host-based-services) }
     */
    public static final Oid NT_HOSTBASED_SERVICE;


    /**
     * Name type used to indicate a named user on a local system. It
     * represents the following value:
     * <p>
     * { iso(1) member-body(2) United States(840) mit(113554)
     * infosys(1) gssapi(2) generic(1) user_name(1) }
     */
    public static final Oid NT_USER_NAME;


    /**
     * Name type used to indicate a numeric user identifier corresponding
     * to a user on a local system. (e.g. Uid). It represents the
     * following value:
     * <p>
     * { iso(1) member-body(2) United States(840) mit(113554) infosys(1)
     * gssapi(2) generic(1) machine_uid_name(2) }
     */
    public static final Oid NT_MACHINE_UID_NAME;

    
    /**
     * Name type used to indicate a string of digits representing the
     * numeric user identifier of a user on a local system. It
     * represents the following value:
     * <p>
     * { iso(1) member-body(2) United States(840) mit(113554) infosys(1)
     * gssapi(2) generic(1) string_uid_name(3) }
     */
    public static final Oid NT_STRING_UID_NAME;
       
    
    /**
     * Name type used to represent an Anonymous identity. It represents
     * the following value:
     * <p>
     * { 1(iso), 3(org), 6(dod), 1(internet), 5(security), 6(nametypes),
     * 3(gss-anonymous-name) }
     */
    public static final Oid NT_ANONYMOUS;
    
    
    /**
     * Name type used to indicate an exported name produced by the
     * export method. It represents the following value:
     * <p>
     * { 1(iso), 3(org), 6(dod), 1(internet), 5(security), 6(nametypes),
     * 4(gss-api-exported-name) }
     */
    public static final Oid NT_EXPORT_NAME;
     
    //initialize the oid objects
    static {
        try {
	    NT_HOSTBASED_SERVICE = new Oid("1.3.6.1.5.6.2");
            NT_USER_NAME = new Oid("1.2.840.113554.1.2.1.1");
            NT_MACHINE_UID_NAME = new Oid("1.2.840.113554.1.2.1.2");
            NT_STRING_UID_NAME = new Oid("1.2.840.113554.1.2.1.3");
            NT_ANONYMOUS = new Oid("1.3.6.1.5.6.3");
            NT_EXPORT_NAME = new Oid("1.3.6.1.5.6.4");

        } catch (GSSException e) {

            //because we are initializeing statics, we can
            //only throw a standard runtime exception
            throw new NumberFormatException();
        }
    }


    /**
     * Converts a contiguous string name to a GSSName object
     * of the specified type. The nameStr parameter is
     * interpreted based on the type specified.
     * In general, the GSSName object created will not be an MN;
     * the exception to this is if the type parameter indicates
     * NT_EXPORT_NAME.
     * <p>
     * <DL><DT><b>RFC 2078</b> <DD>equivalent to gss_import_name</DL>
     * @param nameStr the name to create
     * @param type an oid specifying the name type
     */
    public GSSName(String nameStr, Oid type) {
    
        m_nameStr = nameStr;
        m_nameType = type;
    }

    
    /**
     * Creates a new GSSName object from the specified type. It
     * is envisioned that this constructor will be called with
     * buffers returned from GSSName.export() or for name types
     * that aren't represented by printable strings.
     * <DL><DT><b>RFC 2078</b>
     * <DD>equivalent to gss_import_name</DL>
     * @param name buffer containing name
     * @param type an Oid specifying the name type
     * @exception GSSException with possible major codes of
     *   BAD_NAMETYPE, BAD_NAME, or FAILURE.
     * @see GSSName#GSSName(String,Oid)
     * @see GSSName#GSSName(byte[],Oid,Oid)
     * @see #export
     */
    public GSSName(byte[] name, Oid type) throws GSSException {
    
        m_nameType = type;
        m_nameBytes = new byte[name.length];
        System.arraycopy(name, 0, m_nameBytes, 0, name.length);
    
        //check if export name, which means we can load it right now
        if (type.equals(NT_EXPORT_NAME)) {
            if (name[0] != 0x04 && name[1] != 0x01)
                throw new GSSException(GSSException.BAD_NAME);

            Oid mechOid = new Oid(name, 2);
            GSSNameSpi mn = GSSManager.getNameInstance(mechOid);
            mn.init(name, type);
            addMechName(mn);
        }
    }


    /**
     * Creates a new GSSName object of the specified type.
     * This constructor takes an additional mechanism oid parameter
     * which allows the creation of a mechanism name in one step.
     * <p>
     * <DL><DT><b>RFC 2078</b> <DD>equivalent to gss_import_name
     * followed by gss_canonicalize_name</DL>
     * @param nameStr the name to create
     * @param nameType an oid specifying the name type
     * @param mechType the oid of the mechanism to create this name for
     * @exception GSSException with possible major codes of
     *   BAD_NAMETYPE, BAD_NAME, or FAILURE.
     * @see GSSName#GSSName(String,Oid)
     */
    public GSSName(String nameStr, Oid nameType, Oid mechType)
            throws GSSException {
    
        m_nameStr = nameStr;
        m_nameType = nameType;
        GSSNameSpi mn = GSSManager.getNameInstance(mechType);
        mn.init(nameStr, nameType);
        addMechName(mn);
    }

    
    /**
     * Creates a new GSSName object from the specified type. It
     * is envisioned that this constructor will be called with
     * buffers returned from GSSName.export() or for name types
     * that aren't represented by printable strings. This constructor
     * takes an additional parameter for the mechanism oid.
     * <DL><DT><b>RFC 2078</b><DD>
     * equivalent to gss_import_name followed by gss_canonicalize_name
     * </DL>
     * @param name buffer containing name
     * @param nameType an Oid specifying the name type
     * @param mechType an Oid for the mechanism to create this name for
     * @exception GSSException with possible major codes of
     *   BAD_NAMETYPE, BAD_NAME, or FAILURE.
     * @see GSSName#GSSName(String,Oid)
     * @see GSSName#GSSName(name,Oid)
     * @see #export
     */
    public GSSName(byte[] name, Oid nameType, Oid mechType)
		throws GSSException {
    
        m_nameType = nameType;
        m_nameBytes = new byte[name.length];
        System.arraycopy(name, 0, m_nameBytes, 0, name.length);
    
        GSSNameSpi mn = GSSManager.getNameInstance(mechType);
        mn.init(name, nameType);
        addMechName(mn);
    }


    /**
     * Package private constructor used by the clone method and
     * the credential object. - WE must make sure we set the name type
     * oid.
     */
    GSSName() { }

    
    /**
     * Package private constructor used by canonicalize name
     * and the context object. Sets the specified mechanism name.
     */
    GSSName(GSSNameSpi mechName) {
    
        addMechName(mechName);
                m_nameType = mechName.getNameType();
    }


    /**
     * Compares this name with the specified GSSName for equality. 
     * If either of the names has type NT_ANONYMOUS, this call will
     * return false.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to gss_compare_name</DL>
     * @param another the GSSName object to be compared
     * @return true if they both names refer to the same entity, false
     *    otherwise
     * @overrides equals in class Object
     */
    public boolean equals(Object another) {
    
        if ( !(another instanceof GSSName))
            return (false);
        
        try {
            return (equals((GSSName)another));
            
        } catch (GSSException e) { return false; }
    }
    

    /**
     * A variation of equals method which may throw a GSSException
     * when the names cannot be compared. If either of the names
     * represents an anonymous entity, the method will return false.
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to gss_compare_name</DL>
     * @param another GSSName object to be compared
     * @return true if they both names refer to the same entity, false
     *    otherwise
     * @exception GSSException with possible major codes of
     *    BAD_NAMETYPE, BAD_NAME, FAILURE
     */
    public boolean equals(GSSName another) throws GSSException {
    
        //check if anonymous name
        if (isAnonymousName() || another.isAnonymousName())
            return (false);
            
        if (m_mechNames.size() != another.m_mechNames.size())
            return false;
            
        if (m_mechNames.size() < 1) {
        
            //check if the external names match
            if (! m_nameType.equals(another.m_nameType))
                return false;
                
            if (m_nameStr != null && another.m_nameStr != null)
                return (m_nameStr.equals(another.m_nameStr));
                
            if (m_nameBytes != null && another.m_nameBytes != null)
                return (m_nameBytes.equals(another.m_nameBytes));
                
            return false;
        }
            
        //we have some mechanism names, each name must be over same
        //mechs and the names must equal
        GSSNameSpi mechName1, mechName2;
        for (Enumeration e = m_mechNames.elements(); e.hasMoreElements();) {
            
            mechName1 = (GSSNameSpi)e.nextElement();
            if ((mechName2 = another.getMechName(mechName1.getMech()))
				== null)
                return false;
                
            if (! mechName1.equals(mechName2))
                return false;
        }
        
        //went through all the names and they equal, so must be same name
        return (true);
    }

    
    /**
     * Creates a new name which is guaranteed to be mechanism specific (MN).
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to gss_canonicalize_name</DL>
     * @param mechOid oid of the mechanism for which the name should be
     *    canonicalized
     * @return a mechanism specific internal name (MN)
     * @exception GSSException with possible major codes of
     *    BAD_MECH, BAD_NAMETYPE.
     */
    public GSSName canonicalize(Oid mechOid) throws GSSException {

        //check if we already contain the mechanism name
        GSSNameSpi mechName = getMechName(mechOid);
        if (mechName != null) {
            if (isMechName())
                return (this);

            //need to create new name only for this mechanism
            return (new GSSName((GSSNameSpi)mechName.clone()));
        }
        

        //we don't already have it, so create it
        if (m_nameStr != null)
            return (new GSSName(m_nameStr, m_nameType, mechOid));
        else
            return (new GSSName(m_nameBytes, m_nameType, mechOid));
    }

  
    /**
     * Returns a flat name representation for this GSSName object. The
     * name must be in MN format before making this call. The name is
     * prefixed with a mechanism independent header as specified in
     * RFC 2078.  The returned buffer can be passed into a GSSName
     * constructor with GSSName.EXPORT_NAME as the name type.
     * <DL><DT><b>RFC 2078</b> 
     *    <DD>equivalent to gss_export_name</DL>
     * @return a byte array representing the name
     * @exception GSSException with possible major codes of NAME_NOT_MN,
     *   BAD_NAME, BAD_NAMETYPE, FAILURE.
     * @see #canonicalize
     */
    public byte[] export() throws GSSException {
    
        //check if we have a mechanism specific name
        if (!isMechName())
            throw new GSSException(GSSException.NAME_NOT_MN);
            
        return (getMechName(null).export());
    }

  
    /**
     * Returns a string representation of the GSSName object.
     * To retrieve the printed name format call getStringNameType.
     * @return a String representation for this GSSName
     * @overrides java.lang.Object#toString
     * @see #getStringNameType
     **/
    public String toString() {
    
        if (isMechName())
            return (getMechName(null).toString());
            
        if (m_nameStr != null)
            return (m_nameStr);
            
        if (m_mechNames.size() > 0)
            return (getMechName(null).toString());
        
        //name must in byte format
        return ("Unknown name");
    }

        
    /**
     * Returns the name type for the printed name. 
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to name_type parameter in gss_display_name</DL>
     * @return Oid for the name type as printed with toString()
     * @exception GSSException when the name can't be printed
     * @see #toString
     */
    public Oid getStringNameType() throws GSSException {

        if (isMechName())
            return (getMechName(null).getStringNameType());
            
        if (m_nameStr != null)
            return (m_nameType);
            
        if (m_mechNames.size() > 0)
            return (getMechName(null).getStringNameType());
        
        throw new GSSException(GSSException.BAD_NAME);
    }


    /**
     * Creates a duplicate of this object.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to gss_duplicate_name</DL>
     * @return a copy of this object
     * @exception CloneNotSupportedException may be thrown
     */
    public Object clone() throws CloneNotSupportedException {
    
        GSSName newName;
        
        try {
            if (m_nameStr != null)
                newName = new GSSName(m_nameStr, m_nameType);
            else if (m_nameBytes != null)
                newName = new GSSName(m_nameBytes, m_nameType);
            else 
                newName = new GSSName();
            
            for (Enumeration e = m_mechNames.elements();
                        e.hasMoreElements(); )
                newName.addMechName((GSSNameSpi)
                    ((GSSNameSpi)e.nextElement()).clone());

        } catch (Exception e) { 
            throw new CloneNotSupportedException();
        }
        
        return (newName);
    }


    /**
     * Tests if this is an Anonymous name object.
     * @return boolean indicating if this in an anonymous name
     */
    public boolean isAnonymousName() {
    
        if (m_nameType.equals(NT_ANONYMOUS))
            return (true);
            
        if (m_mechNames.size() > 0)
            return (getMechName(null).isAnonymousName());
        
        return false;
    }
    

    /**
     * Package private method to add a mechanism name to
     * the name. Used by the credential object.
     *
     */
    void addMechName(GSSNameSpi mechName) {
    
        m_mechNames.addElement(mechName);
    }


    /**
     * Tests if this is a mechanism name.
     * Mechanism name is defined as having only one mechanism
     * name element.
     * @return boolean indicating if this is a MN
     */
    private boolean isMechName() {
    
        return (m_mechNames.size() == 1);
    }
    
    
    /**
     * Retrieves a mechanism specific name for the specified oid.
     * If the name is not found, null is returned. null oid can be
     * used to retrieve the first mechanism name.
     *
     * @param Oid for the mechanism name to retrieve     
     * @return GSSNameSpi for the requested mechanism or null if not found
     */
    GSSNameSpi getMechName(Oid mechOid) {
        
        if (mechOid == null) {
            if (m_mechNames.size() < 1)
                return (null);
                
            return ((GSSNameSpi)m_mechNames.firstElement());
        }
        
        for (Enumeration e = m_mechNames.elements(); e.hasMoreElements(); ) {
        
            GSSNameSpi mechName = (GSSNameSpi)e.nextElement();
            
            if (mechName.getMech().equals(mechOid))
                return (mechName);
                
        }
        return (null);
    }
            
            
    /**
     * Returns the mechanism specific name. If this name does not
     * already contain it, it is created.
     * @param mechOid oid of the mechanism for which the name should be
     *    canonicalized
     * @return a mechanism specific internal name
     * @exception GSSException with possible major codes of
     *    BAD_MECH, BAD_NAMETYPE.
     */
    GSSNameSpi canonicalizeInPlace(Oid mechOid) throws GSSException {

        //check if we already contain the mechanism name
        GSSNameSpi mechName = getMechName(mechOid);
        if (mechName != null)
            return (mechName);
            
        //we don't already have it, so create it
        mechName = GSSManager.getNameInstance(mechOid);
        
        if (m_nameStr != null)
            mechName.init(m_nameStr, m_nameType);
        else
            mechName.init(m_nameBytes, m_nameType);
            
        addMechName(mechName);
        return (mechName);
    }
            
        
    //instance variables
    //we use a vector because GSSCredential.getName() must return
    //all the credential names in mechanism format
    Vector m_mechNames = new Vector(3,2);
    Oid m_nameType;
    String m_nameStr;
    byte [] m_nameBytes;
}
