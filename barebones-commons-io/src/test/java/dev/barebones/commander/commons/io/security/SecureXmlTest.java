/*
 * Copyright (C) 2026 barebones-commander contributors
 */
package dev.barebones.commander.commons.io.security;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Ensures {@link SecureXml#newSafeSaxParser()} rejects any document
 * carrying a DOCTYPE declaration — the canonical XXE foothold.
 */
public class SecureXmlTest {

    @Test
    public void rejectsExternalDoctype() throws Exception {
        String xml =
                "<?xml version=\"1.0\"?>" +
                "<!DOCTYPE foo SYSTEM \"file:///etc/passwd\">" +
                "<foo/>";
        try {
            SecureXml.newSafeSaxParser().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                    new DefaultHandler());
            assert false : "DOCTYPE should have been rejected";
        } catch (SAXParseException expected) {
            // good — the parser refused the DOCTYPE.
            assert expected.getMessage().toLowerCase().contains("doctype")
                    : "expected DOCTYPE-related parse error, got: " + expected.getMessage();
        }
    }

    @Test
    public void rejectsInternalEntityDoctype() throws Exception {
        String xml =
                "<?xml version=\"1.0\"?>" +
                "<!DOCTYPE foo [<!ENTITY xxe \"pwned\">]>" +
                "<foo>&xxe;</foo>";
        try {
            SecureXml.newSafeSaxParser().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                    new DefaultHandler());
            assert false : "Internal-entity DOCTYPE should have been rejected";
        } catch (SAXParseException expected) {
            // good
        }
    }

    @Test
    public void plainXmlIsParsed() throws Exception {
        String xml = "<foo><bar/></foo>";
        SecureXml.newSafeSaxParser().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                new DefaultHandler());
        // no exception → pass
    }
}
