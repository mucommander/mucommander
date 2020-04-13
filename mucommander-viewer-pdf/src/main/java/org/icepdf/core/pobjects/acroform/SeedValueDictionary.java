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
import org.icepdf.core.pobjects.Names;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;

import java.util.HashMap;
import java.util.List;

/**
 * The value of the SV entry in the field dictionary is a seed value dictionary whose entries (see Table 234) provide
 * constraining information that shall be used at the time the signature is applied. Its Ff entry specifies whether
 * the other entries in the dictionary shall be honoured or whether they are merely recommendations.
 * <p/>
 * The seed value dictionary may include seed values for private entries belonging to multiple handlers. A given handler
 * shall use only those entries that are pertinent to itself and ignore the others.
 *
 */
public class SeedValueDictionary extends Dictionary {

    /**
     * The type of PDF object that this dictionary describes; if present, shall be SV for a seed value dictionary.
     */
    public static final Name LOCK_TYPE_VALUE = new Name("SV");

    /**
     * A set of bit flags specifying the interpretation of specific entries in this dictionary. A value of 1 for the
     * flag indicates that the associated entry is a required constraint. A value of 0 indicates that the associated
     * entry is an optional constraint.
     * <p/>
     * Bit positions are 1 (Filter); 2 (SubFilter); 3 (V); 4 (Reasons); 5 (LegalAttestation); 6 (AddRevInfo); and
     * 7 (DigestMethod). Default value: 0.
     */
    public static final Name Ff_KEY = new Name("Ff");

    // Bit 1 (Filter)
    private static final int Ff_FILTER_BIT = 0x1;
    // Bit 2 (SubFilter)
    private static final int Ff_SUB_FILTER_BIT = 0x2;
    // Bit 3 (V)
    private static final int Ff_V_BIT = 0x4;
    // Bit 4 (Reasons)
    private static final int Ff_REASONS_BIT = 0x8;
    // Bit 5 (LegalAttestation)
    private static final int Ff_LEGAL_ATTESTATION_BIT = 0x10;
    // Bit 6 (AddRevInfo); and
    private static final int Ff_ADD_REV_INFO_BIT = 0x20;
    // Bit 7 (DigestMethod). Default value: 0.
    private static final int Ff_DIGEST_METHOD_BIT = 0x40;

    /**
     * (Optional) The signature handler that shall be used to sign the signature field. Beginning with PDF 1.7, if
     * Filter is specified and the Ff entry indicates this entry is a required constraint, then the signature handler
     * specified by this entry shall be used when signing; otherwise, signing shall not take place. If Ff indicates
     * that this is an optional constraint, this handler may be used if it is available. If it is not available, a
     * different handler may be used instead.
     */
    public static final Name FILTER_KEY = new Name("Filter");

    /**
     * (Optional) An array of names indicating encodings to use when signing. The first name in the array that matches
     * an encoding supported by the signature handler shall be the encoding that is actually used for signing.
     * If SubFilter is specified and the Ff entry indicates that this entry is a required constraint, then the
     * first matching encodings shall be used when signing; otherwise, signing shall not take place. If Ff indicates
     * that this is an optional constraint, then the first matching encoding shall be used if it is available. If
     * none is available, a different encoding may be used.
     */
    public static final Name SUB_FILTER_KEY = new Name("SubFilter");

    /**
     * (Optional; PDF 1.7) An array of names indicating acceptable digest algorithms to use while signing. The value
     * shall be one of SHA1, SHA256, SHA384, SHA512 and RIPEMD160. The default value is implementation-specific.
     * <p/>
     * This property is only applicable if the digital credential signing contains RSA public/private keys. If it
     * contains DSA public/ private keys, the digest algorithm is always SHA1 and this attribute shall be ignored.
     */
    public static final Name DIGEST_METHOD_KEY = new Name("DigestMethod");

    /**
     * (Optional) The minimum required capability of the signature field seed value dictionary parser. A value of 1
     * specifies that the parser shall be able to recognize all seed value dictionary entries in a PDF 1.5 file.
     * A value of 2 specifies that it shall be able to recognize all seed value dictionary entries specified.
     * <p/>
     * The Ff entry indicates whether this shall be treated as a required constraint.
     */
    public static final Name V_KEY = new Name("V");

