/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.viewer.image;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.ViewerPresenter;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple image viewer, capable of displaying <code>PNG</code>, <code>GIF</code> and <code>JPEG</code> images.
 *
 * @author Maxence Bernard, Arik Hadas
 */
@ParametersAreNonnullByDefault
class ImageViewer implements FileViewer, ActionListener {

    private static final double ZOOM_RATE = 1.5;
    private static final double MAX_ZOOM = Math.pow(ZOOM_RATE, 8);
    private static final double MIN_ZOOM = Math.pow(ZOOM_RATE, -8);
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageViewer.class);

    private ViewerPresenter presenter;
    private JScrollPane ui = new JScrollPane();

    private InitialViewMode initialViewMode = InitialViewMode.RESIZE_WINDOW;
    private Image image;
    private int imageWidth;
    private int imageHeight;
    private double zoomFactor;

    /** Menu bar */
    // Menus //
    private JMenu viewMenu;
    private JMenu controlsMenu;
    // Items //
    private JMenuItem prevImageItem;
    private JMenuItem nextImageItem;
    private JMenuItem zoomToActualSize;
    private JMenuItem zoomToFit;
    private JMenuItem zoomInItem;
    private JMenuItem zoomOutItem;

    private ImageViewerImpl imageViewerImpl;
    private ImageFileViewerService imageFileViewerService;

    public ImageViewer(ImageFileViewerService imageFileViewerService) {
        this.imageFileViewerService = imageFileViewerService;
        imageViewerImpl = new ImageViewerImpl();

        ui.setWheelScrollingEnabled(false);
        ui.getViewport().setView(imageViewerImpl);

        // Create Go menu
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        viewMenu = MenuToolkit.addMenu(Translator.get("image_viewer.view_menu"), menuMnemonicHelper, null);
        zoomToActualSize = MenuToolkit.addMenuItem(viewMenu,
                Translator.get("image_viewer.zoom_to_actual_size"),
                menuMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK),
                this);
        zoomToFit = MenuToolkit.addMenuItem(viewMenu,
                Translator.get("image_viewer.zoom_to_fit"),
                menuMnemonicHelper,
                null,
                this);
        zoomInItem = MenuToolkit.addMenuItem(viewMenu,
                Translator.get("image_viewer.zoom_in"),
                menuMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0),
                this);
        zoomOutItem = MenuToolkit.addMenuItem(viewMenu,
                Translator.get("image_viewer.zoom_out"),
                menuMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0),
                this);

        controlsMenu = MenuToolkit.addMenu(Translator.get("image_viewer.controls_menu"), menuMnemonicHelper, null);
        nextImageItem = MenuToolkit.addMenuItem(controlsMenu,
                Translator.get("image_viewer.next_image"),
                menuMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                this);
        prevImageItem = MenuToolkit.addMenuItem(controlsMenu,
                Translator.get("image_viewer.previous_image"),
                menuMnemonicHelper,
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                this);
    }

    @Nonnull
    @Override
    public JComponent getUI() {
        return ui;
    }

    @Override
    public void setPresenter(ViewerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void extendMenu(JMenuBar menuBar) {
        menuBar.add(viewMenu);
        menuBar.add(controlsMenu);
    }

    private synchronized void loadImage(AbstractFile file) throws IOException {
        presenter.getWindowFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        int read;
        byte buffer[] = new byte[1024];
        InputStream in;
        byte[] imageBytes;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            in = file.getInputStream();
            while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                bout.write(buffer, 0, read);
            }
            imageBytes = bout.toByteArray();
        }
        in.close();

        this.image = imageViewerImpl.getToolkit().createImage(imageBytes);

        waitForImage(image);

        imageWidth = image.getWidth(null);
        imageHeight = image.getHeight(null);
        presenter.getWindowFrame().setCursor(Cursor.getDefaultCursor());

        switch (initialViewMode) {
        case RESIZE_WINDOW:
            // Fit to screen size / autozoom and resize window
            zoomFactor = 1;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            while (imageWidth * zoomFactor > d.width || imageHeight * zoomFactor > d.height) {
                zoomFactor = zoomFactor / ZOOM_RATE;
                if (zoomFactor < MIN_ZOOM) {
                    zoomFactor = MIN_ZOOM;
                    break;
                }
            }
            zoom(zoomFactor, null);
            presenter.getWindowFrame().pack();
            break;
        case FIT_TO_WINDOW:
            zoomToFit();
            break;
        default:
            zoom(1.0, null);
        }

        ui.revalidate();
        ui.repaint();
        updateStatus();
    }

    private void waitForImage(Image image) {
        // AppLogger.finest("Waiting for image to load "+image);
        MediaTracker tracker = new MediaTracker(imageViewerImpl);
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
        }
        tracker.removeImage(image);
        // AppLogger.finest("Image loaded "+image);
    }

    private synchronized void zoom(double factor, @Nullable Point focusPoint) {
        if (factor == zoomFactor) {
            return;
        }

        double oldFactor = zoomFactor;

        JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, imageViewerImpl);
        Rectangle view;
        Point targetViewportPos = new Point();
        if (viewPort != null) {
            view = viewPort.getViewRect();
            if (focusPoint != null) {
                int offsetX = focusPoint.x - view.x;
                int offsetY = focusPoint.y - view.y;
                targetViewportPos.x = (int) (Math.round(focusPoint.x / oldFactor * factor)) - offsetX;
                targetViewportPos.y = (int) (Math.round(focusPoint.y / oldFactor * factor)) - offsetY;
            } else {
                // Focus center of the viewport instead
                int offsetX = (view.width / 2);
                int offsetY = (view.height / 2);
                targetViewportPos.x = (int) (Math.round(view.x + offsetX) / oldFactor * factor) - offsetX;
                targetViewportPos.y = (int) (Math.round(view.y + offsetY) / oldFactor * factor) - offsetY;
            }
        }

        this.zoomFactor = factor;

        if (viewPort != null) {
            view = viewPort.getViewRect();
            view.x = targetViewportPos.x;
            view.y = targetViewportPos.y;

            imageViewerImpl.setSize(imageViewerImpl.getPreferredSize());
            ui.getViewport().setViewPosition(targetViewportPos);
        }

        imageViewerImpl.invalidate();
        ui.invalidate();
        ui.repaint();

        updateStatus();
    }

    private void updateStatus() {
        presenter.extendTitle(this.getTitleExt());
        zoomInItem.setEnabled(zoomFactor < MAX_ZOOM);
        zoomOutItem.setEnabled(zoomFactor > MIN_ZOOM);
    }

    private void goToImage(Function<Integer, Integer> advance) {
        try {
            presenter.goToFile(advance, imageFileViewerService);
        } catch (IOException e) {
            LOGGER.error("failed to load next/prev image", e);
        }
    }

    ///////////////////////////////
    // FileViewer implementation //
    ///////////////////////////////

    @Override
    public void open(AbstractFile file) throws IOException {
        loadImage(file);
    }

    @Override
    public void close() {
    }

    @Nonnull
    public String getTitleExt() {
        return " - " + imageWidth + "x" + imageHeight + " - " + ((int) (zoomFactor * 100)) + "%";
    }

    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == prevImageItem) {
            goToImage(i -> i - 1);
        } else if (source == nextImageItem) {
            goToImage(i -> i + 1);
        } else if (source == zoomToActualSize) {
            zoom(1, getMousePoint());
        } else if (source == zoomToFit) {
            zoomToFit();
        } else if (source == zoomInItem && zoomInItem.isEnabled()) {
            zoom(zoomFactor * ZOOM_RATE < MAX_ZOOM ? zoomFactor * ZOOM_RATE : MAX_ZOOM, getMousePoint());
        } else if (source == zoomOutItem && zoomOutItem.isEnabled()) {
            zoom(zoomFactor / ZOOM_RATE > MIN_ZOOM ? zoomFactor / ZOOM_RATE : MIN_ZOOM, getMousePoint());
        }
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

    @Nullable
    private Point getMousePoint() {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        JViewport viewport = ui.getViewport();
        SwingUtilities.convertPointFromScreen(mousePoint, viewport);
        if (viewport.contains(mousePoint)) {
            Point viewPosition = viewport.getViewPosition();
            return new Point(mousePoint.x + viewPosition.x, mousePoint.y + viewPosition.y);
        }

        return null;
    }

    private void zoomToFit() {
        JViewport viewport = ui.getViewport();
        double zoomToWidth = (double) viewport.getWidth() / imageWidth;
        double zoomToHeight = (double) viewport.getHeight() / imageHeight;
        double zoom = Math.min(zoomToWidth, zoomToHeight);
        if (zoom < MIN_ZOOM) {
            zoom = MIN_ZOOM;
        }
        if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
        }
        zoom(zoom, getMousePoint());
    }

    @ParametersAreNonnullByDefault
    private class ImageViewerImpl extends JPanel implements ThemeListener {

        private Color backgroundColor;

        ImageViewerImpl() {
            backgroundColor = ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR);
            ThemeManager.addCurrentThemeListener(this);

            MouseAdapter ma = new MouseAdapter() {

                private Point origin;

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.isControlDown() || e.isMetaDown()) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            zoom(zoomFactor * ZOOM_RATE < MAX_ZOOM ? zoomFactor * ZOOM_RATE : MAX_ZOOM, e.getPoint());
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            zoom(zoomFactor / ZOOM_RATE > MIN_ZOOM ? zoomFactor / ZOOM_RATE : MIN_ZOOM, e.getPoint());
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        origin = new Point(e.getPoint());
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        origin = null;
                        setCursor(Cursor.getDefaultCursor());
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (origin != null) {
                        JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class,
                                ImageViewerImpl.this);
                        if (viewPort != null) {
                            int deltaX = origin.x - e.getX();
                            int deltaY = origin.y - e.getY();

                            Rectangle view = viewPort.getViewRect();
                            view.x += deltaX;
                            view.y += deltaY;

                            scrollRectToVisible(view);
                        }
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double rotation = e.getPreciseWheelRotation();
                    if (rotation == 0) {
                        return;
                    }

                    if (rotation > 0) {
                        double scaleDiff = rotation * ZOOM_RATE;
                        zoom(zoomFactor / scaleDiff > MIN_ZOOM ? zoomFactor / scaleDiff : MIN_ZOOM, e.getPoint());
                    } else {
                        double scaleDiff = (-rotation) * ZOOM_RATE;
                        zoom(zoomFactor * scaleDiff < MAX_ZOOM ? zoomFactor * scaleDiff : MAX_ZOOM, e.getPoint());
                    }
                    e.consume();
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
            addMouseWheelListener(ma);
        }

        ////////////////////////
        // Overridden methods //
        ////////////////////////

        @Override
        public void paint(Graphics g) {
            int frameWidth = getWidth();
            int frameHeight = getHeight();

            g.setColor(backgroundColor);
            g.fillRect(0, 0, frameWidth, frameHeight);

            final int scaledWidth = getScaledWidth();
            final int scaledHeight = getScaledHeight();
            final int offsetX = Math.max(0, (frameWidth - scaledWidth) / 2);
            final int offsetY = Math.max(0, (frameHeight - scaledHeight) / 2);

            g.drawImage(image, offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight,
                    0, 0, imageWidth, imageHeight, null, null);
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
    }

    public static enum InitialViewMode {
        NATIVE_SIZE,
        FIT_TO_WINDOW,
        RESIZE_WINDOW
    }
}
