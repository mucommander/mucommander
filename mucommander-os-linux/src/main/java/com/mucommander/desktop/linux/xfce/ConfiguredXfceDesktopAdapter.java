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

package com.mucommander.desktop.linux.xfce;

import com.mucommander.process.ProcessRunner;

/**
 * 'Configured' desktop adapter for Xfce. This check content of
 * <code>XDG_CURRENT_DESKTOP</code> system variable.
 * 
 * @author Vadim Kalinnikov
 */
public class ConfiguredXfceDesktopAdapter extends XfceDesktopAdapter {
	public String toString() {return "Xfce Desktop";}

    @Override
    public boolean isAvailable() {
        String var = System.getenv("XDG_CURRENT_DESKTOP");
        if (var != null) {
            var = var.toLowerCase();
            return "xfce".equals(var) || "xfce4".equals(var);
        }
        return false;
    }
}
