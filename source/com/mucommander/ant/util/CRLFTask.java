package com.mucommander.ant.util;

import org.apache.tools.ant.*;
import java.io.*;

/**
 * @author Nicolas Rinaudo
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
