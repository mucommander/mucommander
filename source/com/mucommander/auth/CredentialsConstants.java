/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

    /** Element that defines a property (name/value pair) */
    static final String ELEMENT_PROPERTY = "property";

    /** Name attribute of the property element */
    static final String ATTRIBUTE_NAME = "name";

    /** Value attribute of the property element */
    static final String ATTRIBUTE_VALUE = "value";

    /** Root element's attribute containing the encryption method used for passwords */
    static final String ATTRIBUTE_ENCRYPTION = "encryption";

    /** Weak password encryption method */
    static final String WEAK_ENCRYPTION_METHOD = "weak";

}
