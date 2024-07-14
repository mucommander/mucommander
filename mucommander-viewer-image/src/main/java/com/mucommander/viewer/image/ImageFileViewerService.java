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
import com.mucommander.viewer.CanOpen;
import com.mucommander.viewer.FileViewerService;
import com.mucommander.viewer.FileViewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

/**
 * <code>FileViewerService</code> implementation for creating image viewers.
 *
 * @author Nicolas Rinaudo
 */
@ParametersAreNonnullByDefault
public class ImageFileViewerService implements FileViewerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageFileViewerService.class);

    /**
     * Used to filter out file extensions that the image viewer cannot open.
     */
    private ExtensionFilenameFilter filter;

    public ImageFileViewerService() {
        var acceptedExts = getSupportedImageExtensions();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The list of supported image formats and their handlers:");
            acceptedExts.forEach(ext -> {
                        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(ext);
                        LOGGER.debug("{}:", ext);
                        while (it.hasNext()) {
                            LOGGER.debug("\t {}", it.next());
                        }
                    }
            );
        }
        filter = new ExtensionFilenameFilter(
                acceptedExts.stream().map(suffix -> "." + suffix).toArray(String[]::new),
                false, false);
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
    public CanOpen canOpenFile(AbstractFile file) {
        // Do not allow directories
        if (file.isDirectory()) {
            return CanOpen.NO;
        }

        return filter.accept(file) ? CanOpen.YES : CanOpen.NO;
    }

    @Nonnull
    @Override
    public FileViewer createFileViewer(boolean fromSearchWithContent) {
        return new ImageViewer(this);
    }

    /**
     * Returns a set of image extensions supported by JRE and TwelveMonkeys plugins.
     *
     * @return set of extensions (without leading dot).
     */
    private Set<String> getSupportedImageExtensions() {
        return Arrays.stream(ImageIO.getReaderFileSuffixes()).collect(Collectors.toSet());
    }
}
