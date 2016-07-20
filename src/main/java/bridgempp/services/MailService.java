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
import bridgempp.data.processing.Schedule;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
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
	transient private IMAPFolder inboxFolder;
	transient private IMAPFolder processedFolder;
	@Column(name = "IMAP_HOST", nullable = false, length = 50)
	private String imaphost;
	@Column(name = "IMAP_PORT", nullable = false)
	private int imapport;
	@Column(name = "USERNAME", nullable = false, length = 50)
	private String username;
	@Column(name = "PASSWORD", nullable = false, length = 50)
	private String password;
	@Column(name = "EMAIL_ADDRESS", nullable = false, length = 255)
	private String emailAddress;
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
			inboxFolder = (IMAPFolder) store.getFolder("Inbox");
			inboxFolder.open(Folder.READ_WRITE);
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
			inboxFolder.close(true);
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
			mimeMessage.setFrom(new InternetAddress(emailAddress, "BridgeMPP"));
			Collection<User> recipients = message.getDestination().getUsers();
			Address[] recipientAddresses = new Address[recipients.size()];
			Iterator<User> iterator = recipients.iterator();
			for (int i = 0; i < recipients.size(); i++)
			{
				User user = iterator.next();
				recipientAddresses[i] = new InternetAddress(user.getIdentifier(), user.getName());
			}

			mimeMessage.setRecipients(Message.RecipientType.TO, recipientAddresses);
			mimeMessage.setSubject(message.getDestination().getIdentifier());
			mimeMessage.setText(message.toSimpleString(getSupportedMessageFormats()), StandardCharsets.UTF_8.displayName());
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
		private static final long KEEP_ALIVE_FREQUENCY = 1740000l;
		private Future<?> future;
		private ReentrantLock processingInbox;

		public MailMessageListener()
		{
			processingInbox = new ReentrantLock();
			inboxFolder.addMessageCountListener(this);
			future = Schedule.scheduleRepeatWithPeriod(() -> {
				if (inboxFolder != null && inboxFolder.isOpen())
				{
					try
					{
						ShadowManager.log(Level.INFO, "MailService: Sending Keep-Alive NOOP");

						inboxFolder.doCommand(new IMAPFolder.ProtocolCommand() {

							@Override
							public Object doCommand(IMAPProtocol protocol) throws ProtocolException
							{
								protocol.simpleCommand("NOOP", null);
								return null;
							}
						});
						ShadowManager.log(Level.INFO, "MailService: Sent Keep-Alive NOOP. " + inboxFolder.getMessageCount() + " messages in inbox");
					} catch (MessagingException ex)
					{
						ShadowManager.log(Level.SEVERE, null, ex);
					}
				}
			}, 0, KEEP_ALIVE_FREQUENCY, TimeUnit.MILLISECONDS);
		}

		@Override
		public void run()
		{
			while (inboxFolder.isOpen())
			{
				processingInbox.lock();
				try
				{
					// Process incoming messages
					for (Message message : inboxFolder.getMessages())
					{
						processMessage(message);
					}

				} catch (MessagingException ex)
				{
					ShadowManager.log(Level.SEVERE, "Error while processing messages in inbox", ex);
				}
				processingInbox.unlock();

				try
				{
					inboxFolder.idle();
				} catch (MessagingException e)
				{
					ShadowManager.log(Level.SEVERE, "Error while idling Inbox folder", e);
				}

			}
			future.cancel(false);
		}

		@Override
		public void messagesAdded(MessageCountEvent e)
		{
			processingInbox.lock();

			try
			{
				for (Message message : inboxFolder.getMessages())
				{
					if (message.getFolder().equals(inboxFolder))
					{
						continue;
					}
					processMessage(message);
				}
			} catch (MessagingException e1)
			{
				ShadowManager.log(Level.SEVERE, "Mailbox error", e1);
			}

			processingInbox.unlock();
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
				for (int i = 0; i < address.length; i++)
				{
					if (address[i] != null)
					{
						sender = ((InternetAddress) address[i]).getAddress();
					}
				}
				String subjectName = message.getSubject();
				if (subjectName.length() > 50)
				{
					subjectName = subjectName.substring(0, 50);
				}
				Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(subjectName, MailService.this);
				User user = DataManager.getOrNewUserForIdentifier(sender, endpoint);

				bridgempp.Message bMessage = new bridgempp.Message(user, endpoint, null, null);
				processMessage(message, bMessage);
				inboxFolder.copyMessages(new Message[] { message }, processedFolder);
				inboxFolder.setFlags(new Message[] { message }, new Flags(Flags.Flag.DELETED), true);
				inboxFolder.expunge();
				return;

			} catch (MessagingException | IOException ex)
			{
				ShadowManager.log(Level.SEVERE, null, ex);
			}
		}

		protected void processMessage(Message message, bridgempp.Message bMessage) throws IOException, MessagingException
		{
			Object messageContent = message.getContent();
			if (messageContent instanceof Multipart)
			{
				Multipart container = (Multipart) messageContent;
				processMultiPartMessage(bMessage, container);
			} else
			{
				bMessage.setMessage(messageContent.toString());
				bMessage.setMessageFormat(getMessageFormatFromMimeType(message.getContentType()));
				receiveMessage(bMessage);
			}
		}

		protected void processMultiPartMessage(bridgempp.Message bMessage, Multipart container) throws MessagingException, IOException
		{
			ContentType type = new ContentType(container.getContentType());
			for (int i = 0; i < container.getCount(); i++)
			{
				BodyPart part = container.getBodyPart(i);
				bMessage.setMessageFormat(getMessageFormatFromMimeType(part.getContentType()));
				Object content = part.getContent();
				if (content instanceof Multipart)
				{
					processMultiPartMessage(bMessage, (Multipart) content);
				} else if (content instanceof String)
				{
					bMessage.setMessage(content.toString());
					receiveMessage(bMessage);
				}
				else
				{
					continue;
				}
				
				if(type.getSubType().equals("alternative"))
				{
					return;
				}
			}
		}

		private MessageFormat getMessageFormatFromMimeType(String contentType)
		{
			try
			{
				ContentType type = new ContentType(contentType);
				switch (type.getBaseType())
				{
					case "text/plain":
						return MessageFormat.PLAIN_TEXT;
					case "text/html":
						return MessageFormat.HTML;
					case "image/jpeg":
						return MessageFormat.STRING_EMBEDDED_IMAGE_FORMAT;
					default:
						return MessageFormat.PLAIN_TEXT;
				}
			} catch (ParseException e)
			{
				ShadowManager.log(Level.SEVERE, "Received invalid MIME Type in email message", e);
			}
			return MessageFormat.PLAIN_TEXT;
		}
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats;
	}

	public void configure(String emailAddress, String imaphost, int imapport, String username, String password, String smtphost, int smtpport)
	{
		this.imaphost = imaphost;
		this.imapport = imapport;
		this.username = username;
		this.emailAddress = emailAddress;
		this.password = password;
		this.smtphost = smtphost;
		this.smtpport = smtpport;
	}

}
