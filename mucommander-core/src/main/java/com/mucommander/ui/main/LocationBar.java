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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
 * <p>While the Ctrl key is held <em>and</em> the location text field does not
 * have keyboard focus (i.e. the user is not actively editing the path), the
 * breadcrumb card is shown.  Releasing Ctrl restores the text-field card.
 */
public class LocationBar extends JPanel {

    private static final String CARD_TEXT_FIELD = "textField";
    private static final String CARD_BREADCRUMB = "breadcrumb";

    private final LocationTextField locationTextField;
    private final BreadcrumbBar breadcrumbBar;
    private final CardLayout cardLayout;

    public LocationBar(FolderPanel folderPanel, LocationTextField locationTextField) {
        this.locationTextField = locationTextField;

        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);

        breadcrumbBar = new BreadcrumbBar(folderPanel);

        add(locationTextField, CARD_TEXT_FIELD);
        add(breadcrumbBar, CARD_BREADCRUMB);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() != KeyEvent.VK_CONTROL) {
                return false;
            }

            if (e.getID() == KeyEvent.KEY_PRESSED && !locationTextField.hasFocus()) {
                SwingUtilities.invokeLater(() -> {
                    // Guard against Windows key-repeat: Ctrl held down fires KEY_PRESSED
                    // repeatedly, which would call setFile() on every repeat event and
                    // rebuild all breadcrumb labels from scratch each time → flicker.
                    // Only (re)build when the text-field card is currently visible.
                    if (locationTextField.isShowing()) {
                        breadcrumbBar.setFile(folderPanel.getCurrentFolder());
                        cardLayout.show(LocationBar.this, CARD_BREADCRUMB);
                    }
                });
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                SwingUtilities.invokeLater(() -> cardLayout.show(LocationBar.this, CARD_TEXT_FIELD));
            }
            return false; // never consume — other Ctrl shortcuts must keep working
        });
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
