
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.text.SizeFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.net.Socket;

public class SendMailJob extends ExtendedFileJob {

    private MainFrame mainFrame;
    private Vector filesToSend;
    private MIMEBase64Encoder base64Encoder;

    private boolean connectedToMailServer = false;

    /* Mail parameters */
    private String recipientString;
    private String mailSubject;
    private String mailBody;
    private String mailServer;
    private String fromName;
    private String fromAddress;
	
	private String boundary;

//    private final static String BOUNDARY = "mucommander";

    /* Connection variable */
//    private BufferedWriter out;
    private BufferedReader in;
    private OutputStream out;
    private Socket socket;

    /** Size of current file */
    private long currentFileSize;

	/** Index of file currently being processed */
	private int currentFileIndex;

	/** Info string about the file currently being processed */
	private String currentFileInfo = "";

    /** Number of bytes processed so far */
    private long nbBytesProcessed;

    /** Number of files that this job contains */
    private int nbFiles;
    
    private final static int OK_ACTION = 0;
    private final static int OK_MNEMONIC = KeyEvent.VK_O;
    private final static String OK_CAPTION = "OK";
    
    public SendMailJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToSend, String recipientString, String mailSubject, String mailBody) {
        super(progressDialog, mainFrame);

        this.mainFrame = mainFrame;
        this.filesToSend = filesToSend;
        this.nbFiles = filesToSend.size();
        this.base64Encoder = new MIMEBase64Encoder(this);
        this.boundary = "muco"+System.currentTimeMillis();
		
        this.recipientString = recipientString;
        this.mailSubject = mailSubject;
        this.mailBody = mailBody+"\n\n"+"Sent by muCommander - http://www.mucommander.com\n";

        this.mailServer = ConfigurationManager.getVariable("prefs.mail.smtp_server");
        this.fromName = ConfigurationManager.getVariable("prefs.mail.sender_name");
        this.fromAddress = ConfigurationManager.getVariable("prefs.mail.sender_address");
    }

	/**
	 * Returns true if mail preferences have been set.
	 */
	public static boolean mailPreferencesSet() {
        return ConfigurationManager.isVariableSet("prefs.mail.smtp_server")
			&& ConfigurationManager.isVariableSet("prefs.mail.sender_name")
			&& ConfigurationManager.isVariableSet("prefs.mail.sender_address");
	}


    public long getTotalBytesProcessed() {
        return nbBytesProcessed + base64Encoder.getTotalRead();
    }

    public int getCurrentFileIndex() {
        return currentFileIndex;
    }

    public int getNbFiles() {
        return nbFiles;
    }
    
    public long getCurrentFileBytesProcessed() {
        return connectedToMailServer?base64Encoder.getTotalRead():0;
    }

    public long getCurrentFileSize() {
        return currentFileSize;
    }


    /**
     * Returns a String describing what's currently being done.
     */
    public String getStatusString() {
        AbstractFile currentFile;
		if(connectedToMailServer)
            return "Sending "+currentFileInfo;
        else
            return "Connecting to "+mailServer;
    }

    public void run() {
        // Open socket connection to the mail server, and say hello
        try {
            openConnection();
        }
        catch(IOException e) {
            showErrorDialog("Unable to contact server, check mail server preferences.");
        }

		if(isInterrupted()) {
			cleanUp();
			return;
		}
		
        // Send mail body
		try {
            sendBody();
        }
        catch(IOException e) {
            showErrorDialog("Connection terminated by server, mail not sent.");
		}

		if(isInterrupted()) {
			cleanUp();
			return;
		}
		
        // Send attachments
        AbstractFile file;
        for(int i=0; i<nbFiles && !isInterrupted(); i++) {
            this.currentFileIndex = i;
            file = (AbstractFile)filesToSend.elementAt(i);
            this.currentFileSize = file.getSize();
			this.currentFileInfo = "\""+file.getName()+"\" ("+SizeFormatter.format(currentFileSize, SizeFormatter.DIGITS_MEDIUM|SizeFormatter.UNIT_SHORT|SizeFormatter.ROUND_TO_KB)+")";
            try {
                sendAttachment(file);
                if(i!=nbFiles-1)
					nbBytesProcessed += currentFileSize;
            }
            catch(IOException e) {
                showErrorDialog("Unable to send "+file.getName()+", mail not sent.");
			}

			if(isInterrupted()) {
				cleanUp();
				return;
			}
        }

		// Notifies the mail server that the mail is over
		try {
			sayGoodBye();
		}
		catch(IOException e) {
			showErrorDialog("Unable to close connection, mail may have been sent.");
		}

        // Close the connection
        closeConnection();

        stop();

		cleanUp();
    }    

    private void showErrorDialog(String message) {
        closeConnection();

		JOptionPane.showMessageDialog(progressDialog, message, "Email files error", JOptionPane.ERROR_MESSAGE);	

		stop();
    }

    /********* Methods taking care of mail sending *********/
    
    private void openConnection() throws IOException {
        this.socket = new Socket(mailServer, 25);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.out = socket.getOutputStream();

		this.connectedToMailServer = true;
	}

    private void sendBody() throws IOException {
        // here you are supposed to send your username
        sendln(in, out, "HELO muCommander");
        // warning : some mail server validate the sender address
        //           in the MAIL FROM command, put your real address here
        sendln(in, out, "MAIL FROM: "+fromAddress);
		
		Vector recipients = new Vector();
		recipientString = splitRecipientString(recipientString, recipients);
		int nbRecipients = recipients.size();
		for(int i=0; i<nbRecipients; i++)
			sendln(in, out, "RCPT TO: <"+recipients.elementAt(i)+">" );
        sendln(in, out, "DATA");
        sendln(out, "MIME-Version: 1.0");
        sendln(out, "Subject: "+this.mailSubject);
        sendln(out, "From: "+this.fromName+" <"+this.fromAddress+">");
        sendln(out, "To: "+recipientString);
        sendln(out, "Content-Type: multipart/mixed; boundary=\"" + boundary +"\"");
        sendln(out, "\r\n--" + boundary);

        // Send the body
//        sendln(out, "Content-Type: text/plain; charset=\"us-ascii\"\r\n");
        sendln(out, "Content-Type: text/plain; charset=\"utf-8\"\r\n");
        sendln(out, this.mailBody+"\r\n\r\n");
        sendln(out, "\r\n--" +  boundary );        
    }
    

	/**
	 * Parses the specified string, replaces delimiter characters if needed and adds recipients  (String instances) to the given Vector.
	 *
	 * @param recipientsStr String containing one or several recipients that need to be separated by ',' and/or ';' characters.
	 */
	private String splitRecipientString(String recipientsStr, Vector recipients) {
		recipientsStr.replace(';', ',');

		StringBuffer newRecipientsSb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(recipientsStr, ",");
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
        sendln(out, "Content-Type:"+MimeTypes.getMimeType(file)+"; name="+file.getName());
        sendln(out, "Content-Disposition: attachment;filename=\""+file.getName()+"\"");
        sendln(out, "Content-transfer-encoding: base64\r\n");
        base64Encoder.encode(file, out);
        sendln(out, "\r\n--" + boundary);
    }

    private void sayGoodBye() throws IOException {
        sendln(out, "\r\n\r\n--" + boundary + "--\r\n");
        sendln(in, out,".");
        sendln(in, out, "QUIT");        
    }

    private void closeConnection() {
        try {
            socket.close();
            in.close();
            out.close();
        }
        catch(Exception e){
        }
    }
    
    private void sendln(BufferedReader in, OutputStream out, String s) throws IOException {
		//        out.write((s + "\r\n").getBytes("8859_1"));
        out.write((s + "\r\n").getBytes("UTF-8"));
        out.flush();
        s = in.readLine();
    }

    private void sendln(OutputStream out, String s) throws IOException {
//        out.write((s + "\r\n").getBytes("8859_1"));
        out.write((s + "\r\n").getBytes("UTF-8"));
        out.flush();
    }
    
}
