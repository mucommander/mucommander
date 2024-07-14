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
package com.mucommander.ui.viewer;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.osgi.FileViewerServiceTracker;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.viewer.FileOpenService;
import com.mucommander.viewer.FileViewerService;

import java.awt.Image;
import java.util.List;

/**
 * ViewerRegistrar maintains a list of registered file viewers and provides
 * methods to dynamically register file viewers and create appropriate
 * FileViewer (Panel) and ViewerFrame (Window) instances for a given
 * AbstractFile.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public final class ViewerRegistrar extends BaseOpenFileRegistrar {

    private ViewerRegistrar() {}

    public static ViewerRegistrar getInstance() {
        return ViewerRegistrarHolder.INSTANCE;
    }

    private static class ViewerRegistrarHolder {
        private static final ViewerRegistrar INSTANCE = new ViewerRegistrar();
    }

    @Override
    List<? extends FileOpenService> getOpenFileServices() {
        return FileViewerServiceTracker.getViewerServices();
    }

    @Override
    FileFrame createFrame(MainFrame mainFrame, AbstractFile file, boolean fromSearchWithContent, Image icon) {
        return new ViewerFrame(mainFrame, file, fromSearchWithContent, icon);
    }

    @Override
    void addService(FilePresenter presenter, FileOpenService service) throws UserCancelledException {
        ((FileViewerPresenter)presenter).addViewerService((FileViewerService) service);
    }
}
