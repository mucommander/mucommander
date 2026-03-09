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
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.mucommander.commons.file.AbstractFile;
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
                    breadcrumbBar.setFile(folderPanel.getCurrentFolder());
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
     * Renders the current directory as a horizontal row of hyperlink-style labels
     * separated by {@code ›} glyphs.  Clicking a label navigates the owning
     * {@link FolderPanel} to the corresponding ancestor directory.
     *
     * <p>Uses {@link AbstractFile#getParent()} to walk the hierarchy, so it works
     * uniformly for local paths (Windows, Unix) and remote file systems (SFTP, FTP…).
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

        /**
         * Rebuilds the breadcrumb labels for the given {@link AbstractFile}.
         * Walks up via {@link AbstractFile#getParent()} to collect all ancestors,
         * then renders them root-first.
         */
        void setFile(AbstractFile file) {
            removeAll();

            if (file == null) {
                revalidate();
                repaint();
                return;
            }

            // Collect ancestors from current directory up to the root
            Deque<AbstractFile> stack = new ArrayDeque<>();
            AbstractFile f = file;
            while (f != null) {
                stack.push(f);          // push → top of deque is the root after the loop
                f = f.getParent();
            }

            boolean first = true;
            for (AbstractFile ancestor : stack) {
                if (!first)
                    add(makeSeparatorLabel());
                first = false;

                add(makeLinkLabel(displayName(ancestor), ancestor.getAbsolutePath()));
            }

            revalidate();
            repaint();
        }

        /**
         * Returns a human-readable label for a breadcrumb segment.
         * For most files this is simply {@link AbstractFile#getName()}.
         * For root directories (where {@code getName()} returns an empty string)
         * the absolute path is used instead, with any trailing separator stripped.
         */
        private String displayName(AbstractFile file) {
            String name = file.getName();
            if (!name.isEmpty())
                return name;

            // Root directory: derive a clean label from the absolute path
            String abs = file.getAbsolutePath();
            String sep = file.getURL().getPathSeparator();
            if (abs.endsWith(sep) && abs.length() > sep.length())
                abs = abs.substring(0, abs.length() - sep.length());
            return abs.isEmpty() ? sep : abs;
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

        /** Escapes the minimal HTML special characters that can appear in file names. */
        private String escapeHtml(String text) {
            return text.replace("&", "&amp;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;");
        }
    }
}
