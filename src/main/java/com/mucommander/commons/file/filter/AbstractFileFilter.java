/**
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

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;

import java.util.Vector;


/**
 * <code>AbstractFileFilter</code> implements the bulk of the {@link FileFilter} interface. The only method left for
 * subclasses to implement is {@link #accept(AbstractFile)}.
 *
 * @see AbstractFilenameFilter
 * @author Maxence Bernard
 */
public abstract class AbstractFileFilter implements FileFilter {

    /** True if this filter should operate in inverted mode and invert matches */
    protected boolean inverted;

    /**
     * Creates a new <code>AbstractFileFilter</code> operating in non-inverted mode.
     */
    public AbstractFileFilter() {
        this(false);
    }

    /**
     * Creates a new <code>AbstractFileFilter</code> operating in the specified mode.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AbstractFileFilter(boolean inverted) {
        setInverted(inverted);
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean match(AbstractFile file) {
        if(inverted)
            return reject(file);

        return accept(file);
    }

    public boolean reject(AbstractFile file) {
        return !accept(file);
    }

    public AbstractFile[] filter(AbstractFile files[]) {
        Vector<AbstractFile> filteredFilesV = new Vector<AbstractFile>();
        int nbFiles = files.length;
        AbstractFile file;
        for(int i=0; i<nbFiles; i++) {
            file = files[i];
            if(match(file))
                filteredFilesV.add(file);
        }

        AbstractFile filteredFiles[] = new AbstractFile[filteredFilesV.size()];
        filteredFilesV.toArray(filteredFiles);
        return filteredFiles;
    }

    public void filter(FileSet files) {
        for(int i=0; i<files.size();) {
            if(reject(files.elementAt(i)))
                files.removeElementAt(i);
            else
                i++;
        }
    }

    public boolean match(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            if(!match(files[i]))
                return false;

        return true;
    }

    public boolean match(FileSet files) {
        int nbFiles = files.size();
        for(int i=0; i<nbFiles; i++)
            if(!match(files.elementAt(i)))
                return false;

        return true;
    }

    public boolean accept(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            if(!accept(files[i]))
                return false;

        return true;
    }

    public boolean accept(FileSet files) {
        int nbFiles = files.size();
        for(int i=0; i<nbFiles; i++)
            if(!accept(files.elementAt(i)))
                return false;

        return true;
    }

    public boolean reject(AbstractFile files[]) {
        int nbFiles = files.length;
        for(int i=0; i<nbFiles; i++)
            if(!reject(files[i]))
                return false;

        return true;
    }

    public boolean reject(FileSet files) {
        int nbFiles = files.size();
        for(int i=0; i<nbFiles; i++)
            if(!reject(files.elementAt(i)))
                return false;

        return true;
    }
}