/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

package com.mucommander.ui.button;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * This class allows to add a rollover effect to a <code>JButton</code>. Rollover-enabled buttons have no borders
 * by default. It is only when the mouse cursor is over the button that the button's borders get painted.
 * This rollover effect gives the user a visual indication that the button can be pressed (in other words, that the
 * button is indeed a button), while not cluttering the interface with button borders.
 * Such buttons are particularly effective for toolbars, where a large number of buttons are usually present.
 * <p>
 * <p>
 * To 'rollover-enable' a button, the {@link #decorateButton(javax.swing.JButton)} method must first be called to
 * set decoration properties. Then, the button must register an instance of <code>RolloverButtonAdapter</code> as a
 * mouse listener. Note that a single <code>RolloverButtonAdapter</code> instance can be registered with several buttons.
 * </p>
 *
 * @author Maxence Bernard
 */
public class RolloverButtonAdapter implements MouseListener {

    private static final RolloverButtonAdapter ROLLOVER_BUTTON_ADAPTER = new RolloverButtonAdapter();

    /**
     * Creates a new RolloverButtonAdapter.
     */
    private RolloverButtonAdapter() {

    }

    /**
     * Sets the decoration properties required to give the specified button a 'rollover' look and feel.
     *
     * @param button the button to 'rollover-enable'
     */
    public static void decorateButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        if (OsFamily.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_5.isCurrentOrHigher()) {
            button.putClientProperty("JButton.buttonType", "textured");
        }
        button.setRolloverEnabled(true);
        button.addMouseListener(ROLLOVER_BUTTON_ADAPTER);
    }

    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    @Override
    public void mouseEntered(MouseEvent e) {
        ((JButton) e.getSource()).setBorderPainted(true);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        ((JButton) e.getSource()).setBorderPainted(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

}
