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
package org.icepdf.core.pobjects.security;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.util.Library;

import java.util.HashMap;
import java.util.List;

/**
 * <p>The EncryptionDictionary class is used to hold values needed by the Standard
 * Security Handler, Public Key Handlers and Crypt filters.  This PDF object
 * is found via a document's Trailer object, but only when the Trailer has an
 * encrypted named reference.</p>
 * <p/>
 * <p>The dictionary is composed of combinations of the following entries defined
 * by the different encryption types.  ICEpdf currently only supports the
 * Standard Security Handler.</p>
 * <p/>
 * <p/>
 * <table border="1" cellpadding="1" cellspacing="1" >
 * <tr>
 * <td colspan="3" ><b>Common to all Encryption Dictionaries</b></td>
 * </tr>
 * <tr>
 * <td><b>Key</b></td>
 * <td><b>Type</b></td>
 * <td><b>Value</b></td>
 * </tr>
 * <tr>
 * <td valign="top" >Filter</td>
 * <td valign="top" >name</td>
 * <td>(Required) The name of the preferred security handler for this
 * document; typically it is the name of the security handler that was
 * used to encrypt the document. If SubFilter is not present, only this
 * security handler should be used when opening the document. If it is
 * present, viewer applications are free to use any security handler
 * that implements the format specified by SubFilter.</td>
 * </tr>
 * <tr>
 * <td valign="top" >SubFilter</td>
 * <td valign="top" >name</td>
 * <td>(Optional; PDF 1.3) A name that completely specifies the format and
 * interpretation of the contents of the encryption dictionary. It is
 * needed in order to allow security handlers other than the one
 * specified by Filter to decrypt the document. If it is absent, other
 * security handlers will not be able to decrypt the document.</td>
 * </tr>
 * <tr>
 * <td valign="top" >V</td>
 * <td valign="top" >number</td>
 * <td>(Optional but strongly recommended) A code specifying the algorithm
 * to be used in encrypting and decrypting the document:
 * <ul>
 * <li>0 - An algorithm that is undocumented and no longer
 * supported, and whose use is strongly discouraged.</li>
 * <li>1 - Algorithm 3.1, with an encryption key length
 * of 40 bits; see below.</li>
 * <li>2 - (PDF 1.4) Algorithm 3.1, but allowing
 * encryption key lengths greater than 40 bits.</li>
 * <li>3 - (PDF 1.4) An unpublished algorithm allowing encryption
 * key lengths ranging from 40 to 128 bits. (This algorithm
 * is unpublished as an export requirement of the U.S.
 * Department of Commerce.)</li>
 * <li>(PDF 1.5) The security handler defines the use of encryption
 * and decryption in the document, using the rules specified by
 * the CF, StmF, and StrF entries.</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td valign="top" >Length</td>
 * <td valign="top" >integer</td>
 * <td>(Optional; PDF 1.4; only if V is 2 or 3) The length of the
 * encryption key, in bits. The value must be a multiple of 8, in the
 * range 40 to 128. Default value: 40.</td>
 * </tr>
 * <tr>
 * <td valign="top" >CF</td>
 * <td valign="top" >dictionary</td>
 * <td>(Optional; meaningful only when the value of V is 4; PDF 1.5) A
 * dictionary whose keys are crypt filter names and whose values are
 * the corresponding crypt filter dictionaries.</td>
 * </tr>
 * <tr>
 * <td valign="top" >StmF</td>
 * <td valign="top" >name</td>
 * <td>(Optional; meaningful only when the value of V is 4; PDF 1.5) The
 * name of the crypt filter that is used by default when encrypting
 * streams; it must correspond to a key in the CF dictionary or a
 * standard crypt filter name. All streams in the document, except for
 * cross-reference streams or those that have a crypt entry in their
 * Filter array are decrypted by the security handler, using this
 * crypt filter.</td>
 * </tr>
 * <tr>
 * <td valign="top" >StrF</td>
 * <td valign="top" >name</td>
 * <td>(Optional; meaningful only when the value of V is 4; PDF 1.5) The
 * name of the crypt filter that is used when decrypting all strings
 * in the document; it must correspond to a key in the CF dictionary
 * or a standard crypt filter name.</td>
 * </tr>
 * </table>
 * <p/>
 * <p>The dictionary composes of the following values that can be returned via
 * their named mehtod or by a generic getValue method if the key's name is known.
 * The values of the O and U entries in this dictionary are used to determine
 * whether a password entered when the document is opened is the correct owner
 * password, user password, or neither.</p>
 * <p/>
 * <table border="1" cellpadding="1" cellspacing="1" >
 * <tr>
 * <td colspan="3" ><b>Standard Encryption Dictionary Entries</b> </td>
 * </tr>
 * <tr>
 * <td><b>Key</b></td>
 * <td><b>Type</b></td>
 * <td><b>Value</b></td>
 * </tr>
 * <tr>
 * <td valign="top" >R</td>
 * <td valign="top" >number</td>
 * <td>
 * <p>(Required) A number specifying which revision of the standard
 * security handler should be used to interpret this dictionary. The
 * revision number should be:</p>
 * <ul>
 * <li>2 if the document is encrypted with a V value less than 2
 * and does not have any of the access permissions set (via the
 * P entry, below) that are designated "Revision3"</li>
 * <li>3 if the document is encrypted with a V value of 2 or 3, or
 * has any "Revision 3" access permissions set.</li>
 * <li>4 if the document is encrypted with a V value of 4.</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td valign="top" >O</td>
 * <td valign="top" >String</td>
 * <td>(Required) A 32-byte string, based on both the owner and user
 * passwords, that is used in computing the encryption key and in
 * determining whether a valid owner password was entered.</td>
 * </tr>
 * <tr>
 * <td valign="top" >U</td>
 * <td valign="top" >String</td>
 * <td>U string (Required) A 32-byte string, based on the user password,
 * that is used in determining whether to prompt the user for a
 * password and, if so, whether a valid user or owner password was
 * entered.</td>
 * <tr>
 * <td valign="top" >P</td>
 * <td valign="top" >Integer</td>
 * <td>(Required) A set of flags specifying which operations are permitted
 * when the document is opened with user access.
 * </td>
 * </tr>
 * </table>
 * <p/>
 * <p>Encryption dictionaries for public-key security handlers contain the
 * common entries shown above. In addition,  they may contain the entries
 * shown below.</p>
 * <p/>
 * <table border="1" cellpadding="1" cellspacing="1" >
 * <tr>
 * <td colspan="3" ><b>Additional public-key Dictionary Entries</b> </td>
 * </tr>
 * <tr>
 * <td><b>Key</b></td>
 * <td><b>Type</b></td>
 * <td><b>Value</b></td>
 * </tr>
 * <tr>
 * <td valign="top" >Recipients</td>
 * <td valign="top" >array</td>
 * <td>(Required when SubFilter is adbe.pkcs7.s3 or adbe.pkcs7.s4; PDF 1.3)
 * An array of strings, where each string is a PKCS#7 object listing
 * recipients that have been granted equal access rights to the document.
 * The data contained in the PKCS#7 object includes both a cryptographic
 * key that is used to decrypt the encrypted data and the access
 * permissions that apply to the recipient list. There should be only
 * one object per unique set of access permissions; if a recipient
 * appears in more than one list, the permissions used will be those
 * found in the first matching list.<br />
 * <b>Note:</b><br />
 * When SubFilter is adbe.pkcs7.s5, recipient lists are specified in
 * the crypt filter dictionary.</td>
 * </tr>
 * </table>
 * <p/>
 * <p>Encryption dictionaries for crypt filter security handlers contain the
 * common entries shown above. In addition,  they may contain the entries
 * shown below</p>
 * <p/>
 * <table border="1" cellpadding="1" cellspacing="1" >
 * <tr>
 * <td colspan="3" ><b> Standard Encryption Dictionary Entries</b> </td>
 * </tr>
 * <tr>
 * <td><b>Key</b></td>
 * <td><b>Type</b></td>
 * <td><b>Value</b></td>
 * </tr>
 * <tr>
 * <td valign="top" >Type</td>
 * <td valign="top" >name</td>
 * <td>(Optional) If present, must be CryptFilter for a crypt filter
 * dictionary.</td>
 * </tr>
 * <tr>
 * <td valign="top" >CFM</td>
 * <td valign="top" >name</td>
 * <td>(Optional) The method used, if any, by the viewer application to
 * decrypt data. In PDF 1.5, the following values are supported:
 * <ul>
 * <li>None: (default)the viewer application does not decrypt data,
 * but directs the input stream to the security handler for
 * decryption.</li>
 * <li>V2: the viewer application asks the security handler for the
 * decryption key and implicitly decrypts data using Algorithm
 * 3.1. A viewer application may ask once for this decryption
 * key, then cache the key for subsequent use for streams that
 * use the same crypt filter; therefore, there must be a one-to-one
 * relationship between a crypt filter name and the corresponding
 * decryption key.</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td valign="top" >Length</td>
 * <td valign="top" >integer</td>
 * <td>(Optional) When the value of CFM is V2, this entry is used to
 * indicate the bit length of the decryption key. It must be a multiple
 * of 8 in the range of 40 to 128. Default value: 128. When the value
 * of CFM is None, security handlers can define their own use of this
 * entry, but are encouraged to follow the usage conventions defined
 * for V2.</td>
 * </tr>
 * <tr>
 * <td valign="top" >AuthEvent</td>
 * <td valign="top" >name</td>
 * <td>
 * (Optional) The event to be used to trigger the authorization that is
 * required to access decryption keys used by this filter. If
 * authorization fails, the event should fail. Acceptable values are:
 * <ul>
 * <li>DocOpen: (default) authorization is required when a document
 * is opened.</li>
 * <li>EFOpen: authorization is required when about to access embedded
 * files.</li>
 * </ul>
 * If this filter is used as the value of StrF or StmF in the encryption
 * dictionary, the viewer application should ignore this key and behave
 * as if the value is DocOpen.
 * </td>
 * </tr>
 * </table>
 *
 * @since 1.1
 */
