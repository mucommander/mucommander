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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            @Override
            public TextStyle getDefaultStyle() {
                return new TextStyle(foreground, background);
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
            String[] command;
            if (OsFamily.WINDOWS.isCurrent()) {
                envs = System.getenv();
            } else {
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
                if (OsFamily.MAC_OS.isCurrent()) {
                    envs.put("BASH_SILENCE_DEPRECATION_WARNING", "1");
                }
            }
            PtyProcess process = new PtyProcessBuilder().setDirectory(currentFolder)
                    .setCommand(shellCommand.toArray(new String[0])).setEnvironment(envs).start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<String> getShellCommand() {
        List<String> result = new ArrayList<>();

        // Retrieves the shell interactive command from preferences,
        // falls back to default if not set in prefs.
        String shellCommandAndParams;
        if (MuConfigurations.getPreferences().getVariable(
                MuPreference.USE_CUSTOM_SHELL, MuPreferences.DEFAULT_USE_CUSTOM_SHELL)) {
            shellCommandAndParams = MuConfigurations.getPreferences().getVariable(
                    MuPreference.CUSTOM_SHELL, DesktopManager.getDefaultShell());
        } else {
            shellCommandAndParams = DesktopManager.getDefaultShell();
        }

        String[] shellCommandParts = shellCommandAndParams.split("\\s+");
        for (String part : shellCommandParts) {
            result.add(part);
        }
        return result;
    }
}
