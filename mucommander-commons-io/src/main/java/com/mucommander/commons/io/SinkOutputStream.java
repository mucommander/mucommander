/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * SinkOutputStream is an OutputStream which implements all <code>write()</code> methods as no-ops, loosing data as
 * it gets written, in a similar fashion to the <code>/dev/null</code> UNIX device.
 *
 * @author Maxence Bernard
 */
public class SinkOutputStream extends OutputStream {

    @Override
    public void write(int i) throws IOException {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void write(byte[] bytes) throws IOException {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
    }
}
