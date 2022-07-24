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

package com.mucommander.commons.file.archive.cpio;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.AbstractArchiveFile;
import com.mucommander.commons.file.archive.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.sevenzipjbindings.SevenZipJBindingROArchiveFile;

import net.sf.sevenzipjbinding.ArchiveFormat;

/**
 * This class is the provider for the 'cpio' archive format using 7-Zip-Binding.
 *
 * @author Arik Hadas
 */
public class CpioFormatProvider implements ArchiveFormatProvider {
    /** extensions of archive filenames */
    public static final String[] EXTENSIONS = new String[] {".cpio"};

    private final static byte[] SIGNATURE = {0x30, 0x37, 0x30, 0x37, 0x30, 0x31, 0x30, 0x30, 0x30};

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipJBindingROArchiveFile(file, ArchiveFormat.CPIO, SIGNATURE);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return new ExtensionFilenameFilter(EXTENSIONS);
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList(EXTENSIONS);
    }
}
