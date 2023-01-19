package com.mucommander.viewer.image.ui;

import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

/**
 * Image viewer UI.
 */
@ParametersAreNonnullByDefault
public class ImageViewerPanel extends JPanel implements ThemeListener {

    private Image image;
    private int imageWidth;
    private int imageHeight;
    private double zoomFactor;
    private Color backgroundColor;

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
        setSize(getPreferredSize());
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

        final int scaledWidth = getScaledWidth();
        final int scaledHeight = getScaledHeight();

        final int offsetX = Math.max(0, (frameWidth - scaledWidth) / 2);
        final int offsetY = Math.max(0, (frameHeight - scaledHeight) / 2);
        g.drawImage(image, offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight,
                0, 0, imageWidth, imageHeight, backgroundColor, this);
    }

    @Nonnull
    @Override
    public synchronized Dimension getPreferredSize() {
        return image == null ? new Dimension(320, 200) : new Dimension(getScaledWidth(), getScaledHeight());
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

    private int getScaledWidth() {
        if (image == null) {
            return 0;
        }
        return (int) (zoomFactor * imageWidth);
    }

    private int getScaledHeight() {
        if (image == null) {
            return 0;
        }
        return (int) (zoomFactor * imageHeight);
    }
}
