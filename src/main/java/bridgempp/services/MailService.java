/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.ShadowManager;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
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

			Properties mailProperties = new Properties();
			
			mailProperties.setProperty("mail.store.protocol", "imaps");
			mailProperties.setProperty("mail.smtp.submitter", username);
			mailProperties.setProperty("mail.smtp.auth", "true");
			mailProperties.setProperty("mail.smtp.host", smtphost);
			mailProperties.setProperty("mail.smtp.port", smtpport + "");
			mailProperties.put("mail.smtp.socketFactory.port", smtpport);
			mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			mailProperties.put("mail.smtp.socketFactory.fallback", "false");
			mailProperties.put("mail.smtp.starttls.enable", "true");

			Authenticator authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(username, password);
				}

			};

			session = Session.getDefaultInstance(mailProperties, authenticator);
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
			mimeMessage.setFrom(new InternetAddress(username, "BridgeMPP"));
			Collection<User> recipients = message.getDestination().getUsers();
			Address[] recipientAddresses = new Address[recipients.size()];
			Iterator<User> iterator = recipients.iterator();
			for(int i = 0; i < recipients.size(); i++)
			{
				User user = iterator.next();
				recipientAddresses[i] = new InternetAddress(user.getIdentifier(), user.getName());
			}
			
			mimeMessage.setRecipients(Message.RecipientType.TO, recipientAddresses);
			mimeMessage.setSubject(message.getDestination().getIdentifier());
			mimeMessage.setText(message.toSimpleString(getSupportedMessageFormats()));
			Transport.send(mimeMessage, username, password);
		} catch (Exception ex)
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
//					for (Message message : folder.getMessages())
//					{
//						processMessage(message);
//					}
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

		public synchronized void processMessage(Message message)
		{
			try
			{
				Address[] address = message.getFrom();
				String sender = "";
				for(int i = 0; i < address.length; i++)
				{
					if(address[i] != null)
					{
						sender = ((InternetAddress)address[i]).getAddress();
					}
				}
				
				Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(message.getSubject(), MailService.this);
				User user = DataManager.getOrNewUserForIdentifier(sender, endpoint);

				bridgempp.Message bMessage = new bridgempp.Message(user, endpoint, getMessageContent(message), getSupportedMessageFormats()[0]);
				receiveMessage(bMessage);
				folder.copyMessages(new Message[] { message }, processedFolder);
				folder.setFlags(new Message[] { message }, new Flags(Flags.Flag.DELETED), true);
				folder.expunge();
				return;

			} catch (MessagingException | IOException ex)
			{
				ShadowManager.log(Level.SEVERE, null, ex);
			}
		}

		protected String getMessageContent(Message message) throws IOException, MessagingException
		{
			Object messageContent = message.getContent();
			if(messageContent instanceof Multipart)
			{
				Multipart container = (Multipart) messageContent;
				for(int i = 0; i < container.getCount(); i++)
				{
					BodyPart part = container.getBodyPart(i);
					Object content = part.getContent();
					if(content instanceof String)
					{
						return (String) content;
					}
				}
			}
			return messageContent.toString();
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
