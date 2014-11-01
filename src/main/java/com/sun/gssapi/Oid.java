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

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


/** 
 * This class represents Universal Object Identifiers (Oids) and
 * their associated operations.
 * <p>
 * Oids are hierarchically globally-interpretable identifiers
 * used within the GSS-API framework to identify mechanisms and
 * name formats. The structure and encoding of Oids is defined
 * in ISOIEC-8824 and ISOIEC-8825.  For example the Oid 
 * representation of Kerberos V5 mechanism is 1.2.840.113554.1.2.2
 * <DL><DT><b>RFC 2078</b>
 * <DD>This class replaces the following GSS-API functions:
 * <ul><li>gss_test_oid_set_member
 * <li>gss_release_oid
 * <li>gss_oid_to_str
 * <li>gss_str_to_oid</ul>
 * </DL>
 */

public class Oid {

    /**
     * Creates an oid object from its DER octets.
     */
    static Oid getFromDEROctets(InputStream is, int len)
		throws GSSException {
    
        return new Oid(DERParser.decodeOidOctets(is, len));
    }
        

    /**
     * Creates an oid object from a vector of its integer components.
     * The vector is not copied.
     */
    private Oid(Vector v) {
    
        m_v = v;
    }
    
    
    /**
     * Constructs an Oid object from a string representation of its
     * integer components. Will throw a GSSException if the string
     * is improperly formatted.
     *
     * @param strOid the string in either of these two formats:
     *        "{1 2 3 3}" or "1.2.3.3".
     * @exception GSSException may be thrown when the
     *    string is incorrectly formatted
     */
    public Oid(String strOid) throws GSSException {
        
        m_v = new Vector(8, 3);
        parseFromStr(strOid);
    }

        
    /**
     * Constructs an Oid object from its DER encoding. The structure
     * and encoding of Oids is defined in ISOIEC-8824 and ISOIEC-8825.
     *
     * @param derOid stream containing the DER encoded oid
     * @exception GSSException may be thrown when the DER
     *    encoding does not follow the prescribed format.
     */
    public Oid(InputStream derOid) throws GSSException {
    
        m_v = DERParser.decodeOid(derOid);
    }


    /**
     * Constructs an Oid object from its DER encoding. The structure
     * and encoding of Oids is defined in ISOIEC-8824 and ISOIEC-8825.
     *
     * @param data byte array containing the DER encoded oid
     * @param offset where in the data byte array to start from
     * @exception GSSException may be thrown when the DER
     *    encoding does not follow the prescribed format.
     */
    public Oid(byte [] data, int offset) throws GSSException {
    
	m_v = DERParser.decodeOid(new ByteArrayInputStream(data,
                    offset, data.length - offset));
    }

    
    /**
     * Constructs an Oid object from its DER encoding. The structure
     * and encoding of Oids is defined in ISOIEC-8824 and ISOIEC-8825.
     *
     * @param DEROid a byte array containing the DER encoding of the Oid
     * @exception GSSException may be thrown when the DER
     *    encoding does not follow the prescribed format.
     * @see Oid#Oid(java.io.InputStream)
     */
     public Oid(byte[] DEROid) throws GSSException {
        
        m_v = DERParser.decodeOid(new ByteArrayInputStream(DEROid));
     }
 
       
    /**
     * Returns a string representation of the oid's integer components
     * in dot separated notation.
     * @return string representation in the following format: "1.2.3.4.5"
     * @see #toRFC2078String
     * @overrides java.lang.Object#toString
     */
    public String toString() {
    
        StringBuffer sb = new StringBuffer(50);
    
        if (m_v.size() < 1)
            return (new String(""));
            
        for (Enumeration e = m_v.elements(); e.hasMoreElements();) {
            sb.append(e.nextElement().toString());
            if (e.hasMoreElements())
                sb.append(".");
        }
        
        return (sb.toString());
    }


    /**
     * Returns a string representation of the Oid's integer components
     * in the format specified within RFC 2078. For example { 1 2 3 4 }
     * <DL><DT><b>RFC 2078</b>
     * <DD>equivalent to gss_oid_to_str</DL>
     * @return string representation in the following format: { 1 2 3 4 5 }
     * @see #toString
     */
    public String toRFC2078String() {
    
        StringBuffer sb = new StringBuffer(50);
        
        if (m_v.size() < 1)
            return (new String(""));
            
        sb.append("{ ");
        for (Enumeration e = m_v.elements(); e.hasMoreElements(); ) {
            sb.append(e.nextElement().toString());
            sb.append(" ");
        }
        sb.append("}");
        
        return (sb.toString());
    }
    
    
    /**
     * Equality test for oid objects.
     * @param Obj Oid object that has to be compared to this one
     * @return true if they represent the same Oid, false otherwise
     * @overrides java.lang.Object#equals
     */
    public boolean equals(Object Obj) {
    
        if (! (Obj instanceof Oid))
            return (false);

        //check if both reference the same object
        if (this == Obj)
            return (true);

        Oid anOid = (Oid) Obj;
        
        if (m_v.size() != anOid.m_v.size())
            return (false);
            
        for (Enumeration e1 = m_v.elements(), e2 = anOid.m_v.elements();
            e1.hasMoreElements(); ) {
        
            if (! e1.nextElement().equals(e2.nextElement()))
                return (false);
        }
        
        return (true);
    }

    
    /**
     * Returns the full ASN.1 DER encoding for this oid object.
     *
     * @return byte array containing the DER encoding of this oid object.
     * @exception GSSException may be thrown when the oid can't be encoded
     */
    public byte[] getDER() throws GSSException {
    
        if (m_der != null)
            return (m_der);
        
        m_der = DERParser.encodeOid(m_v);
        return (m_der);
    }
    
        
    /**
     * A utility method which takes an array of Oids and checks if
     * it contains this oid object.
     * <p><DL><DT><b>RFC 2078</b>
     * <DD>In the JGSS package Oid sets are represented as arrays of
     * Oids. This method is equivalent to gss_test_oid_set_member.</DL>
     * @param An array of Oids to search
     * @return true if the array contains this object, false otherwise
     */
    public boolean containedIn(Oid[] oids) {
    
        for (int i = 0; i < oids.length; i++) {
            if (oids[i].equals(this))
                return (true);
        }
    
        return (false);
    }
    
    
    /**
     * Parses in the string encoding of the object.
     *
     * @param src string to parse
     * @exception GSSException when src string is incorrectly formatted
     **/
    private void parseFromStr(String src) throws GSSException {
    
        int firstIndex = src.indexOf("{");
    
        try {
            //skip over the { and } first
            if (firstIndex != -1)
                src = src.substring(firstIndex, src.lastIndexOf("}"));
                
            StringTokenizer st = new StringTokenizer(src, " .");
            while (st.hasMoreTokens()) {
                m_v.addElement(new Integer(st.nextToken()));
            }
                
        } catch (Exception e) {
            throw new GSSException(GSSException.FAILURE);
        }
    }
    
    
    //Instance variables
    private Vector m_v;
    byte [] m_der;
}
