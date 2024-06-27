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
package com.mucommander.viewer.image;

import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.viewer.FileViewerService;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.spi.IIORegistry;

/**
 * Activator for viewer for image files.
 */
@ParametersAreNonnullByDefault
public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<FileViewerService> viewerRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        MuSnapshot.registerHandler(new ImageViewerSnapshot());
        try {
            IIORegistry registry = IIORegistry.getDefaultInstance();
            registry.registerServiceProvider(new JPEGImageReaderSpi());
            registry.registerServiceProvider(new PSDImageReaderSpi());
            registry.registerServiceProvider(new TIFFImageReaderSpi());
            registry.registerServiceProvider(new WebPImageReaderSpi());
        } catch (Exception e) {
            LOGGER.error("Error registering additional image service providers", e);
        }
        viewerRegistration = context.registerService(FileViewerService.class,
                new ImageFileViewerService(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        viewerRegistration.unregister();
    }
}
