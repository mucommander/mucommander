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

package com.mucommander.desktop.linux;

import java.util.Arrays;
import java.util.List;

import com.mucommander.desktop.DesktopAdapter;
import com.mucommander.desktop.linux.gnome.ConfiguredGnomeDesktopAdapter;
import com.mucommander.desktop.linux.gnome.GuessedGnomeDesktopAdapter;
import com.mucommander.desktop.linux.kde.ConfiguredKde3DesktopAdapter;
import com.mucommander.desktop.linux.kde.ConfiguredKde4DesktopAdapter;
import com.mucommander.desktop.linux.kde.ConfiguredKde5DesktopAdapter;
import com.mucommander.desktop.linux.kde.GuessedKde3DesktopAdapter;
import com.mucommander.desktop.linux.kde.GuessedKde4DesktopAdapter;
import com.mucommander.desktop.linux.kde.GuessedKde5DesktopAdapter;
import com.mucommander.desktop.linux.xfce.ConfiguredXfceDesktopAdapter;
import com.mucommander.desktop.linux.xfce.GuessedXfceDesktopAdapter;
import com.mucommander.module.OperatingSystemService;

/**
 * JPMS service provider for Linux operating system support.
 *
 * @author Arik Hadas
 */
public class LinuxOperatingSystemServiceProvider implements OperatingSystemService {

    @Override
    public List<DesktopAdapter> getDesktopAdapters() {
        // Unix desktops:
        // - check for Gnome before KDE, as it seems to be more popular.
        // - check for 'configured' before 'guessed', as guesses are less reliable and more expensive.
        return Arrays.asList(
                new GuessedXfceDesktopAdapter(),
                new GuessedKde3DesktopAdapter(),
                new GuessedKde4DesktopAdapter(),
                new GuessedKde5DesktopAdapter(),
                new GuessedGnomeDesktopAdapter(),
                new ConfiguredXfceDesktopAdapter(),
                new ConfiguredKde3DesktopAdapter(),
                new ConfiguredKde4DesktopAdapter(),
                new ConfiguredKde5DesktopAdapter(),
                new ConfiguredGnomeDesktopAdapter());
    }
}
