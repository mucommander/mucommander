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

import java.awt.LayoutManager;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

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

    private static final Queue<PreloadedJFrame> preloadedFrame = new ConcurrentLinkedDeque<>();

    private static final Queue<JPanel> preloadedPanels = new ConcurrentLinkedDeque<>();

    private Object mainFrameObj;

    private static void preLoad() {
        new Thread(() -> {
            LOGGER.info("Going to pre-create a couple of JFrames...");
            var pre = System.currentTimeMillis();
            preloadedFrame.add(new PreloadedJFrame());
            preloadedFrame.add(new PreloadedJFrame());
            LOGGER.info("JFrames pre-creation completed in {}ms", (System.currentTimeMillis() - pre));

            LOGGER.info("Going to pre-create a couple of JPanels...");
            pre = System.currentTimeMillis();
            preloadedPanels.add(new JPanel());
            preloadedPanels.add(new JPanel());
            preloadedPanels.add(new JPanel());
            preloadedPanels.add(new JPanel());
            preloadedPanels.add(new JPanel());
            preloadedPanels.add(new JPanel());
            LOGGER.info("JPanel pre-creation completed in {}ms", (System.currentTimeMillis() - pre));

        }, "Preload-JFrame").start();
    }

    private void setMainFrameObj(Object mainFrameObj) {
        this.mainFrameObj = mainFrameObj;
    }

    public Object getMainFrameObject() {
        return mainFrameObj;
    }

    public static void init() {
        // noop
    }

    public static JFrame getJFrame(Object mainFrame) {
        var result = preloadedFrame.poll();
        result = result != null ? result : new PreloadedJFrame();
        result.setMainFrameObj(mainFrame);
        return result;
    }

    public static JPanel getJPanel(LayoutManager layout) {
        var result = preloadedPanels.poll();
        if (result == null) {
            result = new JPanel(layout);
        } else {
            result.setLayout(layout);
        }
        return result;
    }

}
