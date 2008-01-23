/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.chooser;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Component used to let users pick a color.
 * <p>
 * The main reason for this component's existence is Swing's <code>JColorChooser</code> does not offer
 * alpha transparency edition, and that its localisation is incomplete. This is a wrapper that fixes both
 * of these shortcomings.
 * </p>
 * <p>
 * This component can be used either as a regular widget and be added to a <code>container</code> or as
 * a dialog box through the {@link #createDialog(Frame,ColorChooser) createDialog} method.
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class ColorChooser extends YBoxPanel implements ChangeListener {

    // - UI components ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Component that displays a preview of the current color */
    private JComponent previewComponent;
    /** Color chooser. */
    private JColorChooser  chooser;
    /** Alpha transparency chooser. */
    private IntegerChooser alpha;


    // - Color editing ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Currently selected color. */
    private Color          currentColor;
    /** Color on which the dialog was initialised. */
    private Color initialColor;
    /** Property to change in the preview component when the current color changes */
    private String previewColorPropertyName;


    // - Localisation -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Overrides the default, incomplete Swing translations.
     */
    static {
        String buffer;

        // Tab labels.
        UIManager.put("ColorChooser.rgbNameText",        Translator.get("color_chooser.rgb"));
        UIManager.put("ColorChooser.hsbNameText",        Translator.get("color_chooser.hsb"));
        UIManager.put("ColorChooser.swatchesNameText",   Translator.get("color_chooser.swatches"));

        // Red color name.
        UIManager.put("ColorChooser.rgbRedText",         buffer = Translator.get("color_chooser.red"));
        UIManager.put("ColorChooser.hsbRedText",         buffer);

        // Green color name.
        UIManager.put("ColorChooser.rgbGreenText",       buffer = Translator.get("color_chooser.green"));
        UIManager.put("ColorChooser.hsbGreenText",       buffer);

        // Blue color name.
        UIManager.put("ColorChooser.rgbBlueText",        buffer = Translator.get("color_chooser.blue"));
        UIManager.put("ColorChooser.hsbBlueText",        buffer);

        // HSB tab specific strings.
        UIManager.put("ColorChooser.hsbHueText",         Translator.get("color_chooser.hue"));
        UIManager.put("ColorChooser.hsbSaturationText",  Translator.get("color_chooser.saturation"));
        UIManager.put("ColorChooser.hsbBrightnessText",  Translator.get("color_chooser.brightness"));

        // Swatches tab specific strings.
        UIManager.put("ColorChooser.swatchesRecentText", Translator.get("color_chooser.recent"));
    }


    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    public ColorChooser() {
        this(Color.WHITE, null, null);
    }

    public ColorChooser(Color initialColor) {
        this(initialColor, null, null);
    }

    public ColorChooser(Color initialColor, JComponent previewComponent, String previewColorPropertyName) {
        this.currentColor  = initialColor;
        this.initialColor = initialColor;

        // Initialises the UI.
        add(createChooserPanel());
        add(createTransparencyPanel());

        alpha.setValue(initialColor.getAlpha());
        chooser.setColor(initialColor);

        if(previewComponent!=null && previewColorPropertyName!=null) {
            this.previewComponent = previewComponent;
            this.previewColorPropertyName = previewColorPropertyName;
            add(createPreviewPanel(previewComponent));

            updatePreview();
        }
    }



    // - Dialog creation --------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a dialog containing the specified color chooser.
     * @param  parent  component on which to center the dialog.
     * @param  chooser chooser to use within the dialog.
     * @return         a dialog containing the specified chooser.
     */
    public static FocusDialog createDialog(Dialog parent, ColorChooser chooser) {
        return new ChooserDialog(parent, chooser);
    }

    /**
     * Creates a dialog containing the specified color chooser.
     * @param  parent  component on which to center the dialog.
     * @param  chooser chooser to use within the dialog.
     * @return         a dialog containing the specified chooser.
     */
    public static FocusDialog createDialog(Frame parent, ColorChooser chooser) {
        return new ChooserDialog(parent, chooser);
    }



    // - UI Initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------


    /**
     * Creates the preview panel.
     */
    private JPanel createPreviewPanel(JComponent previewComponent) {
        JPanel    panel;
        Dimension size;

        // Sets the label's preferred size (same width as the chooser, twice the normal label height).
        size = previewComponent.getPreferredSize();
        size.width = chooser.getPreferredSize().width;
        size.height *= 2;
        previewComponent.setPreferredSize(size);

        // Sets the preview label appearance.
        previewComponent.setOpaque(true);

        // Creates the preview panel.
        panel = new JPanel();
        panel.add(previewComponent);
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        return panel;
    }

    /**
     * Creates the color chooser panel.
     */
    private JColorChooser createChooserPanel() {
        // Creates the color chooser.
        chooser = new JColorChooser();
        chooser.setPreviewPanel(new JPanel());
        chooser.getSelectionModel().addChangeListener(this);

        return chooser;
    }

    /**
     * Creates the transparency selection panel.
     */
    private JPanel createTransparencyPanel() {
        // Creates and initialises the transparency selector.
        alpha = new IntegerChooser(0, 255, 255);
        alpha.setMajorTickSpacing(85);
        alpha.setMinorTickSpacing(17);
        alpha.setPaintTicks(true);
        alpha.setPaintLabels(true);
        alpha.setBorder(BorderFactory.createTitledBorder(Translator.get("color_chooser.alpha")));
        alpha.addChangeListener(this);

        return alpha;
    }


    // - Color / font management ------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns the color selected by the user.
     * @return the color selected by the user.
     */
    public Color getColor() {
        return currentColor;
    }

    /**
     * Resets the dialog to the initial color.
     */
    public void reset() {
        reset(true);
    }

    /**
     * Resets the dialog to the initial color.
     * @param updateUI if set to <code>false</code>, the component's UI won't be updated.
     */
    private void reset(boolean updateUI) {
        currentColor = initialColor;

        /// Propagates the color to the choosers.
        if(updateUI) {
            alpha.setValue(currentColor.getAlpha());
            chooser.setColor(currentColor);

            currentColor = initialColor;    // Need to set it again as the value is changed by stateChanged()
            updatePreview();
        }
    }

    /**
     * Update the preview panel to the current color.
     */
    private void updatePreview() {
        if(previewComponent!=null) {
            previewComponent.putClientProperty(previewColorPropertyName, currentColor);
        }
    }

    /**
     * This method is public as an implementation side effect and should not be called directly.
     */
    public void stateChanged(ChangeEvent e) {
        Color buffer;

        // Creates the new current color.
        buffer       = chooser.getColor();
        currentColor = new Color(buffer.getRed(), buffer.getGreen(), buffer.getBlue(), alpha.getValue());

        updatePreview();
    }


    // - Chooser dialog ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Component used to present a <code>ColorChooser</code> from within a modal dialog.
     * @author Nicolas Rinaudo
     */
    private static class ChooserDialog extends FocusDialog implements ActionListener {
        /** Resets the color to the original one. */
        private JButton resetButton;
        /** Closes the dialog without applying the color selection. */
        private JButton cancelButton;
        /** Color chooser contained by this dialog. */
        private ColorChooser chooser;

        /**
         * Creates a new dialog containing the specified color chooser.
         */
        public ChooserDialog(Frame parent, ColorChooser chooser) {
            super(parent, Translator.get("color_chooser.title"), parent);
            initUI(chooser);
        }

        /**
         * Creates a new dialog containing the specified color chooser.
         */
        public ChooserDialog(Dialog parent, ColorChooser chooser) {
            super(parent, Translator.get("color_chooser.title"), parent);
            initUI(chooser);
        }

        /**
         * Initialises the dialog's UI.
         */
        private void initUI(ColorChooser chooser) {
            Container   contentPane;

            this.chooser = chooser;

            // Initialises the dialog and its content pane.
            contentPane = getContentPane();
            contentPane.setLayout(new BorderLayout());

            // Creates the content of the dialog.
            contentPane.add(chooser, BorderLayout.CENTER);
            contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);
        }

        /**
         * Creates the panel that contains the dialog's buttons.
         */
        private JPanel createButtonsPanel() {
            XBoxPanel buttonsPanel;
            JPanel    panel;
            JButton okButton;

            // Creates the panel and buttons.
            buttonsPanel = new XBoxPanel();
            buttonsPanel.add(resetButton = new JButton(Translator.get("reset")));
            buttonsPanel.addSpace(20);
            buttonsPanel.add(okButton = new JButton(Translator.get("ok")));
            buttonsPanel.add(cancelButton = new JButton(Translator.get("cancel")));

            // Tracks events.
            resetButton.addActionListener(this);
            okButton.addActionListener(this);
            cancelButton.addActionListener(this);

            // OK will be selected when the user presses enter.
            getRootPane().setDefaultButton(okButton);

            // Aligns the buttons to the right of the panel.
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            panel.add(buttonsPanel);

            return panel;
        }

        /**
         * In case the dialog was cancelled, resets the color before closing it.
         */
        public void cancel() {
            chooser.reset(false);
            super.cancel();
        }

        /**
         * This method is public as an implementation side effect and should not be called directly.
         */
        public void actionPerformed(ActionEvent e) {
            // Resets the current color.
            if(e.getSource() == resetButton)
                chooser.reset(true);

            // Closes the dialog, applying modifications if necessary.
            else {
                if(e.getSource() == cancelButton)
                    chooser.reset(false);
                dispose();
            }
        }
    }
}
