/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;
import java.util.List;

/**
 * A certificate seed value dictionary (see Table 235) containing information about the characteristics of the
 * certificate that shall be used when signing.
 */
public class CertSeedValueDictionary extends Dictionary {

    public static final Name SV_CERT_TYPE_VALUE = new Name("SVCert");

    /**
     * (Optional) A set of bit flags specifying the interpretation of specific entries in this dictionary.
     * A value of 1 for the flag means that a signer shall be required to use only the specified values for the entry.
     * A value of 0 means that other values are permissible.
     * <p/>
     * Bit positions are 1 (Subject); 2 (Issuer); 3 (OID); 4 (SubjectDN); 5 (Reserved); 6 (KeyUsage); 7 (URL).
     * <p/>
     * Default value: 0.
     */
    public static final Name Ff_KEY = new Name("Ff");

    // Bit 1 (Subject)
    private static final int Ff_SUBJECT_BIT = 0x1;
    // Bit 2 (Issuer)
    private static final int Ff_ISSUER_BIT = 0x2;
    // Bit 3 (OID)
    private static final int Ff_OID_BIT = 0x4;
    // Bit 4 (SubjectDN)
    private static final int Ff_SUBJECT_DN_BIT = 0x8;
    // Bit 5 (Reserved)
    private static final int Ff_RESERVED_BIT = 0x10;
    // Bit 6 (KeyUsage); and
    private static final int Ff_KEY_USAGE_BIT = 0x20;
    // Bit 7 (URL).
    private static final int Ff_URL_BIT = 0x40;

    /**
     * (Optional) An array of byte strings containing DER-encoded X.509v3 certificateChain that are acceptable for
     * signing. X.509v3 certificateChain are described in RFC 3280, Internet X.509 Public Key Infrastructure, Certificate
     * and Certificate Revocation List (CRL) Profile (see the Bibliography). The value of the corresponding flag in the
     * Ff entry indicates whether this is a required constraint.
     */
    public static final Name SUBJECT_KEY = new Name("Subject");

    /**
     * (Optional; PDF 1.7) An array of dictionaries, each specifying a Subject Distinguished Name (DN) that shall be
     * present within the certificate for it to be acceptable for signing. The certificate ultimately used for the
     * digital signature shall contain all the attributes specified in each of the dictionaries in this array.
     * (PDF keys and values are mapped to certificate attributes and values.) The certificate is not constrained to use
     * only attribute entries from these dictionaries but may contain additional attributes.The Subject Distinguished
     * Name is described in RFC 3280 (see the Bibliography). The key can be any legal attribute identifier (OID).
     * Attribute names shall contain characters in the set a-z A-Z 0-9 and PERIOD.
     * <p/>
     * Certificate attribute names are used as key names in the dictionaries in this array. Values of the attributes are
     * used as values of the keys. Values shall be text strings.
     * <p/>
     * The value of the corresponding flag in the Ff entry indicates whether this entry is a required constraint.
     */
    public static final Name SUBJECT_DN_KEY = new Name("SubjectDN");

    /**
     * (Optional; PDF 1.7) An array of ASCII strings, where each string specifies an acceptable key-usage extension that
     * shall be present in the signing certificate. Multiple strings specify a range of acceptable key-usage extensions.
     * The key-usage extension is described in RFC 3280.
     * <p/>
     * Each character in a string represents a key-usage type, where the order of the characters indicates the key-usage
     * extension it represents. The first through ninth characters in the string, from left to right, represent the
     * required value for the following key-usage extensions:
     * <ul>
     * <li>1 digitalSignature</li>
     * <li>2 non-Repudiation</li>
     * <li>3 keyEncipherment</li>
     * <li>4 dataEncipherment</li>
     * <li>5 keyAgreement</li>
     * <li>6 keyCertSign</li>
     * <li>7 cRLSign</li>
     * <li>8 encipherOnly</li>
     * <li>9 decipherOnly</li>
     * </ul>
     * Any additional characters shall be ignored. Any missing characters or characters that are not one of the following
     * values, shall be treated as X. The following character values shall be supported:
     * <ul>
     * <li>0 Corresponding key-usage shall not be set.</li>
     * <li>1 Corresponding key-usage shall be set.</li>
     * <li>X State of the corresponding key-usage does not matter.</li>
     * </ul>
     * EXAMPLE 1<br />
     * The string values 1 and 1XXXXXXXX represent settings where the key-usage type digitalSignature is set
     * and the state of all other key-usage types do not matter.
     * The value of the corresponding flag in the Ff entry indicates whether this is a required constraint.
     */
    public static final Name KEY_USAGE_KEY = new Name("KeyUsage");

