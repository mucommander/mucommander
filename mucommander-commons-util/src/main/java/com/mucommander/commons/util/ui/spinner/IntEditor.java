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

package com.mucommander.commons.util.ui.spinner;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * Modified version of {@link NumberEditor}
 * @author Arik Hadas
 */
public class IntEditor extends JSpinner.DefaultEditor {

    public IntEditor(JSpinner spinner, String decimalFormatPattern) {
        this(spinner, decimalFormatPattern, null);
    }

    public IntEditor(JSpinner spinner, String decimalFormatPattern, String defaultStr) {
        this(spinner, new DecimalFormat(decimalFormatPattern), defaultStr);
    }

    private IntEditor(JSpinner spinner, DecimalFormat format, String defaultStr) {
        super(spinner);
        if (!(spinner.getModel() instanceof SpinnerNumberModel)) {
            throw new IllegalArgumentException(
                      "model not a SpinnerNumberModel");
        }

        SpinnerNumberModel model = (SpinnerNumberModel)spinner.getModel();
        NumberFormatter formatter;
        if (defaultStr == null)
            formatter = new NumberEditorFormatter(model, format);
        else {
            formatter = new NumberEditorFormatter(model, format) {
                @Override
                public Object stringToValue(String text) throws ParseException {
                    if (text == null || text.equals(defaultStr))
                        return 0;
                    return super.stringToValue(text);
                }
                @Override
                public String valueToString(Object value) throws ParseException {
                    if (value == null || ((Number) value).intValue() == 0)
                        return defaultStr;
                    return super.valueToString(value);
                }
            };
        }
        DefaultFormatterFactory factory = new DefaultFormatterFactory(
                                              formatter);
        JFormattedTextField ftf = getTextField();
        ftf.setEditable(true);
        ftf.setFormatterFactory(factory);

        ftf.setHorizontalAlignment(JTextField.LEADING);

        /* TBD - initializing the column width of the text field
         * is imprecise and doing it here is tricky because
         * the developer may configure the formatter later.
         */
        try {
            String maxString = formatter.valueToString(model.getMinimum());
            String minString = formatter.valueToString(model.getMaximum());
            ftf.setColumns(Math.max(maxString.length(),
                                    minString.length()));
        }
        catch (ParseException e) {
            // TBD should throw a chained error here
        }
    }

    /**
     * This subclass of javax.swing.NumberFormatter maps the minimum/maximum
     * properties to a SpinnerNumberModel and initializes the valueClass
     * of the NumberFormatter to match the type of the initial models value.
     */
    static class NumberEditorFormatter extends NumberFormatter {
        private final SpinnerNumberModel model;

        NumberEditorFormatter(SpinnerNumberModel model, NumberFormat format) {
            super(format);
            this.model = model;
            setValueClass(model.getValue().getClass());
        }

        @Override
        public void setMinimum(Comparable min) {
            model.setMinimum(min);
        }

        @Override
        public Comparable<?> getMinimum() {
            return  model.getMinimum();
        }

        @Override
        public void setMaximum(Comparable max) {
            model.setMaximum(max);
        }

        @Override
        public Comparable<?> getMaximum() {
            return model.getMaximum();
        }
    }
}
