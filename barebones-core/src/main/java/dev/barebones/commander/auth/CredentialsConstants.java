/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.barebones.commander.auth;

/**
 * Contains XML elements and attributes used to parse and write the credentials file.
 *
 * @author Maxence Bernard
 */
interface CredentialsConstants {

    /** Root element */
    static final String ELEMENT_ROOT     = "credentials_list";

    /** Element for each credential item, containing a URL, login and password */
    static final String ELEMENT_CREDENTIALS = "credentials";

    /** Element containing the credentials' URL */
    static final String ELEMENT_URL      = "url";

    /** Element containing the credentials' login */
    static final String ELEMENT_LOGIN    = "login";

    /** Pre-Phase-12 element containing the credentials' XOR-encrypted
     *  password. New writes never produce this element; the parser
     *  still reads it for one-shot migration into the SecretStore,
     *  then it gets dropped on the next save. */
    static final String ELEMENT_PASSWORD = "password";

    /** Phase-12 element. Empty (no body) — its presence tells the
     *  parser to look the password up in the SecretStore using the
     *  surrounding {@link #ELEMENT_URL}'s text as the account ref. */
    static final String ELEMENT_SECRET_REF = "secret-ref";

    /** Element that defines a property (name/value pair) */
    static final String ELEMENT_PROPERTY = "property";

    /** Name attribute of the property element */
    static final String ATTRIBUTE_NAME = "name";

    /** Value attribute of the property element */
    static final String ATTRIBUTE_VALUE = "value";

    /** Name of the root element's attribute containing the muCommander version that was used to create the credentials file */
    static final String ATTRIBUTE_VERSION = "version";

    /** Root element's attribute containing the encryption method used for passwords */
    static final String ATTRIBUTE_ENCRYPTION = "encryption";

    /** Pre-Phase-12 encryption method tag (XOR-Base64). */
    static final String WEAK_ENCRYPTION_METHOD = "weak";

    /** Phase-12 encryption method tag — secrets live in the
     *  OS keychain / libsecret / AES-GCM file rather than the XML. */
    static final String SECRET_STORE_METHOD = "secret-store";

    /** SecretStore service-name shared by every credentials entry. */
    static final String SECRET_STORE_SERVICE = "barebones-commander";

}
