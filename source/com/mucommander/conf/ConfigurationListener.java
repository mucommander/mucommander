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

package com.mucommander.conf;

/**
 * Used to monitor configuration file changes.
 * <p>
 * Once a <i>ConfigurationListener</i> instance has been registered to the
 * {@link com.mucommander.conf.ConfigurationManager} through the
 * {@link com.mucommander.conf.ConfigurationManager#addConfigurationListener(ConfigurationListener)}
 * method, it will be warned whenever a configuration variable has been changed.
 * </p>
 * <p>
 * Configuration listeners have the possibility of vetoing a configuration change.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ConfigurationListener {
    /**
     * Called whenever a configuration variable has been changed.
     * @param  event describes the configuration variable that has been modified.
     * @return true if the configuration change is accepted, false otherwise.
     */
    public boolean configurationChanged(ConfigurationEvent event);
}
