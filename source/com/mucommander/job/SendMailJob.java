
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.text.Translator;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.net.Socket;

import javax.swing.*;


/**
 * This job sends one or several files by email.
 */
public class SendMailJob extends ExtendedFileJob {

    /** Files that are going to be sent */
	private Vector filesToSend;
    
	/** True after connection to mail server has been established */
	private boolean connectedToMailServer;

	/** Error dialog title */
	private String errorDialogTitle;
	
	
	/////////////////////
    // Mail parameters //
	/////////////////////
	/** Email recipient(s) */
    private String recipientString;
	/** Email subject */
    private String mailSubject;
	/** Email body */
    private String mailBody;

	/** SMTP server */
    private String mailServer;
	/** From name */
    private String fromName;
	/** From address */
    private String fromAddress;
	
	/** Email boundary string, delimits the end of the body and attachments */
	private String boundary;

    /** Connection variable */
//    private BufferedReader in;
    private DataInputStream in;
	/** OuputStream to the SMTP server */
    private OutputStream out;
	/** Base64OuputStream to the SMTP server */
    private Base64OutputStream out64;
	/** Socket connection to the SMTP server */
    private Socket socket;

	
	private final static String CLOSE_TEXT = Translator.get("close");
	private final static int CLOSE_ACTION = 11;
	
	
    public SendMailJob(ProgressDialog progressDialog, MainFrame mainFrame, Vector filesToSend, String recipientString, String mailSubject, String mailBody) {
        super(progressDialog, mainFrame, filesToSend);

        this.filesToSend = filesToSend;

		this.boundary = "mucommander"+System.currentTimeMillis();
        this.recipientString = recipientString;
        this.mailSubject = mailSubject;
        this.mailBody = mailBody+"\n\n"+"Sent by muCommander - http://www.mucommander.com\n";

        this.mailServer = ConfigurationManager.getVariable("prefs.mail.smtp_server");
        this.fromName = ConfigurationManager.getVariable("prefs.mail.sender_name");
        this.fromAddress = ConfigurationManager.getVariable("prefs.mail.sender_address");
    
		this.errorDialogTitle = Translator.get("email_dialog.error_title");
	}

	/**
	 * Returns true if mail preferences have been set.
	 */
	public static boolean mailPreferencesSet() {
        return ConfigurationManager.isVariableSet("prefs.mail.smtp_server")
			&& ConfigurationManager.isVariableSet("prefs.mail.sender_name")
			&& ConfigurationManager.isVariableSet("prefs.mail.sender_address");
	}


	/**
	 * Shows an error dialog with a single action : close, and stops the job.
	 */
    private void showErrorDialog(String message) {
		showErrorDialog(errorDialogTitle, message, new String[]{CLOSE_TEXT}, new int[]{CLOSE_ACTION});
		stop();
	}
	
	
	/**
	 * This method is called when this job starts, before the first call to {@link #processFile(AbstractFile,Object) processFile()} is made.
	 * This method here does nothing but it can be overriden by subclasses to perform some first-time initializations.
	 */
	protected void jobStarted() {
        // Open socket connection to the mail server, and say hello
        try {
            openConnection();
        }
        catch(IOException e) {
            showErrorDialog("Unable to contact mail server "+mailServer+", check mail server preferences.");
        }

		if(isInterrupted())
			return;
		
        // Send mail body
		try {
            sendBody();
        }
        catch(IOException e) {
            showErrorDialog("Connection terminated by server, mail not sent.");
		}
	}
	

	/**
	 * Overriden method to say 'goodbye' to the mail server.
	 */
	protected void jobCompleted() {
		// Notifies the mail server that the mail is over
		try {
			// Writes padding bytes without closing the underlying stream.
			out64.writePadding();

			// Say goodbye to the server
			sayGoodBye();
		}
		catch(IOException e) {
			showErrorDialog("Unable to close connection, mail may not have been sent.");
		}
	}
	
	
	/**
     * Overrident method to close connection to the mail server.
	 */
	protected void jobStopped() {
        // Close the connection
        closeConnection();
	}
	
	
    protected boolean processFile(AbstractFile file, Object recurseParams) {
		if(isInterrupted())
			return false;

        // Send file attachment
		try {
			sendAttachment(file);
			return true;
		}
		catch(IOException e) {
			showErrorDialog("Unable to send "+file.getName()+", mail not sent.");
			return false;
		}
    }    

	
    /***********************************************
	 *** Methods taking care of sending the mail ***
	 ***********************************************/
    
