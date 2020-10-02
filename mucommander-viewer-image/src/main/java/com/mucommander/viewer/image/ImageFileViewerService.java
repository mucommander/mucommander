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
import com.mucommander.osgi.FileViewerService;
import com.mucommander.ui.viewer.FileFrame;
import com.mucommander.viewer.FileViewerWrapper;
import java.awt.Frame;
import java.io.IOException;
import javax.swing.JComponent;

/**
 * <code>FileViewerService</code> implementation for creating image viewers.
 *
 * @author Nicolas Rinaudo
 */
public class ImageFileViewerService implements FileViewerService {

    /**
     * Used to filter out file extensions that the image viewer cannot open.
     */
    private ExtensionFilenameFilter filter;

    public ImageFileViewerService() {
        filter = new ExtensionFilenameFilter(new String[]{".png", ".gif", ".jpg", ".jpeg"});
        filter.setCaseSensitive(false);
    }

    @Override
    public String getTabTitle() {
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

    @Override
    public FileViewerWrapper createFileViewer() {
        final ImageViewer viewer = new ImageViewer(this);

        return new FileViewerWrapper() {
            @Override
            public void open(AbstractFile file) throws IOException {
                viewer.open(file);
            }

            @Override
            public JComponent getViewerComponent() {
                return viewer;
            }

            @Override
            public void setFrame(Frame frame) {
                viewer.setFrame((FileFrame) frame);
            }
        };
    }
}
