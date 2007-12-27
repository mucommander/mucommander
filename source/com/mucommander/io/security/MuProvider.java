/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.io.security;

import java.security.Provider;
import java.security.Security;

/**
 * This custom <code>java.security.Provider</code> exposes muCommander's own <code>MessageDigest</code> implementations,
 * and the ones used aggregated from third party libraries.
 *
 * <p>The {@link #registerProvider()} method should be called once to register this <code>Provider</code> with the
 * Java Cryptography Extension and advertise the additional <code>MessageDigest</code> implementations.</p>
 *
 * @author Maxence Bernard
 */
public class MuProvider extends Provider {

    /** True if an instance of this Provider has already been registered with java.security.Security */
    private static boolean initialized;

    private MuProvider() {
        super("muCommander", 1.0, "muCommander's additional MessageDigest implementations.");
    }

    /**
     * Registers an instance of this Provider with the <code>java.security.Security</code> class, to expose the
     * additional <code>MessageDigest</code> implementations and have them returned by
     * <code>java.security.Security.getAlgorithms("MessageDigest")</code>.
     * This method should be called once
     */
    public static void registerProvider() {
        // A Provider must be registered only once
        if(initialized)
            return;

        MuProvider provider = new MuProvider();

        // Add our own MessageDigest implementations
        provider.put("MessageDigest."+Adler32MessageDigest.getAlgorithmName(), Adler32MessageDigest.class.getName());
        provider.put("MessageDigest."+CRC32MessageDigest.getAlgorithmName(), CRC32MessageDigest.class.getName());

        // Aggregate MessageDigest implementations from 3rd party libraries
        provider.put("MessageDigest.MD4", jcifs.util.MD4.class.getName());

        // Register the provider with java.security.Security
        Security.addProvider(provider);

        // A Provider must be registered only once
        initialized = true;
    }
}