public class EncryptionDictionary extends Dictionary {

    public static final Name FILTER_KEY = new Name("Filter");
    public static final Name SUB_FILTER_KEY = new Name("SubFilter");
    public static final Name V_KEY = new Name("V");
    public static final Name LENGTH_KEY = new Name("Length");
    public static final Name R_KEY = new Name("R");
    public static final Name O_KEY = new Name("O");
    public static final Name U_KEY = new Name("U");
    public static final Name P_KEY = new Name("P");
    public static final Name CF_KEY = new Name("CF");
    public static final Name STMF_KEY = new Name("StmF");
    public static final Name STRF_KEY = new Name("StrF");
    public static final Name EEF_KEY = new Name("EEF");
    public static final Name OE_KEY = new Name("OE");
    public static final Name UE_KEY = new Name("UE");
    public static final Name PERMS_KEY = new Name("Perms");
    public static final Name ENCRYPT_METADATA_KEY = new Name("EncryptMetadata");

    // File ID,  generated when document is created, first index used by
    // encryption algorithms
    private List fileID = null;

    private CryptFilter cryptFilter;

    // Revision 5 authentication holders as they are passwords
    // are validate when the key is calculated.
    private boolean isAuthenticatedUserPassword;
    private boolean isAuthenticatedOwnerPassword;

