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

package com.mucommander.ui.icon;

import junit.framework.TestCase;

import javax.swing.*;

/**
 * A test case for the custom file icon set.
 *
 * @author Maxence Bernard
 */
public class CustomFileIconProviderTest extends TestCase {

    /**
     * Tests all icons that the custom icon set contains by instanciating them one by one and checking for
     * <code>null</code> values.
     */
    public void testIconsExistence() {
        testIconExistence(CustomFileIconProvider.FOLDER_ICON_NAME);
        testIconExistence(CustomFileIconProvider.FILE_ICON_NAME);
        testIconExistence(CustomFileIconProvider.ARCHIVE_ICON_NAME);
        testIconExistence(CustomFileIconProvider.PARENT_FOLDER_ICON_NAME);
        testIconExistence(CustomFileIconProvider.MAC_OS_X_APP_ICON_NAME);

        for(int i=0; i<CustomFileIconProvider.ICON_EXTENSIONS.length; i++)
            testIconExistence(CustomFileIconProvider.ICON_EXTENSIONS[i][0]);
    }


    /////////////////////
    // Support methods //
    /////////////////////

    /**
     * Asserts that the {@link IconManager#FILE_ICON_SET custom file icon set} contains a non-null value for the icon
     * designated by the given filename.
     *
     * @param iconName filename of the icon to test
     */
    private void testIconExistence(String iconName) {
        assertNotNull("icon "+iconName+" is either missing or corrupt!", getIcon(iconName));
    }

    /**
     * Retreives the icon designated by the given filename from the {@link IconManager#FILE_ICON_SET custom file icon set}
     * and returns it.
     *
     * @param iconName filename of the icon to retrieve
     * @return Retreives the icon designated by the given filename and returns it
     */
    private Icon getIcon(String iconName) {
        return IconManager.getIcon(IconManager.FILE_ICON_SET, iconName);
    }

}