    /**
     * (Optional) An array of byte strings containing DER-encoded X.509v3 certificateChain of acceptable issuers. If the
     * signer's certificate refers to any of the specified issuers (either directly or indirectly), the certificate shall
     * be considered acceptable for signing. The value of the corresponding flag in the Ff entry indicates whether this
     * is a required constraint.
     * <p/>
     * This array may contain self-signed certificateChain.
     */
    public static final Name ISSUER_KEY = new Name("Issuer");

    /**
     * (Optional) An array of byte strings that contain Object Identifiers (OIDs) of the certificate policies that shall
     * be present in the signing certificate.
     * <p/>
     * EXAMPLE 2<br />
     * An example of such a string is: (2.16.840.1.113733.1.7.1.1).
     * <p/>
     * This field shall only be used if the value of Issuer is not empty. The certificate policies extension is
     * described in RFC 3280 (see the Bibliography). The value of the corresponding flag in the Ff entry indicates
     * whether this is a required constraint.
     */
    public static final Name OID_KEY = new Name("OID");

    /**
     * (Optional) A URL, the use for which shall be defined by the URLType entry.
     */
    public static final Name URL_KEY = new Name("URL");

    /**
     * (Optional; PDF 1.7) A name indicating the usage of the URL entry. There are standard uses and there can be
     * implementation-specific uses for this URL. The following value specifies a valid standard usage:
     * <p/>
     * Browser - The URL references content that shall be displayed in a web browser to allow enrolling for a new
     * credential if a matching credential is not found. The Ff attribute's URL bit shall be ignored for this usage.
     * <p/>
     * Third parties may extend the use of this attribute with their own attribute values, which shall conform to the
     * guidelines described in Annex E.
     * <p/>
     * The default value is Browser.
     */
    public static final Name URL_TYPE_KEY = new Name("URLType");

    private int flags;

    public CertSeedValueDictionary(Library library, HashMap entries) {
        super(library, entries);

        flags = library.getInt(entries, Ff_KEY);
    }

    public List getSubject() {
        Object tmp = library.getArray(entries, SUBJECT_KEY);
        if (tmp != null) {
            return (List) tmp;
        } else {
            return null;
        }
    }

    public List<HashMap> getSubjectDn() {
        Object tmp = library.getArray(entries, SUBJECT_DN_KEY);
        if (tmp != null) {
            return (List) tmp;
        } else {
            return null;
        }
    }

    public List<String> getKeyUsage() {
        Object tmp = library.getArray(entries, KEY_USAGE_KEY);
        if (tmp != null) {
            return (List) tmp;
        } else {
            return null;
        }
    }

    public List getIssuer() {
        Object tmp = library.getArray(entries, ISSUER_KEY);
        if (tmp != null) {
            return (List) tmp;
        } else {
            return null;
        }
    }

    public List getIOD() {
        Object tmp = library.getArray(entries, OID_KEY);
        if (tmp != null) {
            return (List) tmp;
        } else {
            return null;
        }
    }

    public String getUrl() {
        return library.getString(entries, URL_KEY);
    }

    public Name getUrlType() {
        return library.getName(entries, URL_TYPE_KEY);
    }

    public boolean isSubject() {
        return ((flags & Ff_SUBJECT_BIT)
                == Ff_SUBJECT_BIT);
    }

    public boolean isIssuer() {
        return ((flags & Ff_ISSUER_BIT)
                == Ff_ISSUER_BIT);
    }

    public boolean isOid() {
        return ((flags & Ff_OID_BIT)
                == Ff_OID_BIT);
    }

    public boolean isSubjectDn() {
        return ((flags & Ff_SUBJECT_DN_BIT)
                == Ff_SUBJECT_DN_BIT);
    }

    public boolean isReserved() {
        return ((flags & Ff_RESERVED_BIT)
                == Ff_RESERVED_BIT);
    }

    public boolean isKeyUsage() {
        return ((flags & Ff_KEY_USAGE_BIT)
                == Ff_KEY_USAGE_BIT);
    }

    public boolean isUrl() {
        return ((flags & Ff_URL_BIT)
                == Ff_URL_BIT);
    }
}
