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
package com.mucommander.ui.terminal;

import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;

/**
 * A class providing configuration for Terminal - it is based on DefaultSettingsProvider
 * and overrides certain settings based on Preferences.
 * Currently, only 'altSendsEscape' driven by USE_OPTION_AS_META_KEY setting (https://github.com/mucommander/mucommander/issues/933)
 */
public class TerminalSettingsProvider extends DefaultSettingsProvider implements ConfigurationListener {

    private boolean altSendsEscape;

    public TerminalSettingsProvider() {
        if (OsFamily.MAC_OS.isCurrent()) {
            altSendsEscape = MuConfigurations.getPreferences().getVariable(
                    MuPreference.USE_OPTION_AS_META_KEY,
                    MuPreferences.DEFAULT_USE_OPTION_AS_META_KEY);
            MuConfigurations.addPreferencesListener(this);
        } else {
            altSendsEscape = super.altSendsEscape();
        }
    }

    @Override
    public boolean altSendsEscape() {
        return altSendsEscape;
    }

    @Override
    public void configurationChanged(ConfigurationEvent event) {
        if (MuPreference.USE_OPTION_AS_META_KEY.equals(
                MuPreference.getMuPreferenceFromString(event.getVariable()))) {
            altSendsEscape = event.getBooleanValue();
        }
    }
}
