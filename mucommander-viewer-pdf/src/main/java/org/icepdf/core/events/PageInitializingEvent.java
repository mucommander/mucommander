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
package org.icepdf.core.events;

/**
 * The PageProcessingEvent event marks the start and end of all page level events.
 *
 * @since 5.1.0
 */
@SuppressWarnings("serial")
public class PageInitializingEvent extends java.util.EventObject {

    private boolean interrupted;

    public PageInitializingEvent(Object source, boolean interrupted) {
        super(source);
        this.interrupted = interrupted;
    }

    /**
     * Indication if page processing was successful.
     *
     * @return true if processing event was successful otherwise false.
     */
    public boolean isInterrupted() {
        return interrupted;
    }
}
