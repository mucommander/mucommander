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

import java.io.*;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * This class implements functionality common to the entire GSS-API
 * package. This includes the management of the gss mechanisms
 * and their providers.
 * <p><DL><DT><b>RFC 2078</b>
 * <DD>A side from JGSS provider management, this class
 * implements the equivalent of the following GSS-API routines:
 * <ul><li>gss_indicate_mechs
 *    <li>gss_inquire_mechs_for_name
 *    <li> gss_inquire_name_for_mech</ul></DL>
 */

public class GSSManager {
 
    static {
        initialize();
    }

    private static void initialize() {
        int i = 1;

        try {
	    ResourceBundle props = ResourceBundle.getBundle(
			"com.sun.gssapi.mechs");

	    while (true) {

		String name = props.getString("gss.provider." + i++);
		if (name == null) {
		    break;
		} else {
		    Class cl = Class.forName(name);
		    Object instance = cl.newInstance();
		    if (instance instanceof Provider) {
			Security.addProvider((Provider)instance);
		    } else {
			// This is not a valid Provider
		    }
		}
	    }
        } catch (Exception e) {
	    // no-op
        }
    }

    /**
     * Indicates which mechanisms are configured within JGSS.
     * <p><DL><DT><b>RFC 2078</b>
     * <DD>equivalent to gss_indicate_mechs</dl>
     *
     * @return array of Oids indicating available mechanisms;
     *    null when no mechanism are available
     */
    public static Oid[] getMechs() {

        /*
         * Go through all the providers and retrieve jgss ones
         * note we skip the ProviderTable, because we want all
         * jgss providers not just ones loaded.
         */
        Provider [] p = java.security.Security.getProviders();
        Vector aV = new Vector(5, 3);
        
        for (int i = 0; i < p.length; i++) {
            String []mechs = MechInfo.getMechsForProvider(p[i]);
            
            if (mechs == null)
                continue;
                
            for (int j=0; j < mechs.length; j++) {
                try {
                    addUniqueOid(aV, new Oid(mechs[j]));
                } catch (GSSException e) {}
            }
        }

        if (aV.size() == 0)
            return (null);
            
        Oid []mechs = new Oid[aV.size()];
        aV.copyInto(mechs);
        return (mechs);
    }
         
    
    /**
     * Returns name types (Oids) supported by the selected mechanism.
     * Note that in cases where several providers implement the same
     * mechanism, only the first provider implementing the mechanism
     * is queried.
     * <p><DL><DT><b>RFC 2078</b>
     * <DD>equivalent to gss_inquire_names_for_mech</DL>
     *
     * @param mech the Oid for mechanism to be queried
     * @return names array of Oids indicating names supported by the
     *    requested mechanism; or null if the mech is not supported
     * @exception GSSException with major code of BAD_MECH will be thrown
     *    for invalid mechanism oids
     */
    public static Oid[] getNamesForMech(Oid mech) throws GSSException {
    
        MechInfo aMech = getMechInfo(mech, false);
        
        return (aMech.getNames());        
    }
    

    /**
     * Returns all the mechanisms that support the specific name type.
     * <p>
     * <DL><DT><b>RFC 2078</b>
     * <DD>equivalent to gss_inquire_mechs_for_name</DL>
     *
     * @param nameType the Oid of the name type to be queried
     * @return mechs an array of mechanism Oids supporting the
     *    requested name type; null if no mechanism supports the 
     *    requested name type
     */
    public static Oid[] getMechsForName(Oid nameType) {
    
        Provider []p = java.security.Security.getProviders();
        Vector v = new Vector(5,3);
        
        for (int i = 0; i < p.length; i++) {
            MechInfo [] mechs = MechInfo.getInfoForAllMechs(p[i]);
            
            if (mechs == null)
                continue;
                
            for (int j = 0; j < mechs.length; j++) {
            
                if (mechs[j].supportsName(nameType))
                    addUniqueOid(v, mechs[j].getOid());
            }
        }
        
        if (v.size() == 0)
            return (null);
            
        Oid [] oids = new Oid[v.size()];
        v.copyInto(oids);
        return (oids);
    }
    
    
    /**
     * Determines the default mechanism.  The default mechanism is 
     * determined through the setting in the security properties file 
     * when the provider is installed or through dynamic configuration
     * of the providers.. The default mech is the first mechanism in
     * the first jgss provider.
     * @return the Oid for the default mechanism
     * @exception GSSException with major code set to BAD_MECH if
     *     no jgss providers exist.
     */
    public static Oid getDefaultMech() throws GSSException {

        if (m_defaultMech != null)
            return (m_defaultMech.getOid());
            
        Provider []p = java.security.Security.getProviders();
        
        //check each provider
        for (int i = 0; i < p.length; i++) {
            String []mechs = MechInfo.getMechsForProvider(p[i]);
            
            if (mechs == null)
                continue;
                
            m_defaultMech = new MechInfo(p[i], mechs[0]);
            return (m_defaultMech.getOid());
        }
        
        throw new GSSException(GSSException.BAD_MECH);
    }


