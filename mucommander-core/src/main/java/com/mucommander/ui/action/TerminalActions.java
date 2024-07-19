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

package com.mucommander.ui.action;

import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class manages some keyboard associations for Terminal.
 */
public class TerminalActions {

    public enum Action {
        PAGE_UP,
        PAGE_DOWN,
        LINE_UP,
        LINE_DOWN,
        FIND,
        ;

        private final Descriptor descriptor;

        Action() {
            descriptor = new Descriptor(this);
        }

        public String getId() {
            return "terminal." + this.name().toLowerCase();
        }

        Descriptor getDescriptor() {
            return descriptor;
        }
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {

        private final Action action;

        Descriptor(Action action) {
            this.action = action;
        }

        @Override
        public String getId() {
            return action.getId();
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.TERMINAL;
        }

        @Override
        public String getLabel() {
            return Translator.get("terminal.prefix", Translator.get(getLabelKey()));
        }
    }

    public static class NullAction extends MuAction {

        private final Descriptor descriptor;

        public NullAction(MainFrame mainFrame, Map<String,Object> properties, Descriptor descriptor) {
            super(mainFrame, properties);
            this.descriptor = descriptor;
        }

        @Override
        public void performAction() {
            LOGGER.error("Null Action for {} - should not have been executed!", descriptor);
        }

        @Override
        public ActionDescriptor getDescriptor() {
            return descriptor;
        }
    }

    public static List<Descriptor> actionDescriptors() {
        return Arrays.stream(Action.values()).map(Action::getDescriptor).collect(Collectors.toUnmodifiableList());
    }

}
