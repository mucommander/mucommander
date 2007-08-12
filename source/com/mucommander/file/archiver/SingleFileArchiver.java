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


package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Generic single file Archiver.
 *
 * @author Maxence Bernard
 */
class SingleFileArchiver extends Archiver {

    private boolean firstEntry = true;


    protected SingleFileArchiver(OutputStream outputStream) {
        super(outputStream);
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    /**
     * This method is a no-op, and does nothing but throw an IOException if it is called more than once,
     * which should never be the case as this Archiver is only meant to store one file. 
     */
    public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
        if(firstEntry)
            firstEntry = false;
        else
            throw new IOException();

        return out;
    }
	
	
    public void close() throws IOException {
        out.close();
    }
}
