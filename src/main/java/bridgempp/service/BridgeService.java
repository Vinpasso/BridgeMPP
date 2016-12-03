/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;


/**
 *
 * @author Vinpasso
 */
@Entity(name="SERVICE")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "SERVICE_TYPE", discriminatorType=DiscriminatorType.STRING, length = 50)
public abstract class BridgeService {
	@Id
	@Column(name = "SERVICE_IDENTIFIER", nullable = false, length = 50)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int serviceIdentifier;
	
	@OneToMany(mappedBy="service")
	protected Collection<Endpoint> endpoints = new LinkedList<Endpoint>();

	@Column(name = "STATUS", nullable = false)
	private ServiceStatus status;
	
	private transient ArrayList<Function<Message, Message>> filters = new ArrayList<>();

	
    //Initialize Service
    public abstract void connect() throws Exception;
    //Deinitialize Service
    public abstract void disconnect() throws Exception;
    
    //Send message bridged from other Messages
    public abstract void sendMessage(Message message, DeliveryGoal deliveryGoal);
    
    /**
     * Method to process the Messages received by this Service
     * @param message
     */
    public void receiveMessage(Message message)
    {
    	CommandInterpreter.processMessage(message);
    }
    
	public BridgeService()
	{
		super();
		ServiceFilter[] filterAnnotations = getClass().getAnnotationsByType(ServiceFilter.class);
		for(ServiceFilter filter : filterAnnotations)
		{
			try
			{
				addFilter(filter.value().newInstance());
			} catch (Exception e) {
				ShadowManager.log(Level.SEVERE, "Failed to apply filter to service", e);
			}
		}
	}
	
	public final void processMessage(Message message, DeliveryGoal deliveryGoal)
	{
		Iterator<Function<Message, Message>> iterator = filters.iterator();
		while(iterator.hasNext())
		{
			message = iterator.next().apply(message);
			if(message == null)
			{
				return;
			}
		}
		sendMessage(message, deliveryGoal);
	}
	
	protected void addFilter(Function<Message, Message> filter)
	{
		filters.add(filter);
	}
	
	protected void removeFilter(Function<Message, Message> filter)
	{
		filters.remove(filter);
	}
    
    //Get user-friendly name of this Service
    public abstract String getName();
    //Check whether this Service is persistent across restarts
    public abstract boolean isPersistent();
            
    public int getIdentifier()
    {
    	return serviceIdentifier;
    }
    
    public String toString()
    {
    	return serviceIdentifier + ": " + getName() + (isPersistent()?" (Persistent) ":" (Non-Persistent) ") + getStatus().toString();
    }
    
    public ServiceStatus getStatus()
    {
    	return status;
    }
    
	public void setStatus(ServiceStatus status)
	{
		this.status = status;
	}
	
}
