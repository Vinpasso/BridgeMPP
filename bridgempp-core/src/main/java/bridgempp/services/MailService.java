/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.ServiceManager;
import bridgempp.binarydistribution.BinaryDistributionManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.data.processing.Schedule;
import bridgempp.log.Log;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.MessageBody;
import bridgempp.message.formats.media.ImageMessageBody;
import bridgempp.message.formats.text.HTMLMessageBody;
import bridgempp.message.formats.text.PlainTextMessageBody;
import bridgempp.service.BridgeService;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.MessageIDTerm;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import java.io.IOException;
import java.net.URL;
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
	transient private IMAPFolder sentItemsFolder;
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

	@Override
	public void connect() throws Exception
	{
		Log.log(Level.INFO, "Loading Mail Service...");

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
		sentItemsFolder = (IMAPFolder) store.getFolder("Sent");
		sentItemsFolder.open(Folder.READ_WRITE);
		new Thread(new MailMessageListener(), "Mail Message Listener").start();

		Log.log(Level.INFO, "Mail service loaded");
	}

	@Override
	public void disconnect() throws Exception
	{
		inboxFolder.close(true);
		processedFolder.close(true);
		sentItemsFolder.close(true);
		store.close();
	}

	@Override
	public void sendMessage(bridgempp.message.Message message, DeliveryGoal deliveryGoal)
	{
		Endpoint destination = deliveryGoal.getTarget();
		try
		{
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress(emailAddress, "BridgeMPP"));
			Collection<User> recipients = destination.getUsers();
			Address[] recipientAddresses = new Address[recipients.size()];
			Iterator<User> iterator = recipients.iterator();
			for (int i = 0; i < recipients.size(); i++)
			{
				User user = iterator.next();
				recipientAddresses[i] = new InternetAddress(user.getIdentifier(), user.getName());
			}

			mimeMessage.setRecipients(Message.RecipientType.TO, recipientAddresses);
			mimeMessage.setSubject(destination.getIdentifier());
			MimeMultipart multiPart = new MimeMultipart("alternative");

			if (message.hasMessageBody(HTMLMessageBody.class))
			{
				MimeBodyPart htmlMimeBodyPart = new MimeBodyPart();
				htmlMimeBodyPart.setText(message.getMessageBody(HTMLMessageBody.class).getText(), StandardCharsets.UTF_8.name(), "html");
				multiPart.addBodyPart(htmlMimeBodyPart);
			}

			MimeBodyPart plaintextMimeBodyPart = new MimeBodyPart();
			plaintextMimeBodyPart.setText(message.getPlainTextMessageBody(), StandardCharsets.UTF_8.name());
			multiPart.addBodyPart(plaintextMimeBodyPart);

			mimeMessage.setContent(multiPart);

			Transport.send(mimeMessage, username, password);
			deliveryGoal.setDelivered();
			Log.log(Level.INFO, "Sent mail message to " + recipients.size() + " recipients");

			sentItemsFolder.addMessages(new Message[] { mimeMessage });
			Log.log(Level.INFO, "Stored sent message in sent items IMAP folder");
		} catch (Exception ex)
		{
			Log.log(Level.WARNING, "Failed to send Message", ex);
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
						Log.log(Level.INFO, "MailService: Sending Keep-Alive NOOP");

						IMAPFolder.ProtocolCommand noopCommand = new IMAPFolder.ProtocolCommand() {

							@Override
							public Object doCommand(IMAPProtocol protocol) throws ProtocolException
							{
								protocol.simpleCommand("NOOP", null);
								return null;
							}
						};

						inboxFolder.doCommand(noopCommand);
						processedFolder.doCommand(noopCommand);
						sentItemsFolder.doCommand(noopCommand);
						Log.log(Level.INFO, "MailService: Sent Keep-Alive NOOP. " + inboxFolder.getMessageCount() + " messages in inbox");
					} catch (MessagingException ex)
					{
						ServiceManager.onServiceError(MailService.this, "Message listening error", ex);
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
					ServiceManager.onServiceError(MailService.this, "Error while processing messages in inbox", ex);
				}
				processingInbox.unlock();

				try
				{
					inboxFolder.idle();
				} catch (MessagingException e)
				{
					ServiceManager.onServiceError(MailService.this, "Error idling inbox folder", e);
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
				ServiceManager.onServiceError(MailService.this, "Mailbox error", e1);
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

				// Get Subject by Mail Subject
				String subjectName = message.getSubject();
				if (subjectName.length() > 50)
				{
					subjectName = subjectName.substring(0, 50);
				}

				// Get Subject by In-Reply-To Field
				String[] inReplyTo = message.getHeader("In-Reply-To");
				if (inReplyTo != null && inReplyTo.length > 0)
				{
					Message[] replyMessages = sentItemsFolder.search(new MessageIDTerm(inReplyTo[0]));
					if (replyMessages != null && replyMessages.length > 0)
					{
						subjectName = replyMessages[0].getSubject();
						Log.log(Level.INFO, "Extracted Subject from In-Reply-To Header: " + subjectName);
					} else
					{
						Log.log(Level.INFO, "Could not find original In-Reply-To message");
					}
				} else
				{
					Log.log(Level.INFO, "In-Reply-To Header not set");
				}

				Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(subjectName, MailService.this);
				User user = DataManager.getOrNewUserForIdentifier(sender, endpoint);

				bridgempp.message.Message bMessage = new bridgempp.message.Message(user, endpoint);
				processMessage(message, bMessage);
				inboxFolder.copyMessages(new Message[] { message }, processedFolder);
				inboxFolder.setFlags(new Message[] { message }, new Flags(Flags.Flag.DELETED), true);
				inboxFolder.expunge();
				return;

			} catch (MessagingException | IOException ex)
			{
				ServiceManager.onServiceError(MailService.this, "Error while processing message", ex);
			}
		}

		protected void processMessage(Message message, bridgempp.message.Message bMessage) throws IOException, MessagingException
		{
			Object messageContent = message.getContent();
			if (messageContent instanceof Multipart)
			{
				Multipart container = (Multipart) messageContent;
				processMultiPartMessage(bMessage, container);
				// Receive Message handled in process multipart
			} else if (messageContent instanceof MimeBodyPart)
			{
				bMessage.addMessageBody(getMessageFormatFromMimeType(message.getContentType(), (MimeBodyPart) messageContent));
				receiveMessage(bMessage);
			} else
			{
				bMessage.addMessageBody(new PlainTextMessageBody(message.getContent().toString()));
				receiveMessage(bMessage);
			}
		}

		protected void processMultiPartMessage(bridgempp.message.Message bMessage, Multipart container) throws MessagingException, IOException
		{
			ContentType type = new ContentType(container.getContentType());
			for (int i = 0; i < container.getCount(); i++)
			{
				BodyPart part = container.getBodyPart(i);
				Object content = part.getContent();
				if (content instanceof Multipart)
				{
					processMultiPartMessage(bMessage, (Multipart) content);
				} else if (content instanceof MimeBodyPart)
				{
					bMessage.addMessageBody(getMessageFormatFromMimeType(part.getContentType(), (MimeBodyPart) content));
					receiveMessage(bMessage);
				} else
				{
					continue;
				}

				if (type.getSubType().equals("alternative"))
				{
					return;
				}
			}
		}

		private MessageBody getMessageFormatFromMimeType(String contentType, MimeBodyPart content)
		{
			try
			{
				ContentType type = new ContentType(contentType);
				switch (type.getBaseType())
				{
					case "text/plain":
						return new PlainTextMessageBody(content.getContent().toString());
					case "text/html":
						return new HTMLMessageBody(content.getContent().toString());
					case "image/*":
						URL publishURL = BinaryDistributionManager.defaultPublish(content.getFileName(), content.getInputStream());
						return new ImageMessageBody(content.getFileName(), new MimeType(contentType), publishURL);
					default:
						return new PlainTextMessageBody(content.getContent().toString());
				}
			} catch (IOException | MimeTypeParseException | MessagingException e)
			{
				ServiceManager.onServiceError(MailService.this, "Error in multipart e-mail message.", e);
			}
			return new PlainTextMessageBody(content.toString());
		}
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
