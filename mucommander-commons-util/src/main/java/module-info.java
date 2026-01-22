/**
 * This file is part of muCommander, http://www.mucommander.com
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

module org.mucommander.commons.util {
    requires org.slf4j;
    requires org.mucommander.commons.runtime;
    requires java.desktop;
    requires java.xml;

    exports com.mucommander.commons.util;
    exports com.mucommander.commons.util.cache;
    exports com.mucommander.commons.util.ui.border;
    exports com.mucommander.commons.util.ui.button;
    exports com.mucommander.commons.util.ui.combobox;
    exports com.mucommander.commons.util.ui.dialog;
    exports com.mucommander.commons.util.ui.helper;
    exports com.mucommander.commons.util.ui.layout;
    exports com.mucommander.commons.util.ui.spinner;
    exports com.mucommander.commons.util.ui.text;
    exports com.mucommander.commons.util.xml;
}
