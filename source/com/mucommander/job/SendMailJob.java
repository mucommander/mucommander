
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

public class SendMailJob extends FileJob {

    private MainFrame mainFrame;
    private Vector filesToSend;

    /* Mail parameters */
    private String recipientString;
    private String mailSubject;
    private String mailBody;
    private String mailServer;
    private String fromName;
    private String fromAddress;

    private final static String BOUNDARY = "muCommander_rednammoCum";

    /* Connection variable */
//    private BufferedWriter out;
    private BufferedReader in;
    private OutputStream out;
    private Socket socket;

    private int currentFilePercent;
    private int currentFileIndex = -1;

    private final static int OK_ACTION = 0;
    private final static int OK_MNEMONIC = KeyEvent.VK_O;
    private final static String OK_CAPTION = "OK";
    
    public SendMailJob(MainFrame mainFrame, ProgressDialog progressDialog, Vector filesToSend, String recipientString, String mailSubject, String mailBody) {
        super(progressDialog);

        this.mainFrame = mainFrame;
        this.filesToSend = filesToSend;
        this.recipientString = recipientString;
        this.mailSubject = mailSubject;
        this.mailBody = mailBody;

        this.mailServer = ConfigurationManager.getVariable("prefs.mail.smtp_server");
        this.fromName = ConfigurationManager.getVariable("prefs.mail.name");
        this.fromAddress = ConfigurationManager.getVariable("prefs.mail.from");
    }

    /**
     * Returns the percent done for current file.
     */
    public int getFilePercentDone() {
        return currentFilePercent;
    }

    /**
     * Returns the percent of job done so far.
     */
    public int getTotalPercentDone() {
        return (int)(100*(currentFileIndex/(float)filesToSend.size()));
    }

    /**
     * Returns a String describing what's currently being done.
     */
    public String getCurrentInfo() {
        if(currentFileIndex==-1)
            return "Connecting to "+mailServer;
        else {
            return "Sending "+((AbstractFile)filesToSend.elementAt(currentFileIndex)).getName();
        }
    }

    public void run() {
        try { openConnection(); }
        catch(IOException e) { showErrorDialog("Unable to contact server, check mail server settings."); return; }

        try { sendBody(); }
        catch(IOException e) { showErrorDialog("Mail refused by server."); }

        int nbFiles = filesToSend.size();
        for(int i=0; i<nbFiles; i++) {
            this.currentFileIndex = i;
            try { sendAttachment((AbstractFile)filesToSend.elementAt(i)); }
            catch(IOException e) { showErrorDialog("Unable to send "+((AbstractFile)filesToSend.elementAt(i)).getName()+"."); return; }
        }

        try { sayGoodBye(); }
        catch(IOException e) { showErrorDialog("Unable to terminate connection."); return; }

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
//        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "8859_1"));
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
            // send the GIF
        sendln(out, "Content-Type:text/plain; name="+file.getName());
        sendln(out, "Content-Disposition: attachment;filename=\""+file.getName()+"\"");
        sendln(out, "Content-transfer-encoding: base64\r\n");
        MIMEBase64Encoder.encode(file, out);
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
