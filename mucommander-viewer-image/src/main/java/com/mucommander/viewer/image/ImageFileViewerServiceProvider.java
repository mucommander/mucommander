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

import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.viewer.FileViewerService;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.spi.IIORegistry;

/**
 * Service provider for image file viewer.
 *
 * @author Arik Hadas
 */
@ParametersAreNonnullByDefault
public class ImageFileViewerServiceProvider implements FileViewerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageFileViewerServiceProvider.class);

    private final ImageFileViewerService service;

    public ImageFileViewerServiceProvider() {
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
        this.service = new ImageFileViewerService();
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
    public com.mucommander.viewer.FileViewer createFileViewer(boolean fromSearchWithContent) {
        return service.createFileViewer(fromSearchWithContent);
    }
}
