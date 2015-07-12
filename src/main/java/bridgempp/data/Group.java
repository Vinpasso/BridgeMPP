/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Version;

import bridgempp.Message;
import bridgempp.ShadowManager;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "GROUP")
public class Group
{

	@Id
	@Column(name = "GROUP_ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long groupID;

	@Column(name = "GROUP_NAME", nullable = false, length = 50)
	private String name;

	@ManyToMany
	@JoinTable(name = "SUBSCRIPTIONS", joinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "ENDPOINT_ID", referencedColumnName = "ENDPOINT_ID"))
	private Collection<Endpoint> endpoints;

	@Version
	@Column(name = "LAST_UPDATED_TIME")
	private Date updatedTime;

	// Send message to all recipients in this group
	public void sendMessage(Message message)
	{
		message.setGroup(this);
		Iterator<Endpoint> iterator = endpoints.iterator();
		while (iterator.hasNext())
		{
			Endpoint endpoint = iterator.next();
			try
			{
				endpoint.sendMessage(message);
			} catch (Exception e)
			{
				ShadowManager.log(Level.WARNING, "Delivery failed! Message could not be delivered to " + endpoint.toString(), e);
				message.getOrigin().sendOperatorMessage("Delivery failed! Message could not be delivered to " + endpoint.toString());
			}
		}
	}

	// public void sendOperatorMessage(String message)
	// {
	// sendMessage(new Message(null, null, this, message));
	// }

	// Send message without Loopback to sender
	public void sendMessageWithoutLoopback(Message message)
	{
		message.setGroup(this);
		Iterator<Endpoint> iterator = endpoints.iterator();
		while (iterator.hasNext())
		{
			Endpoint endpoint = iterator.next();
			// Check that Sender does not get the Message sent back to him
			if (!endpoint.equals(message.getOrigin()))
			{
				try
				{
					endpoint.sendMessage(message);
				} catch (Exception e)
				{
					ShadowManager.log(Level.WARNING, "Delivery failed! Message could not be delivered to " + endpoint.toString(), e);
					message.getOrigin().sendOperatorMessage("Delivery failed! Message could not be delivered to " + endpoint.toString());
				}
			}
		}
	}

	// Add a user/group to this groups recipients
	public void addEndpoint(Endpoint endpoint)
	{
		getEndpoints().add(endpoint);
	}

	// Remove a user/group from this groups recipients
	public void removeEndpoint(Endpoint endpoint)
	{
		getEndpoints().remove(endpoint);
	}

	// Remove all endpoints in preperation for Group destruction
	public void removeAllEndpoints()
	{
		Iterator<Endpoint> iterator = endpoints.iterator();
		while (iterator.hasNext())
		{
			iterator.next();
			iterator.remove();
		}
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	// Initialize the group endpoints
	public Group()
	{
		endpoints = new ArrayList<>();
		name = "NewGroup";
	}

	public boolean hasEndpoint(Endpoint endpoint)
	{
		return getEndpoints().contains(endpoint);
	}

	@Override
	// Prints out its Members
	public String toString()
	{
		String members = "";
		Iterator<Endpoint> iterator = endpoints.iterator();
		while (iterator.hasNext())
		{
			Endpoint endpoint = iterator.next();
			members += "Endpoint: " + endpoint.toString() + "\n";
			Iterator<User> userIterator = endpoint.getUsers().iterator();
			while (userIterator.hasNext())
			{
				User user = userIterator.next();
				members += "Member: " + user.toString() + "\n";
			}
		}
		return members;
	}

	/**
	 * @return the endpoints
	 */
	public Collection<Endpoint> getEndpoints()
	{
		return endpoints;
	}

}