    /**
     * (Optional) A certificate seed value dictionary (see Table 235) containing information about the characteristics
     * of the certificate that shall be used when signing.
     */
    public static final Name CERT_KEY = new Name("Cert");

    /**
     * (Optional) An array of text strings that specifying possible reasons for signing a document. If specified, the
     * reasons supplied in this entry replace those used by conforming products.
     * <p/>
     * If the Reasons array is provided and the Ff entry indicates that Reasons is a required constraint, one of the
     * reasons in the array shall be used for the signature dictionary; otherwise, signing shall not take place. If
     * the Ff entry indicates Reasons is an optional constraint, one of the reasons in the array may be chosen or a
     * custom reason can be provided.
     * <p/>
     * If the Reasons array is omitted or contains a single entry with the value PERIOD (2Eh) and the Ff entry
     * indicates that Reasons is a required constraint, the Reason entry shall be omitted from the signature
     * dictionary (see Table 252).
     */
    public static final Name REASONS_KEY = new Name("Reasons");

    /**
     * (Optional; PDF 1.6) A dictionary containing a single entry whose key is P and whose value is an integer
     * between 0 and 3. A value of 0 defines the signature as an author signature (see 12.8, Digital Signatures).
     * The values 1 through 3 shall be used for certification signatures and correspond to the value of P in a DocMDP
     * transform parameters dictionary (see Table 254).
     * <p/>
     * If this MDP key is not present or the MDP dictionary does not contain a P entry, no rules shall be defined
     * regarding the type of signature or its permissions.
     */
    public static final Name MDP_KEY = new Name("MDP");

    /**
     * (Optional; PDF 1.6) A time stamp dictionary containing two entries:
     * <ul>
     * <li>URL An ASCII string specifying the URL of a time-stamping server, providing a time stamp that is compliant
     * ith RFC 3161, Internet X.509 Public Key Infrastructure Time-Stamp Protocol (see the Bibliography).</li>
     * <li>Ff An integer whose value is 1 (the signature shall have a time stamp) or 0 (the signature need not have a
     * time stamp). Default value: 0.</li>
     * </ul>
     * NOTEPlease see 12.8.3.3, "PKCS#7 Signatures as used in ISO 32000" for more details about hashing.
     */
    public static final Name TIME_STAMP_KEY = new Name("TimeStamp");

    /**
     * (Optional; PDF 1.6) An array of text strings specifying possible legal attestations
     * (see 12.8.5, Legal Content Attestations). The value of the corresponding flag in the Ff entry indicates
     * whether this is a required constraint.
     */
    public static final Name LEGAL_ATTESTATION_KEY = new Name("LegalAttestation");

    /**
     * (Optional; PDF 1.7) A flag indicating whether revocation checking shall be carried out. If AddRevInfo is true,
     * the conforming processor shall perform the following additional tasks when signing the signature field:
     * <p/>
     * Perform revocation checking of the certificate (and the corresponding issuing certificateChain) used to sign.
     * <p/>
     * Include the revocation information within the signature value.
     * <p/>
     * Three SubFilter values have been defined for ISO 32000. For those values the AddRevInfo value shall be true
     * only if SubFilter is adbe.pkcs7.detached or adbe.pkcs7.sha1. If SubFilter is x509.rsa_sha1, this entry shall
     * be omitted or set to false. Additional SubFilters may be defined that also use AddRevInfo values.
     * <p/>
     * If AddRevInfo is true and the Ff entry indicates this is a required constraint, then the preceding tasks shall
     * be performed. If they cannot be performed, then signing shall fail.
     * Default value: false
     * <p/>
     * NOTE 1Revocation information is carried in the signature data as specified by PCKS#7. See 12.8.3.3,
     * "PKCS#7 Signatures as used in ISO 32000".
     * <p/>
     * NOTE 2The trust anchors used are determined by the signature handlers for both creation and validation.
     */
    public static final Name ADD_REV_INFO_KEY = new Name("AddRevInfo");