    /**
     * Creates a new Encryption Dictionary object.
     *
     * @param lib                  library dictionary of all objects in document.
     * @param encryptionDictionary dictionary of all values taken from encrypt key
     *                             in the documents Trailer reference.
     * @param fileID               Vector containing the two file ID values originally
     *                             parsed from the Trailer reference.
     */
    public EncryptionDictionary(Library lib, HashMap<Object, Object> encryptionDictionary, List fileID) {
        super(lib, encryptionDictionary);
        this.entries = encryptionDictionary;
        this.fileID = fileID;
    }

    /**
     * Gets the document's File ID.
     *
     * @return vector containing two values that represent the file ID
     */
    public List getFileID() {
        return fileID;
    }

/**
 * Entries common to all encryption dictionaries
 */

    /**
     * Gets the preferred security handler name.
     *
     * @return handler name.
     */
    public Name getPreferredSecurityHandlerName() {
        return library.getName(entries, FILTER_KEY);
    }

    /**
     * Gets the preferred security handler sub-name.
     *
     * @return handler sub-name.
     */
    public Name getPreferredSecurityHandlerSubName() {
        return library.getName(entries, SUB_FILTER_KEY);
    }

    /**
     * Gets a code specifying the algorithm to be used in encrypting and
     * decrypting the document:
     * <ul>
     * <li>0 An algorithm that is undocumented. This value shall not be used.</li>
     * <li>1 "Algorithm 1: Encryption of data using the RC4 or AES algorithms"
     * in 7.6.2, "General Encryption Algorithm," with an encryption key length
     * of 40 bits; see below.</li>
     * <li>2 (PDF 1.4) "Algorithm 1: Encryption of data using the RC4 or AES
     * algorithms"in 7.6.2, "General Encryption Algorithm," but permitting
     * encryption key lengths greater than 40 bits.</li>
     * <li>3(PDF 1.4) An unpublished algorithm that permits encryption key
     * lengths ranging from 40 to 128 bits. This value shall not appear in a
     * conforming PDF file.</li>
     * <li>4(PDF 1.5) The security handler defines the use of encryption and
     * decryption in the document, using the rules specified by the CF, StmF,
     * and StrF entries. The default value if this entry is omitted shall be
     * 0, but when present should be a value of 1 or greater.</li>
     * </ul>
     *
     * @return encryption version.
     */
    public int getVersion() {
        return library.getInt(entries, V_KEY);
    }

