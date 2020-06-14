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

package com.mucommander.desktop.linux.gnome;

import com.mucommander.process.ProcessRunner;

/**
 * @author Nicolas Rinaudo
 */
public class ConfiguredGnomeDesktopAdapter extends GnomeDesktopAdapter {

    public String toString() {return "Gnome Desktop";}

    @Override
    public boolean isAvailable() {
        String var = System.getenv("DESKTOP_SESSION");
        if ("gnome".equalsIgnoreCase(var))
            return true;

        var = System.getenv("XDG_CURRENT_DESKTOP");
        if (var != null) {
            var = var.toLowerCase();
            if (var.contains("gnome"))
                return true;
            if (var.contains("unity"))
                return true;
        }

        var = System.getenv("GNOME_DESKTOP_SESSION_ID");
        return var != null && !var.trim().isEmpty();
    }

    @Override
    protected String getFileOpenerCommand() {
        try {
            ProcessRunner.execute(GVFS_OPEN);
            return GVFS_OPEN;
        }
        catch(Exception e) {}

        try {
            ProcessRunner.execute(GNOME_OPEN);
            return GNOME_OPEN;
        }
        catch(Exception e) {}

        return XDG_OPEN;
    }
}
