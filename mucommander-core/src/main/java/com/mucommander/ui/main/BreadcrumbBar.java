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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Renders the current directory as a horizontal row of hyperlink-style labels
 * separated by {@code ›} glyphs.  Clicking a label navigates the owning
 * {@link FolderPanel} to the corresponding ancestor directory.
 *
 * <p>Extends {@link JTextField} (rather than {@code JPanel}) so that the
 * look-and-feel paints the correct native border automatically.  On macOS Aqua
 * the border renderer checks {@code instanceof JTextComponent}; a plain
 * {@code JPanel} would not receive the beveled round-rect treatment.
 * {@link #paintComponent} is overridden to suppress text rendering and just
 * fill the interior with the background colour.
 *
 * <p>Uses {@link AbstractFile#getParent()} to walk the hierarchy, so it works
 * uniformly for local paths (Windows, Unix) and remote file systems (SFTP, FTP…).
 */
class BreadcrumbBar extends JTextField {

    /** The {@code ›} glyph rendered between path segments. */
    private static final String SEPARATOR_GLYPH = " \u203A ";

    private static final String HTML_AMP = "&amp;";
    private static final String HTML_LT  = "&lt;";
    private static final String HTML_GT  = "&gt;";

    private final FolderPanel folderPanel;

    BreadcrumbBar(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
        setEditable(false);
        setFocusable(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));
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
            if (!first) {
                add(makeSeparatorLabel());
            }
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
        if (!name.isEmpty()) {
            return name;
        }

        // Root directory: derive a clean label from the absolute path
        String abs = file.getAbsolutePath();
        String sep = file.getURL().getPathSeparator();
        if (abs.endsWith(sep) && abs.length() > sep.length()) {
            abs = abs.substring(0, abs.length() - sep.length());
        }
        return abs.isEmpty() ? sep : abs;
    }

    /** A label that looks and behaves like a hyperlink. */
    private JLabel makeLinkLabel(String text, String targetPath) {
        var escapedText = escapeHtml(text);
        var normalContent = "<html>" + escapedText + "</html>";
        var hoverContent = "<html><u>" + escapedText + "</u></html>";

        JLabel lbl = new JLabel(normalContent);
        lbl.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
        lbl.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                folderPanel.tryChangeCurrentFolder(targetPath);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lbl.setText(hoverContent);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lbl.setText(normalContent);
            }
        });
        return lbl;
    }

    private JLabel makeSeparatorLabel() {
        JLabel sep = new JLabel(SEPARATOR_GLYPH);
        sep.setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
        sep.setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
        sep.setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));
        return sep;
    }

    /** Escapes the minimal HTML special characters that can appear in file names. */
    private String escapeHtml(String text) {
        return text.replace("&", HTML_AMP)
                   .replace("<", HTML_LT)
                   .replace(">", HTML_GT);
    }
}
