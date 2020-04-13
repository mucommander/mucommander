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

import org.icepdf.ri.common.tools.ToolHandler;

import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusListener;

/**
 * <p>The DocumentView interface should be used when create a new multipage view. </p>
 *
 * @see org.icepdf.ri.common.views.AbstractDocumentView
 * @since 2.5
 */
public interface DocumentView extends AdjustmentListener, FocusListener {
    /**
     * Indicates that a two column view will have odd-numbered pages on the left.
     */
    public int LEFT_VIEW = 0;
    /**
     * Indicates that a two column view will have odd-numbered pages on the right.
     */
    public int RIGHT_VIEW = 1;

    /**
     * Get the next page index.  This will number will very depending on the
     * page view type.  Two column page views usually increment page counts by 2
     * and single page views by 1 page.
     *
     * @return number of pages to increment page count on a page increment command.
     */
    public int getNextPageIncrement();

    /**
     * Get the previous page index.  This will number will very depending on the
     * page view type.  Two column page views usually increment page counts by 2
     * and single page views by 1 page.
     *
     * @return number of pages to increment page count on a page increment command.
     */
    public int getPreviousPageIncrement();

    /**
     * Gets the total size of the document view.  This size will very depending
     * on the view type.  The size dimension has been normalized to a zoom
     * factor of 1.0f and rotation is taken care off.
     *
     * @return size of document in pixels for all pages represented in the view.
     */
    public Dimension getDocumentSize();

    /**
     * Parent document view controller
     *
     * @return document view controller
     */
    public DocumentViewController getParentViewController();

    /**
     * Gets the view model associated with this document view.
     *
     * @return document view model used by this view.
     */
    public DocumentViewModel getViewModel();

    /**
     * Dispose all resources associated with this views.
     */
    public void dispose();

    /**
     * Update the child components which make up this view.
     */
    public void updateDocumentView();

    /**
     * Sets the tool type/mode that is to be enabled for the particular
     * view.  Mouse and keyboard listeners are associated with this call.  No
     * actual state is stored in the view this is only for setup purposes.  The
     * tool state is stored in the DocumentViewModel.
     *
     * @param viewToolMode tool mode type.
     */
    public void setToolMode(final int viewToolMode);

    /**
     * Uninstalls the current tool Handler.
     *
     * @return tool handler taht was removed.
     */
    public ToolHandler uninstallCurrentTool();

    /**
     * Installs the current tool handler.
     *
     * @param currentTool tool ot install.
     */
    public void installCurrentTool(ToolHandler currentTool);

    /**
     * Component repaint call.
     */
    public void repaint();
}
