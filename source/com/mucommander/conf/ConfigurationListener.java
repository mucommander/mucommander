/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf;

/**
 * Listener interface for receiving configuration events.
 * <p>
 * Implementations of this interface can register themselves through
 * {@link Configuration#addConfigurationListener(ConfigurationListener)} to be notified of configuration changes.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ConfigurationListener {
    /**
     * Invoked when the configuration changes.
     * @param event describes the configuration modification.
     */
    public void configurationChanged(ConfigurationEvent event);
}
