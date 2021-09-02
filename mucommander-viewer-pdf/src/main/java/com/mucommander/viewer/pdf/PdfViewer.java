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

import java.io.IOException;
import java.io.InputStream;

import org.icepdf.ri.common.MyAnnotationCallback;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.ViewerPresenter;
import javax.swing.JComponent;
import javax.swing.JMenuBar;

/**
 * A simple pdf viewer
 *
 * @author Oleg Trifonov
 */
public class PdfViewer implements FileViewer {

    private SwingController controller;
    private SwingViewBuilder factory;
    
    private ViewerPresenter presenter;

    PdfViewer() {
        // create a controller and a swing factory
        controller = new SwingController();
        factory = new SwingViewBuilder(controller);
        // add interactive mouse link annotation support via callback
        controller.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(
                        controller.getDocumentViewController()));

        // build viewer component and add it to the applet content pane.
        MyAnnotationCallback myAnnotationCallback = new MyAnnotationCallback(
                controller.getDocumentViewController());
        controller.getDocumentViewController().setAnnotationCallback(myAnnotationCallback);

        // build the viewer with a menubar
        //getContentPane().setLayout(new BorderLayout());
        //getContentPane().add(factory.buildViewerPanel(), BorderLayout.CENTER);
        //getContentPane().add(factory.buildCompleteMenuBar(), BorderLayout.NORTH);
    }

    @Override
    public void open(AbstractFile file) throws IOException {
        String description = "";
        String path = file.getPath();
        org.icepdf.core.util.Library.initializeThreadPool();
        try (InputStream is = file.getInputStream()) {
            controller.openDocument(is, description, path);
        }
    }
    
    @Override
    public void close() {
    }

//    @Override
//    protected StatusBar getStatusBar() {
//        return null;
//    }
//
//    @Override
//    protected void saveStateOnClose() {
//        org.icepdf.core.util.Library.shutdownThreadPool();
//    }
//
//    @Override
//    protected void restoreStateOnStartup() {
//        org.icepdf.core.util.Library.initializeThreadPool();
//    }
    @Override
    public JComponent getUI() {
        return factory.buildViewerPanel();
    }

    @Override
    public void setPresenter(ViewerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void extendMenu(JMenuBar menuBar) {
    }
}
