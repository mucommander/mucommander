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

import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalWidget;
import com.jediterm.terminal.ui.TerminalWidgetListener;
import com.jediterm.terminal.ui.UIUtil;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyListener;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates JediTerm widget.
 * Based on JediTerm's BasicTerminalShellExample class.
 */
public final class TerminalWindow {

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
            Map<String, String> envs;
            String[] command;
            if (UIUtil.isWindows) {
                command = new String[]{"cmd.exe"};
                envs = System.getenv();
            } else {
                command = new String[]{"/bin/bash", "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("BASH_SILENCE_DEPRECATION_WARNING", "1");
                envs.put("TERM", "xterm-256color");
            }

            PtyProcess process = new PtyProcessBuilder().setDirectory(currentFolder).setCommand(command).setEnvironment(envs).start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}