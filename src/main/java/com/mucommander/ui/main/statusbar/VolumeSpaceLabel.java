package com.mucommander.ui.main.statusbar;

import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.border.MutableLineBorder;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import java.awt.*;

/**
 * This label displays the amount of free and/or total space on a volume.
 */
class VolumeSpaceLabel extends JLabel implements ThemeListener {

    /**
     * SizeFormat's format used to display volume info in status bar
     */
    private static final int VOLUME_INFO_SIZE_FORMAT = SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_SHORT | SizeFormat.INCLUDE_SPACE | SizeFormat.ROUND_TO_KB;

    private long freeSpace = -1;
    private long totalSpace = -1;

    Color backgroundColor;
    Color okColor;
    Color warningColor;
    Color criticalColor;

    private static final float SPACE_WARNING_THRESHOLD = 0.1f;
    private static final float SPACE_CRITICAL_THRESHOLD = 0.05f;

    VolumeSpaceLabel() {
        super("");
        setHorizontalAlignment(CENTER);
        backgroundColor = ThemeManager.getCurrentColor(Theme.STATUS_BAR_BACKGROUND_COLOR);
        okColor = ThemeManager.getCurrentColor(Theme.STATUS_BAR_OK_COLOR);
        warningColor = ThemeManager.getCurrentColor(Theme.STATUS_BAR_WARNING_COLOR);
        criticalColor = ThemeManager.getCurrentColor(Theme.STATUS_BAR_CRITICAL_COLOR);
        setBorder(new MutableLineBorder(ThemeManager.getCurrentColor(Theme.STATUS_BAR_BORDER_COLOR)));
        ThemeManager.addCurrentThemeListener(this);
    }

    /**
     * Sets the new volume total and free space, and updates the label's text to show the new values and,
     * only if both total and free space are available (different from -1), paint a graphical representation
     * of the amount of free space available and set a tooltip showing the percentage of free space on the volume.
     *
     * @param totalSpace total volume space, -1 if not available
     * @param freeSpace  free volume space, -1 if not available
     */
    void setVolumeSpace(long totalSpace, long freeSpace) {
        this.freeSpace = freeSpace;
        this.totalSpace = totalSpace;

        // Set new label's text
        String volumeInfo;
        if (freeSpace != -1) {
            volumeInfo = SizeFormat.format(freeSpace, VOLUME_INFO_SIZE_FORMAT);
            if (totalSpace != -1)
                volumeInfo += " / " + SizeFormat.format(totalSpace, VOLUME_INFO_SIZE_FORMAT);

            volumeInfo = Translator.get("status_bar.volume_free", volumeInfo);
        } else if (totalSpace != -1) {
            volumeInfo = SizeFormat.format(totalSpace, VOLUME_INFO_SIZE_FORMAT);
            volumeInfo = Translator.get("status_bar.volume_capacity", volumeInfo);
        } else {
            volumeInfo = "";
        }
        setText(volumeInfo);

        // Set tooltip
        if (freeSpace == -1 || totalSpace == -1)
            setToolTipText(null);       // Removes any previous tooltip
        else
            setToolTipText("" + (int) (100 * freeSpace / (float) totalSpace) + "%");

        repaint();
    }

    /**
     * Adds some empty space around the label.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 4, d.height + 2);
    }

    /**
     * Returns an interpolated color value, located at percent between c1 and c2 in the RGB space.
     *
     * @param c1      first color
     * @param c2      end color
     * @param percent distance between c1 and c2, comprised between 0 and 1.
     * @return an interpolated color value, located at percent between c1 and c2 in the RGB space.
     */
    Color interpolateColor(Color c1, Color c2, float percent) {
        return new Color(
                (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * percent),
                (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * percent),
                (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * percent)
        );
    }

    @Override
    public void paint(Graphics g) {

        // If free or total space is not available, this label will just be painted as a normal JLabel
        if (freeSpace != -1 && totalSpace != -1) {
            int width = getWidth();
            int height = getHeight();

            // Paint amount of free volume space if both free and total space are available
            float freeSpacePercentage = freeSpace / (float) totalSpace;

            Color c;
            if (freeSpacePercentage <= SPACE_CRITICAL_THRESHOLD) {
                c = criticalColor;
            } else if (freeSpacePercentage <= SPACE_WARNING_THRESHOLD) {
                c = interpolateColor(warningColor, criticalColor, (SPACE_WARNING_THRESHOLD - freeSpacePercentage) / SPACE_WARNING_THRESHOLD);
            } else {
                c = interpolateColor(okColor, warningColor, (1 - freeSpacePercentage) / (1 - SPACE_WARNING_THRESHOLD));
            }

            g.setColor(c);

            int freeSpaceWidth = Math.max(Math.round(freeSpacePercentage * (float) (width - 2)), 1);
            g.fillRect(1, 1, freeSpaceWidth + 1, height - 2);

            // Fill background
            g.setColor(backgroundColor);
            g.fillRect(freeSpaceWidth + 1, 1, width - freeSpaceWidth - 1, height - 2);
        }

        super.paint(g);
    }

    @Override
    public void fontChanged(FontChangedEvent event) {

    }

    @Override
    public void colorChanged(ColorChangedEvent event) {
        switch (event.getColorId()) {
            case Theme.STATUS_BAR_BACKGROUND_COLOR:
                backgroundColor = event.getColor();
                break;
            case Theme.STATUS_BAR_BORDER_COLOR:
                // Some (rather evil) look and feels will change borders outside of muCommander's control,
                // this check is necessary to ensure no exception is thrown.
                if (getBorder() instanceof MutableLineBorder)
                    ((MutableLineBorder) getBorder()).setLineColor(event.getColor());
                break;
            case Theme.STATUS_BAR_OK_COLOR:
                okColor = event.getColor();
                break;
            case Theme.STATUS_BAR_WARNING_COLOR:
                warningColor = event.getColor();
                break;
            case Theme.STATUS_BAR_CRITICAL_COLOR:
                criticalColor = event.getColor();
                break;
            default:
                return;
        }
        repaint();
    }

}
