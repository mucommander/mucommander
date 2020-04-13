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
package org.icepdf.ri.images;

import java.net.URL;

/**
 * <p>Utility class to allow easy access to image resources in the
 * package com.icesoft.pdf.ri.images.
 * Used as an accessor to the images. Just call:</p>
 * <ul>
 * Images.get("<filename>.gif")
 * </ul>
 *
 * @author Mark Collette
 * @since 2.0
 */
public class Images {

    public static final String SIZE_LARGE = "_32";
    public static final String SIZE_MEDIUM = "_24";
    public static final String SIZE_SMALL = "_16";

    public static URL get(String name) {
        return Images.class.getResource(name);
    }
}
