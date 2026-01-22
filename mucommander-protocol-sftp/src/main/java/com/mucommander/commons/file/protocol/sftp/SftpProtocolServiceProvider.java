/**
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.commons.file.protocol.sftp;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.DefaultSchemeHandler;
import com.mucommander.commons.file.DefaultSchemeParser;
import com.mucommander.commons.file.SchemeHandler;
import com.mucommander.commons.file.module.FileProtocolService;
import com.mucommander.commons.file.protocol.ProtocolProvider;

/**
 * JPMS service provider for SFTP protocol.
 *
 * @author Arik Hadas
 */
public class SftpProtocolServiceProvider implements FileProtocolService {

    @Override
    public String getSchema() {
        return "sftp";
    }

    @Override
    public ProtocolProvider getProtocolProvider() {
        return new SFTPProtocolProvider();
    }

    @Override
    public SchemeHandler getSchemeHandler() {
        return new DefaultSchemeHandler(
            new DefaultSchemeParser(),
            22,
            "/",
            AuthenticationType.AUTHENTICATION_REQUIRED,
            null
        );
    }
}
