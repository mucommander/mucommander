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
package com.mucommander.commons.file.protocol.s3;

import javax.swing.JFrame;

import com.mucommander.protocol.ui.ProtocolPanelProvider;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;

/**
 * JPMS service provider for S3 protocol UI panel.
 *
 * @author Arik Hadas
 */
public class S3ProtocolPanelProvider implements ProtocolPanelProvider {

    @Override
    public String getSchema() {
        return "s3";
    }

    @Override
    public ServerPanel get(ServerPanelListener listener, JFrame mainFrame) {
        return new S3Panel(listener, mainFrame);
    }

    @Override
    public int priority() {
        return 3000;
    }

    @Override
    public Class<? extends ServerPanel> getPanelClass() {
        return S3Panel.class;
    }
}
