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
 * This class manages GSS-API credentials and their associated 
 * operations. A credential contains all the necessary cryptographic 
 * information to enable the creation of a context on behalf of the 
 * entity that it represents. It may contain multiple distinct mechanism 
 * specific credential elements, each containing mechanism specific 
 * information, and all referring to the same entity.
 * <p>
 * A credential may be used to perform context initiation, acceptance, 
 * or both.
 * <DL><DT><b>RFC 2078</b>
 * <DD>This class represents the credential management GSS-API calls,
 * which are:<ul><li>gs_acquire_cred
 *    <li>gss_release_cred
 *    <li>gss_inquire_cred
 *     <li>gss_add_cred
 *    <li>gss_inquire_cred_by_mech</ul>
 * The gss_inquire_cred and gss_inquire_cred_by_mech calls have been
 * distributed over several property querying methods each returning
 * specific GSSCredential information.
 *  </DL> 
 */
public class GSSCredential {

    /**
     * Credential usage flag requesting that it be able to be used
     * for both context initiation and acceptance.
     */      
    public static final int INITIATE_AND_ACCEPT = 0;


    /**
     * Credential usage flag requesting that it be able to be used
     * for context initiation only.
     */
    public static final int INITIATE_ONLY = 1;


    /**
     * Credential usage flag requesting that it be able to be used
     * for context acceptance only.
     */
    public static final int ACCEPT_ONLY = 2;


    /**
     * Indefinite lifetime for a credential. It is set to the
     * largest value for an int in Java.
     * @see #getRemainingLifetime
     **/
    public static final int INDEFINITE = Integer.MAX_VALUE;
  

    /**
     * Constructor for default credentials.
     * This will use the default mechanism, default mechanism name,
     * and an INDEFINITE lifetime.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to gss_acquire_cred</dl>
     * @param usage - the intended usage for this credential; this
     *     must be one of the constants defined in this class.
     * @exception GSSException with possible major code of FAILURE
     **/
    public GSSCredential(int usage) throws GSSException {

        add((GSSName)null, INDEFINITE, INDEFINITE, (Oid)null, usage);
    }
    
    
    /**
     * Constructor for default mechanism credential.
     * Uses default mechanism and INDEFINITE lifetime.
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to gss_acquire_cred</DL>
     * @param aName - name of the principal for whom this credential
     *     is to be acquired
     * @param usage - the intended usage for this credential; this
     *     must be one of the constants defined in this class
     * @exception GSSException with possible major codes of FAILURE and
     *     BAD_NAME
     **/
    public GSSCredential(GSSName aName, int usage) throws GSSException {
    
        add(aName, INDEFINITE, INDEFINITE, (Oid)null, usage);
    }
    
    
    /**
     * Constructor for a single mechanism credential.
     * null values can be specified for name and mechanism to obtain
     * system specific defaults.
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to gss_acquire_cred</DL>
     * @param aName - name of the principal for whom this credential
     *     is to be acquired; use null for system specific default
     *     principal
     * @param lifetime - the duration of this credential
     * @param mechOid - mechanism over which this credential is to
     *     be acquired
      * @param usage - the intended usage for this credential; this
     *     must be one of the constants defined in this class
     * @exception GSSException with possible major codes of FAILURE,
     *    BAD_MECH, and BAD_NAME
     **/
    public GSSCredential(GSSName aName, int lifetime, Oid mechOid,
            int usage) throws GSSException {
        
        add(aName, lifetime, lifetime, mechOid, usage);
    }
    
