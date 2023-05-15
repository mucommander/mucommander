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

import java.awt.event.KeyListener;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalWidget;
import com.jediterm.terminal.ui.TerminalWidgetListener;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

/**
 * Creates JediTerm widget.
 * Based on JediTerm's BasicTerminalShellExample class.
 */
public final class TerminalWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminalWindow.class);

    private TerminalWindow() {
    }

    public static JediTermWidget createTerminal(String currentFolder, Runnable listener, KeyListener keyListener) {
        DefaultSettingsProvider settings = getDefaultSettings(
                new TerminalColor(() -> ThemeManager.getCurrentColor(Theme.SHELL_BACKGROUND_COLOR)),
                new TerminalColor(() -> ThemeManager.getCurrentColor(Theme.SHELL_FOREGROUND_COLOR)));
        JediTermWidget jediTermWidget = createTerminalWidget(currentFolder, settings);
        jediTermWidget.addListener(new TerminalWidgetListener() {
            public void allSessionsClosed(TerminalWidget widget) {
                listener.run();
            }
        });
        jediTermWidget.getTerminalPanel().addCustomKeyListener(keyListener);
        return jediTermWidget;
    }

    public static JediTermWidget createTerminal(String currentFolder) {
        return createTerminalWidget(currentFolder, getDefaultSettings(TerminalColor.BLACK, TerminalColor.WHITE));
    }

    private static JediTermWidget createTerminalWidget(String currentFolder, DefaultSettingsProvider settings) {
        JediTermWidget widget = new JediTermWidget(80, 24, settings);
        widget.setTtyConnector(createTtyConnector(currentFolder));
        widget.start();
        return widget;
    }

    private static DefaultSettingsProvider getDefaultSettings(TerminalColor background, TerminalColor foreground) {
        return new DefaultSettingsProvider() {

            // see: https://github.com/mucommander/mucommander/issues/933
            private final boolean altSendsEscape = shouldSendEscape();

            @Override
            public TextStyle getDefaultStyle() {
                return new TextStyle(foreground, background);
            }

            @Override
            public boolean altSendsEscape() {
                return altSendsEscape;
            }

            private boolean shouldSendEscape() {
                if (OsFamily.MAC_OS.isCurrent()) {
                    return MuConfigurations.getPreferences().getVariable(
                                MuPreference.USE_OPTION_AS_META_KEY,
                                MuPreferences.DEFAULT_USE_OPTION_AS_META_KEY);
                } else {
                    return super.altSendsEscape();
                }
            }
        };
    }

    private static @NotNull TtyConnector createTtyConnector(String currentFolder) {
        try {
            List<String> shellCommand = getShellCommand();
            if (shellCommand.isEmpty()) {
                LOGGER.error("Shell command is not properly set, terminal won't be created");
                throw new IllegalStateException("Shell command is not properly set");
            }

            Map<String, String> envs;
            if (OsFamily.WINDOWS.isCurrent()) {
                envs = System.getenv();
            } else {
                envs = new HashMap<>(System.getenv());
                envs.putAll(getDefaultLocaleSetting());
                envs.put("TERM", "xterm-256color");
                envs.put("HISTCONTROL", "ignoreboth");
                if (OsFamily.MAC_OS.isCurrent()) {
                    envs.put("BASH_SILENCE_DEPRECATION_WARNING", "1");
                }
            }
            PtyProcess process = new PtyProcessBuilder()
                    .setDirectory(currentFolder)
                    .setCommand(shellCommand.toArray(String[]::new))
                    .setEnvironment(envs)
                    .start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Maps "LC_ALL" to the value of the LC_ALL environment variable if set,
     * or to a value that is determined based on the best JVM knowledge otherwise.
     *
     * @return a map of "LC_ALL" to its desired value
     */
    private static Map<String, String> getDefaultLocaleSetting() {
        String lcAll = System.getenv("LC_ALL");
        if (lcAll == null || lcAll.isEmpty()) {
            lcAll = String.format("%s.%s",
                    Locale.getDefault().toLanguageTag().replace('-', '_'),
                    Charset.defaultCharset());
        }
        return Collections.singletonMap("LC_ALL", lcAll);
    }

    private static List<String> getShellCommand() {
        // Retrieves the shell interactive command from preferences,
        // falls back to default if not set in preferences.
        String shellCommandAndParams;
        if (MuConfigurations.getPreferences().getVariable(
                MuPreference.USE_CUSTOM_SHELL, MuPreferences.DEFAULT_USE_CUSTOM_SHELL)) {
            shellCommandAndParams = MuConfigurations.getPreferences().getVariable(
                    MuPreference.CUSTOM_SHELL, DesktopManager.getDefaultShell());
        } else {
            shellCommandAndParams = DesktopManager.getDefaultShell();
        }

        String[] shellCommandParts = shellCommandAndParams.split("\\s+");
        return Arrays.asList(shellCommandParts);
    }
}
