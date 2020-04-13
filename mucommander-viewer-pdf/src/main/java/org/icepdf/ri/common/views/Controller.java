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

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.ri.util.PropertiesManager;

import java.util.ResourceBundle;

/**
 * A Controller is the glue between the model and view components.
 * These methods allow the different parts of the view to remain
 * in lock-step with each other and have access to the model,
 * as necessary
 *
 * @since 2.0
 */
public interface Controller {
    /**
     * A Document is the root of the object hierarchy, giving access
     * to the contents of a PDF file.
     * Significantly, getDocument().getCatalog().getPageTree().getPage(int pageNumber)
     * gives access to each Page, so that it might be drawn.
     *
     * @return Document root of the PDF file.
     */
    public Document getDocument();

    /**
     * When viewing a PDF file, one or more pages may be viewed at
     * a single time, but this page is the single page which is most
     * predominantly being displayed.
     *
     * @return The zero-based index of the current Page being displayed
     */
    public int getCurrentPageNumber();

    /**
     * Each Page may have its own rotation, but on top of that, the user
     * may select to have the Page further rotated by 90, 180 or 270 degrees.
     *
     * @return The user's requested rotation
     */
    public float getUserRotation();

    /**
     * The Page being shown may be zoomed in or out, to show more detail,
     * or provide an overview.
     *
     * @return The user's requested zoom
     */
    public float getUserZoom();

    /**
     * Gets controller responsible for Page view UI interaction.
     *
     * @return document view controller.
     */
    public DocumentViewController getDocumentViewController();

    /**
     * Gets controller responsible for the document text searches.
     *
     * @return page view controller.
     */
    public DocumentSearchController getDocumentSearchController();

    /**
     * Sets the tool mode used for the controller view. Tools such as
     * text selection, panning and annotation selection can be used.
     *
     * @param toolType tool mode constants defined in DocumentViewModel
     */
    public void setDocumentToolMode(final int toolType);

    /**
     * Gets the message bundle used by this class.  Message bundle resources
     * are loaded via the JVM default locale.
     *
     * @return message bundle used by this class.
     */
    public ResourceBundle getMessageBundle();

    /**
     * Gets the properties manager used to build a dynamically created UI.
     *
     * @return currently properties manager instance.
     */
    public PropertiesManager getPropertiesManager();
}