    private int flags;

    public SeedValueDictionary(Library library, HashMap entries) {
        super(library, entries);

        flags = library.getInt(entries, Ff_KEY);
    }

    public Name getFilterKey() {
        Object tmp = library.getObject(entries, FILTER_KEY);
        if (tmp instanceof Name) {
            return (Name) tmp;
        } else {
            return null;
        }
    }

    /**
     * @return filter array or null.
     * @see #FILTER_KEY
     */
    public List<Names> getSubFilter() {
        List tmp = library.getArray(entries, FILTER_KEY);
        if (tmp != null) {
            return tmp;
        }
        return null;
    }

    /**
     * @return digest methods or null.
     * @see #DIGEST_METHOD_KEY
     */
    public List<Names> getDigestMethod() {
        List tmp = library.getArray(entries, FILTER_KEY);
        if (tmp != null) {
            return tmp;
        }
        return null;
    }

    /**
     * @return minimum capability
     * @see #V_KEY
     */
    public double getV() {
        return library.getFloat(entries, V_KEY);
    }

    /**
     * @return Get the certificate see value dictionary,  can be null.
     * @see #CERT_KEY
     */
    public CertSeedValueDictionary getCert() {
        Object tmp = library.getObject(entries, CERT_KEY);
        if (tmp instanceof HashMap) {
            return new CertSeedValueDictionary(library, (HashMap) tmp);
        } else {
            return null;
        }
    }

    /**
     * @return an array of text strings the specify possible reasons for singing.
     * @see #REASONS_KEY
     */
    public List<StringObject> getReasons() {
        List tmp = library.getArray(entries, REASONS_KEY);
        if (tmp != null) {
            return tmp;
        }
        return null;
    }

    /**
     * todo consider class for dictionary def.
     *
     * @return
     */
    public HashMap getMDP() {
        Object tmp = library.getObject(entries, MDP_KEY);
        if (tmp instanceof HashMap) {
            return (HashMap) tmp;
        } else {
            return null;
        }
    }

    /**
     * todo consider class for dictionary def.
     *
     * @return
     */
    public HashMap getTimeStamp() {
        Object tmp = library.getObject(entries, TIME_STAMP_KEY);
        if (tmp instanceof HashMap) {
            return (HashMap) tmp;
        } else {
            return null;
        }
    }

    /**
     * @return an array of text strings the specify possible legal attestations.
     * @see #LEGAL_ATTESTATION_KEY
     */
    public List<StringObject> getLegalAttestation() {
        List tmp = library.getArray(entries, LEGAL_ATTESTATION_KEY);
        if (tmp != null) {
            return tmp;
        }
        return null;
    }

    /**
     * @return Flag indicating whether revocation checking shall be carried out.
     * @see #ADD_REV_INFO_KEY
     */
    public boolean getAddRevInfo() {
        return library.getBoolean(entries, ADD_REV_INFO_KEY);
    }

    public boolean isFilter() {
        return ((flags & Ff_FILTER_BIT)
                == Ff_FILTER_BIT);
    }

    public boolean isSubFilter() {
        return ((flags & Ff_SUB_FILTER_BIT)
                == Ff_SUB_FILTER_BIT);
    }

    public boolean isV() {
        return ((flags & Ff_V_BIT)
                == Ff_V_BIT);
    }

    public boolean isReasons() {
        return ((flags & Ff_REASONS_BIT)
                == Ff_REASONS_BIT);
    }

    public boolean isLegalAttenstation() {
        return ((flags & Ff_LEGAL_ATTESTATION_BIT)
                == Ff_LEGAL_ATTESTATION_BIT);
    }

    public boolean isAddRevInfo() {
        return ((flags & Ff_ADD_REV_INFO_BIT)
                == Ff_ADD_REV_INFO_BIT);
    }

    public boolean isDigestMethod() {
        return ((flags & Ff_DIGEST_METHOD_BIT)
                == Ff_DIGEST_METHOD_BIT);
    }
}
