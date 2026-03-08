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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

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

    // -------------------------------------------------------------------------

    /**
     * Renders the current path as a horizontal row of hyperlink-style labels
     * separated by {@code ›} glyphs.  Clicking a label navigates the owning
     * {@link FolderPanel} to the corresponding ancestor directory.
     *
     * <p>Handles:
     * <ul>
     *   <li>Unix absolute paths: {@code /home/user/docs}</li>
     *   <li>Windows absolute paths: {@code C:\Users\name\docs}</li>
     *   <li>UNC paths: {@code \\server\share\folder}</li>
     *   <li>URL-scheme paths (sftp, ftp, …): displayed as a single
     *       non-navigable label to avoid mangling the URL.</li>
     * </ul>
     */
    private class BreadcrumbBar extends JPanel {

        /** Normal link colour — visible on light and most medium backgrounds. */
        private final Color LINK_COLOR       = new Color(0x2874A6);
        /** Darker shade shown on mouse-over. */
        private final Color LINK_HOVER_COLOR = new Color(0x1A5276);

        BreadcrumbBar() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));
            setOpaque(true);
            // Fresh border from UIManager — sharing the instance from locationTextField
            // would corrupt both components on L&Fs that tie border state to the owner.
            setBorder(UIManager.getBorder("TextField.border"));
        }

        /** Rebuilds the breadcrumb labels for the given path string. */
        void setPath(String path) {
            removeAll();

            if (path == null || path.isEmpty()) {
                revalidate();
                repaint();
                return;
            }

            if (path.contains("://")) {
                // URL scheme (sftp://, ftp://, …) — show as non-navigable label
                add(makePlainLabel(path));
            } else {
                buildLocalBreadcrumbs(path);
            }

            revalidate();
            repaint();
        }

        /**
         * Walks up the file hierarchy from the given local path to the root,
         * then renders each ancestor as a clickable link separated by {@code ›}.
         */
        private void buildLocalBreadcrumbs(String path) {
            // Collect ancestors from root to the current directory
            Deque<File> stack = new ArrayDeque<>();
            File f = new File(path);
            while (f != null) {
                stack.push(f);          // push so the top of deque is the root
                f = f.getParentFile();
            }

            boolean first = true;
            for (File ancestor : stack) {
                if (!first)
                    add(makeSeparatorLabel());
                first = false;

                String name = ancestor.getName();
                if (name.isEmpty()) {
                    // Root: getName() returns "" for "C:\" or "/"
                    String abs = ancestor.getAbsolutePath();
                    // Strip trailing separator so we show "C:" not "C:\" or "" not "/"
                    if (abs.endsWith(File.separator) && abs.length() > File.separator.length())
                        abs = abs.substring(0, abs.length() - File.separator.length());
                    name = abs.isEmpty() ? File.separator : abs;
                }

                add(makeLinkLabel(name, ancestor.getAbsolutePath()));
            }
        }

        /** A label that looks and behaves like a hyperlink. */
        private JLabel makeLinkLabel(String text, String targetPath) {
            JLabel lbl = new JLabel("<html><u>" + escapeHtml(text) + "</u></html>");
            lbl.setForeground(LINK_COLOR);
            lbl.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    folderPanel.tryChangeCurrentFolder(targetPath);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    lbl.setForeground(LINK_HOVER_COLOR);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    lbl.setForeground(LINK_COLOR);
                }
            });
            return lbl;
        }

        /** The {@code ›} glyph rendered between path segments. */
        private JLabel makeSeparatorLabel() {
            JLabel sep = new JLabel(" \u203A "); // single right-pointing angle quotation mark
            sep.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
            sep.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
            return sep;
        }

        /** A non-interactive label for paths that cannot be segmented (e.g. URLs). */
        private JLabel makePlainLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
            lbl.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
            return lbl;
        }

        /** Escapes the minimal HTML special characters that can appear in file names. */
        private String escapeHtml(String text) {
            return text.replace("&", "&amp;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;");
        }
    }
}
