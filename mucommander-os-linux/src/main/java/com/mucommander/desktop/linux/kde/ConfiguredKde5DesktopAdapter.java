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

package com.mucommander.desktop.linux.kde;

/**
 * 'Configured' desktop adapter for KDE 5. The availability of this desktop depends on the presence of the
 * <code>KDE_SESSION_VERSION</code> environment variable that was introduced in KDE 4.
 * The variable contains major KDE version
 *
 * @author Vadim Kalinnikov
 */
public class ConfiguredKde5DesktopAdapter extends Kde5DesktopAdapter {

    private static final String KDE_SESSION_VERSION_VAR = "KDE_SESSION_VERSION";

    public String toString() {
        return "KDE 5 Desktop";
    }

    @Override
    public boolean isAvailable() {
        String var = getConfiguredEnvVariable(KDE_SESSION_VERSION_VAR);
        return "5".equals(var);
    }
}
