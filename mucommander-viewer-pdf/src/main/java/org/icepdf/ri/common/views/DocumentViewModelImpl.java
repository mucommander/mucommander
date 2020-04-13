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
import org.icepdf.core.pobjects.PageTree;

import javax.swing.*;
import java.util.ArrayList;

/**
 * <p>Default Swing implementation of the AbstractDocumentViewModel class.  The
 * constructor for this class constructs the needed PageViewComponentImpl objects
 * and associates a reference to the parent JScrollPane.</p>
 * <p/>
 * <p>Swing specific setup is handle by this class.</p>
 *
 * @since 2.5
 */
public class DocumentViewModelImpl extends AbstractDocumentViewModel {

    public DocumentViewModelImpl(Document document, JScrollPane parentScrollPane) {
        // construct abstract parent
        super(document);

        // load the page components into the layout
        AbstractPageViewComponent pageViewComponent = null;
        PageTree pageTree = document.getPageTree();
        int numberOfPages = document.getNumberOfPages();
        int avgPageWidth = 0;
        int avgPageHeight = 0;

        // add components for every page in the document
        pageComponents = new ArrayList<AbstractPageViewComponent>(numberOfPages);
        for (int i = 0; i < numberOfPages; i++) {
            // also a way to pass in an average document size.
            if (i < MAX_PAGE_SIZE_READ_AHEAD) {
                pageViewComponent =
                        buildPageViewComponent(this, pageTree, i, parentScrollPane, 0, 0);
                avgPageWidth += pageViewComponent.getPreferredSize().width;
                avgPageHeight += pageViewComponent.getPreferredSize().height;
            } else if (i > MAX_PAGE_SIZE_READ_AHEAD) {
                pageViewComponent =
                        buildPageViewComponent(this, pageTree, i,
                                parentScrollPane,
                                avgPageWidth, avgPageHeight);
            }
            // calculate average page size
            else if (i == MAX_PAGE_SIZE_READ_AHEAD) {
                avgPageWidth /= (MAX_PAGE_SIZE_READ_AHEAD);
                avgPageHeight /= (MAX_PAGE_SIZE_READ_AHEAD);
                pageViewComponent = buildPageViewComponent(this, pageTree, i,
                                parentScrollPane,
                                avgPageWidth, avgPageHeight);
            }
            pageComponents.add(pageViewComponent);
        }
    }

    protected AbstractPageViewComponent buildPageViewComponent(
            DocumentViewModel documentViewModel, PageTree pageTree, final int pageIndex,
            JScrollPane parentScrollPane, int width, int height){
        return new PageViewComponentImpl(this, pageTree, pageIndex, parentScrollPane, width, height);
    }
}
