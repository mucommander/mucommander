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
package org.icepdf.core;

import org.icepdf.core.pobjects.Document;

/**
 * Security callback.
 * An application that uses ICEpdf can provide it with the callback.
 * This will allow the document class to ask an application for security related
 * resources.
 *
 * @since 1.1
 */
public interface SecurityCallback {

    /**
     * This method is called when a security manager needs to receive a
     * password for opening an encrypted PDF document.
     *
     * @param document document being opened.
     * @return received password.
     */
    public String requestPassword(Document document);
}
