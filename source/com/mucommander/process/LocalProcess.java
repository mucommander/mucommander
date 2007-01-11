package com.mucommander.process;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Nicolas Rinaudo
 */
public class LocalProcess extends AbstractProcess {
    private Process process;

    public LocalProcess(String[] tokens, File dir) throws IOException {
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_5) {
            ProcessBuilder pb = new ProcessBuilder(tokens);
            // Set the process' working directory
            pb.directory(dir);
            // Merge the process' stdout and stderr 
            pb.redirectErrorStream(true);

            process = pb.start();
        }
        // Java 1.4 or below, use Runtime.exec() which separates stdout and stderr (harder to manipulate)
        else
            process = Runtime.getRuntime().exec(tokens, null, dir);
    }

    public boolean usesMergedStreams() {return PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_6;}
    public int waitFor() throws InterruptedException {return process.waitFor();}
    public void destroyProcess() {process.destroy();}
    public int exitValue() {return process.exitValue();}
    public OutputStream getOutputStream() {return process.getOutputStream();}
    public InputStream getErrorStream()  throws IOException{
        if(usesMergedStreams()) {
            if(Debug.ON) Debug.trace("Tried to access the error stream of a merged streams process.");
            throw new IOException();
        }
        return process.getErrorStream();
    }
    public InputStream getInputStream() {return process.getInputStream();}
}
