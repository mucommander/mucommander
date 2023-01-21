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
package com.mucommander.viewer.image.ui;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mucommander.text.Translator;

/**
 * Image viewer status panel.
 */
@ParametersAreNonnullByDefault
public class ImageStatusPanel extends javax.swing.JPanel {

    private javax.swing.JLabel imageSizeLabel;
    private javax.swing.JLabel zoomLabel;

    public ImageStatusPanel() {
        initComponents();
    }

    private void initComponents() {
        imageSizeLabel = new javax.swing.JLabel();
        zoomLabel = new javax.swing.JLabel();

        imageSizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageSizeLabel.setText("-");
        imageSizeLabel.setToolTipText(Translator.get("image_viewer.status.imageSizeLabel.toolTipText"));
        imageSizeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        zoomLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        zoomLabel.setText("-");
        zoomLabel.setToolTipText(Translator.get("image_viewer.status.zoomLabel.toolTipText"));
        zoomLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                layout.createSequentialGroup()
                                        .addContainerGap(195, Short.MAX_VALUE)
                                        .addComponent(zoomLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                148,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(imageSizeLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                168,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(imageSizeLabel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(zoomLabel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE));
    }

    public void setZoomFactor(double zoomFactor) {
        zoomLabel.setText((int) (zoomFactor * 100) + " %");
    }

    public void setImageSize(int imageWidth, int imageHeight) {
        imageSizeLabel.setText(imageWidth + " x " + imageHeight);
    }
}