    /**
     * Constructor for a credential over a set of mechanisms.
     * Acquires credentials for each of the mechanisms specified
     * in mechs array. null value can be used for Name to obtain
     * system specific default. To determine for which mechanisms
     * acquiring of the credential was successful use the getMechs
     * method.  Note that this call is equivalent to creating a
     * single mechanism credential and using addCred to extend the
     * credential over other mechanisms.
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to gss_acquire_cred</dl>
     * @param aName - name of the principal for whom this credential
     *     is to be acquired; use null for system specific default
     *     principal
     * @param lifetime - the desired duration of this credential
     * @param mechs - mechanisms over which this credential is to
     *     be acquired
     * @param usage - the intended usage for this credential; this
     *     must be one of the constants defined in this class
     * @exception GSSException with possible major codes of FAILURE,
     *    BAD_MECH, and BAD_NAME
     */
    public GSSCredential(GSSName aName, int lifetime, Oid [] mechs,
	int usage) throws GSSException {
        
        for (int i = 0; i < mechs.length; i++)
            add(aName, lifetime, lifetime, mechs[i], usage);
    }

 
    /**
     * Package private constructor used to create a credential
     * object using the supplied mechanism credential element.
     *
     * @param GSSCredSpi mechanism specific credential object
     */
    GSSCredential(GSSCredSpi mechCred) {
    
        m_mechCreds.addElement(mechCred);
    }
    
 
    /**
     * Used to dispose of any sensitive information that the
     * GSSCredential may be containing.  Should be called as soon
     * as the credential is no longer needed to minimize the time
     * sensitive information is maintained.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to gss_release_cred</dl>
     * @exception GSSException with possible major code of FAILURE
     */
    public void dispose() throws GSSException {
    
        for (Enumeration e = m_mechCreds.elements(); e.hasMoreElements();) {
            ((GSSCredSpi)e.nextElement()).dispose();
        }
        m_mechCreds.removeAllElements();
    }

    /**
     * Retrieves the name of the entity that the credential has been
     * acquired for.
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to obtaining the cred_name parameter
     *    from gss_inquire_cred</dl>
     * @return GSSName for the credential's principal
     * @exception GSSException with possible major codes of FAILURE,
     *    NO_CRED, DEFECTIVE_CREDENTIAL, CREDENTIAL_EXPIRED
     */
    public GSSName getGSSName() throws GSSException {

        /*
         * will return name containing mechanism elements for
         * all mechs this credential supports
         */
        if (m_mechCreds.size() < 1)
            throw new GSSException(GSSException.NO_CRED);

        GSSName aName = new GSSName();
        for (Enumeration e = m_mechCreds.elements(); e.hasMoreElements(); ) {
        
            aName.addMechName(((GSSCredSpi)e.nextElement()).getName());
        }
        
        return (aName);    
    }

    /**
     * Queries the selected mechanism for the principal name of the
     * credential. The mechanism must be one of the mechanisms over
     * which the credential is acquired.
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to obtaining the cred_name parameter from
     *    gss_inquire_cred_by_mech</dl>
     * @param mechOID the credential mechanism to be queried
     * @Return GSSName for the credential's principal; this GSSName
     *     object will be an MN
     * @exception GSSException with possible major codes of NO_CRED,
     *    DEFECTIVE_CREDENTIAL, CREDENTIALS_EXPIRED, FAILURE and
     *     BAD_MECH
     */
    public GSSName getGSSName(Oid mechOID) throws GSSException {

        GSSName aName = new GSSName();
        aName.addMechName(getMechCred(mechOID, true).getName());
        return (aName);
    }
    
    
    /**
     * Obtains the remaining lifetime for a credential. The remaining 
     * lifetime is the minimum lifetime for any of the credential
     * elements.  Return of 0 indicates the credential is already
     * expired.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to lifetime parameter in gss_inquire_cred
     *    </dl>
     * @return lifetime in seconds
     * @exception GSSException with possible major codes of NO_CRED,
     *   DEFECTIVE_CREDENTIAL, FAILURE.
     */
    public int getRemainingLifetime() throws GSSException {

        int lifetime = GSSCredential.INDEFINITE;
        GSSCredSpi aCred;

        if (m_mechCreds.size() < 0)
            throw new GSSException(GSSException.NO_CRED);
            
        /* find the minimum lifetime */        
        for (Enumeration e = m_mechCreds.elements(); e.hasMoreElements();) {
        
            aCred = (GSSCredSpi)e.nextElement();
            if (aCred.getLifetime() < lifetime)
                lifetime = aCred.getLifetime();
        }
        return (lifetime);
    }

