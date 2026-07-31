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
package com.mucommander.viewer.binary;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.mucommander.viewer.FileEditorService;

/**
 * Service provider for binary file editor.
 *
 * @author Arik Hadas
 */
@ParametersAreNonnullByDefault
public class BinaryFileEditorServiceProvider implements FileEditorService {

    private final BinaryFileViewerService service;

    public BinaryFileEditorServiceProvider() {
        this.service = new BinaryFileViewerService();
    }

    @Nonnull
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

    @Nonnull
    @Override
    public com.mucommander.viewer.FileEditor createFileEditor(boolean fromSearchWithContent) {
        return service.createFileEditor(fromSearchWithContent);
    }
}
