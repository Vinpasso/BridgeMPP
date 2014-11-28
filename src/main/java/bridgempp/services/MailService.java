/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.BridgeService;
import bridgempp.CommandInterpreter;
import bridgempp.Endpoint;
import bridgempp.ShadowManager;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vinpasso
 */
public class MailService implements BridgeService {

    private ArrayList<Endpoint> endpoints;
    private Session session;
    private Store store;
    private IMAPFolder folder;
    private String imaphost;
    private int imapport;
    private String username;
    private String password;
    private String smtphost;
    private String smtpport;

    @Override
    public void connect(String argumentString) {
        ShadowManager.log(Level.INFO, "Loading Mail Service...");

        endpoints = new ArrayList<>();
        String[] args = argumentString.split("; ");
        if (args.length != 6) {
            throw new UnsupportedOperationException("Incorrect Arguments for mailer service");
        }
        imaphost = args[0];
        imapport = Integer.parseInt(args[1]);
        smtphost = args[2];
        smtpport = args[3];
        username = args[4];
        password = args[5];
        try {

            System.getProperties().setProperty("mail.store.protocol", "imaps");
            System.getProperties().setProperty("mail.smtp.auth", "true");
            System.getProperties().setProperty("mail.smtp.host", smtphost);
            System.getProperties().setProperty("mail.smtp.port", smtpport);
            session = Session.getDefaultInstance(System.getProperties(), null);
            store = session.getStore("imaps");
            store.connect(imaphost, imapport, username, password);
            folder = (IMAPFolder) store.getFolder("Inbox");
            folder.open(Folder.READ_WRITE);
            new Thread(new MailMessageListener(), "Mail Message Listener").start();
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
        }
        ShadowManager.log(Level.INFO, "Mail service loaded");
    }

    @Override
    public void disconnect() {
        try {
            folder.close(true);
            store.close();
        } catch (MessagingException ex) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendMessage(bridgempp.Message message) {
        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(username);
            mimeMessage.setRecipients(Message.RecipientType.TO, message.getTarget().getTarget());
            mimeMessage.setSubject(message.toSimpleString());
            mimeMessage.setText(message.toSimpleString());
            Transport.send(mimeMessage, username, password);
        } catch (MessagingException ex) {
            Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getName() {
        return "Mail";
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    private class MailMessageListener implements Runnable, MessageCountListener {

        Thread autoRenewThread;

        public MailMessageListener() {
            endpoints = new ArrayList<>();
            folder.addMessageCountListener(this);
            autoRenewThread = new Thread(new Runnable() {
                private static final long KEEP_ALIVE_FREQUENCY = 1740000l;

                @Override
                public void run() {
                    try {
                        while (folder.isOpen()) {
                            try {
                                ShadowManager.log(Level.INFO, "MailService: Sending Keep-Alive NOOP");

                                folder.doCommand(new IMAPFolder.ProtocolCommand() {

                                    @Override
                                    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                        protocol.simpleCommand("NOOP", null);
                                        return null;
                                    }
                                });
                                ShadowManager.log(Level.INFO, "MailService: Sent Keep-Alive NOOP");
                                Thread.sleep(KEEP_ALIVE_FREQUENCY);
                            } catch (MessagingException ex) {
                                Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (InterruptedException e) {
                        Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }, "Mail Connection Renew Thread");
            autoRenewThread.start();
        }

        @Override
        public void run() {
            while (folder.isOpen()) {
                try {
                    for (Message message : folder.getMessages()) {
                        processMessage(message);
                    }
                    folder.idle();
                } catch (MessagingException ex) {
                    Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            autoRenewThread.interrupt();
        }

        @Override
        public void messagesAdded(MessageCountEvent e) {
            /*Message[] messages = e.getMessages();
             for(Message message : messages)
             {
             processMessage(message);
             }*/
        }

        @Override
        public void messagesRemoved(MessageCountEvent e) {
        }

        public void processMessage(Message message) {
            try {
                String sender = message.getFrom()[0].toString();
                for (int i = 0; i < endpoints.size(); i++) {
                    if (endpoints.get(i).getTarget().equals(sender)) {
                        bridgempp.Message bMessage = new bridgempp.Message(endpoints.get(i), message.getContent().toString());
                        CommandInterpreter.processMessage(bMessage);
                        folder.setFlags(new Message[]{message}, new Flags(Flags.Flag.DELETED), true);
                        folder.expunge();
                        return;
                    }
                }
                Endpoint endpoint = new Endpoint(MailService.this, message.getFrom()[0].toString());
                endpoints.add(endpoint);
                bridgempp.Message bMessage = new bridgempp.Message(endpoint, message.getContent().toString());
                CommandInterpreter.processMessage(bMessage);
            } catch (MessagingException | IOException ex) {
                Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
