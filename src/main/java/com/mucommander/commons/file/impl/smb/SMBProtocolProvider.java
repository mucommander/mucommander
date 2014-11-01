/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.impl.smb;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;
import jcifs.smb.SmbFile;

import java.io.IOException;

/**
 * This class is the provider for the SMB filesystem implemented by {@link com.mucommander.commons.file.impl.smb.SMBFile}.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see com.mucommander.commons.file.impl.smb.SMBFile
 */
public class SMBProtocolProvider implements ProtocolProvider {

    static {
        // Silence jCIFS's output if not in debug mode
        // Quote from jCIFS's documentation : "0 - No log messages are printed -- not even crticial exceptions."
        System.setProperty("jcifs.util.loglevel", "0");

        // Lower the timeout values

        // "The time period in milliseconds that the client will wait for a response to a request from the server.
        // The default value is 30000."
        System.setProperty("jcifs.smb.client.responseTimeout", "10000");

        // "To prevent the client from holding server resources unnecessarily, sockets are closed after this time period
        // if there is no activity. This time is specified in milliseconds. The default is 35000."
        System.setProperty("jcifs.smb.client.soTimeout", "15000");

        // Leaving this option enabled has a serious impact on performance (observed with jCIFS 1.2.25).
        // "If this property is true, domain based DFS referrals will be disabled. The default value is false.
        // This property can be important in non-domain environments where domain-based DFS referrals that normally run
        // when JCIFS first tries to resolve a path would timeout causing a long startup delay (e.g. running JCIFS only
        // on the local machine without a network like on a laptop)."
        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
    }


    /**
     * Sets the authentication protocol to use when connecting to SMB servers. This configuration method must be called
     * before {@link SMBFile} is first instantiated ; calling it after that will have no effect.
     * <p>
     * This configuration option is mapped onto jCIFS's <code>jcifs.smb.lmCompatibility</code> client property.
     * jCIFS's default will be used if this method is not called.
     * </p>
     * <p>
     * Here's a list of allowed values ; refer to JCIFS's documentation for more information:
     * <dl>
     *   <dt>0,1</dt><dd>Sends LM and NTLM responses</dd>
     *   <dt>2</dt><dd>Sends only the NTLM response. This is more secure than Levels 0 and 1, because it eliminates the
     * cryptographically-weak LM response</dd>
     *   <dt>3,4,5</dt><dd>Sends LMv2 and NTLMv2 data. NTLMv2 session security is also negotiated if the server supports
     * it. This is the default behavior (in 1.3.0 or later)</dd>
     * </dl>
     * </p>
     *
     * @param value one of the allowed values, refer to JCIFS's documentation for more information.
     */
    public static void setLmCompatibility(int value) {
        // Since jCIFS 1.3.0, the default is to use NTLM v2 authentication (value=3).
        // Note: jCIFS configuration is unfortunately global and cannot be set per connection.
        System.setProperty("jcifs.smb.lmCompatibility", Integer.toString(value));
    }

    /**
     * Sets whether or not 'extended security' should be used when connecting to SMB servers. This configuration method
     * must be called before {@link SMBFile} is first instantiated ; calling it after that will have no effect.
     * <p>
     * This configuration option is mapped onto jCIFS's <code>jcifs.smb.client.useExtendedSecurity</code> client
     * property. jCIFS's default value will be used if this method is not called, which is <code>true</code> since
     * jCIFS 1.3.0.
     * </p>
     *
     * @param value <code>true</code> to enable extended security, refer to JCIFS's documentation for more information.
     */
    public static void setExtendedSecurity(boolean value) {
        // Since jCIFS 1.3.0, extended security is turned on by default, which causes issues when connecting to older
        // SMB servers such as Samba 3.0.
        // Note jCIFS configuration is unfortunately global and cannot be set per connection.
        System.setProperty("jcifs.smb.client.useExtendedSecurity", Boolean.toString(value));
    }


    /////////////////////////////////////
    // ProtocolProvider implementation //
    /////////////////////////////////////

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return instantiationParams.length==0
            ?new SMBFile(url)
            :new SMBFile(url, (SmbFile)instantiationParams[0]);
    }
}
