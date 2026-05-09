/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package dev.barebones.commander.commons.io.security;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerFactory;

/**
 * Helpers for constructing XML parsers that are immune to XXE
 * (External XML Entity) attacks.
 *
 * SECURITY_REVIEW.md §5.5 flagged 9 SAX entry points across the
 * codebase that built {@link SAXParserFactory} instances without
 * setting any of the FEATURE_SECURE_PROCESSING / disallow-doctype-decl
 * features. This class centralises the hardening so individual readers
 * call one method and inherit a known-safe configuration.
 */
public final class SecureXml {

    private SecureXml() {
    }

    /**
     * Returns a {@link SAXParser} configured to refuse external entities,
     * external DTDs, and any DOCTYPE declaration. Suitable for parsing
     * the application's own XML config files (theme, bookmarks, action
     * keymap, toolbar, command bar, association, command, credentials,
     * configuration) where DOCTYPEs are never expected.
     */
    public static SAXParser newSafeSaxParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setNamespaceAware(true);
            return factory.newSAXParser();
        } catch (Exception e) {
            throw new IllegalStateException("Could not build XXE-hardened SAXParser", e);
        }
    }

    /**
     * Returns a {@link SAXTransformerFactory} configured for secure
     * processing — used by the configuration writer to emit XML that
     * cannot reference external resources.
     */
    public static SAXTransformerFactory newSafeSaxTransformerFactory() {
        try {
            SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            return factory;
        } catch (Exception e) {
            throw new IllegalStateException("Could not build XXE-hardened SAXTransformerFactory", e);
        }
    }
}
