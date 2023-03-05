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


package com.mucommander.commons.file.archive.rar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.junrar.exception.UnsupportedRarV5Exception;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.AbstractArchiveFile;
import com.mucommander.commons.file.archive.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.sevenzipjbindings.SevenZipJBindingROArchiveFile;

import net.sf.sevenzipjbinding.ArchiveFormat;

/**
 * This class is the provider for the 'Rar' archive format implemented by {@link RarArchiveFile}.
 *
 * @see com.mucommander.commons.file.archive.rar.RarArchiveFile
 * @author Arik Hadas
 */
public class RarFormatProvider implements ArchiveFormatProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RarFormatProvider.class);

    /** extensions of archive filenames */
    public static final String[] EXTENSIONS = new String[] {".rar", ".cbr"};

    private final static byte[] RAR5_SIGNATURE = {0x52, 0x61, 0x72, 0x21, 0x1a, 0x07, 0x01, 0};

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        RarArchiveFile rar4 = new RarArchiveFile(file);
        AbstractArchiveFile rar = rar4;
        try {
            rar4.check();
        } catch (UnsupportedRarV5Exception e) {
            LOGGER.info("failed to open RAR file with junrar, trying as RAR 5+");
            rar = new SevenZipJBindingROArchiveFile(file, ArchiveFormat.RAR5, RAR5_SIGNATURE);
        } catch (Exception e) {
            // no-op, failures will be thrown when browsing/extracting the file
        }
        return rar;
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
