/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.file.icon.impl;

import com.mucommander.file.icon.CachedFileIconProvider;
import com.mucommander.file.icon.LocalFileIconProvider;

/**
 * This {@link LocalFileIconProvider} returns system file icons fetched from the Swing API. Those icons are only
 * available under one resolution, usually 16x16 but this may vary across platforms.
 *
 * <p>Icons are provided by one of the two following Swing classes; the one that provides the best results on the
 * target platform is used:
 * <ul>
 *   <li><code>javax.swing.filechooser.FileSystemView</code>: used on all platforms but Mac OS X</li>
 *   <li><code>javax.swing.JFileChooser: used on Mac OS X only</code></li>
 * </ul>
 * Those classes are only capable of returning icons for <code>java.io.File</code> instances, thus only work with local
 * files. {@link com.mucommander.file.icon.LocalFileIconProvider} provides transparent creation of local temporary file
 * to handle non-local files.<br>
 * It is also noteworthy that those Swing classes maintain an icon cache. Therefore, local file icons are not cached,
 * only non-local files (remote protocol or archive entries) have their icons cached to avoid excessive temporary file
 * creation, using their extension as the cache's key.
 * </p>
 *
 * @see com.mucommander.ui.icon.FileIcons#hasProperSystemIcons()
 * @author Maxence Bernard
 */
public class SwingFileIconProvider extends CachedFileIconProvider {

    public SwingFileIconProvider() {
        super(new SwingFileIconProviderImpl());
    }
}
