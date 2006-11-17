package com.mucommander.auth;

/**
 * Contains XML elements and attributes used to parse and write the credentials file.
 *
 * @author Maxence Bernard
 */
interface CredentialsConstants {

    /** Root element */
    static final String ELEMENT_ROOT     = "credentials_list";

    /** Element containing the last muCommander version that was used to create the file */
    static final String ELEMENT_VERSION  = "version";

    /** Element for each credential item, containg a URL, login and password */
    static final String ELEMENT_CREDENTIALS = "credentials";

    /** Element containing the credentials' URL */
    static final String ELEMENT_URL      = "url";

    /** Element containing the credentials' login */
    static final String ELEMENT_LOGIN    = "login";

    /** Element containing the credentials' (encrypted) password*/
    static final String ELEMENT_PASSWORD = "password";

    /** Root element's attribute containing the encryption method used for passwords */
    static final String ATTRIBUTE_ENCRYPTION = "encryption";

    /** Weak password encryption method */
    public static final String WEAK_ENCRYPTION_METHOD = "weak";

}
