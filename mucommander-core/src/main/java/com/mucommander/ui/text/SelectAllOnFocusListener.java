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

package com.mucommander.ui.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.text.JTextComponent;


/**
 * @author Gerolf Scherr
 */
public class SelectAllOnFocusListener implements FocusListener {

    private final static class InstanceHolder {
        final static SelectAllOnFocusListener me = new SelectAllOnFocusListener();
    }

    public static SelectAllOnFocusListener get() {
        return InstanceHolder.me;
    }

    private SelectAllOnFocusListener() { }

    @Override
    public void focusGained(FocusEvent e) {
        Object o = e.getSource();
        if (o instanceof JTextComponent) ((JTextComponent)o).selectAll();
    }

    @Override
    public void focusLost(FocusEvent e) {
    }
}