    /**
     * Adds only unique values to the specified vector.
     * @param v - vector to add to
     * @param oid - the oid to add
     */
    private static void addUniqueOid(Vector v, Oid anOid) {
    
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            if ( ((Oid)e.nextElement()).equals(anOid))
                return;
        }
        v.addElement(anOid);
    }
    

    /**
     * Returns a provider specific implementation of the credential
     * object.
     */
    static GSSCredSpi getCredInstance(Oid mech) throws GSSException {
    
        //get mech out of the mech table, and if need be load it
        MechInfo aMech = getMechInfo(mech, true);
        return (aMech.getCredInstance());
    }


    /**
     * Returns a provider specific implementation of the name
     * object.
     */
    static GSSNameSpi getNameInstance(Oid mech) throws GSSException {
    
        //get mech out of the mech table, and if need be load it
        MechInfo aMech = getMechInfo(mech, true);
        return (aMech.getNameInstance());
    }


    /**
     * Returns a provider specific implementation of the C018FE95
     * object.
     */
    static C018FE95 _M4092FBA (Oid mech) throws GSSException {
    
        //get mech out of the mech table, and if need be load it
        MechInfo aMech = getMechInfo(mech, true);
        return (aMech._M4092FBA ());
    }
        

    /**
     * Obtains the MechInfo object for the specified mechanism.
     *
     * @param installMech this boolean indicates if the mechanism
     *     should be loaded if it isn't already
     */    
    private static synchronized MechInfo getMechInfo(Oid oid,
		boolean installMech) throws GSSException {
            
        //look in the hash table first
        MechInfo aMech = MechTable.getMechInfo(oid);
        if (aMech != null) {
            if (installMech)
                MechTable.putMechInfo(aMech);
            return (aMech);
        }
        
        //need to search all providers
        Provider [] p = java.security.Security.getProviders();
        String mechString = oid.toString();
        
        for (int i=0; i < p.length; i++) {
        
            if (MechInfo.implementsMech(p[i], mechString)) {

                try {
                    aMech = new MechInfo(p[i], mechString);
                    
                    if (installMech)
                        MechTable.putMechInfo(aMech);
                        
                    return (aMech);
                } catch (GSSException e) {
                
                    //skip over this provider, there might be
                    //other good ones
                    continue;
                }
            }
        }
        
        //this mechanism is not installed on the system
        throw new GSSException(GSSException.BAD_MECH);
    }
    
            
    /**
     * Debug method.
     */
    private static void showProviderDetails() {
    
        Provider [] p = java.security.Security.getProviders();
        MechInfo [] mechs;
        boolean foundGSSProv = false;
        
        for (int i = 0; i < p.length; i++ ) {
            mechs = MechInfo.getInfoForAllMechs(p[i]);
            if (mechs == null)
                continue;
                
            foundGSSProv = true;
        }
    }

    //private class variable - default mechanism oid
    private static MechInfo m_defaultMech;
} // end of Class GSSManager


/**
 * Class to store mechanism information extracted from the providers.
 * It contains all the provider parsing code.
 */
class MechInfo {

    //hides the default constructor
    private MechInfo() {
    
    }
    
    /**
     * Constructor to populate the object with information from
     * the provider.
     * @param p - the provider to be queried for mechanism info
     * @param oid - the mechanism which the provider is to be
     *    queried for
     * @exception GSSException with major status of BAD_MECH when
     *    this provider does not implement the specified mechanism
     */
    MechInfo(Provider p, String oid) throws GSSException {
    
        String aStr;
        
        m_oid = new Oid(oid);
        _V510CA83 = p;
        updateOidAlias(p, oid);
        if ((aStr = _M73F1AC8 (p, oid, "_K872D1AC")) != null) {
            StringTokenizer st = new StringTokenizer(aStr, ":");
            m_names = new Oid[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                m_names[i] = new Oid(st.nextToken());
                i++;
            }
        } else
            throw new GSSException(GSSException.BAD_MECH);
        
        _V2395ABD = _M73F1AC8 (p, oid, "_K532D1BD");
        _V108CA91 = _M73F1AC8 (p, oid, "_K1000A49");
        _V901D6C2 = _M73F1AC8 (p, oid, "_K2102CC5");
        
        if (_V2395ABD == null || _V108CA91 == null
            || _V901D6C2 == null)
            throw new GSSException(GSSException.BAD_MECH);
    }
    

