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
import org.icepdf.core.pobjects.HexStringObject;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A digital signature (PDF 1.3) may be used to authenticate the identity of a user and the document’s contents. It stores
 * information about the signer and the state of the document when it was signed. The signature may be purely mathematical,
 * such as a public/private-key encrypted document digest, or it may be a biometric form of identification, such as a
 * handwritten signature, fingerprint, or retinal scan. The specific form of authentication used shall be implemented by
 * a special software module called a signature handler.
 *
 * The SignatureDictionary store root data for signing and verifying signature in a document.
 */
public class SignatureDictionary extends Dictionary {

    /**
     * (Required; inheritable) The name of the preferred signature handler to use when validating this signature.
     * If the Prop_Build entry is not present, it shall be also the name of the signature handler that was used to
     * create the signature. If Prop_Build is present, it may be used to determine the name of the handler that
     * created the signature (which is typically the same as Filter but is not needed to be). A conforming reader may
     * substitute a different handler when verifying the signature, as long as it supports the specified SubFilter
     * format. Example signature handlers are Adobe.PPKLite, Entrust.PPKEF, CICI.SignIt, and VeriSign.PPKVS. The name
     * of the filter (i.e. signature handler) shall be identified in accordance with the rules defined in Annex E.
     */
    public static final Name FILTER_KEY = new Name("Filter");

    /**
     * (Optional) A name that describes the encoding of the signature value and key information in the signature dictionary.
     * A conforming reader may use any handler that supports this format to validate the signature.
     * <p/>
     * (PDF 1.6) The following values for public-key cryptographic signatures shall be used: adbe.x509.rsa_sha1,
     * adbe.pkcs7.detached, and adbe.pkcs7.sha1 (see 12.8.3, Signature Interoperability). Other values may be defined
     * by developers, and when used, shall be prefixed with the registered developer identification. All prefix names
     * shall be registered (see Annex E). The prefix adbe has been registered by Adobe Systems and the three subfilter
     * names listed above and defined in 12.8.3, Signature Interoperability may be used by any developer.
     */
    public static final Name SUB_FILTER_KEY = new Name("SubFilter");

    /**
     * (Required) The signature value. When ByteRange is present, the value shall be a hexadecimal string (see 7.3.4.3,
     * "Hexadecimal Strings") representing the value of the byte range digest.
     * <br />
     * For public-key signatures, Contents should be either a DER-encoded PKCS#1 binary data object or a DER-encoded
     * PKCS#7 binary data object.
     * Space for the Contents value must be allocated before the message digest is computed. (See 7.3.4, String Objects)
     */
    public static final Name CONTENTS_KEY = new Name("Contents");

    /**
     * (Required when SubFilter is adbe.x509.rsa_sha1) An array of byte strings that shall represent the X.509
     * certificate chain used when signing and verifying signatures that use public-key cryptography, or a byte
     * string if the chain has only one entry. The signing certificate shall appear first in the array; it shall
     * be used to verify the signature value in Contents, and the other certificateChain shall be used to verify the
     * authenticity of the signing certificate.
     * <br />
     * If SubFilter is adbe.pkcs7.detached or adbe.pkcs7.sha1, this entry shall not be used, and the certificate chain
     * shall be put in the PKCS#7 envelope in Contents.
     */
    public static final Name CERT_KEY = new Name("Cert");

    /**
     * (Required for all signatures that are part of a signature field and usage rights signatures referenced from the
     * UR3 entry in the permissions dictionary) An array of pairs of integers (starting byte offset, length in bytes)
     * that shall describe the exact byte range for the digest calculation. Multiple discontiguous byte ranges shall
     * be used to describe a digest that does not include the signature value (theContents entry) itself.
     */
    public static final Name BYTE_RANGE_KEY = new Name("ByteRange");

    /**
     * (Optional; PDF 1.5) An array of signature reference dictionaries (see Table 253).
     */
    public static final Name REFERENCE_KEY = new Name("Reference");

    /**
     * (Optional) An array of three integers that shall specify changes to the document that have been made between the
     * previous signature and this signature: in this order, the number of pages altered, the number of fields altered,
     * and the number of fields filled in.
     * <br />
     * The ordering of signatures shall be determined by the value of ByteRange. Since each signature results in an
     * incremental save, later signatures have a greater length value.
     */
    public static final Name CHANGES_KEY = new Name("Changes");

    /**
     * (Optional) The name of the person or authority signing the document. This value should be used only when it is
     * not possible to extract the name from the signature.
     * <br />
     * EXAMPLE 1 <br />
     * From the certificate of the signer.
     */
    public static final Name NAME_KEY = new Name("Name");

    /**
     * (Optional) The time of signing. Depending on the signature handler, this may be a normal unverified computer
     * time or a time generated in a verifiable way from a secure time server.
     * <br />
     * This value should be used only when the time of signing is not available in the signature.
     * <br />
     * EXAMPLE 2<br />
     * A  time stamp can be embedded in a PKCS#7 binary data object
     * (see 12.8.3.3, PKCS#7 Signatures as used in ISO 32000).
     */
    public static final Name M_KEY = new Name("M");

    /**
     * (Optional) The CPU host name or physical location of the signing.
     */
    public static final Name LOCATION_KEY = new Name("Location");

    /**
     * (Optional) The reason for the signing, such as (I agree).
     */
    public static final Name REASON_KEY = new Name("Reason");

    /**
     * (Optional) The version of the signature handler that was used to create the signature. (PDF 1.5) This entry
     * shall not be used, and the information shall be stored in the Prop_Build dictionary.
     */
    public static final Name R_KEY = new Name("R");

