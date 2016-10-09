/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.message;

import java.util.ArrayList;
import java.util.List;

import bridgempp.data.Endpoint;
import bridgempp.data.User;

/**
 *
 * @author Vincent Bode
 */
public abstract class Message
{
	private User sender;
	private Endpoint origin;
	private List<DeliveryGoal> destinations;

	public Message()
	{
		this(null, null);
	}

	public Message(User sender, Endpoint origin)
	{
		this.sender = sender;
		this.origin = origin;
		this.destinations = new ArrayList<>();
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
	
	public abstract String getFormatName();

	public String getMetadataInfo()
	{
		String messageFormat = getFormatName() + ": ";
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

}
