/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.file.icon;

import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.*;

/**
 * FileIconProvider provides a standard interface for retrieving file icons. Icon providers can fetch icons from any
 * type of source, whether they be some API or a set of icon images. There is no requirement on the resolution in which
 * the icons are provided, nor on the set of file criteria used to select the icon (file kind, name, size, ...).
 * Implementations should however be able to return icons for any type of {@link AbstractFile}.
 * The specialized {@link com.mucommander.file.icon.LocalFileIconProvider} class can come in handy for icon sources
 * that can only cope with local files.
 *
 * @see com.mucommander.file.FileFactory#getDefaultFileIconProvider()
 * @author Maxence Bernard
 */
public interface FileIconProvider {

    /**
     * Returns an icon for the given file, or <code>null</code> if it couldn't be retrieved, either because the
     * given file doesn't exist or for any other reason.
     *
     * <p>The specified <code>Dimension</code> is used as a hint at the preferred icon's resolution; there is
     * absolutely no guarantee that the returned <code>Icon</code> will indeed have this resolution. This dimension is
     * only used to choose between different resolutions should more than one resolution be available, and return the
     * one that most closely matches the specified one.<br/>
     * The implementation is not expected to perform any rescaling (either up or down), this method should only return
     * icons in their 'native' resolutions, using the preferred resolution to choose between different native dimensions.
     * For example, if this provider is able to create icons both in 16x16 and 32x32 resolutions, and a 48x48 resolution
     * is preferred, the 32x32 resolution should be favored as it is closer to 32x32.</p>
     *
     * @param file the AbstractFile instance for which an icon is requested
     * @param preferredResolution the preferred icon resolution
     * @return an icon for the requested file
     */
    public abstract Icon getFileIcon(AbstractFile file, Dimension preferredResolution);
}
