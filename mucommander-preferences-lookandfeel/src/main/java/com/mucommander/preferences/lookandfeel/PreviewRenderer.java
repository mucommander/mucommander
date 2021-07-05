package com.mucommander.preferences.lookandfeel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.IntSupplier;

class PreviewRenderer extends JComponent {

    private final LookAndFeelsListModel listModel;
    private final IntSupplier selectedIndexSupplier;

    public PreviewRenderer(LookAndFeelsListModel listModel, IntSupplier selectedIndexSupplier) {
        this.listModel = listModel;
        this.selectedIndexSupplier = selectedIndexSupplier;
        setFocusable(false);
    }

    @Override
    public void paint(Graphics g) {
        // fill entire whitespace as background:
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // borders color:
        g.setColor(Color.BLACK);
        // outer border:
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        int selectedIndex = selectedIndexSupplier.getAsInt();
        if (selectedIndex < 0) {
            return;
        }
        LAFInfo lafInfo = listModel.getElementAt(selectedIndexSupplier.getAsInt());
        BufferedImage image = lafInfo.image;
        int imageX = (getWidth() - image.getWidth()) / 2;
        int imageY = (getHeight() - image.getHeight()) / 2;

        // image border:
        g.drawRect(imageX - 1, imageY - 1, image.getWidth() + 1, image.getHeight() + 1);
        // image itself:
        g.drawImage(image, imageX, imageY, image.getWidth(), image.getHeight(), null);
    }
}
