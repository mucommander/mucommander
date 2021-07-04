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
package com.mucommander.ui.plaf;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javax.swing.*;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        SwingUtilities.invokeAndWait(this::registerLafs);
    }

    private void registerLafs() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        UIManager.installLookAndFeel(FlatDarkLaf.NAME, FlatDarkLaf.class.getName());
        UIManager.installLookAndFeel(FlatLightLaf.NAME, FlatLightLaf.class.getName());
        UIManager.installLookAndFeel(FlatDarculaLaf.NAME, FlatDarculaLaf.class.getName());
        UIManager.installLookAndFeel(FlatIntelliJLaf.NAME, FlatIntelliJLaf.class.getName());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
