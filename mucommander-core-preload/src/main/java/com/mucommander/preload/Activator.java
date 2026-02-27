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
package com.mucommander.preload;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        setWMClass();
        PreloadedJFrame.init();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

     /**
     * Sets the WM_CLASS for Linux window managers.
     */
     private static void setWMClass() {
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            java.lang.reflect.Field awtAppClassNameField = toolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(null, "mucommander-muCommander");
        } catch (NoSuchFieldException e) {
            // Not running on X11/Linux, or field doesn't exist in this JDK version
            System.out.println("DEBUG: Could not set WM_CLASS - field not found (probably not Linux/X11)");
        } catch (IllegalAccessException e) {
            System.err.println("Warning: Could not set WM_CLASS due to access restrictions: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Warning: Unexpected error setting WM_CLASS: " + e.getMessage());
        }
    }
}
