/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import java.util.zip.Adler32;

/**
 * Provides a <code>ChecksumMessageDigest</code> implementation of the <i>Adler32</i> algorithm, using the
 * <code>java.util.zip.Adler32</code> class.
 *
 * @author Maxence Bernard
 */
public class Adler32MessageDigest extends ChecksumMessageDigest {

    public Adler32MessageDigest() {
        super(new Adler32(), getAlgorithmName());
    }

    /**
     * Returns the name of the algorithm implemented by this MessageDigest.
     *
     * @return the name of the algorithm implemented by this MessageDigest
     */
    protected static String getAlgorithmName() {
        return "Adler32";
    }
}
