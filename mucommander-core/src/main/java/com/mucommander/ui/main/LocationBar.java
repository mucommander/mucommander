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
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

/**
 * A wrapper panel that sits in place of the location text field in each FolderPanel.
 *
 * <p>It hosts two cards in a {@link CardLayout}:
 * <ul>
 *   <li>The regular {@link LocationTextField} (always visible by default).</li>
 *   <li>A {@link BreadcrumbBar} that renders the current path as a row of
 *       clickable buttons, one per directory segment.</li>
 * </ul>
 *
 * <p>While the Ctrl key is held <em>and</em> the location text field does not have
 * keyboard focus (i.e. the user is not actively editing the path), the breadcrumb
 * card is shown.  Releasing Ctrl restores the text-field card.
 */
public class LocationBar extends JPanel {

    private static final String CARD_TEXT_FIELD = "textField";
    private static final String CARD_BREADCRUMB = "breadcrumb";

    private final LocationTextField locationTextField;
    private final FolderPanel folderPanel;
    private final BreadcrumbBar breadcrumbBar;
    private final CardLayout cardLayout;

    public LocationBar(FolderPanel folderPanel, LocationTextField locationTextField) {
        this.folderPanel = folderPanel;
        this.locationTextField = locationTextField;

        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);

        breadcrumbBar = new BreadcrumbBar();

        add(locationTextField, CARD_TEXT_FIELD);
        add(breadcrumbBar, CARD_BREADCRUMB);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() != KeyEvent.VK_CONTROL)
                return false;

            if (e.getID() == KeyEvent.KEY_PRESSED && !locationTextField.hasFocus()) {
                SwingUtilities.invokeLater(() -> {
                    breadcrumbBar.setPath(locationTextField.getText());
                    cardLayout.show(LocationBar.this, CARD_BREADCRUMB);
                });
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                SwingUtilities.invokeLater(() -> cardLayout.show(LocationBar.this, CARD_TEXT_FIELD));
            }
            return false; // never consume — other Ctrl shortcuts must keep working
        });
    }

    // -------------------------------------------------------------------------

    /**
     * Renders the current path as a horizontal row of clickable buttons separated
     * by the platform file separator character.  Clicking any button navigates
     * the owning {@link FolderPanel} to the corresponding ancestor directory.
     *
     * <p>Non-local paths (those containing {@code ://}) are shown as a single
     * non-navigable label so that URL-scheme locations are not mangled.
     */
    private class BreadcrumbBar extends JPanel {

        BreadcrumbBar() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));
            setOpaque(true);
        }

        /** Rebuilds the breadcrumb buttons for the given path string. */
        void setPath(String path) {
            removeAll();

            if (path == null || path.isEmpty()) {
                revalidate();
                repaint();
                return;
            }

            // Non-local URL: show as a single non-clickable label
            if (path.contains("://")) {
                add(makeLabel(path));
                revalidate();
                repaint();
                return;
            }

            // Walk up the file hierarchy to collect ancestors (root → current)
            Deque<File> stack = new ArrayDeque<>();
            File f = new File(path);
            while (f != null) {
                stack.push(f);
                f = f.getParentFile();
            }

            boolean first = true;
            for (File ancestor : stack) {
                if (!first)
                    add(makeSeparatorLabel());
                first = false;

                String name = ancestor.getName();
                if (name.isEmpty()) {
                    // Root directory: use absolute path but strip trailing separator
                    String abs = ancestor.getAbsolutePath();
                    name = abs.endsWith(File.separator)
                            ? abs.substring(0, abs.length() - File.separator.length())
                            : abs;
                    if (name.isEmpty())
                        name = File.separator; // Unix root "/"
                }

                add(makeSegmentButton(name, ancestor.getAbsolutePath()));
            }

            revalidate();
            repaint();
        }

        private JButton makeSegmentButton(String label, String targetPath) {
            JButton btn = new JButton(label);
            btn.setFocusable(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
            btn.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
            btn.addActionListener(e -> folderPanel.tryChangeCurrentFolder(targetPath));
            return btn;
        }

        private JLabel makeSeparatorLabel() {
            JLabel sep = new JLabel(File.separator);
            sep.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
            sep.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
            return sep;
        }

        private JLabel makeLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
            lbl.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
            return lbl;
        }
    }
}
