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
package org.icepdf.core.util.content;

import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.util.Library;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ContentParserFactory will reflectively load the PRO parsing engine
 * if found on the class path.
 *
 * @since 5.0
 */
public class ContentParserFactory {

    private static final Logger logger =
            Logger.getLogger(ContentParserFactory.class.toString());

    private static ContentParserFactory contentParserFactory;

    private static final String N_CONTENT_PARSER =
            "org.icepdf.core.util.content.NContentParser";

    private static boolean foundPro;

    static {
        // check class bath for NFont library, and declare results.
        try {
            Class.forName(N_CONTENT_PARSER);
            foundPro = true;
        } catch (ClassNotFoundException e) {
            logger.log(Level.FINE, "ICEpdf PRO was not found on the class path");
        }
    }

    private ContentParserFactory() {
    }

    /**
     * <p>Returns a static instance of the ContentParserclass.</p>
     *
     * @return instance of the ContentParser.
     */
    public static ContentParserFactory getInstance() {
        // make sure we have initialized the manager
        if (contentParserFactory == null) {
            contentParserFactory = new ContentParserFactory();
        }
        return contentParserFactory;
    }

    /**
     * Factory call to return the content parser associated with the given
     * product version.
     *
     * @param library   document library
     * @param resources page's parent resource object.
     * @return implementation of the ContentParser interface.
     */
    public ContentParser getContentParser(Library library, Resources resources) {
        if (foundPro) {
            // load each know file type reflectively.
            try {
                Class<?> contentParserClass = Class.forName(N_CONTENT_PARSER);
                Class[] parserArgs = {Library.class, Resources.class};
                Constructor fontClassConstructor =
                        contentParserClass.getDeclaredConstructor(parserArgs);
                Object[] args = {library, resources};
                return (ContentParser) fontClassConstructor.newInstance(args);
            } catch (Throwable e) {
                logger.log(Level.FINE, "Could not load font dictionary class", e);
            }
        }
        return new OContentParser(library, resources);
    }

}
