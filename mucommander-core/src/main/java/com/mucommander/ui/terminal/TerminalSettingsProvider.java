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

import java.awt.Font;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.ui.AwtTransformers;
import com.jediterm.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.action.ActionId;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.TerminalActions;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.KeyStroke;

/**
 * A class providing configuration for Terminal - it is based on DefaultSettingsProvider
 * and overrides certain settings based on Preferences, for example:
 * - 'altSendsEscape' driven by USE_OPTION_AS_META_KEY setting (<a href="https://github.com/mucommander/mucommander/issues/933">...</a>)
 * - font and colors
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
    public TextStyle getDefaultStyle() {
        return new TextStyle(
                new TerminalColor(() ->
                        AwtTransformers.fromAwtColor(ThemeManager.getCurrentColor(Theme.TERMINAL_FOREGROUND_COLOR))),
                new TerminalColor(() ->
                        AwtTransformers.fromAwtColor(ThemeManager.getCurrentColor(Theme.TERMINAL_BACKGROUND_COLOR)))
        );
    }

    @Override
    public TextStyle getSelectionColor() {
        // since we set #useInverseSelectionColor to return false, this method is called.
        return new TextStyle(
                new TerminalColor(() ->
                        AwtTransformers.fromAwtColor(ThemeManager.getCurrentColor(Theme.TERMINAL_SELECTED_FOREGROUND_COLOR))),
                new TerminalColor(() ->
                        AwtTransformers.fromAwtColor(ThemeManager.getCurrentColor(Theme.TERMINAL_SELECTED_BACKGROUND_COLOR)))
        );
    }

    @Override
    public boolean useInverseSelectionColor() {
        return false;
    }

    // The returned font is already in a good size (no need to override or call #getTerminalFontSize).
    @Override
    public Font getTerminalFont() {
        return ThemeManager.getCurrentFont(Theme.TERMINAL_FONT);
    }

    @Override
    public void configurationChanged(ConfigurationEvent event) {
        if (MuPreference.USE_OPTION_AS_META_KEY == MuPreference.getByLabel(event.getVariable())) {
            altSendsEscape = event.getBooleanValue();
        }
    }

    @Override
    public @NotNull TerminalActionPresentation getPageUpActionPresentation() {
        TerminalActionPresentation custom = getTerminalActionPresentation("Page Up",
                TerminalActions.Action.PAGE_UP);
        return custom != null ? custom : super.getPageUpActionPresentation();
    }

    @Override
    public @NotNull TerminalActionPresentation getPageDownActionPresentation() {
        TerminalActionPresentation custom = getTerminalActionPresentation("Page Down",
                TerminalActions.Action.PAGE_DOWN);
        return custom != null ? custom : super.getPageUpActionPresentation();
    }

    @Override
    public @NotNull TerminalActionPresentation getLineUpActionPresentation() {
        TerminalActionPresentation custom = getTerminalActionPresentation("Line Up",
                TerminalActions.Action.LINE_UP);
        return custom != null ? custom : super.getPageUpActionPresentation();
    }

    @Override
    public @NotNull TerminalActionPresentation getLineDownActionPresentation() {
        TerminalActionPresentation custom = getTerminalActionPresentation("Line Down",
                TerminalActions.Action.LINE_DOWN);
        return custom != null ? custom : super.getPageUpActionPresentation();
    }

    @Override
    public @NotNull TerminalActionPresentation getFindActionPresentation() {
        TerminalActionPresentation custom = getTerminalActionPresentation("Find",
                TerminalActions.Action.FIND);
        return custom != null ? custom : super.getPageUpActionPresentation();
    }

    private TerminalActionPresentation getTerminalActionPresentation(String name, TerminalActions.Action action) {
        KeyStroke accelerator = ActionKeymap.getAccelerator(
                ActionId.asTerminalAction(action.getId()));
        KeyStroke alternateAccelerator = ActionKeymap.getAlternateAccelerator(
                ActionId.asTerminalAction(action.getId()));

        List<KeyStroke> keys = Stream.of(accelerator, alternateAccelerator).filter(k -> k != null).collect(Collectors.toList());

        return !keys.isEmpty() ? new TerminalActionPresentation(name, keys) : null;
    }
}
