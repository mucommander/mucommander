
package com.mucommander;

import com.mucommander.file.AbstractFile;

import java.io.IOException;
import java.util.Vector;

// public class PlatformManager implements Runnable {

public class PlatformManager {

	// OS types
	// Windows 95, 98, Me
	public final static int WINDOWS_9X = 11;
	// Windows NT, 2000, XP and up
	public final static int WINDOWS_NT = 12;
	// Mac OS 7.x, 8.x or 9.x
	public final static int MAC_OS = 21;
	// Mac OS X and up
	public final static int MAC_OS_X = 22;
	// Other OS
	public final static int OTHER = 0;
	
	private static String osName;
	private static String osVersion;
	private static int osType;

    private static Process currentProcess;
//    private Process process;

    
	/**
	 * Finds out what kind of OS muCommander is running on
	 */
	static {
		osName = System.getProperty("os.name");
		osVersion = System.getProperty("os.version");
		
		// Windows family
		if (osName.startsWith("Windows")) {
			// Windows 95, 98, Me
			if (osName.startsWith("Windows 95") || osName.startsWith("Windows 98") || osName.startsWith("Windows Me"))
				osType = WINDOWS_9X;
			// Windows NT, 2000, XP and up
			else
				osType = WINDOWS_NT;
		}
		// Mac OS family
		else if (osName.startsWith("Mac OS")) {
			// Mac OS 7.x, 8.x or 9.x
			if(osVersion.startsWith("7.")
			|| osVersion.startsWith("8.")
			|| osVersion.startsWith("9."))
				osType = MAC_OS;
			// Mac OS X or up
			else		 
				osType = MAC_OS_X;
		}
		// Any other OS
		else {
			osType = OTHER;
		}
	}

/*
    private PlatformManager(Process p) {
        this.process = p;
    }
 */  
	
	/**
	 * Returns OS type (OS family if you wish).
	 */
	public static int getOsType() {
		return osType;
	}
	
	
	/**
	 * Returns <code>true</code> if the current platform is capable of opening the given URL
	 * in a new browser window.
	 */
	public static boolean canOpenURL() {
		return osName.startsWith("Mac OS") || osName.startsWith("Windows");
	}


	/**
	 * Executes an arbitrary command in the given folder and returns the corresponding Process object,
	 * or <code>null</code> if the command failed to execute
	 */
	public static Process execute(String command, AbstractFile currentFolder) {
		try {
            if(com.mucommander.Debug.TRACE)
                System.out.println("Executing "+command);

            Vector tokensV = new Vector();
			if (osType == WINDOWS_NT) {
				tokensV.add("cmd");
				tokensV.add("/c");
			}
			// Splits the command into tokens ourself, as exec() does it anyway
			command.trim();
			char c;
			int pos = 0;
			int len = command.length();
			StringBuffer token = new StringBuffer();
			while(pos<len) {
				c = command.charAt(pos);
				if((c==' ' && command.charAt(pos-1)!='\\') || c=='\t' || c=='\n' || c=='\r' || c=='\f') {
// if(com.mucommander.Debug.TRACE) System.out.println("token= "+token.toString());
					tokensV.add(token.toString());
					token = new StringBuffer();
				}
				else if(!(c=='\\' && pos!=len-1 && command.charAt(pos+1)==' ')) {
					token.append(c);
				}
				pos ++;
			}
			tokensV.add(token.toString());


if(com.mucommander.Debug.TRACE) {
	for(int i=0; i<tokensV.size(); i++)
		System.out.println("token"+i+"= ["+tokensV.elementAt(i)+"]");
}
	
			String tokens[] = new String[tokensV.size()];
			tokensV.toArray(tokens);

//			return Runtime.getRuntime().exec(tokens, null, new java.io.File(currentFolder.getAbsolutePath()));
			return Runtime.getRuntime().exec(command, null, new java.io.File(currentFolder.getAbsolutePath()));
		}
		catch(IOException e) {
            if(com.mucommander.Debug.TRACE)
                System.out.println("Error while executing "+command+": "+e);
            return null;
		}
	}


	/**
	 * Opens/executes the given file, from the given folder and returns <code>true</code>
	 * if the operation succeeded.
	 */
	public static boolean open(String filePath, AbstractFile currentFolder) {
		try {
			// Here, we use exec(String[],String[],File) instead of exec(String,String[],File)
			// so we parse the tokens ourself (messes up the command otherwise)

            if(com.mucommander.Debug.TRACE)
                System.out.println("Opening "+filePath);
            
            Runtime.getRuntime().exec(getOpenTokens(filePath), null, new java.io.File(currentFolder.getAbsolutePath()));
            
/*            if(com.mucommander.Debug.TRACE) {
                new Thread(new PlatformManager(currentProcess)).start();
            }
*/                
            return true;
		}
		catch(IOException e) {
            if(com.mucommander.Debug.TRACE)
                System.out.println("Error while opening "+filePath+": "+e);
            return false;
		}
	}


	private static String[] getOpenTokens(String filePath) {
		// Under Win32, the 'start' command opens a file with the program
		// registered with this file's extension (great!)
		// Windows 95, 98, Me : syntax is start "myfile"
		if (osType == WINDOWS_9X) {
			return new String[] {"start", "\""+filePath+"\""};
		}
		// Windows NT, 2000, XP : syntax is cmd /c start "" "myfile"
		else if (osType == WINDOWS_NT) {
			return new String[] {"cmd", "/c", "start", "\"\"", "\""+filePath+"\""};
		}
		// Mac OS X can do the same with 'open'
		else if (osType == MAC_OS_X)  {
			return new String[] {"open", filePath};
		}
		else {
			StringBuffer sb = new StringBuffer();
			char c;
			for(int i=0; i<filePath.length(); i++) {
				c = filePath.charAt(i);
				if(c==' ')
					sb.append("\\ ");
				else
					sb.append(c);
			}
			filePath = sb.toString();

			return new String[] {filePath};
		}
	}


    /**
     * Prints a process' output to System.out (for debugging purposes)
     */
/*
    public void run() {
        java.io.InputStream pin = process.getInputStream();

        byte b[] = new byte[512];
        int nbRead;
        int exitValue;

        System.out.println("process "+process);

        while(true) {
            try {
                while((nbRead=pin.read())!=-1) {
                    System.out.print(process);
                    System.out.write(b, 0, nbRead);
                }

                exitValue = process.exitValue();
                System.out.println("process "+process+" exit: "+exitValue);

                pin.close();
                return;
            }
            catch(Exception e) {
            }
        }
    }
*/	
}