    /**
     * Gets the length of the encryption key, in bits.
     *
     * @return length of encryption key.
     */
    public int getKeyLength() {
        int length = 40;
        int len = library.getInt(entries, LENGTH_KEY);
        if (len != 0) {
            length = len;
        }
        return length;
    }

/**
 * Entries added for standard encryption dictionaries
 */

    /**
     * Gets the revision number of the standard security handler.
     *
     * @return revision number.
     */
    public int getRevisionNumber() {
        return library.getInt(entries, R_KEY);
    }

    /**
     * Gets the 32-byte string used for verifying the owner password.
     *
     * @return 32-byte string representing the key O.
     */
    public String getBigO() {
        Object tmp = library.getObject(entries, O_KEY);
        return getLiteralString(tmp);
    }

    /**
     * Gets the 32-byte string used for verifying the user password.
     *
     * @return 32-byte string representing the key U.
     */
    public String getBigU() {
        Object tmp = library.getObject(entries, U_KEY);
        return getLiteralString(tmp);
    }

    /**
     * Gets the integer flag which specifies the operation permitted when the
     * document is opened with user access.
     *
     * @return return flag specifying user access.
     */
    public int getPermissions() {
        return library.getInt(entries, P_KEY);
    }


    /**
     * (Optional; meaningful only when the value of V is 4; PDF 1.5)
     * A dictionary whose keys shall be crypt filter names and whose values
     * shall be the corresponding crypt filter dictionaries (see Table 25).
     * Every crypt filter used in the document shall have an entry in this
     * dictionary, except for the standard crypt filter names (see Table 26).
     * <p/>
     * The conforming reader shall ignore entries in CF dictionary with the keys
     * equal to those listed in Table 26 and use properties of the respective
     * standard crypt filters.
     *
     * @return crypt filter object if found, null otherwise.
     */
    public CryptFilter getCryptFilter() {
        if (cryptFilter == null) {
            HashMap tmp = (HashMap) library.getObject(entries, CF_KEY);
            if (tmp != null) {
                cryptFilter = new CryptFilter(library, tmp);
                return cryptFilter;
            }
        }
        return cryptFilter;
    }

    /**
     * (Optional; meaningful only when the value of V is 4; PDF 1.5)
     * The name of the crypt filter that shall be used by default when decrypting
     * streams. The name shall be a key in the CF dictionary or a standard crypt
     * filter name specified in Table 26. All streams in the document, except
     * for cross-reference streams (see 7.5.8, "Cross-Reference Streams") or
     * streams that have a Crypt entry in their Filterarray (see Table 6),
     * shall be decrypted by the security handler, using this crypt filter.
     * <p/>
     * Default value: Identity.
     * <p/>
     *
     * @return name of the default stream filter name.
     */
    public Name getStmF() {
        Object tmp = library.getObject(entries, STMF_KEY);
        if (tmp != null && tmp instanceof Name) {
            return (Name) tmp;
        }
        return null;
    }


    /**
     * (Optional; meaningful only when the value of V is 4; PDF 1.5)
     * The name of the crypt filter that shall be used when decrypting all
     * strings in the document. The name shall be a key in the CF dictionary or
     * a standard crypt filter name specified in Table 26.
     * <p/>
     * Default value: Identity.
     *
     * @return name of the default string filter name.
     */
    public Name getStrF() {
        Object tmp = library.getObject(entries, STRF_KEY);
        if (tmp != null && tmp instanceof Name) {
            return (Name) tmp;
        }
        return null;
    }

