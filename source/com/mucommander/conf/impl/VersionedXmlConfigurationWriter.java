/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.conf.impl;

import com.mucommander.RuntimeConstants;
import com.mucommander.conf.ConfigurationException;
import com.mucommander.conf.XmlConfigurationWriter;
import com.mucommander.xml.XmlAttributes;

import java.io.IOException;

/**
 * @author Maxence Bernard
 */
class VersionedXmlConfigurationWriter extends XmlConfigurationWriter {

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void startConfiguration() throws ConfigurationException {
        try {
            // Version the file.
            // Note: the version attribute was introduced in muCommander 0.8.4.
            XmlAttributes attributes = new XmlAttributes();
            attributes.add(MuConfiguration.VERSION_ATTRIBUTE, RuntimeConstants.VERSION);

            out.startElement(ROOT_ELEMENT, attributes);
            out.println();
        }
        catch(IOException e) {throw new ConfigurationException(e);}
    }
}
