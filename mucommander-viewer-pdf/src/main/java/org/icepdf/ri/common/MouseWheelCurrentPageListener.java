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
package org.icepdf.ri.common;

import javax.swing.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 *
 */
public class MouseWheelCurrentPageListener implements MouseWheelListener {
    private JScrollPane scrollpane;
    private CurrentPageChanger currentPageChanger;

    /**
     * KeyEvents can queue up, if the user holds down a key,
     * causing us to do several page changes, unless we use
     * flagging to ignore the extraneous KeyEvents
     */
    private boolean calculatingCurrentPage;


    /**
     * Install a MouseWheelCurrentPageListener as a MouseWheelListener
     */
    public static Object install(JScrollPane scrollpane,
                                 CurrentPageChanger currentPageChanger) {
        MouseWheelCurrentPageListener listener = null;
        if (scrollpane != null && currentPageChanger != null) {
            listener =
                    new MouseWheelCurrentPageListener(scrollpane, currentPageChanger);
            scrollpane.addMouseWheelListener(listener);
        }
        return listener;
    }

    public static void uninstall(JScrollPane scrollpane, Object listener) {
        if (scrollpane != null && listener != null &&
                listener instanceof MouseWheelCurrentPageListener) {
            scrollpane.removeMouseWheelListener((MouseWheelCurrentPageListener) listener);
        }
    }

    protected MouseWheelCurrentPageListener(JScrollPane scrollpane,
                                            CurrentPageChanger currentPageChanger) {
        this.scrollpane = scrollpane;
        this.currentPageChanger = currentPageChanger;
        calculatingCurrentPage = false;
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (calculatingCurrentPage)
            return;
        int amount = e.getScrollAmount();
        if (amount > 0) {
            calculatingCurrentPage = true;
            currentPageChanger.calculateCurrentPage();
            calculatingCurrentPage = false;
        }
    }
}
