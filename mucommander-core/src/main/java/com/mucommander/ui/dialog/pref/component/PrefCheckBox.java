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

package com.mucommander.ui.dialog.pref.component;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Supplier;

import javax.swing.JCheckBox;

import com.mucommander.ui.dialog.pref.PreferencesDialog;

/**
 * @author Arik Hadas
 */
public class PrefCheckBox extends JCheckBox implements PrefComponent {

    private Supplier<Boolean> conf;

    public PrefCheckBox(String description, Supplier<Boolean> conf) {
        super(description);
        this.conf = conf;
        setSelected(conf.get());
    }

    @Override
    public boolean hasChanged() {
        return isSelected() != conf.get();
    }

    public void addDialogListener(final PreferencesDialog dialog) {
        addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                dialog.componentChanged(PrefCheckBox.this);
            }
        });
    }
}
