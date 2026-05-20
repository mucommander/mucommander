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

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jediterm.terminal.ui.TerminalPanel;

/**
 * Helper for invoking package-protected JediTerm {@link TerminalPanel} APIs.
 */
final class TerminalPanelHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminalPanelHelper.class);

    private static final Method REINIT_FONT_AND_RESIZE_METHOD;

    static {
        Method method = null;
        try {
            method = TerminalPanel.class.getDeclaredMethod("reinitFontAndResize");
            method.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to access TerminalPanel.reinitFontAndResize", e);
        }
        REINIT_FONT_AND_RESIZE_METHOD = method;
    }

    private TerminalPanelHelper() {
    }

    static void reinitFontAndResize(TerminalPanel terminalPanel) {
        if (REINIT_FONT_AND_RESIZE_METHOD == null) {
            return;
        }
        try {
            REINIT_FONT_AND_RESIZE_METHOD.invoke(terminalPanel);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to reinitialize terminal font", e);
        }
    }
}
