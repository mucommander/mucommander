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

import java.awt.*;

/**
 * Modified Flow layout allos the layout to be recalculated when parent container
 * is resized.
 */
public class ModifiedFlowLayout extends FlowLayout {

    private static final long serialVersionUID = -5384365489254593185L;

    public ModifiedFlowLayout() {
        super();
    }

    public Dimension computeSize(int w, Container target) {
        synchronized (target.getTreeLock()) {
            int hgap = getHgap();
            int vgap = getVgap();

            if (w == 0)
                w = Integer.MAX_VALUE;

            Insets insets = target.getInsets();
            if (insets == null)
                insets = new Insets(0, 0, 0, 0);
            int reqdWidth = 0;

            int maxwidth = w - (insets.left + insets.right + hgap * 2);
            int n = target.getComponentCount();
            int x = 0;
            int y = insets.top + vgap;
            int rowHeight = 0;

            for (int i = 0; i < n; i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    if ((x == 0) || ((x + d.width) <= maxwidth)) {
                        // fits in current row.
                        if (x > 0) {
                            x += hgap;
                        }
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        x = d.width;
                        y += vgap + rowHeight;
                        rowHeight = d.height;
                    }
                    reqdWidth = Math.max(reqdWidth, x);
                }
            }
            y += rowHeight;
            y += insets.bottom;
            return new Dimension(reqdWidth + insets.left + insets.right, y);
        }
    }
}