/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.auth;

import dev.barebones.commander.commons.file.AuthenticationType;
import dev.barebones.commander.commons.file.Credentials;
import dev.barebones.commander.commons.file.DefaultSchemeHandler;
import dev.barebones.commander.commons.file.DefaultSchemeParser;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.SchemeHandler;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class CredentialsMappingTest {

    @BeforeClass
    public void registerScheme() throws Exception {
        SchemeHandler handler = new DefaultSchemeHandler(
            new DefaultSchemeParser(), 22, "/",
            AuthenticationType.AUTHENTICATION_REQUIRED, null);
        Method m = FileURL.class.getDeclaredMethod("registerHandler",
            String.class, SchemeHandler.class);
        m.setAccessible(true);
        m.invoke(null, "sftp", handler);
    }

    private static CredentialsMapping mapping(String url, String login, String password) throws Exception {
        return new CredentialsMapping(
            new Credentials(login, password),
            FileURL.getFileURL(url), true);
    }

    @Test
    public void toStringDoesNotIncludePassword() throws Exception {
        CredentialsMapping cm = mapping("sftp://host/", "alice", "shh-secret");
        String s = cm.toString();
        assertFalse(s.contains("shh-secret"),
            "toString() must not leak the password, got: " + s);
        assertTrue(s.contains("alice"), "should still show the login: " + s);
    }

    @Test
    public void hashCodeMatchesEqualsForLoginAndRealm() throws Exception {
        CredentialsMapping a = mapping("sftp://host/", "alice", "p1");
        CredentialsMapping b = mapping("sftp://host/", "alice", "p2"); // different password
        // equals() uses compareUserAndPassword=false → these are equal.
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode(),
            "hashCode contract: a.equals(b) implies a.hashCode() == b.hashCode()");
    }

    @Test
    public void differentLoginYieldsDifferentHash() throws Exception {
        CredentialsMapping a = mapping("sftp://host/", "alice", "p");
        CredentialsMapping b = mapping("sftp://host/", "bob", "p");
        assertNotEquals(a, b);
        // hashes can collide in theory but with login as the only
        // varying input, alice/bob will hash differently.
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void worksInHashSet() throws Exception {
        Set<CredentialsMapping> set = new HashSet<>();
        set.add(mapping("sftp://h1/", "u", "p"));
        set.add(mapping("sftp://h2/", "u", "p"));
        set.add(mapping("sftp://h1/", "u", "different-password"));
        assertEquals(set.size(), 2,
            "third entry has same (realm, login) as first → HashSet should dedupe");
    }
}
