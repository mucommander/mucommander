package com.mucommander.ui.main.statusbar;

import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;

import java.awt.*;

/**
 * This label displays the amount of used and total heap size.
 */
public class MemoryLabel extends VolumeSpaceLabel {

    /**
     * SizeFormat's format used to display memory info in status bar
     */
    public static final int MEMORY_INFO_SIZE_FORMAT = SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_SHORT | SizeFormat.INCLUDE_SPACE | SizeFormat.ROUND_TO_KB;

    private long totalMemory;
    private long usedMemory;

    private static final float MEMORY_WARNING_THRESHOLD = 0.9f;
    private static final float MEMORY_CRITICAL_THRESHOLD = 0.95f;

    void setMemory(long maxMemory, long totalMemory, long freeMemory) {
        this.totalMemory = totalMemory;
        this.usedMemory = totalMemory - freeMemory;

        // Set new label's text
        String memoryInfo = SizeFormat.format(usedMemory, MEMORY_INFO_SIZE_FORMAT) + " / " + SizeFormat.format(this.totalMemory, MEMORY_INFO_SIZE_FORMAT) + " (" + SizeFormat.format(maxMemory, MEMORY_INFO_SIZE_FORMAT) + ")";

        memoryInfo = Translator.get("status_bar.memory_used", memoryInfo);
        setText(memoryInfo);

        // Set tooltip
        setToolTipText("" + (int) (100 * usedMemory / (float) this.totalMemory) + "%");

        repaint();
    }

    @Override
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        float usedMemoryPercentage = usedMemory / (float) totalMemory;

        Color c;
        if (usedMemoryPercentage >= MEMORY_CRITICAL_THRESHOLD) {
            c = criticalColor;
        } else if (usedMemoryPercentage >= MEMORY_WARNING_THRESHOLD) {
            c = interpolateColor(warningColor, criticalColor, (usedMemoryPercentage - MEMORY_WARNING_THRESHOLD) / MEMORY_WARNING_THRESHOLD);
        } else {
            c = interpolateColor(okColor, warningColor, (1 - MEMORY_WARNING_THRESHOLD) / (1 - usedMemoryPercentage));
        }

        g.setColor(c);

        int usedMemoryWidth = Math.max(Math.round(usedMemoryPercentage * (float) (width - 2)), 1);
        g.fillRect(1, 1, usedMemoryWidth + 1, height - 2);

        // Fill background
        g.setColor(backgroundColor);
        g.fillRect(usedMemoryWidth + 1, 1, width - usedMemoryWidth - 1, height - 2);

        super.paint(g);
    }

}
