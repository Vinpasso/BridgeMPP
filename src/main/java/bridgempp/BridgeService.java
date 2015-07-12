/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import com.sun.corba.se.spi.ior.Identifiable;

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
	private String serviceIdentifier;
	
	@OneToMany(mappedBy="serviceConfiguration")
	protected Collection<Endpoint> endpoints;
	    
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
    
    public abstract void interpretCommand(Message message);
    
//    //Add Endpoint from Save to list of Endpoints
//    public void addEndpoint(Endpoint endpoint);
    
    //Get the Supported Message Encodings by this Endpoint in order of descending priority
    public abstract MessageFormat[] getSupportedMessageFormats();
    
    public String getIdentifier()
    {
    	return serviceIdentifier;
    }
}