    private void openConnection() throws IOException {
        this.socket = new Socket(mailServer, 25);
//        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.in = new DataInputStream(socket.getInputStream());
        this.out = socket.getOutputStream();
		this.out64 = new Base64OutputStream(out);
		
		this.connectedToMailServer = true;
	}

    private void sendBody() throws IOException {
        // here you are supposed to send your username
        readWriteLine("HELO muCommander");
        // warning : some mail server validate the sender address and will fail is an invalid
		// address is provided
        readWriteLine("MAIL FROM: "+fromAddress);
		
		Vector recipients = new Vector();
		recipientString = splitRecipientString(recipientString, recipients);
		int nbRecipients = recipients.size();
		for(int i=0; i<nbRecipients; i++)
			readWriteLine("RCPT TO: <"+recipients.elementAt(i)+">" );
        readWriteLine("DATA");
        writeLine("MIME-Version: 1.0");
        writeLine("Subject: "+this.mailSubject);
        writeLine("From: "+this.fromName+" <"+this.fromAddress+">");
        writeLine("To: "+recipientString);
        writeLine("Content-Type: multipart/mixed; boundary=\"" + boundary +"\"");
        writeLine("\r\n--" + boundary);

        // Send the body
//        writeLine( "Content-Type: text/plain; charset=\"us-ascii\"\r\n");
        writeLine("Content-Type: text/plain; charset=\"utf-8\"\r\n");
        writeLine(this.mailBody+"\r\n\r\n");
        writeLine("\r\n--" +  boundary );        
    }
    

	/**
	 * Parses the specified string, replaces delimiter characters if needed and adds recipients  (String instances) to the given Vector.
	 *
	 * @param recipientsStr String containing one or several recipients that need to be separated by ',' and/or ';' characters.
	 */
	private String splitRecipientString(String recipientsStr, Vector recipients) {

		// /!\ this piece of code is far from being bullet proof but I'm too lazy now to rewrite it
		StringBuffer newRecipientsSb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(recipientsStr, ",;");
		String rec;
		int pos1, pos2;
		while(st.hasMoreTokens()) {
			rec = st.nextToken().trim();
			if((pos1=rec.indexOf('<'))!=-1 && (pos2=rec.indexOf('>', pos1+1))!=-1)
				recipients.add(rec.substring(pos1+1, pos2));
			else
				recipients.add(rec);
			newRecipientsSb.append(rec+(st.hasMoreTokens()?", ":""));
		}
		
		return newRecipientsSb.toString();
	}
	
	
    private void sendAttachment(AbstractFile file) throws IOException {
        // sends MIME type of attachment file
        writeLine("Content-Type:"+MimeTypes.getMimeType(file)+"; name="+file.getName());
        writeLine("Content-Disposition: attachment;filename=\""+file.getName()+"\"");
        writeLine("Content-transfer-encoding: base64\r\n");
        copyStream(in, out, 0);
        writeLine("\r\n--" + boundary);
    }
	
    private void sayGoodBye() throws IOException {
        writeLine("\r\n\r\n--" + boundary + "--\r\n");
        readWriteLine(".");
        readWriteLine("QUIT");
    }

    private void closeConnection() {
        try {
            socket.close();
            in.close();
            out64.close();
        }
        catch(Exception e){
        }
    }
    
    private void readWriteLine(String s) throws IOException {
		//        out.write((s + "\r\n").getBytes("8859_1"));
        out.write((s + "\r\n").getBytes("UTF-8"));
//        out.flush();
		// We use DataInputStream's readLine method even though it's deprecated
		// because we need the input stream to be an InputStream and not a Reader,
		// and we cannot both a InputStream and a BufferedReader above since BufferedReader
		// is, well, buffered.
        s = in.readLine();
    }

    private void writeLine(String s) throws IOException {
//        out.write((s + "\r\n").getBytes("8859_1"));
        out.write((s + "\r\n").getBytes("UTF-8"));
//        out.flush();
    }


	/*******************************************
	 *** ExtendedFileJob implemented methods ***
	 *******************************************/

    public String getStatusString() {
		if(connectedToMailServer)
            return "Sending "+getCurrentFileInfo();
        else
            return "Connecting to "+mailServer;
    }
}
