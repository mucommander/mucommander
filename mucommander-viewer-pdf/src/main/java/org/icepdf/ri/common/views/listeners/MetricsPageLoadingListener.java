/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.common.views.listeners;

import org.icepdf.core.events.*;
import org.icepdf.core.pobjects.Page;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * MetricsPageLoadingListener is an example of how the PageLoadingListener
 * interface can be used to page loading metrics information.
 *
 * @since 5.1.0
 */
public class MetricsPageLoadingListener implements PageLoadingListener {

    private static final Logger logger =
            Logger.getLogger(MetricsPageLoadingListener.class.toString());

    public static final DecimalFormat formatter = new DecimalFormat("#.###");
    public static final DecimalFormat percentFormatter = new DecimalFormat("#");
    private int pageIndex;
    private int pageCount;

    private long startLoading;
    private long endLoading;

    private long startInit;
    private long endInit;

    private long imageCount;
    private long imageLoadDuration;

    private long startPaint;
    private long endPaint;
    private long paintCount;

    public MetricsPageLoadingListener(int pageCount) {
        this.pageCount = pageCount;
    }

    public void pageLoadingStarted(PageLoadingEvent event) {
        startLoading = System.nanoTime();
        pageIndex = ((Page) event.getSource()).getPageIndex();
        imageCount = event.getImageResourceCount();
    }

    public void pageInitializationStarted(PageInitializingEvent event) {
        startInit = System.nanoTime();
    }

    public void pageInitializationEnded(PageInitializingEvent event) {
        endInit = System.nanoTime();
    }

    public void pageImageLoaded(PageImageEvent event) {
        imageLoadDuration += event.getDuration();
    }

    public void pagePaintingStarted(PagePaintingEvent event) {
        startPaint = System.nanoTime();
        paintCount = event.getShapesCount();
    }

    public void pagePaintingEnded(PagePaintingEvent event) {
        endPaint = System.nanoTime();
    }

    public void pageLoadingEnded(PageLoadingEvent event) {
        endLoading = System.nanoTime();
        displayConsoleMetrics();
    }

    private void displayConsoleMetrics() {
        System.out.println("Loading page: " + (pageIndex + 1) + "/" + pageCount);
        double totalTime = convert(endLoading - startLoading);
        double initTime = convert(endInit - startInit);
        double paintTime = convert(endPaint - startPaint);
        double imageTime = convert(imageLoadDuration);
        System.out.println("        init time: " + formatter.format(initTime) +
                "ms (" + percentFormatter.format((initTime / totalTime) * 100) + "%)");

        System.out.println("       paint time: " + formatter.format(paintTime) +
                "ms (" + percentFormatter.format((paintTime / totalTime) * 100) + "%) " +
                paintCount + " shapes");
        System.out.println("       image time: " + formatter.format(imageTime) + "ms");
        System.out.println("  avg. image time: " + formatter.format(imageTime / imageCount) + "ms for "
                + NumberFormat.getNumberInstance(Locale.US).format(imageCount) + " image(s)");
        System.out.println("       total time: " + formatter.format(totalTime));
    }

    private double convert(long duration) {
        return duration / 1.0E09;
    }
}
