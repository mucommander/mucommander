/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.combobox;

/**
 * Interface to be implemented by classes that wish to be notified of actions occuring on a {@link EditableComboBox}.
 * Those classes need to be registered to receive those events, this can be done by calling
 * {@link EditableComboBox#addEditableComboBoxListener(EditableComboBoxListener)}.
 *
 * @author Maxence Bernard
 */
public interface EditableComboBoxListener extends ComboBoxListener {

    /**
     * This method is called when the text field has been validated, that is the 'Enter' key has been pressed
     * in the text field, without the popup menu being visible.
     *
     * <p>Note: Unlike JComboBox's weird ActionEvent handling, this method is *not* called when 'Enter' is pressed
     * in the combo popup menu.
     *
     * @param source the EditableComboBox containing the JTextField on which the event was triggered
     */
    public void textFieldValidated(EditableComboBox source);


    /**
     * Notifies all registered EditableComboBoxListener instances that the text field has been cancelled, that is
     * the 'Escape' key has been pressed in the text field, without the popup menu being visible.
     *
     * <p>Note: This method is *not* called when 'Escape' is pressed in the combo popup menu.
     *
     * @param source the EditableComboBox containing the JTextField on which the event was triggered
     */
    public void textFieldCancelled(EditableComboBox source);
}
