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

/**
 * This {@link com.mucommander.file.icon.FileIconProvider} returns system file icons fetched from the Mac OS X
 * Cocoa-Java API. As such, it is only available under Mac OS X. Unlike {@link SwingFileIconProvider}, icons are
 * available in the several resolutions: 16x16, 32x32, 48x48 and 128x128. Note however that all resolutions may not be
 * available for a given icon: some icons may be available in all resolutions, some others in just one.
 *
 * @author Maxence Bernard
 */
public class CocoaFileIconProvider extends CachedFileIconProvider {

    public CocoaFileIconProvider() {
        super(new CocoaFileIconProviderImpl());
    }
}
