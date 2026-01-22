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
package com.mucommander.viewer.text;

import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.viewer.FileViewerService;

/**
 * Service provider for text file viewer.
 *
 * @author Arik Hadas
 */
public class TextFileViewerServiceProvider implements FileViewerService {

    private final TextFileViewerService service;

    public TextFileViewerServiceProvider() {
        MuSnapshot.registerHandler(new TextViewerSnapshot());
        this.service = new TextFileViewerService();
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public int getOrderPriority() {
        return service.getOrderPriority();
    }

    @Override
    public com.mucommander.viewer.CanOpen canOpenFile(com.mucommander.commons.file.AbstractFile file) {
        return service.canOpenFile(file);
    }

    @Override
    public String getConfirmationMsg() {
        return service.getConfirmationMsg();
    }

    @Override
    public com.mucommander.viewer.FileViewer createFileViewer(boolean fromSearchWithContent) {
        return service.createFileViewer(fromSearchWithContent);
    }
}
