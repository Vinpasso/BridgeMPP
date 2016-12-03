/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.data.User;
import bridgempp.message.formats.media.MediaMessageBody;
import bridgempp.message.formats.text.MarkupTextMessageBody;
import bridgempp.message.formats.text.PlainTextMessageBody;

/**
 *
 * @author Vincent Bode
 */
@Entity(name = "Message")
public class Message
{
	@Column(name = "Sender", nullable = false)
	private User sender;

	@Column(name = "Origin", nullable = false)
	private Endpoint origin;

	@OneToMany(mappedBy = "Message")
	private List<DeliveryGoal> destinations;

	@OneToMany()
	private List<Group> groups;

	// TODO: CONTINUE HERE
	private HashMap<Class<? extends MessageBody>, MessageBody> messageBodies;

	private MessageBody originalMessageBody;

	public Message()
	{
		this(null, null);
	}

	public Message(User sender, Endpoint origin)
	{
		this.sender = sender;
		this.origin = origin;
		this.destinations = new ArrayList<>();
		this.groups = new ArrayList<>();
		this.messageBodies = new HashMap<>();
	}

	/**
	 * @return the sender
	 */
	public User getSender()
	{
		return sender;
	}

	/**
	 * @param sender
	 *            the sender to set
	 */
	public void setSender(User sender)
	{
		this.sender = sender;
	}

	/**
	 * @return the Origin
	 */
	public Endpoint getOrigin()
	{
		return origin;
	}

	/**
	 * @param sender
	 *            the Origin to set
	 */
	public void setOrigin(Endpoint sender)
	{
		this.origin = sender;
	}

	public void addDestinationEndpoint(Endpoint endpoint)
	{
		destinations.add(new DeliveryGoal(endpoint));
	}

	private List<DeliveryGoal> getDeliveryGoals()
	{
		return destinations;
	}

	public String getMetadataInfo()
	{
		String messageFormat = (messageBodies.isEmpty() ? "Empty" : originalMessageBody.getFormatName()) + ": ";
		String sender = (getSender() != null) ? getSender().toString() : "Unknown";
		String origin = (getOrigin() != null) ? getOrigin().toString() : "Unknown";
		String target = getDeliveryGoals().stream().filter(e -> e.getStatus().equals(DeliveryStatus.DELIVERED)).count() + "/" + getDeliveryGoals().size();
		return messageFormat + sender + " (" + origin + "): " + target + " delivered.";
	}

	@Override
	public String toString()
	{
		return getMetadataInfo();
	}

	public void addMessageBody(MessageBody messageBody)
	{
		messageBodies.put(messageBody.getClass(), messageBody);
		if (originalMessageBody == null)
		{
			originalMessageBody = messageBody;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends MessageBody> T getMessageBody(Class<T> messageBodyClass)
	{
		MessageBody messageBody = messageBodies.get(messageBodyClass);
		if (messageBody != null)
		{
			return (T) messageBody;
		}

		T convertedMessageBody = MessageBodyRegister.convert(originalMessageBody, messageBodyClass);
		if (convertedMessageBody != null)
		{
			messageBodies.put(messageBodyClass, convertedMessageBody);
			return convertedMessageBody;
		}
		return null;
	}

	public String getPlainTextMessageBody()
	{
		String result = getMessageBody(PlainTextMessageBody.class).getText();
		return (result != null) ? result : "";
	}

	/**
	 * Includes conversions
	 * 
	 * @param bodyClass
	 *            The target body class
	 * @return Whether the message is available or can be converted
	 */
	public boolean hasMessageBody(Class<? extends MessageBody> bodyClass)
	{
		return messageBodies.containsKey(bodyClass) || MessageBodyRegister.canConvert(originalMessageBody, bodyClass);
	}

	private MessageBody getOriginalMessageBody()
	{
		return originalMessageBody;
	}

	public boolean hasOriginalMessageBody(Class<? extends MessageBody> bodyClass)
	{
		return bodyClass.isAssignableFrom(getOriginalMessageBody().getClass());
	}

	public boolean isPlainTextMessage()
	{
		return originalMessageBody instanceof PlainTextMessageBody;
	}

	public boolean isMarkupTextMessage()
	{
		return originalMessageBody instanceof MarkupTextMessageBody;
	}

	public boolean isMediaMessage()
	{
		return originalMessageBody instanceof MediaMessageBody;
	}

	public boolean isTextMessage()
	{
		return isPlainTextMessage() || isMarkupTextMessage();
	}

	public void send()
	{
		CommandInterpreter.processMessage(this);
	}

	public void addDestinationsFromGroupNoLoopback(Group group)
	{
		group.getEndpoints().stream().filter(e -> !e.equals(getOrigin())).forEach(e -> addDestinationEndpoint(e));
		groups.add(group);
	}

	public List<Group> getGroups()
	{
		return groups;
	}

	public List<DeliveryGoal> getDestinations()
	{
		return destinations;
	}

	public Collection<MessageBody> getMessageBodies()
	{
		return messageBodies.values();
	}

	public void deliver()
	{
		getDeliveryGoals().stream().filter(e -> e.getStatus() != DeliveryStatus.DELIVERED).forEach(e -> {
			try
			{
				e.getTarget().sendMessage(this, e);
			} catch (Exception ex)
			{
				ShadowManager.log(Level.WARNING, "Delivery failed to endpoint " + e.toString(), ex);
			}
		});
	}

}
