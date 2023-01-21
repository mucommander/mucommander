package com.mucommander.viewer.image.ui;

import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * Image viewer UI.
 */
@ParametersAreNonnullByDefault
public class ImageViewerPanel extends JComponent implements ThemeListener {

    private Image image;
    private int imageWidth;
    private int imageHeight;
    private double zoomFactor;
    private Color backgroundColor;
    private final ScallingImageObserver imageObserver = new ScallingImageObserver();

    public ImageViewerPanel() {
        backgroundColor = ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR);
        ThemeManager.addCurrentThemeListener(this);
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public void setImage(Image image) {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = image;
        imageWidth = image.getWidth(null);
        imageHeight = image.getHeight(null);
        notifySizeChanged();
    }

    public void close() {
        if (this.image != null) {
            this.image.flush();
        }
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void paint(Graphics g) {
        int frameWidth = getWidth();
        int frameHeight = getHeight();

        final int scaledWidth = getScaledX(imageWidth);
        final int scaledHeight = getScaledY(imageHeight);

        final int offsetX = Math.max(0, (frameWidth - scaledWidth) / 2);
        final int offsetY = Math.max(0, (frameHeight - scaledHeight) / 2);
        g.drawImage(image, offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight,
                0, 0, imageWidth, imageHeight, backgroundColor, imageObserver);
    }

    @Nonnull
    @Override
    public synchronized Dimension getPreferredSize() {
        return image == null ? new Dimension(320, 200) : new Dimension(getScaledX(imageWidth), getScaledY(imageHeight));
    }

    //////////////////////////////////
    // ThemeListener implementation //
    //////////////////////////////////

    /**
     * Receives theme color changes notifications.
     */
    @Override
    public void colorChanged(ColorChangedEvent event) {
        if (event.getColorId() == Theme.EDITOR_BACKGROUND_COLOR) {
            backgroundColor = event.getColor();
            repaint();
        }
    }

    /**
     * Not used, implemented as a no-op.
     */
    @Override
    public void fontChanged(FontChangedEvent event) {
    }

    private void notifySizeChanged() {
        setSize(getPreferredSize());
    }

    private int getScaledX(int x) {
        if (image == null) {
            return 0;
        }
        return (int) (zoomFactor * x);
    }

    private int getScaledY(int y) {
        if (image == null) {
            return 0;
        }
        return (int) (zoomFactor * y);
    }

    private class ScallingImageObserver implements ImageObserver {

        @Override public boolean imageUpdate(Image img, int infoFlags, int x, int y, int width, int height) {
            // Update image size when changed during GIF animation
            if ((infoFlags & ImageObserver.WIDTH) > 0) {
                imageWidth = image.getWidth(null);
            }
            if ((infoFlags & ImageObserver.HEIGHT) > 0) {
                imageHeight = image.getHeight(null);
            }
            if ((infoFlags & (ImageObserver.WIDTH + ImageObserver.HEIGHT)) > 0) {
                notifySizeChanged();
            }

            // Notify image updated, but adjust affected position according to current zoom level
            int frameWidth = getWidth();
            int frameHeight = getHeight();

            final int scaledWidth = getScaledX(imageWidth);
            final int scaledHeight = getScaledY(imageHeight);

            final int offsetX = Math.max(0, (frameWidth - scaledWidth) / 2);
            final int offsetY = Math.max(0, (frameHeight - scaledHeight) / 2);

            int adjX = x + offsetX;
            int adjY = y + offsetY;
            int adjWidth = getScaledX((int) (width + zoomFactor + 1)) + offsetX;
            int adjHeight = getScaledY((int) (height + zoomFactor + 1)) + offsetY;
            return ImageViewerPanel.this.imageUpdate(img, infoFlags, adjX, adjY, adjWidth, adjHeight);
        }
    }
}
