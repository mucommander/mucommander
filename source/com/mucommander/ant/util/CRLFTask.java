/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ant.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.*;

/**
 * @author Nicolas Rinaudo
 * @ant.task name="crlf" category="util"
 */
public class CRLFTask extends Task {
    private File input;
    private File output;


    public CRLFTask() {}

    public void init() {
        input  = null;
        output = null;
    }

    public void setIn(File file) {input = file;}
    public void setOut(File file) {output = file;}

    public void execute() throws BuildException {
        BufferedReader in;
        PrintStream    out;
        String         line;

        if(input == null)
            throw new BuildException("Unspecified input file.");
        if(output == null)
            throw new BuildException("Unspecified output file.");

        in  = null;
        out = null;
        try {
            in  = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
            out = new PrintStream(new FileOutputStream(output));

            while((line = in.readLine()) != null) {
                out.print(line);
                out.print("\r\n");
            }
        }
        catch(Exception e) {throw new BuildException(e);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }

            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }
}