    /**
     * (Optional; PDF 1.5) The version of the signature dictionary format. It corresponds to the usage of the signature
     * dictionary in the context of the value of SubFilter. The value is 1 if the Reference dictionary shall be
     * considered critical to the validation of the signature.
     * <br />
     * Default value: 0.
     */
    public static final Name V_KEY = new Name("V");

    /**
     * (Optional; PDF 1.5) A dictionary that may be used by a signature handler to record information that captures the
     * state of the computer environment used for signing, such as the name of the handler used to create the signature,
     * software build date, version, and operating system.
     * <br />
     * The PDF Signature Build Dictionary Specification, provides implementation guidelines for the use of this dictionary.
     */
    public static final Name PROP_BUILD_KEY = new Name("Prop_Build");

    /**
     * (Optional; PDF 1.5) The number of seconds since the signer was last authenticated, used in claims of signature
     * repudiation. It should be omitted if the value is unknown.
     */
    public static final Name PROP_AUTH_TYPE_KEY = new Name("Prop_AuthType");

    /**
     * Optional; PDF 1.5) The method that shall be used to authenticate the signer, used in claims of signature
     * repudiation. Valid values shall be PIN, Password, and Fingerprint.
     */
    public static final Name PROP_AUTH_TIME_KEY = new Name("Prop_AuthTime");

    /**
     * (Optional) Information provided by the signer to enable a recipient to contact the signer to verify the signature.
     * <br />
     * EXAMPLE 3<br />
     * A phone number.
     */
    public static final Name CONTACT_INFO_KEY = new Name("ContactInfo");

    public SignatureDictionary(Library library, HashMap entries) {
        super(library, entries);
    }

    public Name getFilter() {
        return library.getName(entries, FILTER_KEY);
    }

    public void setFilter(Name filterName) {
        entries.put(FILTER_KEY, filterName);
    }

    public Name getSubFilter() {
        return library.getName(entries, SUB_FILTER_KEY);
    }

    public void setSubFilter(Name filterName) {
        entries.put(SUB_FILTER_KEY, filterName);
    }

    public HexStringObject getContents() {
        Object tmp = library.getObject(entries, CONTENTS_KEY);
        if (tmp instanceof HexStringObject) {
            return (HexStringObject) tmp;
        } else {
            return null;
        }
    }

    public void setContents(HexStringObject hexString) {
        entries.put(CONTENTS_KEY, hexString);
    }

    public boolean isCertArray() {
        return library.getObject(entries, CERT_KEY) instanceof List;
    }

    public boolean isCertString() {
        return library.getObject(entries, CERT_KEY) instanceof StringObject;
    }

    public ArrayList<StringObject> getCertArray() {
        Object tmp = library.getObject(entries, CERT_KEY);
        if (tmp instanceof List) {
            return (ArrayList) tmp;
        } else {
            return null;
        }
    }

    public StringObject getCertString() {
        Object tmp = library.getObject(entries, CERT_KEY);
        if (tmp instanceof StringObject) {
            return (StringObject) tmp;
        } else {
            return null;
        }
    }

    public void setCert(Object cert) {
        entries.put(CERT_KEY, cert);
    }

    public ArrayList<Integer> getByteRange() {
        Object tmp = library.getObject(entries, BYTE_RANGE_KEY);
        if (tmp instanceof List) {
            return (ArrayList) tmp;
        } else {
            return null;
        }
    }

    public void setByteRangeKey(ArrayList<Integer> range) {
        entries.put(BYTE_RANGE_KEY, range);
    }

    public ArrayList<SignatureReferenceDictionary> getReferences() {
        List<HashMap> tmp = library.getArray(entries, REFERENCE_KEY);
        if (tmp != null && tmp.size() > 0) {
            ArrayList<SignatureReferenceDictionary> references = new ArrayList<SignatureReferenceDictionary>(tmp.size());
            for (HashMap reference : tmp) {
                references.add(new SignatureReferenceDictionary(library, reference));
            }
            return references;
        } else {
            return null;
        }
    }

    public ArrayList<Integer> getChanges() {
        Object tmp = library.getArray(entries, CHANGES_KEY);
        if (tmp instanceof ArrayList) {
            return (ArrayList<Integer>) tmp;
        } else {
            return null;
        }
    }

    public String getName() {
        Object tmp = library.getObject(entries, NAME_KEY);
        if (tmp instanceof StringObject) {
            return Utils.convertStringObject(library, (StringObject) tmp);
        } else {
            return null;
        }
    }

    public String getDate() {
        return library.getString(entries, M_KEY);
    }

    public String getLocation() {
        Object tmp = library.getObject(entries, LOCATION_KEY);
        if (tmp instanceof StringObject) {
            return Utils.convertStringObject(library, (StringObject) tmp);
        } else {
            return null;
        }
    }

    public String getReason() {
        Object tmp = library.getObject(entries, REASON_KEY);
        if (tmp instanceof StringObject) {
            return Utils.convertStringObject(library, (StringObject) tmp);
        } else {
            return null;
        }
    }

    public String getContactInfo() {
        Object tmp = library.getObject(entries, CONTACT_INFO_KEY);
        if (tmp instanceof StringObject) {
            return Utils.convertStringObject(library, (StringObject) tmp);
        } else {
            return null;
        }
    }

    public int getHandlerVersion() {
        return library.getInt(entries, R_KEY);
    }

    public int getDictionaryVersion() {
        return library.getInt(entries, V_KEY);
    }

    public HashMap getBuildDictionary() {
        return library.getDictionary(entries, PROP_BUILD_KEY);
    }

    public int getAuthTime() {
        return library.getInt(entries, PROP_AUTH_TIME_KEY);
    }

    public Name getAuthType() {
        return library.getName(entries, PROP_AUTH_TYPE_KEY);
    }

}
