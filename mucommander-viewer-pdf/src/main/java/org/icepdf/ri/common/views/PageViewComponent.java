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

import org.icepdf.core.pobjects.Page;

/**
 * <p>The <code>PageViewComponent</code> interfaces should be used by any page view
 * implementation to represent a single page view.  The methods defined in this
 * interface are the most commonly used methods and are used by the
 * <code>AbstractDocumentView</code> and <code>AbstractDocumentViewModel</code>.</p>
 *
 * @see org.icepdf.ri.common.views.PageViewComponentImpl
 * @since 2.0
 */
public interface PageViewComponent {

    /**
     * Set the parent Document View class which is responsible for drawing and
     * the general management of PageViewComponents for a particular view.
     *
     * @param parentDocumentView type of view, single page, continuous, etc.
     */
    void setDocumentViewCallback(DocumentView parentDocumentView);

    /**
     * Gets the page index which this PageViewComponent is drawing.
     *
     * @return zero pages page index of the page drawn by this component.
     */
    int getPageIndex();

    /**
     * Called to free resources used by this component.
     */
    void dispose();

    /**
     * Called from parent controls when a UI control has manipulated the view, property
     * change is picked up and the view is updated accordingly.  If the worker is currently working
     * it should be canceled with an interrupt.
     *
     * @param propertyConstant document view change property.
     * @param oldValue         old value
     * @param newValue         new value
     */
    void updateView(String propertyConstant, Object oldValue, Object newValue);

    /**
     * This callback is called when the page is successfully initialized at which point an implementation may
     * like to work with the page object before the parent method turns.  This method should return as quickly
     * as possible.
     *
     * @param page page that was just initialized.
     */
    void pageInitializedCallback(Page page);


    /**
     * This callback is called when a page is scheduled for dispose.  This generally only happens when the page
     * goes out of view and it and it's resources are no longer needed. This method in the default implementation
     * is executed on a worker thread.  Any AWT work should be queued to run on the AWT thread.
     */
    void pageTeardownCallback();


}
