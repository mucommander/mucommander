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

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Construct a JWindow, paints a splash image, and centers the frame on the
 * screen.
 *
 * @author Icesoft Technologies.
 */
@SuppressWarnings("serial")
final class SplashWindow extends JWindow {

    private static final Logger logger =
            Logger.getLogger(SplashWindow.class.toString());

    private Image splashImage;
    private MediaTracker mediaTracker;

    /**
     * Constructs a splash window and takes splash image
     *
     * @param image The splash image to be displayed
     */
    public SplashWindow(Image image) {
        splashImage = image;
    }

    /**
     * Shows splash screen and centers it on screen
     */
    public void splash() {
        mediaTracker = new MediaTracker(this);
        setSize(splashImage.getWidth(null), splashImage.getHeight(null));

        mediaTracker.addImage(splashImage, 0);
        try {
            mediaTracker.waitForID(0);
        } catch (InterruptedException ex) {
            logger.log(Level.FINE, "Failed to track splash image load.", ex);
        }

        setSize(splashImage.getWidth(null), splashImage.getHeight(null));
        center();
        setVisible(true);
    }

    /**
     * Centers this frame on the screen.
     */
    private void center() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle frame = getBounds();
        setLocation((screen.width - frame.width) / 2,
                (screen.height - frame.height) / 2);
    }

    /**
     * Paint the splash image to the frame
     */
    public void paint(Graphics graphics) {
        if (splashImage != null) {
            graphics.drawImage(splashImage, 0, 0, this);
        }
    }
}