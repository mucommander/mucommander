/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A class that extends JFrame to be later on used by core.
 * Since this bundle is loaded almost as the first (as it has no deps), it can be
 * used by core having JFrame "preloaded" aka "cached" in JVM.
*/
public class PreloadedJFrame extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreloadedJFrame.class);

    static {
        preLoad();
    }

    public static PreloadedJFrame instance;
    private static JPanel preloadedPanel;

    private static void preLoad() {
        new Thread(() -> {
            System.out.println("---- FRAME!");
            var pre = System.currentTimeMillis();
            instance = new PreloadedJFrame();
            instance.setVisible(true);
            instance.setVisible(false);
            preloadedPanel = new JPanel(new BorderLayout());
            System.out.println("---- FRAME in " + (System.currentTimeMillis() - pre));

        }).start();
    }

    public static void init() {
        // do nothing
    }
}