    /**
     * Returns the remaining lifetime in seconds for the credential
     * to remain capable of initiating security context under the
     * specified mechanism. Return of 0 indicates that the
     * credential is already expired.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to lifetime_init parameter in
     *    gss_inquire_cred_by_mech</DL>
     * @param mech Oid for the credential mechanism to be queried
     * @return the remaining initiation lifetime in seconds
     * @exception GSSException with possible major codes of NO_CRED,
     *    DEFECTIVE_CREDENTIAL, FAILURE and BAD_MECH
     */
    public int getRemainingInitLifetime(Oid mech) throws GSSException {
    
        GSSCredSpi aCred = getMechCred(mech, true);
        return (aCred.getInitLifetime());
    }

  
    /**
     * Returns the remaining lifetime in seconds for the credential
     * to remain capable of accepting security context under the
     * specified mechanism. Return of 0 indicates that the
     * credential is already expired.
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to lifetime_accept parameter in
     *    gss_inquire_cred_by_mech</DL> 
     * @param mech Oid for the credential mechanism to be queried
     * @return the remaining acceptance lifetime in seconds
     * @exception GSSException with possible major codes of NO_CRED,
     *    DEFECTIVE_CREDENTIAL, FAILURE and BAD_MECH
     */
    public int getRemainingAcceptLifetime(Oid mech) throws GSSException {
    
        GSSCredSpi aCred = getMechCred(mech, true);
        return (aCred.getAcceptLifetime());
    }

    
    /**
     * Retrieve the credential usage flag, which is one of
     * INITIATE_ONLY, ACCEPT_ONLY, INITIATE_AND_ACCEPT.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to usage parameter in gss_inquire_cred</DL>
     * @return credential usage which will be only of
     *     INITIATE_ONLY, ACCEPT_ONLY, or INITIATE_AND_ACCEPT
     * @exception GSSException with possible major codes of NO_CRED,
     *   DEFECTIVE_CREDENTIAL, CREDENTIALS_EXPIRED, FAILURE.
     */
    public int getUsage() throws GSSException {
    
        boolean init = false, accept = false;
        GSSCredSpi aCred;

        if (m_mechCreds.size() < 0)
            throw new GSSException(GSSException.NO_CRED);
            
        /* find the usage for the credential */
        for (Enumeration e = m_mechCreds.elements(); e.hasMoreElements();) {
        
            aCred = (GSSCredSpi)e.nextElement();
            if (aCred.getUsage() == GSSCredential.INITIATE_AND_ACCEPT)
                return (GSSCredential.INITIATE_AND_ACCEPT);
            else if (aCred.getUsage() == GSSCredential.INITIATE_ONLY)
                init = true;
            else if (aCred.getUsage() == GSSCredential.ACCEPT_ONLY)
                accept = true;
                
            //if both are set, then we are done....
            if (init && accept)
                return (GSSCredential.INITIATE_AND_ACCEPT);
        }
        
        //can only be a single use credential
        if (init)
            return (GSSCredential.INITIATE_ONLY);
            
        return (GSSCredential.ACCEPT_ONLY);
    }


    /**
     * Retrieve the credential usage flag, which is one of
     * INITIATE_ONLY, ACCEPT_ONLY, INITIATE_AND_ACCEPT, for a
     * specific credential mechanism.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to usage parameter in
     *    gss_inquire_cred_by_mech</DL>
     * @param oid for the credential mechanism to query
     * @return credential usage which will be only of
     *     INITIATE_ONLY, ACCEPT_ONLY, or INITIATE_AND_ACCEPT
     * @exception GSSException with possible major codes of NO_CRED,
     *   DEFECTIVE_CREDENTIAL, CREDENTIALS_EXPIRED, FAILURE.
     */
    public int getUsage(Oid mechOID) throws GSSException {
    
        GSSCredSpi aCred = getMechCred(mechOID, true);
        return (aCred.getUsage());
    }


    /**
     * Returns the mechanism oids over which the credential has been
     * acquired.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to mech_set parameter of gss_inquire_cred</DL>
     * @return the array of mechanism oid's over which this credential
     *    has been acquired
     * @exception GSSException with possible major codes of FAILURE,
     *    NO_CRED, DEFECTIVE_CREDENTIAL, CREDENTIAL_EXPIRED
     */
    public Oid [] getMechs() throws GSSException {

        Oid [] oids = new Oid[m_mechCreds.size()];
        int i = 0;

        if (m_mechCreds.size() < 1)
            throw new GSSException(GSSException.NO_CRED);
                    
        for (Enumeration e = m_mechCreds.elements(); e.hasMoreElements();)
            oids[i++] = ((GSSCredSpi)e.nextElement()).getMechanism();
            
        return (oids);
    }
  

