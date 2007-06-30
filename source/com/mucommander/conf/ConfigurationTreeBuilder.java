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
 * Class used to build a configuration tree.
 * <p>
 * When passed to the {@link com.mucommander.conf.ConfigurationManager#buildConfigurationTree(ConfigurationTreeBuilder)}
 * method, an instance of the ConfigurationTreeBuilder interface will receive a description of the whole configuration tree.<br>
 * For example, a client software used to modify the server configuration could use this interface to create a graphical
 * representation of the configuration tree.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ConfigurationTreeBuilder {

    /**
     * Method called when a new node is found in the tree.
     * @param name node's name.
     */
    public void addNode(String name);

    /**
     * Method called when a node is closed.
     * @param name node's name.
     */
    public void closeNode(String name);

    /**
     * Method called when a new leaf is found.
     * @param name  leaf's name.
     * @param value leaf's value.
     */
    public void addLeaf(String name, String value);
}
