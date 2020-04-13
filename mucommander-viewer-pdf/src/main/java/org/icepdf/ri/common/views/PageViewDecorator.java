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
package org.icepdf.ri.common.views;

import org.icepdf.core.util.ColorUtil;
import org.icepdf.core.util.Defs;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The PageViewDecorator class adds a page border and shadow to all of the page
 * views defined in the corg.icepdf.core.views.swing package.  This class can
 * easily be modified for extended for custom page decorations.</p>
 * <p/>
 * <p>By default the this class paints with the following colors:</p>
 * <ul>
 * <li>paper color - default color is white, can be changed using the system
 * property org.icepdf.core.views.page.paper.color
 * </li>
 * <li>paper border color - default color is black, can be changed using the
 * system property org.icepdf.core.views.page.border.color</li>
 * <li>paper shadow color - default color is darkGrey, can be changed using the
 * system property org.icepdf.core.views.page.shadow.color</li>
 * </ul>
 * <p>All color values can be set using the hex rgb values.
 * eg. black=000000 or white=FFFFFFF. </p>
 *
 * @since 2.5
 */
@SuppressWarnings("serial")
public class PageViewDecorator extends JComponent {

    private static final Logger log =
            Logger.getLogger(PageViewDecorator.class.toString());

    protected JComponent pageViewComponent;

    protected static final int SHADOW_SIZE = 3;

    protected Dimension preferredSize = new Dimension();

    private static Color pageBorderColor;
    private static Color pageShadowColor;
    private static Color pageColor;
    static {
        // sets the shadow colour of the decorator.
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.shadow.color", "#333333");
            int colorValue = ColorUtil.convertColor(color);
            pageShadowColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("333333", 16));

        } catch (NumberFormatException e) {
            if (log.isLoggable(Level.WARNING)) {
                log.warning("Error reading page shadow colour");
            }
        }

        // background colours for paper, border and shadow.
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.paper.color", "#FFFFFF");
            int colorValue = ColorUtil.convertColor(color);
            pageColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("FFFFFF", 16));
        } catch (NumberFormatException e) {
            if (log.isLoggable(Level.WARNING)) {
                log.warning("Error reading page paper color.");
            }
        }
        // border colour for the page decoration.
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.border.color", "#000000");
            int colorValue = ColorUtil.convertColor(color);
            pageBorderColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("000000", 16));
        } catch (NumberFormatException e) {
            if (log.isLoggable(Level.WARNING)) {
                log.warning("Error reading page paper color.");
            }
        }
    }

    public PageViewDecorator(JComponent pageViewComponent) {
        setLayout(new GridLayout(1, 1, 0, 0));

        this.pageViewComponent = pageViewComponent;
        Dimension size = pageViewComponent.getPreferredSize();
        preferredSize.setSize(size.width + SHADOW_SIZE, size.height + SHADOW_SIZE);
        add(pageViewComponent);
    }

    public Dimension getPreferredSize() {
        Dimension size = pageViewComponent.getPreferredSize();
        preferredSize.setSize(size.width + SHADOW_SIZE, size.height + SHADOW_SIZE);
        return preferredSize;
    }

    /**
     * Paints a default a black border and dark gray shadow for the given page.
     *
     * @param g java graphic context which is decorated with page graphics.
     */
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        Point location = pageViewComponent.getLocation();
        Dimension size = pageViewComponent.getPreferredSize();

        // paper
        g2d.setColor(pageColor);
        g2d.fillRect(location.x, location.y, size.width, size.height);

        // paper shadow
        g2d.setColor(pageShadowColor);
        g2d.fillRect(location.x + SHADOW_SIZE, location.y + size.height, size.width - SHADOW_SIZE, SHADOW_SIZE);
        g2d.fillRect(location.x + size.width, location.y + SHADOW_SIZE, SHADOW_SIZE, size.height);

        super.paint(g);

        // paper border
        g2d.setColor(pageBorderColor);
        g2d.drawRect(location.x, location.y, size.width, size.height);
    }

    public PageViewComponent getPageViewComponent() {
        return (PageViewComponent) pageViewComponent;
    }
}
