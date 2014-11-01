/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.RuntimeConstants;
import com.mucommander.commons.conf.ConfigurationException;
import com.mucommander.commons.conf.XmlConfigurationWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.Writer;

/**
 * @author Maxence Bernard
 */
class VersionedXmlConfigurationWriter extends XmlConfigurationWriter {

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public VersionedXmlConfigurationWriter(Writer out, String rootElementName) {
        super(out, rootElementName);
    }

    @Override
    public void startConfiguration() throws ConfigurationException {
        // Version the file.
        // Note: the version attribute was introduced in muCommander 0.8.4.
        AttributesImpl attributes;

        attributes = new AttributesImpl();

        attributes.addAttribute("", MuPreferences.VERSION_ATTRIBUTE, MuPreferences.VERSION_ATTRIBUTE, "string", RuntimeConstants.VERSION);

        try {out.startElement("", rootElementName, rootElementName, attributes);}
        catch(SAXException e) {throw new ConfigurationException(e);}
    }
}
