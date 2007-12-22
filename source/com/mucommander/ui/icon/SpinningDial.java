package com.mucommander.ui.icon;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Animated icon of a spinning dial used to notify users that an application is performing a task.
 * <p>
 * This behaves as any animated icon except for one thing: when the animation is stopped using
 * {@link #setAnimated(boolean)}, the dial won't be displayed anymore until the animation is
 * resumed.
 * </p>
 * @author twall, Nicolas Rinaudo
 */
public class SpinningDial extends AnimatedIcon {
    // - Class constants -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Default creation animation status. */
    public  static final boolean DEFAULT_ANIMATE    = false;
    /** Dial's default color. */
    public  static final Color DEFAULT_COLOR        = Color.BLACK;
    /** Minimum alpha-transparency value that must be applied to the dial's color as it fades out. */
    private static final int   MIN_ALPHA            = 32;
    /** Icon's default width and height. */
    public  static final int   DEFAULT_SIZE         = 16;
    /** Default number of spokes in the dial. */
    public  static final int   DEFAULT_SPOKES       = 12;
    /** Dial's full size, will be scaled down at paint time. */
    private static final int   FULL_SIZE            = 256;
    /** Width of each of the dial's strokes. */
    private static final float DEFAULT_STROKE_WIDTH = FULL_SIZE / 10f;
    /** Scale down factor for the dial. */
    private static final float FRACTION             = 0.6f;



    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Icon's width. */
    private int     width;
    /** Icon's height. */
    private int     height;
    /** All images that compose the spinning dial. */
    private Image[] frames;
    /** Color used to paint the dial. */
    private Color   color;
    /** Width of each stroke. */
    private float   strokeWidth;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new spinning dial.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     * </p>
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     * </p>
     */
    public SpinningDial() {this(DEFAULT_SIZE, DEFAULT_SIZE);}

    /**
     * Creates a new spinning dial.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     * </p>
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(boolean animate) {this(DEFAULT_SIZE, DEFAULT_SIZE, animate);}

    /**
     * Creates a new spinning dial with the specified color.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     * </p>
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     * </p>
     * @param c color in which to paint the dial.
     */
    public SpinningDial(Color c) {this(DEFAULT_SIZE, DEFAULT_SIZE, c);}

    /**
     * Creates a new spinning dial with the specified color.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     * </p>
     * @param c       color in which to paint the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(Color c, boolean animate) {this(DEFAULT_SIZE, DEFAULT_SIZE, c, animate);}

    /**
     * Creates a new spinning dial with the specified dimensions.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     * </p>
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     * </p>
     * @param w width of the icon.
     * @param h height of the icon.
     */
    public SpinningDial(int w, int h) {this(w, h, DEFAULT_SPOKES);}

    /**
     * Creates a new spinning dial with the specified dimensions.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     * </p>
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, boolean animate) {this(w, h, DEFAULT_SPOKES, animate);}

    /**
     * Creates a new spinning dial with the specified dimensions and color.
     * <p>
     * The new instance will use {@link #DEFAULT_SPOKES} for its number of spokes.
     * </p>
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     * </p>
     * @param w width of the icon.
     * @param h height of the icon.
     * @param c color in which to paint the dial.
     */
    public SpinningDial(int w, int h, Color c) {this(w, h, DEFAULT_SPOKES, c);}

    /**
     * Creates a new spinning dial with the specified dimensions and color.
     * <p>
     * The new instance will use {@link #DEFAULT_SPOKES} for its number of spokes.
     * </p>
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param c       color in which to paint the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, Color c, boolean animate) {this(w, h, DEFAULT_SPOKES, c, animate);}

    /**
     * Creates a new spinning dial with the specified dimensions and number of spokes.
     * <p>
     * The new instance will use {@link #DEFAULT_COLOR} for its color.
     * </p>
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     * </p>
     * @param w      width of the icon.
     * @param h      height of the icon.
     * @param spokes number of spokes that compose the dial.
     */
    public SpinningDial(int w, int h, int spokes) {this(w, h, spokes, DEFAULT_COLOR);}

    /**
     * Creates a new spinning dial with the specified dimensions and number of spokes.
     * <p>
     * The new instance will use {@link #DEFAULT_COLOR} for its color.
     * </p>
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param spokes  number of spokes that compose the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, int spokes, boolean animate) {this(w, h, spokes, DEFAULT_COLOR, animate);}

    /**
     * Creates a new spinning dial with the specified characteristics.
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     * </p>
     * @param w      width of the icon.
     * @param h      height of the icon.
     * @param spokes number of spokes that compose the dial.
     * @param c      color in which to paint the dial.
     */
    public SpinningDial(int w, int h, int spokes, Color c) {this(w, h, spokes, c, DEFAULT_ANIMATE);}

    /**
     * Creates a new spinning dial with the specified characteristics.
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param spokes  number of spokes that compose the dial.
     * @param c       color in which to paint the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, int spokes, Color c, boolean animate) {
        super(spokes, 1000 / spokes);

        // Initialises the icon.
        width       = w;
        height      = h;
        color       = c;
        frames      = new Image[getFrameCount()];
        strokeWidth = DEFAULT_STROKE_WIDTH;

        // Animates the icon if necessary.
        if(animate)
            setAnimated(true);
    }

    /**
     * Sets the width of the strokes used to paint each of the dial's spokes.
     * @param width width of the strokes used to paint each of the dial's spokes.
     */
    public synchronized void setStrokeWidth(float width) {strokeWidth = width;}

    /**
     * Returns the width of the strokes used to paint each of the dial's spokes.
     * @return the width of the strokes used to paint each of the dial's spokes.
     */
    public synchronized float getStrokeWidth() {return strokeWidth;}



    // - Color management ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the color used to draw the dial.
     * @param c color in which to paint the dial.
     */
    public synchronized void setColor(Color c) {
        // Ignores calls that don't actually change anything.
        if(!color.equals(c)) {
            color = c;

            // Resets stored images to make sure they get repainted
            // with the right color.
            for(int i = 0; i < frames.length; i++)
                frames[i] = null;
        }
    }

    /**
     * Returns the color used to paint the dial.
     * @return the color used to paint the dial.
     */
    public synchronized Color getColor() {return color;}

    /**
     * Computes the dial color according to the specified alpha-transparency value.
     * @param alpha transparency value that must be applied to the dial's color.
     */
    protected Color getSpokeColor(int alpha) {return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(MIN_ALPHA, alpha));}



    // - Size methods --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the icon's height.
     * @return the icon's height.
     */
    public int getIconHeight() {return height;}

    /**
     * Returns the icon's width.
     * @return the icon's width.
     */
    public int getIconWidth() {return width;}



    // - Rendering methods ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Initialises graphics for painting one of the dial's frames.
     * @param graphics graphics instance to initialise.
     */
    private void initialiseGraphics(Graphics2D graphics) {
        float scale;

        scale = (float)Math.min(width, height) / FULL_SIZE;

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, width, height);
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.translate((float)width / 2, (float)height / 2);
        graphics.scale(scale, scale);
    }

    /**
     * Paints the current frame on the specified component.
     * @param c        component on which to paint the dial.
     * @param graphics graphic context to use when painting the dial.
     * @param x        horizontal coordinate at which to paint the dial.
     * @param y        vertical coordinate at which to paint the dial.
     */
    public synchronized void paintFrame(Component c, Graphics graphics, int x, int y) {
        int currentFrame;

        // Ignores paint calls while not animated.
        if(isAnimated()) {
            // Checks whether the current frame has already been generated or not, generates
            // it if not.
            if((frames[currentFrame = getFrame()]) == null) {
                Image      frame;
                Graphics2D g;
                int        alpha;
                double     cos;
                double     sin;
                int        radius;

                // Initialises the frame.
                if(c == null)
                    frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                else
                    frame = c.getGraphicsConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);

                // Initialises the frame's g.
                initialiseGraphics(g = (Graphics2D)frame.getGraphics());

                // Draws each spoke in the dial.
                alpha  = 255;
                radius = FULL_SIZE / 2 - 1 - (int)(strokeWidth / 2);
                for(int i = 0; i < getFrameCount(); i++) {
                    cos = Math.cos((Math.PI * 2) - (Math.PI * 2 * (i - currentFrame)) / getFrameCount());
                    sin = Math.sin((Math.PI * 2) - (Math.PI * 2 * (i - currentFrame)) / getFrameCount());

                    g.setColor(getSpokeColor(alpha));
                    g.drawLine((int)(radius * FRACTION * cos), (int)(radius * FRACTION * sin),
                               (int)(radius * cos), (int)(radius * sin));
                    alpha = Math.max(MIN_ALPHA, (alpha * 3) / 4);
                }
                g.dispose();

                // Stores the newly generated frame.
                frames[currentFrame] = frame;
            }

            // Draws the current frame.
            graphics.drawImage(frames[currentFrame], x, y, null);
        }
    }

    /**
     * Starts / stops the spinning dial.
     * <p>
     * If <code>a</code> is <code>false</code>, the animation will stop and the
     * the dial won't be displayed anymore until the animationr resumes.
     * </p>
     * @param a whether to start or stop the animation.
     */
    public void setAnimated(boolean a) {
        super.setAnimated(a);

        // Makes sure the dial disapears when the animation is stopped.
        if(!a)
            repaint();
    }
}
