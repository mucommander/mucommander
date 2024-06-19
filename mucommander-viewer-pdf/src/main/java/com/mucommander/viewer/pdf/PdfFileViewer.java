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
package com.mucommander.viewer.pdf;


import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.viewer.CanOpen;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.FileViewerService;

/**
 * Arik Hadas
 */
public class PdfFileViewer implements FileViewerService {

    public final static ExtensionFilenameFilter filter = new ExtensionFilenameFilter(new String[] {".pdf"});

    @Override
    public String getName() {
        return "PDF";
    }

    @Override
    public int getOrderPriority() {
        return 20;
    }

    @Override
    public CanOpen canOpenFile(AbstractFile file) {
        return !file.isDirectory() && filter.accept(file) ? CanOpen.YES : CanOpen.NO;
    }

    @Override
    public FileViewer createFileViewer(boolean fromSearchWithContent) {
        return new PdfViewer();
    }
}
