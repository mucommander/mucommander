/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.desktop;

import com.mucommander.commons.file.AbstractFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Nicolas Rinaudo
 */
class InternalOpen extends LocalFileOperation {
    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Underlying desktop instance. */
    private Desktop desktop;
    private boolean initialized = false;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new <code>InternalOpen</code> instance.
     */
    public InternalOpen() {
    }

    private Desktop getDesktop() {
        if (!initialized) {
            if(Desktop.isDesktopSupported())
                desktop = Desktop.getDesktop();
            initialized = true;
        }
        return desktop;
    }


    // - DesktopOperation implementation -------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Returns <code>true</code> if this operation is available.
     * <p>
     * This operation is available if:
     * <ul>
     *   <li>Desktops are supported by the current system (<code>Desktop.isDesktopSupported()</code> returns <code>true</code>).</li>
     *   <li>File opening is supported by the desktop (<code>Desktop.isSupported(Desktop.Action.OPEN)</code> returns <code>true</code>).</li>
     * </ul>
     * </p>
     * @return <code>true</code> if this operations is available, <code>false</code> otherwise.
     */
    @Override
    public boolean isAvailable() {return getDesktop() != null && getDesktop().isSupported(Desktop.Action.OPEN);}

    @Override
    public void execute(AbstractFile file) throws IOException {
        if(isAvailable())
            getDesktop().open(new File(file.getAbsolutePath()));
        else
            throw new UnsupportedOperationException();
    }
    /**
     * Returns the action's label.
     * @return the action's label.
     */
    @Override
    public String getName() {return "java.awt.Desktop open file";}
}
