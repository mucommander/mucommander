/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.viewer;

import org.icepdf.ri.images.Images;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * <p>This class is the Main class for the ICEpdf Viewer application. It does
 * the following tasks:</p>
 * <ul>
 * <li>Presents a splash window
 * <li>Loads the viewer application
 * </ul>
 *
 * @since 1.0
 */
public class Main {

    private static SplashWindow splashWindow = null;

    public static void main(final String[] args) {
        // Read the image data and display the splash screen
        URL imageURL = Images.get("icepdf-splash.png");

        if (imageURL != null) {
            Image splashImage =
                    Toolkit.getDefaultToolkit().getImage(imageURL);
            if (splashImage != null) {
                splashWindow = new SplashWindow(splashImage);
                splashWindow.splash();
            }
        }

        // Call the main method of the application's Main class
        // using Reflection so that related classes resoving happens
        // after splash window is shown up
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    Class.forName("org.icepdf.ri.viewer.Launcher")
                            .getMethod("main", String[].class)
                            .invoke(null, new Object[]{args});
                } catch (Throwable e) {
                    e.printStackTrace();
                    System.err.flush();
                    System.exit(10);
                }
            }
        });

        // Dispose the splash screen
        if (splashWindow != null) {
            splashWindow.dispose();
        }
    }
}
