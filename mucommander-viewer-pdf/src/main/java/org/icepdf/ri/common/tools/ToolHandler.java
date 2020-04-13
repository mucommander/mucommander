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
package org.icepdf.ri.common.tools;

import javax.swing.event.MouseInputListener;
import java.awt.*;

/**
 * The ToolHandler interface should be implemented by any tool handler that
 * needs to paints to the screen.
 */
public interface ToolHandler extends MouseInputListener {

    /**
     * Paints the tools pre-annotation creation state.
     *
     * @param g graphics context
     */
    void paintTool(Graphics g);


    /**
     * Callback code that allows post construct task to take place when the
     * tool is selected via the
     * {@link org.icepdf.ri.common.views.AbstractDocumentView#setToolMode(int)}
     * call.
     */
    void installTool();

    /**
     * Callback code that allows pre destroy task to take place when the
     * tool is unselected via the
     * {@link org.icepdf.ri.common.views.AbstractDocumentView#setToolMode(int)}
     * call.
     */
    void uninstallTool();

}
