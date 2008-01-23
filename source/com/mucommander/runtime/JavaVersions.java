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

package com.mucommander.runtime;

/**
 * This interface contains a list of all known major Java versions.
 * 
 * @see JavaVersion
 * @author Maxence Bernard
 */
public interface JavaVersions {

    /** Java 1.0.x */
    public static final JavaVersion JAVA_1_0 = new JavaVersion("1.0", 0);

    /** Java 1.1.x */
    public static final JavaVersion JAVA_1_1 = new JavaVersion("1.1", 1);

    /** Java 1.2.x */
    public static final JavaVersion JAVA_1_2 = new JavaVersion("1.2", 2);

    /** Java 1.3.x */
    public static final JavaVersion JAVA_1_3 = new JavaVersion("1.3", 3);

    /** Java 1.4.x */
    public static final JavaVersion JAVA_1_4 = new JavaVersion("1.4", 4);

    /** Java 1.5.x */
    public static final JavaVersion JAVA_1_5 = new JavaVersion("1.5", 5);

    /** Java 1.6.x */
    public static final JavaVersion JAVA_1_6 = new JavaVersion("1.6", 6);

    /** Java 1.7.x */
    public static final JavaVersion JAVA_1_7 = new JavaVersion("1.7", 7);
}
