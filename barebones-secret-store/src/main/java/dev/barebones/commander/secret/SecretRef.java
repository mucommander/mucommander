/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret;

import java.util.Objects;

/**
 * Identifier for one stored secret. Both {@code service} and
 * {@code account} are required and used together as the lookup key.
 *
 * Convention for barebones-commander credentials:
 * <ul>
 *   <li>{@code service} = {@code "barebones-commander"} (constant
 *       across the app — keeps related entries grouped in the
 *       OS keychain UI).</li>
 *   <li>{@code account} = the canonical URL the credentials are for,
 *       e.g. {@code "sftp://user@host:22"}. The user / login is
 *       embedded so two distinct logins to the same host don't
 *       collide.</li>
 * </ul>
 */
public record SecretRef(String service, String account) {
    public SecretRef {
        Objects.requireNonNull(service, "service");
        Objects.requireNonNull(account, "account");
        if (service.isBlank()) {
            throw new IllegalArgumentException("service must not be blank");
        }
        if (account.isBlank()) {
            throw new IllegalArgumentException("account must not be blank");
        }
    }
}
