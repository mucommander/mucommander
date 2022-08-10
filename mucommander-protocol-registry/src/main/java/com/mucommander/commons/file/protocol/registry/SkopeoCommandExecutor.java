/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2019
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

package com.mucommander.commons.file.protocol.registry;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.process.AbstractProcess;
import com.mucommander.process.ProcessListener;
import com.mucommander.process.ProcessRunner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkopeoCommandExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SkopeoCommandExecutor.class);
	
    public final static String UTF8 = "UTF-8";

    /**
     * Check whether skopeo tool exists in the system.
     */
    public static boolean checkSkopeo() {
        StringBuilder output = new StringBuilder();
        try {
            SkopeoCommandExecutor.execute(output, "", null);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * The inspect command fetches the repository's manifest and it is able to
     * show you a docker inspect-like json output about a whole repository or a tag.
     */
    public static JSONObject inspect(String imageUrl, Credentials creds) throws IOException {
        StringBuilder output = new StringBuilder();
        SkopeoCommandExecutor.execute(output, "inspect", creds, imageUrl);
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(output.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (JSONObject)obj;
    }

    /**
     * Copy container images between various storage mechanisms (docker/oci/dir/etc).
     */
    public static String copy(String imageUrl, String imageDigest, Credentials creds) throws IOException {
        String tempFolder = FileFactory.getTemporaryFolder() + imageDigest;
        StringBuilder output = new StringBuilder();
        SkopeoCommandExecutor.execute(output, "copy", creds, imageUrl, "dir:" + tempFolder);
        return tempFolder;
    }

    private static String getSkopeoPath() {
        switch (OsFamily.getCurrent()) {
            case MAC_OS:
                return "/usr/local/bin/skopeo";
            case LINUX:
                return "/usr/bin/skopeo";
            default:
                return "skopeo";
        }
    }

    public static void execute(StringBuilder outputBuffer, String command, Credentials creds, String... args) throws IOException {
        LOGGER.debug("Executing skopeo command: " + command);

        List<String> tokens = new ArrayList<>(Arrays.asList(
                getSkopeoPath(),
                "--insecure-policy",
                "--override-os",
                "linux", // TODO: make this configurable
                command
        ));
        tokens.addAll(Arrays.asList(args));
        if (creds != null) {
            tokens.add("--creds");
            tokens.add(String.format("%s:%s", creds.getLogin(), creds.getPassword()));
        }

        try {
            // Execute the skopeo command.
            AbstractProcess process = ProcessRunner.execute(tokens.toArray(new String[0]),
                    new SkopeoCommandExecutor.CommandOutputListener(outputBuffer, UTF8));

            // Wait for the process to die
            int returnCode = process.waitFor();

            LOGGER.debug("skopeo returned code="+returnCode+", output="+ outputBuffer);

            if(returnCode!=0) {
            	LOGGER.debug("skopeo terminated abnormally");
            }
        }
        catch(Exception e) {
            // Shouldn't normally happen
        	LOGGER.debug("Unexcepted exception while executing skopeo", e);
            throw new IOException("skopeo execution failure");
        }
    }

    /**
     * This ProcessListener accumulates the output of the 'osascript' command and suppresses the trailing '\n' character
     * from the script's output.
     */
    private static class CommandOutputListener implements ProcessListener {

        private StringBuilder outputBuffer;
        private String outputEncoding;

        private CommandOutputListener(StringBuilder outputBuffer, String outputEncoding) {
            this.outputBuffer = outputBuffer;
            this.outputEncoding = outputEncoding;
        }

        ////////////////////////////////////
        // ProcessListener implementation //
        ////////////////////////////////////

        public void processOutput(byte[] buffer, int offset, int length) {
            try {
                outputBuffer.append(new String(buffer, offset, length, outputEncoding));
            }
            catch(UnsupportedEncodingException e) {
                // The encoding is necessarily supported
            }
        }

        public void processOutput(String s) {
        }

        public void processDied(int returnValue) {
            // Remove the trailing "\n" character that osascript returns.
            int len = outputBuffer.length();
            if(len>0 && outputBuffer.charAt(len-1)=='\n')
                outputBuffer.setLength(len-1);    
        }
    }
}