    /**
     * Checks if this mechanism supports the supplied name oid.
     * @param nameOid - the oid for name to be checked
     *    in dot notation
     * @return true if name type is supported, false otherwise
     */
    boolean supportsName(Oid nameOid) {
    
        for (int i = 0; i < m_names.length; i++) {
            if (m_names[i].equals(nameOid))
                return (true);
        }
        return (false);
    }
    
    
    /**
     * Returns the names supported by this mech.
     */
    Oid[] getNames() {
        
        return (m_names);
    }
    

    /**
     * Returns the oid for this mechanism.
     */
    Oid getOid() {
    
        return (m_oid);
    }


    /**
     * Returns an instance of the class implementing the GSSCredSpi
     * interface for this mechanism.
     */
    GSSCredSpi getCredInstance() throws GSSException {
    
        try {
            if (_V29ED8BF == null) {
        
                //create the class object
                _V29ED8BF = Class.forName(_V901D6C2);
            }
        
            return ((GSSCredSpi)_V29ED8BF.newInstance());
            
        } catch (Exception e) {
            throw new GSSException(GSSException.UNAVAILABLE);
        }
    }
    

    /**
     * Returns an instance of the class implementing the C018FE95
     * interface for this mechanism.
     */
    C018FE95 _M4092FBA () throws GSSException {
    
        try {
            if (_V30FDA16 == null) {
        
                //create the class object
                _V30FDA16 = Class.forName(_V108CA91);
            }
        
            return ((C018FE95)_V30FDA16.newInstance());
            
        } catch (Exception e) {
            throw new GSSException(GSSException.UNAVAILABLE);
        }
    }


    /**
     * Returns an instance of the class implementing the JGSSNameSpi
     * interface for this mechanism.
     */
    GSSNameSpi getNameInstance() throws GSSException {
    
        try {
            if (_V80013BE == null) {
        
                //create the class object
                _V29ED8BF = Class.forName(_V2395ABD);
            }
        
            return ((GSSNameSpi)_V29ED8BF.newInstance());
            
        } catch (Exception e) {
            throw new GSSException(GSSException.UNAVAILABLE);
        }
    }


    /**
     * Returns the provider for this mechanism.
     *
     * @return p - provider for this mechanism.
     */
    Provider getProvider() {
    
        return (_V510CA83);
    }
    
            
    /**
     * Returns a provider value for the given key.
     * Both alias and oid is attempted to get the value.
     * @param provider to query
     * @param mechanism oid to be queried
     * @param key for the value to retrieve (JGSS.Mech. is prepended to it)
     * @return the value from the provider for the give mechanism and key
     */
    private static String _M73F1AC8 (Provider p, String _V0095DCA,
            String key) {
    
        String aStr;

        if ((aStr = p.getProperty("JGSS.Mech." + _V0095DCA + "." + key))
                != null) {
            return (aStr);
        }

        if ((aStr = oidStrToAlias(_V0095DCA)) == null)
            return null;

        return (p.getProperty("JGSS.Mech." + aStr + "." + key));
    }
    
    
    /**
     * Returns the alias name for the passed in mechanism oid.
     * @param mechanism oid in dot notation
     * @return alias name for the oid; null if not found
     */
    private static String oidStrToAlias(String _V0095DCA) {
    
        return M_oidAlias.getProperty(_V0095DCA);
    }
    
    
    /**
     * Returns the oid string for the given alias.
     * @return oid str in dot notation for the supplied alias name;
     *    null if it does net exist
     */
    private static String aliasToOidStr(String mechAlias) {
    
        return M_oidAlias.getProperty(mechAlias);
    }    


