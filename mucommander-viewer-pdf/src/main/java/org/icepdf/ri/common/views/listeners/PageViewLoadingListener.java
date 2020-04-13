package org.icepdf.ri.common.views.listeners;

import org.icepdf.core.events.PageLoadingAdapter;
import org.icepdf.ri.common.views.DocumentViewController;

/**
 * PageViewLoadingListener allows for multiple implementation of a
 * PageViewLoading Listener.
 *
 * @since 5.1.0
 */
public abstract class PageViewLoadingListener extends PageLoadingAdapter {

    /**
     * Sets the ne document view controller set when a view type changes.
     *
     * @param documentViewController currently selected document view controller.
     */
    public abstract void setDocumentViewController(DocumentViewController documentViewController);

}
