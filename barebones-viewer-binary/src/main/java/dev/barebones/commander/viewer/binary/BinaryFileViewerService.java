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
package dev.barebones.commander.viewer.binary;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.viewer.CanOpen;
import dev.barebones.commander.viewer.FileEditor;
import dev.barebones.commander.viewer.FileEditorService;
import dev.barebones.commander.viewer.FileViewer;
import dev.barebones.commander.viewer.FileViewerService;

/**
 * <code>FileViewerService</code> implementation for creating binary viewers.
 *
 * @author Miroslav Hajda
 */
@ParametersAreNonnullByDefault
public class BinaryFileViewerService implements FileViewerService, FileEditorService {

    @Nonnull
    @Override
    public String getName() {
        return Translator.get("binary_viewer.name");
    }

    @Override
    public int getOrderPriority() {
        return 0;
    }

    @Override
    public CanOpen canOpenFile(AbstractFile file) {
        return !file.isDirectory() ? CanOpen.YES : CanOpen.NO;
    }

    @Nonnull
    @Override
    public FileViewer createFileViewer(boolean fromSearchWithContent) {
        BinaryViewer binaryViewer = new BinaryViewer();
        if (fromSearchWithContent) {
            binaryViewer.performFindFromContent();
        }
        return binaryViewer;
    }

    @Nonnull
    @Override
    public FileEditor createFileEditor(boolean fromSearchWithContent) {
        BinaryEditor binaryEditor = new BinaryEditor();
        if (fromSearchWithContent) {
            binaryEditor.performFindFromContent();
        }
        return binaryEditor;
    }
}
