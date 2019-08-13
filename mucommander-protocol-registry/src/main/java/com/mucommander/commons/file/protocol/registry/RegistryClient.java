/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2019
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

package com.mucommander.commons.file.protocol.registry;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.mucommander.commons.file.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Daniel Erez
 */
public class RegistryClient implements Closeable {

	private static Logger log = LoggerFactory.getLogger(RegistryClient.class);

	private final Credentials creds;
	private final String imageUrl;

	private List<String> layers;
	private String tempFolder;

	public RegistryClient(Credentials creds, String imageUrl) {
		this.creds = creds;
		this.imageUrl = imageUrl;
	}

	public void connect() throws IOException {
		log.debug("RegistryClient::connect");
		JSONObject manifest = SkopeoCommandExecutor.inspect(imageUrl, creds);
		JSONArray layersArray = (JSONArray)manifest.get("Layers");
		layers = (List<String>) layersArray.stream().map(v->v.toString()).collect(Collectors.toList());

		String imageDigest = (String) manifest.get("Digest");
		tempFolder = SkopeoCommandExecutor.copy(imageUrl, imageDigest, creds);
	}

	@Override
	public void close() throws IOException {
	}

	public List<String> getLayers() {
		return layers;
	}

	public String getTempFolder() {
		return tempFolder;
	}
}
