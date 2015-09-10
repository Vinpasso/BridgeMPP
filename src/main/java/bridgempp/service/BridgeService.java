/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.service;

import java.util.Collection;
import java.util.LinkedList;

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

import bridgempp.Message;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;


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
	    
    //Initialize Service
    public abstract void connect();
    //Deinitialize Service
    public abstract void disconnect();
    
    //Send message bridged from other Messages
    public abstract void sendMessage(Message message);
    
    //Get user-friendly name of this Service
    public abstract String getName();
    //Check whether this Service is persistent across restarts
    public abstract boolean isPersistent();
        
    //Get the Supported Message Encodings by this Endpoint in order of descending priority
    public abstract MessageFormat[] getSupportedMessageFormats();
    
    public int getIdentifier()
    {
    	return serviceIdentifier;
    }
    
    public String toString()
    {
    	return serviceIdentifier + ": " + getName() + (isPersistent()?" (Persistent)":" (Non-Persistent)");
    }
}
