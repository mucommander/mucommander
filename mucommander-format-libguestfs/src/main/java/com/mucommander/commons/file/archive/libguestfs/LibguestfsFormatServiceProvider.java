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
package com.mucommander.commons.file.archive.libguestfs;

import com.mucommander.commons.file.archive.ArchiveFormatProvider;
import com.mucommander.commons.file.module.FileFormatService;
import com.mucommander.commons.runtime.OsFamily;

/**
 * JPMS service provider for libguestfs format.
 *
 * @author Arik Hadas
 */
public class LibguestfsFormatServiceProvider implements FileFormatService {

    @Override
    public ArchiveFormatProvider getProvider() {
        // Only provide the service on Linux
        if (!OsFamily.LINUX.isCurrent())
            return null;
        return new LibguestfsFormatProvider();
    }
}
