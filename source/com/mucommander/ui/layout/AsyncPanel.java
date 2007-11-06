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

package com.mucommander.ui.layout;

import com.mucommander.text.Translator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * <code>AsyncPanel</code> is a <code>JPanel</code> aimed at components that potentially take a long time to
 * initialize and that allows to deport their initialization in a separate thread as to not lock the event thread.
 * It works as follows:
 * <ol>
 *  <li>Initially, this panel displays a 'wait component' that symbolizes the contents of the panel is being loaded.
 *  <li>When the panel becomes visible, the {@link #getTargetComponent()} method is called to trigger the initialization
 *      of the real component to display.
 *  <li>As soon as the method returns, the wait component is removed and the target component added to this panel.
 *      If this panel is the child of a <code>java.awt.Window</code>, the window is repacked to take into account the
 *      new size of this panel.
 * </ol>
 *
 * <p>This panel tries to be as 'transparent' as possible for the target component: the borders of this panel are empty
 * and its layout is a <code>BorderLayout</code> where the target component is added to the center.</p>
 *
 * @author Maxence Bernard
 */
public abstract class AsyncPanel extends JPanel implements ComponentListener {

    /** The component displayed while the target component is being loaded */
    private JComponent waitComponent;

    /** True if this panel has already received a componentShown event */
    private boolean componentShown;

    /**
     * Creates a new <code>AsyncPanel</code> with the default wait component.
     */
    public AsyncPanel() {
        this(getDefaultWaitComponent());
    }

    /**
     * Creates a new <code>AsyncPanel</code> that displays the given component while the target component is being
     * loaded.
     *
     * @param waitComponent the component to display while the target component is being loaded
     */
    public AsyncPanel(JComponent waitComponent) {
        super(new BorderLayout());

        this.waitComponent = waitComponent;
        add(waitComponent, BorderLayout.CENTER);

        // Get notified when this component becomes visible
        addComponentListener(this);
    }

    /**
     * Returns the default component to be displayed while the target component is being loaded.
     *
     * @return the default component to be displayed while the target component is being loaded
     */
    private static JComponent getDefaultWaitComponent() {
        JLabel label = new JLabel(Translator.get("loading"));
        JPanel tempPanel = new JPanel(new FlowLayout());
        tempPanel.add(label);

        return tempPanel;
    }


    //////////////////////////////////////
    // ComponentListener implementation //
    //////////////////////////////////////

    public void componentShown(ComponentEvent e) {
        if(componentShown)
            return;

        componentShown = true;

        new Thread() {
            public void run() {
                JComponent targetComponent = getTargetComponent();

                remove(waitComponent);
                setBorder(new EmptyBorder(0, 0, 0, 0));

                add(targetComponent, BorderLayout.CENTER);

                Container tla = getTopLevelAncestor();
                if(tla instanceof Window)
                    ((Window)tla).pack();
            }
        }.start();
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract JComponent getTargetComponent();
}
