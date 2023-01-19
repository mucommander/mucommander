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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.viewer.FileViewer;
import com.mucommander.viewer.ViewerPresenter;
import com.mucommander.viewer.image.ui.ImageStatusPanel;
import com.mucommander.viewer.image.ui.ImageViewerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

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
    private JPanel ui = new JPanel(new BorderLayout());
    private JScrollPane scrollPane = new JScrollPane();
    private ImageStatusPanel statusPanel = new ImageStatusPanel();

    private InitialZoom initialZoom = InitialZoom.RESIZE_WINDOW;
    private boolean firstZoomPerformed = false;

    /** Menu bar */
    // Menus //
    private JMenu viewMenu;
    private JMenu controlsMenu;
    private JMenu viewInitialZoomMenu;

    // Items //
    private JMenuItem prevImageItem;
    private JMenuItem nextImageItem;
    private JMenuItem zoomToActualSize;
    private JMenuItem zoomToFit;
    private JMenuItem zoomInItem;
    private JMenuItem zoomOutItem;
    private JRadioButtonMenuItem viewAsNativeMenuItem;
    private JRadioButtonMenuItem viewAsFitToViewMenuItem;
    private JRadioButtonMenuItem viewAsResizeWindowMenuItem;
    private AbstractAction viewStatusBarAction;
    private JCheckBoxMenuItem viewStatusBarMenuItem;

    private ImageViewerPanel imageViewerPanel;
    private ImageFileViewerService imageFileViewerService;

    public ImageViewer(ImageFileViewerService imageFileViewerService) {
        this.imageFileViewerService = imageFileViewerService;
        imageViewerPanel = new ImageViewerPanel();
        ui.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (firstZoomPerformed) {
                    return;
                }

                if (ui.getWidth() > 0) {
                    initialZoom();
                    firstZoomPerformed = true;
                    ui.removeComponentListener(this);
                }
            }
        });

        init();
    }

    private void init() {
        ui.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.getViewport().setView(imageViewerPanel);

        MouseAdapter mouseAdapter = new MouseAdapter() {

            private Point origin;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isControlDown()) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        double zoomFactor = imageViewerPanel.getZoomFactor();
                        zoom(Math.min(zoomFactor * ZOOM_RATE, MAX_ZOOM), e.getPoint());
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        double zoomFactor = imageViewerPanel.getZoomFactor();
                        zoom(Math.max(zoomFactor / ZOOM_RATE, MIN_ZOOM), e.getPoint());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    origin = new Point(e.getPoint());
                    imageViewerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    origin = null;
                    imageViewerPanel.setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class,
                            imageViewerPanel);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;

                        imageViewerPanel.scrollRectToVisible(view);
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
                    double zoomFactor = imageViewerPanel.getZoomFactor();
                    zoom(Math.max(zoomFactor / scaleDiff, MIN_ZOOM), e.getPoint());
                } else {
                    double scaleDiff = (-rotation) * ZOOM_RATE;
                    double zoomFactor = imageViewerPanel.getZoomFactor();
                    zoom(Math.min(zoomFactor * scaleDiff, MAX_ZOOM), e.getPoint());
                }
                e.consume();
            }
        };

        imageViewerPanel.addMouseListener(mouseAdapter);
        imageViewerPanel.addMouseMotionListener(mouseAdapter);
        imageViewerPanel.addMouseWheelListener(mouseAdapter);

        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        viewInitialZoomMenu =
                MenuToolkit.addMenu(Translator.get("image_viewer.initial_zoom_menu"), menuMnemonicHelper, null);
        viewAsNativeMenuItem = new JRadioButtonMenuItem(Translator.get("image_viewer.initial_zoom_as_native"));
        viewAsNativeMenuItem.addActionListener(e -> switchInitialZoomMode(InitialZoom.NATIVE_SIZE));
        viewAsFitToViewMenuItem = new JRadioButtonMenuItem(Translator.get("image_viewer.initial_zoom_fit_to_view"));
        viewAsFitToViewMenuItem.addActionListener(e -> switchInitialZoomMode(InitialZoom.FIT_TO_WINDOW));
        viewAsResizeWindowMenuItem =
                new JRadioButtonMenuItem(Translator.get("image_viewer.initial_zoom_resize_window"));
        viewAsResizeWindowMenuItem.addActionListener(e -> switchInitialZoomMode(InitialZoom.RESIZE_WINDOW));
        ButtonGroup viewAsButtonGroup = new ButtonGroup();
        viewAsButtonGroup.add(viewAsNativeMenuItem);
        viewAsButtonGroup.add(viewAsFitToViewMenuItem);
        viewAsButtonGroup.add(viewAsResizeWindowMenuItem);
        viewInitialZoomMenu.add(viewAsNativeMenuItem);
        viewInitialZoomMenu.add(viewAsFitToViewMenuItem);
        viewInitialZoomMenu.add(viewAsResizeWindowMenuItem);

        viewMenu = MenuToolkit.addMenu(Translator.get("image_viewer.view_menu"), menuMnemonicHelper, null);
        viewMenu.add(viewInitialZoomMenu);
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
        viewMenu.addSeparator();
        viewStatusBarAction = new AbstractAction(Translator.get("image_viewer.view_status_bar")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchShowStatusBar(viewStatusBarMenuItem.isSelected());
            }
        };
        viewStatusBarMenuItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, viewStatusBarAction, menuMnemonicHelper);

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

        String initialZoomValue = ImageViewerPreferences.INITIAL_ZOOM.getValue();
        if (InitialZoom.FIT_TO_WINDOW.getKey().equals(initialZoomValue)) {
            viewAsFitToViewMenuItem.setSelected(true);
            switchInitialZoomMode(InitialZoom.FIT_TO_WINDOW);
        } else if (InitialZoom.RESIZE_WINDOW.getKey().equals(initialZoomValue)) {
            viewAsResizeWindowMenuItem.setSelected(true);
            switchInitialZoomMode(InitialZoom.RESIZE_WINDOW);
        } else {
            viewAsNativeMenuItem.setSelected(true);
            switchInitialZoomMode(InitialZoom.NATIVE_SIZE);
        }
        boolean showStatusBar = Boolean.parseBoolean(ImageViewerPreferences.SHOW_STATUS_BAR.getValue());
        viewStatusBarMenuItem.setSelected(showStatusBar);
        switchShowStatusBar(showStatusBar);
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

        Image image;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            try (InputStream in = file.getInputStream()) {
                StreamUtils.copyStream(in, bout);
                image = imageViewerPanel.getToolkit().createImage(bout.toByteArray());
            }
        }

        waitForImage(image);
        imageViewerPanel.setImage(image);
        initialZoom();
    }

    private void initialZoom() {
        int imageWidth = imageViewerPanel.getImageWidth();
        int imageHeight = imageViewerPanel.getImageHeight();
        presenter.getWindowFrame().setCursor(Cursor.getDefaultCursor());

        switch (initialZoom) {
        case RESIZE_WINDOW:
            // Fit to screen size / autozoom and resize window
            double zoomFactor = 1;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            while (imageWidth * zoomFactor > d.width || imageHeight * zoomFactor > d.height) {
                zoomFactor = zoomFactor / ZOOM_RATE;
                if (zoomFactor < MIN_ZOOM) {
                    zoomFactor = MIN_ZOOM;
                    break;
                }
            }
            setZoomFactor(zoomFactor);
            imageViewerPanel.setSize(imageViewerPanel.getPreferredSize());
            presenter.getWindowFrame().pack();
            DialogToolkit.centerOnScreen(presenter.getWindowFrame());
            break;
        case FIT_TO_WINDOW:
            zoomToFit();
            break;
        default:
            setZoomFactor(1.0);
        }

        ui.revalidate();
        ui.repaint();
        updateStatus();
    }

    private void waitForImage(Image image) {
        // AppLogger.finest("Waiting for image to load "+image);
        MediaTracker tracker = new MediaTracker(imageViewerPanel);
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
        }
        tracker.removeImage(image);
        // AppLogger.finest("Image loaded "+image);
    }

    private synchronized void zoom(double factor, @Nullable Point focusPoint) {
        double zoomFactor = imageViewerPanel.getZoomFactor();
        if (factor == zoomFactor) {
            return;
        }

        JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, imageViewerPanel);
        Rectangle view;
        Point targetViewportPos = new Point();
        if (viewPort != null) {
            view = viewPort.getViewRect();
            if (focusPoint != null) {
                int offsetX = focusPoint.x - view.x;
                int offsetY = focusPoint.y - view.y;
                targetViewportPos.x = (int) Math.round(focusPoint.x / zoomFactor * factor) - offsetX;
                targetViewportPos.y = (int) Math.round(focusPoint.y / zoomFactor * factor) - offsetY;
            } else {
                // Focus center of the viewport instead
                int offsetX = (view.width / 2);
                int offsetY = (view.height / 2);
                targetViewportPos.x = (int) Math.round((view.x + offsetX) / zoomFactor * factor) - offsetX;
                targetViewportPos.y = (int) Math.round((view.y + offsetY) / zoomFactor * factor) - offsetY;
            }
        }

        imageViewerPanel.setZoomFactor(factor);

        if (viewPort != null) {
            imageViewerPanel.setSize(imageViewerPanel.getPreferredSize());
            scrollPane.getViewport().setViewPosition(targetViewportPos);
        } else {
            imageViewerPanel.revalidate();
        }

        ui.revalidate();
        ui.repaint();

        updateStatus();
    }

    private synchronized void setZoomFactor(double factor) {
        imageViewerPanel.setZoomFactor(factor);
        imageViewerPanel.revalidate();

        ui.revalidate();
        ui.repaint();

        updateStatus();
    }

    private void updateStatus() {
        double zoomFactor = imageViewerPanel.getZoomFactor();
        presenter.extendTitle(this.getTitleExt());
        zoomInItem.setEnabled(zoomFactor < MAX_ZOOM);
        zoomOutItem.setEnabled(zoomFactor > MIN_ZOOM);
        statusPanel.setZoomFactor(zoomFactor);
        statusPanel.setImageSize(imageViewerPanel.getImageWidth(), imageViewerPanel.getImageHeight());
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
        imageViewerPanel.close();
    }

    @Nonnull
    public String getTitleExt() {
        double zoomFactor = imageViewerPanel.getZoomFactor();
        int imageWidth = imageViewerPanel.getImageWidth();
        int imageHeight = imageViewerPanel.getImageHeight();
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
            double zoomFactor = imageViewerPanel.getZoomFactor();
            zoom(Math.min(zoomFactor * ZOOM_RATE, MAX_ZOOM), getMousePoint());
        } else if (source == zoomOutItem && zoomOutItem.isEnabled()) {
            double zoomFactor = imageViewerPanel.getZoomFactor();
            zoom(Math.max(zoomFactor / ZOOM_RATE, MIN_ZOOM), getMousePoint());
        }
    }

    @Nullable
    private Point getMousePoint() {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        JViewport viewport = scrollPane.getViewport();
        SwingUtilities.convertPointFromScreen(mousePoint, viewport);
        if (viewport.contains(mousePoint)) {
            Point viewPosition = viewport.getViewPosition();
            return new Point(mousePoint.x + viewPosition.x, mousePoint.y + viewPosition.y);
        }

        return null;
    }

    private void switchInitialZoomMode(InitialZoom mode) {
        initialZoom = mode;
        ImageViewerPreferences.INITIAL_ZOOM.setValue(mode.key);
    }

    private void switchShowStatusBar(boolean showStatusBar) {
        if ((statusPanel.getParent() == ui) != showStatusBar) {
            ImageViewerPreferences.SHOW_STATUS_BAR.setValue(Boolean.toString(showStatusBar));
            if (showStatusBar) {
                ui.add(statusPanel, BorderLayout.SOUTH);
            } else {
                ui.remove(statusPanel);
            }
            ui.revalidate();
            ui.repaint();
        }
    }

    private void zoomToFit() {
        JViewport viewport = scrollPane.getViewport();
        int imageWidth = imageViewerPanel.getImageWidth();
        int imageHeight = imageViewerPanel.getImageHeight();
        double zoomToWidth = (double) viewport.getWidth() / imageWidth;
        double zoomToHeight = (double) viewport.getHeight() / imageHeight;
        double zoom = Math.min(zoomToWidth, zoomToHeight);
        if (zoom < MIN_ZOOM) {
            zoom = MIN_ZOOM;
        }
        if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
        }
        setZoomFactor(zoom);
    }

    public enum InitialZoom {
        NATIVE_SIZE("native"),
        FIT_TO_WINDOW("fit_to_window"),
        RESIZE_WINDOW("resize_window");

        private String key;

        InitialZoom(@Nonnull String key) {
            this.key = key;
        }

        @Nonnull
        public String getKey() {
            return key;
        }
    }
}