    /**
     * Updates the oid <--> alias mapping.
     * If the mapping already exists, it is *not* replaced.
     */
    private static synchronized void updateOidAlias(Provider p,
			String _V0095DCA) {
    
        //check if mapping already exists
        if (M_oidAlias.getProperty(_V0095DCA) != null)
            return;
            
        String aStr = p.getProperty("JGSS.Mech." + _V0095DCA + ".Alias");
        if (aStr != null) {
            M_oidAlias.put(_V0095DCA, aStr);
            M_oidAlias.put(aStr, _V0095DCA);
        }
    }
    
    
    /**
     * Queries if this is a JGSS provider implementing the
     * specified oid.
     */
    static boolean implementsMech(Provider p, String oid) {
    
        String [] mechs = getMechsForProvider(p);
        
        if (mechs == null)
            return (false);
            
        for (int i = 0; i < mechs.length; i++) {
            if (mechs[i].equals(oid))
                return (true);
        }
        
        return (false);
    }
    
    
    /**
     * Creates MechInfo objects for all the mechs this
     * provider supports.
     */
    static MechInfo[] getInfoForAllMechs(Provider p) {
    
        String mechsStr = p.getProperty("JGSS.Mechs");
        
        //does this provider even support JGSS ?
        if (mechsStr == null)
            return (null);
            
        StringTokenizer st = new StringTokenizer(mechsStr, ":");
        MechInfo[] mInfo = new MechInfo[st.countTokens()];
        for (int i = 0; i < mInfo.length; i++) {
            try {
                mInfo[i] = new MechInfo(p, st.nextToken());
            } catch (GSSException e) {
            }
        }
            
        return (mInfo);
    }
    
    
    /**
     * Queries the provider for all the mechanisms it offers
     * @return mechs - the mechanism oids this provider offers
     */
    static String[] getMechsForProvider(Provider p) {
    
        String aStr;            
        if ((aStr = p.getProperty("JGSS.Mechs")) != null ) {
        
            //get the supported mechs - there may be more then one
            StringTokenizer st = new StringTokenizer(aStr,":");
            String[] res = new String[st.countTokens()];
            for (int i = 0; i < res.length; i++)
                res[i] = st.nextToken();
            return (res);
        }
        return (null);
    }
    
    
    /**
     * Returns string with mechanism information.
     */
    public String toString() {

        StringBuffer aBuf = new StringBuffer(100);
        
        aBuf.append("Mechanism oid:\t").append(m_oid);
        aBuf.append("\nMechanism alias:\t").append(
				oidStrToAlias(m_oid.toString()));
        aBuf.append("\nMy provider: ");
        if (_V510CA83 == null)
            aBuf.append("null");
        else
            aBuf.append(_V510CA83.getInfo());
        aBuf.append("\nSupported Names:\t");
        
        for(int i = 0; i < m_names.length; i++)
            aBuf.append(m_names[i].toString()).append(" ");
        
        aBuf.append("\nName Class:\t").append(_V2395ABD);
        aBuf.append("\nCred Class:\t").append(_V901D6C2);
        aBuf.append("\nCtxt Class:\t").append(_V108CA91);

        return (aBuf.toString());
    }
    
    //instance variables
    private Oid m_oid;    //oid for this mech
    private Oid []m_names;    //oids for names supported by the mech
    private String _V2395ABD;    //name of the name class
    private String _V108CA91;    //name of the ctxt class
    private String _V901D6C2;    //name of the credential class
    private Provider _V510CA83;        //provider for this mech
    private Class _V80013BE;    //class implementing name
    private Class _V29ED8BF;    //class implementing cred
    private Class _V30FDA16;    //class implementing ctxt
    
    //class variables
    private static Properties M_oidAlias;    //oid <-> alias mapping
    
    static {
        M_oidAlias = new Properties();
    }
}

/*
 * Package private class that stores Oid -> MechInfo mapping for jgss
 * Only loaded mechanisms are in this table.
 */
class MechTable {

    /**
     * Hide constructor
     */
    private MechTable() { }


    /**
     * Returns mechanism info for the specified oid. null if
     * it does not exit.
     */
    static MechInfo getMechInfo(Oid oid) {
    
        return ((MechInfo)M_table.get(oid));
    }
    

    /**
     * Puts the mapping for a mechanism into the table.
     * If the mechanism is already in the table, it is not
     * updated. Returns boolean indicating if the mechanism
     * information was stored.
     */
    static boolean putMechInfo(MechInfo aMech) {
    
        if (M_table.containsKey(aMech.getOid()))
            return (false);
            
        M_table.put(aMech.getOid(), aMech);
        return (true);
    }
    

    //private table storing the mapping
    private static Hashtable M_table;
    
    static {
        M_table = new Hashtable(13);
    }
} //end of Class MechTable
