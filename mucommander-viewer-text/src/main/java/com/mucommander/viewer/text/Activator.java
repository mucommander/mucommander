/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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
package com.mucommander.viewer.text;

import com.mucommander.osgi.FileEditorService;
import com.mucommander.osgi.FileViewerService;
import com.mucommander.snapshot.MuSnapshot;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import static com.mucommander.ui.viewer.EditorRegistrar.registerFileEditor;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator for viewer for text files.
 *
 * @author Miroslav Hajda
 */
public class Activator implements BundleActivator {

    private ServiceRegistration<FileViewerService> viewerRegistration;
    private ServiceRegistration<FileEditorService> editorRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        // TODO: The TextFactory must be the last FileViewer to be registered (otherwise it would open other factories file types)
//        registerFileViewer(new com.mucommander.viewer.text.TextFactory());

        MuSnapshot.registerHandler(new TextViewerSnapshot());

        registerFileEditor(new com.mucommander.viewer.text.TextFactory());

        TextFileViewerService service = new TextFileViewerService();

        viewerRegistration = context.registerService(FileViewerService.class, service, null);
        editorRegistration = context.registerService(FileEditorService.class, service, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        viewerRegistration.unregister();
        editorRegistration.unregister();
    }
}
