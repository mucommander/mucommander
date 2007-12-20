/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.icon;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.CellRendererPane;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

/**
 * <code>javax.swing.Icon</code> implementation that manages animation.
 * <p>
 * This heavily borrows code from Technomage's <code>furbelow</code> package, distributed
 * under the GNU Lesser General Public License.<br>
 * The original source code can be found <a href="http://furbelow.svn.sourceforge.net/viewvc/furbelow/trunk/src/furbelow">here</a>.
 * </p>
 * @author twall, Nicolas Rinaudo
 */
public abstract class AnimatedIcon implements Icon {
    // - Default values ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Default number of frames per animation. */
    public static final int DEFAULT_FRAME_COUNT = 8;
    /** Default number of milliseconds between each frame. */
    public static final int DEFAULT_FRAME_DELAY = 1000 / DEFAULT_FRAME_COUNT;



    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** All tracked components. */
    private HashSet components = new HashSet();
    /** Timer used to take the animation from one frame to the next. */
    private Timer   timer;
    /** Index of the current frame. */
    private int     currentFrame;
    /** Total number of frames in the animation. */
    private int     frameCount;
    /** Whether or not the animation should be running. */
    private boolean animate;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new animated icon.
     * <p>
     * This is a convenience constructor and is strictly equivalent to calling
     * <code>{@link #AnimatedIcon(int,int)}({@link #DEFAULT_FRAME_COUNT}, {@link #DEFAULT_FRAME_DLAY});</code>
     * </p>
     */
    public AnimatedIcon() {this(DEFAULT_FRAME_COUNT, DEFAULT_FRAME_DELAY);}

    /**
     * Creates a new animated icon with the specified number of frames.
     * <p>
     * This is a convenience constructor and is strictly equivalent to calling
     * <code>{@link #AnimatedIcon(int,int)}(frameCount, {@link #DEFAULT_FRAME_DLAY});</code>
     * </p>
     * @param frameCount number of frames in the animation.
     */
    public AnimatedIcon(int frameCount) {this(frameCount, DEFAULT_FRAME_DELAY);}

    /**
     * Creates a new animated icon with the specified number of frames and repaint delay.
     * @param frameCount   number of frames in the animation.
     * @param repaintDelay number of milliseconds to sleep between each frame.
     */
    public AnimatedIcon(int frameCount, int repaintDelay) {
        // Initialises the animation timer.
        timer = new Timer(repaintDelay, new AnimationUpdater(this));
        timer.setRepeats(true);

        // Initialises frame control.
        setFrameCount(frameCount);
        setFrameDelay(repaintDelay);
    }



    // - Abstract methods ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the icon's width.
     * @return the icon's width.
     */
    public abstract int getIconWidth();

    /**
     * Returns the icon's height.
     * @return the icon's height.
     */
    public abstract int getIconHeight();

    /**
     * Paints the current frame.
     * @param c component in which the frame is being painted.
     * @param g graphics in which to paint the frame.
     * @param x horizontal coordinate at which to paint the frame.
     * @param y vertical coordinate at which to paint the frame.
     */
    protected abstract void paintFrame(Component c, Graphics g, int x, int y);



    // - Frame management ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the total number of frames in the animation.
     * @param count total number of frames in the animation.
     */
    public synchronized void setFrameCount(int count) {this.frameCount = count;}

    /**
     * Returns the total number of frames in the animation.
     * @return the total number of frames in the animation.
     */
    public synchronized int getFrameCount() {return frameCount;}

    /**
     * Returns the index of the current frame in the animation.
     * @return the index of the current frame in the animation.
     */
    public synchronized int getFrame() {return currentFrame;}

    /**
     * Sets the index of the current frame in the animation.
     * <p>
     * If the method does actually change the current frame, it will trigger a repaint.
     * </p>
     * @param frame index of the current frame in the animation.
     */
    public synchronized void setFrame(int frame) {
        if(frame != currentFrame) {
            if(frame == 0)
                currentFrame = 0;
            else
                currentFrame = frame % frameCount;
            repaint();
        }
    }

    /**
     * Takes the animation to its next frame.
     * <p>
     * This is a convenience method and is strictly equivalent to calling
     * <code>{@link #setFrame(int) setFrame}({@link #getFrame() getFrame()} + 1)</code>.
     * </p>
     */
    public synchronized void nextFrame() {setFrame(currentFrame + 1);}

    /**
     * Sets the number of milliseconds the animation will sleep between each frame.
     * <p>
     * If set to 0, the animation will stop.
     * </p>
     * @param delay number of milliseconds the animation will sleep between each frame.
     */
    public synchronized void setFrameDelay(int delay) {timer.setDelay(delay);}

    /**
     * Starts / stops the animation.
     * @param a whether the animation should be started or stopped.
     */
    public synchronized void setAnimated(boolean a) {
        // Starts the animation if necessary.
        if(a) {
            if(!timer.isRunning())
                timer.restart();
        }

        // Stops the animation if necessary.
        else if(timer.isRunning())
            timer.stop();
        animate = a;
    }

    /**
     * Returns <code>true</code> if the animation is currently running.
     * <p>
     * Note that this method will return <code>true</code> if the animation is <b>meant</b> to be running,
     * for example if the icon is not visible but would be animated if it was.
     * </p>
     * @return <code>true</code> if the animation is currently running, <code>false</code>.
     */
    public synchronized boolean isAnimated() {return animate;}

    /**
     * Returns the number of milliseconds the animation will sleep between each frame.
     * @return the number of milliseconds the animation will sleep between each frame.
     */
    public synchronized int getFrameDelay() {return timer.getDelay();}



    // - Painting ------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Paints the icon's current frame.
     * @param c component in which to paint the icon.
     * @param g graphic context in which to paint the icon.
     * @param x horizontal coordinate at which to paint the icon.
     * @param y vertical coordinate at which to paint the icon.
     */
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        // Paints the current frame.
        paintFrame(c, g, x, y);

        // Stores the component and starts / restarts the timer if necessary.
        if(c != null) {
            AffineTransform transform;

            transform = ((Graphics2D)g).getTransform();
            components.add(new TrackedComponent(c, x, y, (int)(getIconWidth() * transform.getScaleX()), (int)(getIconHeight() * transform.getScaleY())));

            // Restarts the timer if necessary.
            if(!timer.isRunning() && animate)
                timer.restart();
        }
    }

