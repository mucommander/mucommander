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
package com.mucommander.viewer.image;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.viewer.FileViewerService;
import com.mucommander.viewer.FileViewer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <code>FileViewerService</code> implementation for creating image viewers.
 *
 * @author Nicolas Rinaudo
 */
@ParametersAreNonnullByDefault
public class ImageFileViewerService implements FileViewerService {

    /**
     * Used to filter out file extensions that the image viewer cannot open.
     */
    private ExtensionFilenameFilter filter;

    public ImageFileViewerService() {
        filter = new ExtensionFilenameFilter(new String[]{".png", ".gif", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp", ".wbmp"}, false, false);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public int getOrderPriority() {
        return 20;
    }

    @Override
    public boolean canViewFile(AbstractFile file) {
        // Do not allow directories
        if (file.isDirectory()) {
            return false;
        }

        return filter.accept(file);
    }

    @Nonnull
    @Override
    public FileViewer createFileViewer(boolean fromSearchWithContent) {
        return new ImageViewer(this);
    }
}
