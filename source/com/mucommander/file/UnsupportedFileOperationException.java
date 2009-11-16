/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.file;

import java.io.IOException;

/**
 * This exception can be thrown by certain {@link AbstractFile} method implementations, when the corresponding
 * operation is not available, either because the underlying file protocol does not support it, or because it is not
 * implemented. Unlike <code>java.lang.UnsupportedOperationException</code>, this exception is <b>not</b> a
 * <code>RuntimeException</code> and must therefore be caught explicitly.
 * <p>
 * This exception is to be thrown in a way that is independent of the actual file instance, and of I/O or
 * network conditions: an <code>AbstractFile</code> method implementation that throws this exception once must throw it
 * always, for any file instance.
 * </p>
 *
 * @author Maxence Bernard
 */
public class UnsupportedFileOperationException extends IOException {

    public UnsupportedFileOperationException() {
        super();
    }

    public UnsupportedFileOperationException(String message) {
        super(message);
    }

    public UnsupportedFileOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedFileOperationException(Throwable cause) {
        super(cause);
    }
}
