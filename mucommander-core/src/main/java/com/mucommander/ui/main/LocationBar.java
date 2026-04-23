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

package com.mucommander.ui.main;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * A wrapper panel that sits in place of the location text field in each FolderPanel.
 *
 * <p>It hosts two cards in a {@link CardLayout}:
 * <ul>
 *   <li>The regular {@link LocationTextField} (always visible by default).</li>
 *   <li>A {@link BreadcrumbBar} that renders the current path as a row of
 *       clickable hyperlink-style labels, one per directory segment, separated
 *       by {@code ›} glyphs.</li>
 * </ul>
 *
 * <p>When the Ctrl key (or Meta on macOS) is held for {@value #BREADCRUMB_SHOW_DELAY_MS}ms
 * <em>and</em> the location text field does not have keyboard focus (i.e. the user is not
 * actively editing the path), the breadcrumb card is shown. The delayed appearance prevents
 * the breadcrumb from flickering during quick keyboard shortcuts (Ctrl+C, Ctrl+V, etc.).
 *
 * <p>To reduce visual noise, the breadcrumb is only shown for the panel where the mouse is
 * currently hovering. If the mouse is not over either folder panel, breadcrumbs are shown
 * in both panels. The breadcrumb dynamically follows the mouse - if the user moves the mouse
 * from one panel to another while still holding Ctrl, the breadcrumb switches accordingly.
 * The breadcrumb is immediately hidden when Ctrl is released, or if any other key is pressed
 * or the mouse is clicked before the delay expires.
 */
public class LocationBar extends JPanel {

    private static final String CARD_TEXT_FIELD = "textField";
    private static final String CARD_BREADCRUMB = "breadcrumb";

    /** Delay in milliseconds before showing breadcrumb when Ctrl/Meta is held */
    private static final int BREADCRUMB_SHOW_DELAY_MS = 250;

    private final FolderPanel folderPanel;
    private final LocationTextField locationTextField;
    private final BreadcrumbBar breadcrumbBar;
    private final CardLayout cardLayout;

    /** Timer that delays showing the breadcrumb to avoid noise from quick keyboard shortcuts */
    private final Timer showBreadcrumbTimer;

    /** Tracks whether Ctrl/Meta is currently being held down */
    private boolean modifierHeld;

    public LocationBar(FolderPanel folderPanel, LocationTextField locationTextField) {
        this.folderPanel = folderPanel;
        this.locationTextField = locationTextField;

        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);

        breadcrumbBar = new BreadcrumbBar(folderPanel);

        add(locationTextField, CARD_TEXT_FIELD);
        add(breadcrumbBar, CARD_BREADCRUMB);

        // On macOS the menu shortcut key is Cmd (META); on all other platforms it is Ctrl.
        // We want Ctrl to always work, and Cmd to also work on macOS.
        final int menuShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        final boolean isMac = menuShortcutMask == InputEvent.META_DOWN_MASK;

        // Initialize the timer that delays showing the breadcrumb
        showBreadcrumbTimer = new Timer(BREADCRUMB_SHOW_DELAY_MS, e -> {
            // Only show breadcrumb if:
            // 1. Text field is currently visible (not already showing breadcrumb)
            // 2. Mouse is over this panel OR mouse is over neither panel
            if (locationTextField.isShowing() && shouldShowBreadcrumbForMousePosition()) {
                breadcrumbBar.setFile(folderPanel.getCurrentFolder());
                cardLayout.show(LocationBar.this, CARD_BREADCRUMB);
            }
        });
        showBreadcrumbTimer.setRepeats(false);

        // Listen for mouse events globally
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent) {
                var mouseEvent = (MouseEvent) event;
                switch (mouseEvent.getID()) {
                // Cancel timer on mouse clicks
                case MouseEvent.MOUSE_PRESSED:
                    cancelBreadcrumbTimer();
                    break;
                // Update breadcrumb visibility when mouse moves while modifier is held
                case MouseEvent.MOUSE_MOVED:
                    if (modifierHeld) {
                        updateBreadcrumbVisibility();
                    }
                }
            }
        }, java.awt.AWTEvent.MOUSE_EVENT_MASK | java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            boolean isCtrl = e.getKeyCode() == KeyEvent.VK_CONTROL;
            boolean isMeta = isMac && e.getKeyCode() == KeyEvent.VK_META;

            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (isCtrl || isMeta) {
                    // Ctrl/Meta pressed - start the timer to show breadcrumb after delay
                    if (!locationTextField.hasFocus() && !showBreadcrumbTimer.isRunning()) {
                        modifierHeld = true;
                        showBreadcrumbTimer.restart();
                    }
                } else {
                    // Any other key pressed - cancel the timer to prevent breadcrumb from showing
                    // This filters out keyboard shortcuts like Ctrl+C, Ctrl+V, etc.
                    cancelBreadcrumbTimer();
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                if (isCtrl || isMeta) {
                    // Ctrl/Meta released - cancel timer and hide breadcrumb if showing
                    cancelBreadcrumbTimer();

                    // Hide breadcrumb only when neither trigger modifier remains held
                    boolean ctrlStillHeld = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
                    boolean metaStillHeld = isMac && (e.getModifiersEx() & InputEvent.META_DOWN_MASK) != 0;
                    if (!ctrlStillHeld && !metaStillHeld) {
                        modifierHeld = false;
                        SwingUtilities.invokeLater(() -> cardLayout.show(LocationBar.this, CARD_TEXT_FIELD));
                    }
                }
            }
            return false; // never consume — other Ctrl shortcuts must keep working
        });
    }

    /**
     * Cancels the breadcrumb show timer if it's running.
     */
    private void cancelBreadcrumbTimer() {
        if (showBreadcrumbTimer.isRunning()) {
            showBreadcrumbTimer.stop();
        }
    }

    /**
     * Updates the breadcrumb visibility based on current mouse position.
     * Shows breadcrumb if mouse is over this panel or neither panel.
     * Hides breadcrumb if mouse is over the other panel.
     * Called when mouse moves while Ctrl/Meta is held.
     */
    private void updateBreadcrumbVisibility() {
        SwingUtilities.invokeLater(() -> {
            boolean shouldShow = shouldShowBreadcrumbForMousePosition();
            boolean isShowingBreadcrumb = breadcrumbBar.isShowing();

            if (shouldShow && !isShowingBreadcrumb) {
                // Mouse moved to this panel or neutral area - show breadcrumb
                breadcrumbBar.setFile(folderPanel.getCurrentFolder());
                cardLayout.show(LocationBar.this, CARD_BREADCRUMB);
            } else if (!shouldShow && isShowingBreadcrumb) {
                // Mouse moved to other panel - hide breadcrumb
                cardLayout.show(LocationBar.this, CARD_TEXT_FIELD);
            }
        });
    }

    /**
     * Determines whether the breadcrumb should be shown based on the current mouse position.
     * Returns true if:
     * - Mouse is over this panel, OR
     * - Mouse is over neither panel (show in both)
     * Returns false if:
     * - Mouse is over the other panel (don't show noise in the non-hovered panel)
     */
    private boolean shouldShowBreadcrumbForMousePosition() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            // Can't determine mouse position, show breadcrumb to be safe
            return true;
        }

        Point mouseLocation = pointerInfo.getLocation();

        // Check if mouse is over this panel
        JPanel thisPanel = folderPanel.getPanel();
        if (isMouseOverComponent(thisPanel, mouseLocation)) {
            return true;
        }

        // Check if mouse is over the other panel
        MainFrame mainFrame = folderPanel.getMainFrame();
        FolderPanel leftPanel = mainFrame.getLeftPanel();
        FolderPanel rightPanel = mainFrame.getRightPanel();
        FolderPanel otherPanel = (folderPanel == leftPanel) ? rightPanel : leftPanel;

        if (otherPanel != null) {
            JPanel otherPanelComponent = otherPanel.getPanel();
            if (isMouseOverComponent(otherPanelComponent, mouseLocation)) {
                // Mouse is over the other panel, don't show breadcrumb here
                return false;
            }
        }

        // Mouse is over neither panel, show breadcrumb in both
        return true;
    }

    /**
     * Checks if the mouse at the given screen location is over the specified component.
     */
    private boolean isMouseOverComponent(Component component, Point screenLocation) {
        if (component == null || !component.isShowing()) {
            return false;
        }

        Point componentLocation = new Point(screenLocation);
        SwingUtilities.convertPointFromScreen(componentLocation, component);
        return component.contains(componentLocation);
    }

    /**
     * Always report the text field's preferred size so that switching cards does
     * not cause the location bar row to grow or shrink.
     */
    @Override
    public Dimension getPreferredSize() {
        return locationTextField.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return locationTextField.getMinimumSize();
    }
}
