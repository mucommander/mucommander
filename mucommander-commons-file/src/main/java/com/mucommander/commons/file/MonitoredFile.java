/**
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.commons.file;

/**
 * This class is an implementation of {@link ProxyFile} that provides an interface
 * for detecting changes to the content of the specified file.
 * @author Arik Hadas
 */
public abstract class MonitoredFile extends ProxyFile {

    public MonitoredFile(AbstractFile file) {
        super(file);
    }

    /**
     * This method can be called after calling {@link #startWatch()} to detect if
     * changes were made to the content of the specified file since the previous time
     * this method returned {@code true} or since starting to watch this file for changes.
     * @return true if the content of this file has changed, false otherwise.
     */
    public abstract boolean isChanged();

    /**
     * Start watching for changes to the content of this file.
     */
    public void startWatch() {}

    /**
     * Stop watching for changes to the content of this file.
     */
    public void stopWatch() {}
}
