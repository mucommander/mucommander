
package com.mucommander.job;

import com.mucommander.file.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.QuestionDialog;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.conf.ConfigurationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.Vector;
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

    private final static String BOUNDARY = "mucommander";

    /* Connection variable */
//    private BufferedWriter out;
    private BufferedReader in;
    private OutputStream out;
    private Socket socket;

    /** Size of current file */
    private long currentFileSize;

	/** Index of file currently being processed */
	private int currentFileIndex;

    /** Number of bytes processed so far */
    private long nbBytesProcessed;

    /** Number of files that this job contains */
    private int nbFiles;
    
    private final static int OK_ACTION = 0;
    private final static int OK_MNEMONIC = KeyEvent.VK_O;
    private final static String OK_CAPTION = "OK";
    
    public SendMailJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToSend, String recipientString, String mailSubject, String mailBody) {
        super(progressDialog);

        this.mainFrame = mainFrame;
        this.filesToSend = filesToSend;
        this.nbFiles = filesToSend.size();
        this.base64Encoder = new MIMEBase64Encoder();
        
        this.recipientString = recipientString;
        this.mailSubject = mailSubject;
        this.mailBody = mailBody;

        this.mailServer = ConfigurationManager.getVariable("prefs.mail.smtp_server");
        this.fromName = ConfigurationManager.getVariable("prefs.mail.name");
        this.fromAddress = ConfigurationManager.getVariable("prefs.mail.from");
    }


    public long getTotalBytesProcessed() {
        return nbBytesProcessed;
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
        if(connectedToMailServer)
            return "Sending "+((AbstractFile)filesToSend.elementAt(currentFileIndex)).getName();
        else
            return "Connecting to "+mailServer;
    }

    public void run() {
        // Open socket connection to the mail server, and say hello
        try {
            openConnection();
        }
        catch(IOException e) {
            showErrorDialog("Unable to contact server, check mail server settings.");
            return;
        }

        // Send mail body
        try {
            sendBody();
        }
        catch(IOException e) {
            showErrorDialog("Mail refused by server.");
        }

        // Send attachments
        AbstractFile file;
        for(int i=0; i<nbFiles; i++) {
            this.currentFileIndex = i;
            file = (AbstractFile)filesToSend.elementAt(i);
            this.currentFileSize = file.getSize();
            try {
                sendAttachment(file);
                nbBytesProcessed += base64Encoder.getTotalRead();
            }
            catch(IOException e) {
                showErrorDialog("Unable to send "+((AbstractFile)filesToSend.elementAt(i)).getName()+".");
                return; }
        }

        // Notifies the mail server that the mail is over
        try {
            sayGoodBye();
        }
        catch(IOException e) {
            showErrorDialog("Unable to terminate connection."); return;
        }

        // Close the connection
        closeConnection();

        stop();
    }    

    private void showErrorDialog(String message) {
        QuestionDialog dialog = new QuestionDialog(progressDialog, "Send mail error", message, mainFrame,
                                                   new String[] {OK_CAPTION},
                                                   new int[]  {OK_ACTION},
                                                   new int[]  {OK_MNEMONIC},
                                                   0);

        stop();
        closeConnection();
    }

    /********* Methods taking care of mail sending *********/
    
    private void openConnection() throws IOException {
        this.socket = new Socket(mailServer, 25);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "8859_1"));
        this.out = socket.getOutputStream();
    }

    private void sendBody() throws IOException {
        // here you are supposed to send your username
        sendln(in, out, "HELO thisIsMuCommander");
        // warning : some mail server validate the sender address
        //           in the MAIL FROM command, put your real address here
        sendln(in, out, "MAIL FROM: "+fromAddress);
        sendln(in, out, "RCPT TO: <"+recipientString+">" );
        sendln(in, out, "DATA");
        sendln(out, "MIME-Version: 1.0");
        sendln(out, "Subject: "+this.mailSubject);
        sendln(out, "From: "+this.fromName+"<"+this.fromAddress+">");
        sendln(out, "Content-Type: multipart/mixed; boundary=\"" + BOUNDARY +"\"");
        sendln(out, "\r\n--" + BOUNDARY);

        // Send the body
        sendln(out, "Content-Type: text/plain; charset=\"us-ascii\"\r\n");
        sendln(out, this.mailBody+"\r\n\r\n");
        sendln(out, "\r\n--" +  BOUNDARY );        
    }
    
    private void sendAttachment(AbstractFile file) throws IOException {
        // sends MIME type of attachment file
        sendln(out, "Content-Type:"+MimeTypes.getMimeType(file)+"; name="+file.getName());
        sendln(out, "Content-Disposition: attachment;filename=\""+file.getName()+"\"");
        sendln(out, "Content-transfer-encoding: base64\r\n");
        base64Encoder.encode(file, out);
        sendln(out, "\r\n--" + BOUNDARY);
    }

    private void sayGoodBye() throws IOException {
        sendln(out, "\r\n\r\n--" + BOUNDARY + "--\r\n");
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
        out.write((s + "\r\n").getBytes("8859_1"));
        out.flush();
        s = in.readLine();
    }

    private void sendln(OutputStream out, String s) throws IOException {
        out.write((s + "\r\n").getBytes("8859_1"));
        out.flush();
    }
    
}
