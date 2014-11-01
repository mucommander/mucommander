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

import com.mucommander.commons.conf.XmlConfigurationReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Maxence Bernard
 */
public class VersionedXmlConfigurationReader extends XmlConfigurationReader {

    /** True until the root element has been parsed */
    private boolean isRootElement = true;

    /** the version that was used to write the configuration file */
    private String version;

    
    /**
     * Returns the muCommander version that was used to write the configuration file, <code>null</code> if it is unknown.
     * <p>
     * Note: the version attribute was introduced in muCommander 0.8.4.
     * </p>
     *
     * @return the muCommander version that was used to write the configuration file, <code>null</code> if it is unknown.
     */
    public String getVersion() {
        return version;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if(isRootElement) {
            version = attributes.getValue(MuPreferences.VERSION_ATTRIBUTE);
            isRootElement = false;
        }
    }
}