    /**
     * Forces the icon to repaint.
     */
    protected synchronized void repaint() {
        Iterator iterator;

        // If the component list is empty, we can stop the timer.
        if(components.isEmpty())
            timer.stop();

        // Repaints all pending components.
        else {
            iterator = components.iterator();
            while(iterator.hasNext())
                ((TrackedComponent)iterator.next()).repaint();
            components.clear();
        }
    }



    // - Container tracking --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Used to keep track of the various components in which an animated icon is being painted.
     * @author twall, Nicolas Rinaudo
     */
    private static class TrackedComponent {
        /** Component in which the icon must be painted. */
        private Component component;
        /** Horizontal coordinate at which the icon should be painted. */
        private int       x;
        /** Vertical coordinate at which the icon should be painted. */
        private int       y;
        /** Width of the icon (used for clipping). */
        private int       width;
        /** Height of the icon (used for clipping). */
        private int       height;
        /** Component's hashcode. */
        private int       hashCode;

        /**
         * Creates a new tracked component.
         * @param c      component in which to paint the icon.
         * @param x      horizontal coordinate at which to paint the icon.
         * @param y      vertical coordinate at which to paint the icon.
         * @param width  width of the icon.
         * @param height height of the icon.
         */
        public TrackedComponent(Component c, int x, int y, int width, int height) {
            Component ancestor;

            // Identifies the component that displays the icon.
            if((ancestor = findNonRendererAncestor(c)) != c) {
                Point pt = SwingUtilities.convertPoint(c, x, y, ancestor);
                c = ancestor;
                x = pt.x;
                y = pt.y;
            }

            // Stores all the necessary information and computes the tracked component's hashcode.
            component   = c;
            this.x      = x;
            this.y      = y;
            this.width  = width;
            this.height = height;
            hashCode    = (x + "," + y + ":" + c.hashCode()).hashCode();
        }

        /**
         * Finds the specified component's first non-renderer ancestor.
         * @param c component whose ancestors should be explored.
         */
        private Component findNonRendererAncestor(Component c) {
            Component ancestor;

            ancestor = SwingUtilities.getAncestorOfClass(CellRendererPane.class, c);
            if (ancestor != null && ancestor != c && ancestor.getParent() != null)
                c = findNonRendererAncestor(ancestor.getParent());
            return c;
        }

        /**
         * Forces the tracked component to repaint the animated icon.
         */
        public void repaint() {component.repaint(x, y, width, height);}
    }



    // - Timer management ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Receives timer events and notifies the icon.
     * @author twall, Nicolas Rinaudo
     */
    private static class AnimationUpdater implements ActionListener {
        /** Weak reference to the animation. */
        private WeakReference icon;

        /**
         * Creates a new animation updater on the specified icon.
         * @param icon animation to update.
         */
        public AnimationUpdater(AnimatedIcon icon) {this.icon = new WeakReference(icon);}

        /**
         * Notifies the icon that it should update.
         * @param event ignored.
         */
        public void actionPerformed(ActionEvent event) {
            AnimatedIcon i;

            // Makes sure the animation hasn't been garbage collected.
            if((i = (AnimatedIcon)icon.get()) != null)
                i.nextFrame();
        }
    }
}
