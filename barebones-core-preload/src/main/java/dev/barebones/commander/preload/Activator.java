/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */
package dev.barebones.commander.preload;

public final class Activator {

    private Activator() {
    }

    public static void register() {
        setWMClass();
        PreloadedJFrame.init();
    }

    /**
     * Sets the WM_CLASS for Linux window managers.
     */
    private static void setWMClass() {
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            java.lang.reflect.Field awtAppClassNameField = toolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(null, "barebones-commander");
        } catch (NoSuchFieldException e) {
            // Not running on X11/Linux, or field doesn't exist in this JDK version
        } catch (IllegalAccessException e) {
            System.err.println("Warning: Could not set WM_CLASS due to access restrictions: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Warning: Unexpected error setting WM_CLASS: " + e.getMessage());
        }
    }
}
