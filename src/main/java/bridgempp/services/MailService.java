/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.io.IOException;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "MAIL_SERVICE")
@DiscriminatorValue(value = "MAIL_SERVICE")
public class MailService extends BridgeService
{

	transient private Session session;
	transient private Store store;
	transient private IMAPFolder folder;
	transient private IMAPFolder processedFolder;
	@Column(name = "IMAP_HOST", nullable = false, length = 50)
	private String imaphost;
	@Column(name = "IMAP_PORT", nullable = false)
	private int imapport;
	@Column(name = "USERNAME", nullable = false, length = 50)
	private String username;
	@Column(name = "PASSWORD", nullable = false, length = 50)
	private String password;
	@Column(name = "SMTP_HOST", nullable = false, length = 50)
	private String smtphost;
	@Column(name = "SMTP_PORT", nullable = false, length = 50)
	private int smtpport;

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.HTML, MessageFormat.PLAIN_TEXT };

	@Override
	public void connect()
	{
		ShadowManager.log(Level.INFO, "Loading Mail Service...");

		try
		{

			System.getProperties().setProperty("mail.store.protocol", "imaps");
			System.getProperties().setProperty("mail.smtp.submitter", "username");
			System.getProperties().setProperty("mail.smtp.auth", "true");
			System.getProperties().setProperty("mail.smtp.host", smtphost);
			System.getProperties().setProperty("mail.smtp.port", smtpport + "");

			Authenticator authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(username, password);
				}

			};

			session = Session.getDefaultInstance(System.getProperties(), authenticator);
			store = session.getStore("imaps");
			store.connect(imaphost, imapport, username, password);
			folder = (IMAPFolder) store.getFolder("Inbox");
			folder.open(Folder.READ_WRITE);
			processedFolder = (IMAPFolder) store.getFolder("BridgeMPP Processed Messages");
			if (!processedFolder.exists())
			{
				processedFolder.create(Folder.HOLDS_MESSAGES);
			}
			processedFolder.open(Folder.READ_WRITE);
			new Thread(new MailMessageListener(), "Mail Message Listener").start();
		} catch (NoSuchProviderException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		} catch (MessagingException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
		ShadowManager.log(Level.INFO, "Mail service loaded");
	}

	@Override
	public void disconnect()
	{
		try
		{
			folder.close(true);
			processedFolder.close(true);
			store.close();
		} catch (MessagingException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void sendMessage(bridgempp.Message message)
	{
		try
		{
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(username);
			mimeMessage.setRecipients(Message.RecipientType.TO, message.getDestination().getIdentifier());
			mimeMessage.setSubject(message.toSimpleString(getSupportedMessageFormats()));
			mimeMessage.setText(message.toSimpleString(getSupportedMessageFormats()));
			Transport.send(mimeMessage, username, password);
		} catch (MessagingException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String getName()
	{
		return "Mail";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	private class MailMessageListener implements Runnable, MessageCountListener
	{

		Thread autoRenewThread;

		public MailMessageListener()
		{
			folder.addMessageCountListener(this);
			autoRenewThread = new Thread(new Runnable() {
				private static final long KEEP_ALIVE_FREQUENCY = 1740000l;

				@Override
				public void run()
				{
					try
					{
						while (folder.isOpen())
						{
							try
							{
								ShadowManager.log(Level.INFO, "MailService: Sending Keep-Alive NOOP");

								folder.doCommand(new IMAPFolder.ProtocolCommand() {

									@Override
									public Object doCommand(IMAPProtocol protocol) throws ProtocolException
									{
										protocol.simpleCommand("NOOP", null);
										return null;
									}
								});
								ShadowManager.log(Level.INFO, "MailService: Sent Keep-Alive NOOP");
								Thread.sleep(KEEP_ALIVE_FREQUENCY);
							} catch (MessagingException ex)
							{
								ShadowManager.log(Level.SEVERE, null, ex);
							}
						}
					} catch (InterruptedException e)
					{
						ShadowManager.log(Level.WARNING, "Mail Message Listener interrupted. Shutting down Mail Message Listener");
					}
				}
			}, "Mail Connection Renew Thread");
			autoRenewThread.start();
		}

		@Override
		public void run()
		{
			while (folder.isOpen())
			{
				try
				{
					for (Message message : folder.getMessages())
					{
						processMessage(message);
					}
					folder.idle();
				} catch (MessagingException ex)
				{
					ShadowManager.log(Level.SEVERE, null, ex);
				}
			}
			autoRenewThread.interrupt();
		}

		@Override
		public void messagesAdded(MessageCountEvent e)
		{
			try
			{
				for (Message message : folder.getMessages())
				{
					processMessage(message);
				}
			} catch (MessagingException e1)
			{
				ShadowManager.log(Level.SEVERE, "Mailbox error", e1);
			}
		}

		@Override
		public void messagesRemoved(MessageCountEvent e)
		{
		}

		public void processMessage(Message message)
		{
			try
			{
				String sender = message.getFrom()[0].toString();
				Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(sender, MailService.this);
				User user = DataManager.getOrNewUserForIdentifier(sender, endpoint);

				bridgempp.Message bMessage = new bridgempp.Message(user, endpoint, message.getContent().toString(), getSupportedMessageFormats()[0]);
				CommandInterpreter.processMessage(bMessage);
				folder.copyMessages(new Message[] { message }, processedFolder);
				folder.setFlags(new Message[] { message }, new Flags(Flags.Flag.DELETED), true);
				folder.expunge();
				return;

			} catch (MessagingException | IOException ex)
			{
				ShadowManager.log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats;
	}

	public void configure(String imaphost, int imapport, String username, String password, String smtphost, int smtpport)
	{
		this.imaphost = imaphost;
		this.imapport = imapport;
		this.username = username;
		this.password = password;
		this.smtphost = smtphost;
		this.smtpport = smtpport;
	}

}
