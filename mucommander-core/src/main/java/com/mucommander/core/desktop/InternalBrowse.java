/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.core.desktop;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.mucommander.desktop.UrlOperation;

/**
 * @author Nicolas Rinaudo
 */
class InternalBrowse extends UrlOperation {
    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Underlying desktop instance. */
    private Desktop desktop;
    private boolean initialized;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new <code>InternalOpenUrl</code> instance.
     */
    public InternalBrowse() {
    }

    private Desktop getDesktop() {
        if (!initialized) {
            if(Desktop.isDesktopSupported())
                desktop = Desktop.getDesktop();
            initialized = true;
        }
        return desktop;
    }

    // - BrowseOperation implementation --------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Returns <code>true</code> if this operation is available.
     * <p>
     * This operation is available if:
     * <ul>
     *   <li>Desktops are supported by the current system (<code>Desktop.isDesktopSupported()</code> returns <code>true</code>).</li>
     *   <li>Browsing is supported by the desktop (<code>Desktop.isSupported(Desktop.Action.BROWSE)</code> returns <code>true</code>).</li>
     * </ul>
     * </p>
     * @return <code>true</code> if this operations is available, <code>false</code> otherwise.
     */
    @Override
    public boolean isAvailable() {return getDesktop() != null && getDesktop().isSupported(Desktop.Action.BROWSE);}

    /**
     * Opens the specified URL in the system's default browser.
     * @param  url         URL to browse.
     * @return             the {@link CompletionStage} allowing to receive asynchronously the output messages in case of error if any.
     * @throws IOException if an error occurred.
     */
    @Override
    public CompletionStage<Optional<String>> execute(URL url) throws IOException {
        // If java.awt.Desktop browsing is available, use it.
        if(isAvailable()) {
            try {
                getDesktop().browse(url.toURI());
                return CompletableFuture.completedFuture(Optional.empty());
            } catch(URISyntaxException e) {
                throw new IOException(e.getMessage());
            }
        }

        throw new UnsupportedOperationException();
    }

    /**
     * Returns the action's label.
     * @return the action's label.
     */
    @Override
    public String getName() {return "java.awt.Desktop open URL";}
}