    /**
     * (Optional; meaningful only when the value of V is 4; PDF 1.6) The name
     * of the crypt filter that shall be used when encrypting embedded file
     * streams that do not have their own crypt filter specifier; it shall
     * correspond to a key in the CFdictionary or a standard crypt filter name
     * specified in Table 26.
     * <p/>
     * This entry shall be provided by the security handler. Conforming writers
     * shall respect this value when encrypting embedded files, except for
     * embedded file streams that have their own crypt filter specifier. If
     * this entry is not present, and the embedded file stream does not contain
     * a crypt filter specifier, the stream shall be encrypted using the default
     * stream crypt filter specified by StmF.
     * <p/>
     * EFF:name
     */
    public Name getEEF() {
        Object tmp = library.getObject(entries, EEF_KEY);
        if (tmp != null && tmp instanceof Name) {
            return (Name) tmp;
        }
        return null;
    }


    /**
     * Gets the 32-byte string, based on the owner and user passwords, that is
     * used in the computing the encryption key.
     *
     * @return 32-byte string representing the key OE.
     */
    public String getBigOE() {
        Object tmp = library.getObject(entries, OE_KEY);
        return getLiteralString(tmp);
    }

    /**
     * Gets the 32-byte string, based on the user password, that is
     * used in the computing the encryption key.
     *
     * @return 32-byte string representing the key UE.
     */
    public String getBigUE() {
        Object tmp = library.getObject(entries, UE_KEY);
        return getLiteralString(tmp);
    }

    /**
     * A16-byte string, encrypted with the file encryption key, that contains an
     * encrypted copy of the permission flags.
     *
     * @return 16-byte string representing the key Perms.
     */
    public String getPerms() {
        Object tmp = library.getObject(entries, PERMS_KEY);
        return getLiteralString(tmp);
    }

    public  String getLiteralString(Object value){
        if (value instanceof LiteralStringObject) {
            return ((StringObject) value).getLiteralString();
        } else if (value instanceof HexStringObject) {
            return ((HexStringObject) value).getRawHexToString().toString();
        } else {
            return null;
        }
    }

    /**
     * Indicates whether the document-level metadata stream (see Section 10.2.2,
     * "Metadata Streams") is to be encrypted. Applications should respect
     * this value.
     *
     * @return true if document-level metadata is encrypted
     */
    public boolean isEncryptMetaData() {
        if (entries.containsKey(ENCRYPT_METADATA_KEY)) {
            return library.getBoolean(entries, ENCRYPT_METADATA_KEY);
        }else{
            // default value if not specified.
            return true;
        }
    }

    protected boolean isAuthenticatedUserPassword() {
        return isAuthenticatedUserPassword;
    }

    protected void setAuthenticatedUserPassword(boolean authenticatedUserPassword) {
        isAuthenticatedUserPassword = authenticatedUserPassword;
    }

    protected boolean isAuthenticatedOwnerPassword() {
        return isAuthenticatedOwnerPassword;
    }

    protected void setAuthenticatedOwnerPassword(boolean authenticatedOwnerPassword) {
        isAuthenticatedOwnerPassword = authenticatedOwnerPassword;
    }

    /**
     * Class utility methods
     */

    /**
     * Gets any dictionary key specified by the key parameter.
     *
     * @param key named key to retrieve from dictionary.
     * @return return keys value if found; null, otherwise.
     */
    public Object getValue(Object key) {
        return entries.get(key);
    }

    public String toString() {
        return "Encryption Dictionary:  \n" +
                "  fileID: " + getFileID() + " \n" +
                "  Filter: " + getPreferredSecurityHandlerName() + " \n" +
                "  SubFilter: " + getPreferredSecurityHandlerSubName() + " \n" +
                "  V: " + getVersion() + " \n" +
                "  P: " + getPermissions() + " \n" +
                "  Length:" + getKeyLength() + " \n" +
                "  CF: " + cryptFilter + " \n" +
                "  StmF: " + getStmF() + " \n" +
                "  StrF: " + getStrF() + " \n" +
                "  R: " + getRevisionNumber() + " \n" +
                "  O: " + getBigO() + " \n" +
                "  U: " + getBigU() + " \n" +
                " UE: " + getBigUE() + " \n" +
                " OE: " + getBigOE() + " \n" +
                "  Recipients: " + "not done yet" + " \n" +
                "  ";
    }
}
