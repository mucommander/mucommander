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

package dev.barebones.commander.ui.dialog.pref.component;

import dev.barebones.commander.commons.util.ui.dialog.DialogOwner;
import dev.barebones.commander.ui.dialog.pref.PreferencesDialog;
import dev.barebones.commander.ui.encoding.EncodingListener;
import dev.barebones.commander.ui.encoding.EncodingSelectBox;

/**
 * @author Maxence Bernard
 */
public abstract class PrefEncodingSelectBox extends EncodingSelectBox implements PrefComponent {

    // Prevents garbage collection
    private EncodingListener listener;

    public PrefEncodingSelectBox(DialogOwner dialogOwner, String selectedEncoding) {
        super(dialogOwner, selectedEncoding);
    }

    public void addDialogListener(final PreferencesDialog dialog) {
        listener = new EncodingListener() {
            public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
                dialog.componentChanged(PrefEncodingSelectBox.this);
            }
        };
        addEncodingListener(listener);
    }
}