    /**
     * This method enables the construction of credentials one
     * mechanism at a time. A single mechanism credential will
     * be added as specified by the mech parameter. This is
     * equivalent to using the constructor for multiple mechanism
     * but gives finer control and feedback.
     * <DL><DT><b>RFC 2078</b>
     *    <DD>equivalent to gss_add_cred<br>
     *    to obtain a new credential as in gss_add_cred, first call
     *    clone and then addCred</dl>
     * @param aName - name of the principal for whom this credential
     *     is to be acquired; use null for system specific default
     *     principal
     * @param initLifetime - the desired duration of this credential
     *    initiation lifetime; value in seconds
     * @param acceptLifetime - the desired duration of this credential
     *    accept lifetime; value in seconds
     * @param mechs - mechanism over which this credential is to
     *     be acquired
     * @param usage - the intended usage for this credential; this
     *     must be one of the constants defined in this class
     * @exception GSSException with possible major codes of
     *    DUPLICATE_ELEMENT, BAD_MECH, BAD_NAME, BAD_NAME, NO_CRED,
     *    or FAILURE.
     */  
    public synchronized void add(GSSName aName, int initLifetime,
        int acceptLifetime, Oid mech, int usage) throws GSSException {
        
        if (mech == null)
            mech = GSSManager.getDefaultMech();

        //check if this cred already exists
        if (getMechCred(mech, false) != null)
            throw new GSSException(GSSException.DUPLICATE_ELEMENT);
        
        //ok, go ahead create new one.......
        GSSCredSpi newCred = GSSManager.getCredInstance(mech);
        
        newCred.init(aName.canonicalizeInPlace(mech), initLifetime,
                acceptLifetime, usage);
        
        //mechanism credential created successfully, so add
        m_mechCreds.addElement(newCred);
    }


    /**
     * Tests if this GSSCredential refers to the same entity as the
     * supplied object. The two GSSCredentials must be acquired over
     * the same mechanisms.
     * @return true if the two GSSCredentials refer to the same
     *    entity; false otherwise.
     * @override java.lang.Object#equals
     */
    public boolean equals(Object another) {
    
        if ( !(another instanceof GSSCredential))
	    return (false);
            
        GSSCredential aCred = (GSSCredential)another;
        
        if (aCred.m_mechCreds.size() != m_mechCreds.size())
            return (false);

        GSSCredSpi intCred, anotherIntCred;

        try {                                        
            for (Enumeration e = m_mechCreds.elements();
                e.hasMoreElements();) {
            
                intCred = (GSSCredSpi)e.nextElement();
                anotherIntCred = aCred.getMechCred(
                    intCred.getMechanism(), false);
                if (anotherIntCred == null)
                    return (false);
                
                //ask internal creds to compare themselves
                if (intCred.equals(anotherIntCred) == false)
                    return (false);
            
            }
        } catch (GSSException e) {
            return (false);
        }
        
        //all internal creds are equal, so we are equal too...
        return (true);
    }
    

    /**
     * Debugging aid. Returns string with information about
     * this credential object.
     */
    public String toString() {
        
        StringBuffer sb = new StringBuffer(150);
        
        sb.append(super.toString());
        sb.append("\nOver mechs:\t");
        try {
            Oid [] mechs = getMechs();
            for (int i = 0; i < mechs.length; i++)
                sb.append(mechs[i].toString() + " ");
            
            sb.append("\nFor principal:\t" + getGSSName().toString());
            sb.append("\nUsage:\t" + getUsage());
            if (getUsage() == ACCEPT_ONLY)
                sb.append(" (ACCEPT_ONLY)");
            else if (getUsage() == INITIATE_ONLY)
                sb.append(" (INITIATE_ONLY)");
            else
                sb.append(" (INITIATE and ACCEPT)");
            
            sb.append("\nRemaining Lifetime:\t" + getRemainingLifetime());
            
        } catch (GSSException e) {
            sb.append("\n***ERROR getting info:\t" + e.toString());
        }
        
        return (sb.toString());
    }
    
    
    /**
     * Returns the specified mechanism's credential-element.
     *
     * @param mechOid - the oid for mechanism to retrieve
     * @param throwExcep - boolean indicating if the function is
     *    to throw exception or return null when element is not
     *    found.
     * @return mechanism credential object
     * @exception GSSException of invalid mechanism
     */
    GSSCredSpi getMechCred(Oid mechOid, boolean throwExcep)
        throws GSSException {
    
        for (Enumeration e = m_mechCreds.elements(); e.hasMoreElements();) {
        
            GSSCredSpi aCred = (GSSCredSpi)e.nextElement();
            if (aCred.getMechanism().equals(mechOid))
                return (aCred);
        }
        
        /* not found */
        if (throwExcep == true)
            throw new GSSException(GSSException.BAD_MECH);
        else
            return (null);
    }
        

    /* private instance variables */
    Vector m_mechCreds = new Vector(3, 3);
}

